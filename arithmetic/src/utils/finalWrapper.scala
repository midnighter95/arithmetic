package utils

import chisel3._
import chisel3._
import chisel3.util._
import division.srt.{SRT, SRTOutput}

class SRTIn extends Bundle {
  val dividend = UInt(32.W)
  val divisor = UInt(32.W)
}
/** input: oprand1 oprand2 singed
  * output: quient and remainder
  *
  * condition:
  * divede 0
  * negative input
  */
class finalWrapper extends Module{

  val input = IO(Flipped(DecoupledIO(new SRTIn)))
  val signIn = IO(Input(Bool()))
  val output = IO(ValidIO(new SRTOutput(32, 32)))

  //abs
  val abs = Module(new Abs(32))
  abs.io.aIn := input.bits.dividend.asSInt
  abs.io.bIn := input.bits.divisor.asSInt
  abs.io.signIn := signIn
  val negative = abs.io.aSign ^ abs.io.bSign

  //LZC
  val LZC0 = Module(new LZC32)
  val LZC1 = Module(new LZC32)
  LZC0.io.a := abs.io.aOut
  LZC1.io.a := abs.io.bOut

  val srt: SRT = Module(new SRT(32, 32, 32))
  // pre-process

  // 6-bits , above zero
  // add one bit for calculate complement
  val zeroHeadDividend = Wire(UInt(6.W))
  val zeroHeadDivisor = Wire(UInt(6.W))
  zeroHeadDividend := ~LZC0.io.z
  zeroHeadDivisor := ~LZC1.io.z
  // sub = zeroHeadDivider - zeroHeadDividend
  val sub = Wire(UInt(6.W))
  sub := addition.prefixadder.koggeStone(-zeroHeadDividend, zeroHeadDivisor, false.B)
  // needComputerWidth: Int = zeroHeadDivider - zeroHeadDividend + 2
  val needComputerWidth = Wire(UInt(7.W))
  needComputerWidth := addition.prefixadder.koggeStone(sub, 2.U, false.B)
  // noguard: Boolean = needComputerWidth % radixLog2 == 0
  val noguard = !needComputerWidth(0)
  // counter: Int = (needComputerWidth + 1) / 2
  val counter = (addition.prefixadder.koggeStone(needComputerWidth, 1.U, false.B) >> 1).asUInt
  // leftShiftWidthDividend: Int = zeroHeadDividend - (if (noguard) 0 else 1)
  val leftShiftWidthDividend = Wire(UInt(6.W))
  val leftShiftWidthDivisor = Wire(UInt(6.W))
  leftShiftWidthDividend := Mux(noguard,zeroHeadDividend(4,0),
    addition.prefixadder.koggeStone(zeroHeadDividend(4,0), "b111111".U, false.B))
  leftShiftWidthDivisor := zeroHeadDivisor(4,0)

  // do SRT
  srt.input.bits.divider := input.bits.divisor << leftShiftWidthDivisor
  srt.input.bits.dividend := input.bits.dividend << leftShiftWidthDividend
  srt.input.bits.counter := counter
  srt.input.valid := input.valid && !(input.bits.divisor === 0.U)
  input.ready := srt.input.ready

  // logic
  val divideZero = Wire(Bool())
  divideZero := (input.bits.divisor === 0.U) && input.fire

  // post-process
  output.valid := srt.output.valid | divideZero
  output.bits.quotient := Mux(divideZero,"hffffffff".U(32.W), Mux(negative, -srt.output.bits.quotient, srt.output.bits.quotient))
  output.bits.reminder := Mux(abs.io.aSign, -srt.output.bits.reminder, srt.output.bits.reminder)
}
