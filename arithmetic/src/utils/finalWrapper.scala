package utils

import chisel3._
import chisel3._
import chisel3.util._
import division.srt.{SRT, SRTOutput}

class SRTIn extends Bundle {
  val dividend = SInt(32.W)
  val divisor = SInt(32.W)
}

class SRTOut extends Bundle {
  val reminder = SInt(32.W)
  val quotient = SInt(32.W)
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
  val output = IO(ValidIO(new SRTOut))
  val debug = IO(new Bundle() {
    val bigdivisor = Output(Bool())
    val dividend = Output(UInt(32.W))
    val divisor = Output(UInt(32.W))
    val gap = Output(UInt(33.W))
  })
  //abs
  val abs = Module(new Abs(32))
  abs.io.aIn := input.bits.dividend
  abs.io.bIn := input.bits.divisor
  abs.io.signIn := signIn
  val negative = abs.io.aSign ^ abs.io.bSign

  //LZC
  val LZC0 = Module(new LZC32)
  val LZC1 = Module(new LZC32)
  LZC0.io.a := abs.io.aOut
  LZC1.io.a := abs.io.bOut

  val srt: SRT = Module(new SRT(32, 32, 32))
  // pre-process

  // dividezero logic
  val divideZero = Wire(Bool())
  divideZero := (input.bits.divisor === 0.S) && input.fire

  // dividend must > divisor
  val dividend = Wire(UInt(33.W))
  val divisor = Wire(UInt(33.W))
  val gap = Wire(UInt(34.W))
  dividend := abs.io.aOut
  divisor := abs.io.bOut
  gap := addition.prefixadder.koggeStone(divisor, -dividend, false.B)
  val biggerdivisor = gap(33)  && !(gap(32,0).orR === false.B)
  debug.dividend := dividend(31,0)
  debug.divisor := divisor(31,0)
  debug.gap := gap
  debug.bigdivisor := biggerdivisor

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
  srt.input.bits.dividend := abs.io.aOut << leftShiftWidthDividend
  srt.input.bits.divider := abs.io.bOut << leftShiftWidthDivisor
  srt.input.bits.counter := counter
  // if dividezero or biggerdivisor; bypass SRT
  srt.input.valid := input.valid && !(input.bits.divisor === 0.S) && (!biggerdivisor)
  // copy srt ready to top
  input.ready := srt.input.ready

  // deal with  sign issue
  val quotientAbs = Wire(UInt(32.W))
  val remainderAbs = Wire(UInt(32.W))
  quotientAbs := srt.output.bits.quotient
  remainderAbs := srt.output.bits.reminder >> zeroHeadDivisor

  // if dividezero or biggerdivisor, bypass SRT
  output.valid := srt.output.valid | divideZero | biggerdivisor
  // the quotient of division by zero has all bits set, and the remainder of division by zero equals the dividend.
  output.bits.quotient := Mux(divideZero,"hffffffff".U(32.W),
    Mux(biggerdivisor, 0.U,
    Mux(negative, -quotientAbs, quotientAbs))).asSInt
  output.bits.reminder := Mux(divideZero, dividend(31,0),
    Mux(biggerdivisor, dividend(31,0),
    Mux(abs.io.aSign, -remainderAbs, remainderAbs))).asSInt
}
