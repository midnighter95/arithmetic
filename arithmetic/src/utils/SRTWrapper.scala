package utils

import chisel3._
import chisel3.util.RegEnable



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
  // Mux for dividend and divisor
  LZC0.io.a := io.dividendIn
  LZC1.io.a := io.divisorIn
  val zeroHeadDividend = ~LZC0.io.z
  val zeroHeadDivider  = ~LZC1.io.z
  val needComputerWidth = UInt(5.W)
  needComputerWidth := zeroHeadDivider - zeroHeadDividend
  val noguard: Boolean = needComputerWidth
  val counter: Int = (needComputerWidth + 1) / 2


}
