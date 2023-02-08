package utils

import Chisel.Cat
import chisel3._
import chisel3.util.RegEnable


/** 32bit SRT Wrapper to pre-processer dividend and divisor
  * @todo use one LZC32
  */
class SRTWrapper extends Module{
  val io = IO(new Bundle{
    val dividendIn  = Input(UInt(32.W))
    val divisorIn   = Input(UInt(32.W))
    val dividendOut = Output(UInt(32.W))
    val divisorOut  = Output(UInt(32.W))
    val counter     = Output(UInt(5.W))
  })
  val LZC0 = Module(new LZC32)
  val LZC1 = Module(new LZC32)
  LZC0.io.a := io.dividendIn
  LZC1.io.a := io.divisorIn

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
                                addition.prefixadder.koggeStone(zeroHeadDividend(4,0), "b111111".asUInt, false.B))
  leftShiftWidthDivisor := zeroHeadDivisor(4,0)
  io.divisorOut := io.divisorIn << leftShiftWidthDivisor
  io.dividendOut := io.dividendIn << leftShiftWidthDividend
  io.counter := counter
}
