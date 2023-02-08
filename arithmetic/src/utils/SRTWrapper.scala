package utils

import chisel3._
import chisel3.util.RegEnable


/** SRT Wrapper to pre-processer dividend and divisor
  *
  * doesn't check ((divider == 0) || (divider > dividend) || ( <= 0))
  *
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
  // Mux for dividend and divisor
  LZC0.io.a := io.dividendIn
  LZC1.io.a := io.divisorIn
//  val zeroHeadDividend: Int = m - zeroCheck(dividend)
//  val zeroHeadDivider: Int = m - zeroCheck(divider)
//  val needComputerWidth: Int = zeroHeadDivider - zeroHeadDividend + 1 + radixLog2 - 1
//  val noguard: Boolean = needComputerWidth % radixLog2 == 0
//  val counter: Int = (needComputerWidth + 1) / 2
//  if ((divider == 0) || (divider > dividend) || ( <= 0))
//    return
//  val quotient: BigInt = dividend / divider
//  val remainder: BigInt = dividend % divider
//  val leftShiftWidthDividend: Int = zeroHeadDividend - (if (noguard) 0 else 1)
//  val leftShiftWidthDivider: Int = zeroHeadDivider
  val zeroHeadDividend = UInt(5.W)
  val zeroHeadDivisor = UInt(5.W)
  zeroHeadDividend := ~LZC0.io.z
  zeroHeadDivisor  := ~LZC1.io.z
  val substract =  addition.prefixadder.koggeStone((~zeroHeadDividend).asUInt,zeroHeadDivisor,false.B)
  val needComputerWidth = addition.prefixadder.koggeStone(substract, 2.U, false.B)
  val noguard = !needComputerWidth(0)
  val counter = (needComputerWidth >>1).asUInt
  val leftShiftWidthDividend = Mux(noguard, zeroHeadDividend,addition.prefixadder.koggeStone(zeroHeadDividend,(~1.U).asUInt,false.B))
  val leftShiftWidthDivisor = zeroHeadDivisor
  io.divisorOut := io.divisorIn << leftShiftWidthDivisor
  io.dividendOut := io.dividendIn << leftShiftWidthDividend
  io.counter := counter
}
