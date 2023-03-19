package utils

import Chisel.Cat
import chisel3._
import chisel3.util.RegEnable
import chisel3.util._
import division.srt._
import division.srt.srt16.SRT16


/** 32bit SRT Wrapper to pre-processer dividend and divisor
  * @todo use one LZC32
  */
class SRT16Wrapper extends Module{
  class SRTIn extends Bundle {
    val dividend = SInt(32.W)
    val divisor = SInt(32.W)
    val signIn = Bool()
  }

  class SRTOut extends Bundle {
    val reminder = SInt(32.W)
    val quotient = SInt(32.W)
  }

  val input = IO(Flipped(DecoupledIO(new SRTIn)))
  val output = IO(ValidIO(new SRTOut))

  val abs = Module(new Abs(32))
  abs.io.aIn := input.bits.dividend
  abs.io.bIn := input.bits.divisor
  abs.io.signIn := input.bits.signIn
  val negative = abs.io.aSign ^ abs.io.bSign

  val LZC0 = Module(new LZC32)
  val LZC1 = Module(new LZC32)
  LZC0.io.a := abs.io.aOut
  LZC1.io.a := abs.io.bOut

  val srt: SRT16 = Module(new SRT16(32, 32, 32))

  /** divided by zero detection */
  val divideZero = (input.bits.divisor === 0.S)

  /** bigger divisor detection */
  val dividend = Wire(UInt(33.W))
  val divisor = Wire(UInt(33.W))
  val gap = Wire(UInt(34.W))
  val biggerdivisor = Wire(Bool())
  dividend := abs.io.aOut
  divisor := abs.io.bOut
  gap := divisor +& (-dividend)
  biggerdivisor := gap(33) && !(gap(32, 0).orR === false.B)

  // bypass
  val bypassSRT = (divideZero || biggerdivisor) && input.fire

  /** Leading Zero component */
  // extend one bit for calculation
  val zeroHeadDividend = Wire(UInt(6.W))
  val zeroHeadDivisor = Wire(UInt(6.W))
  zeroHeadDividend := ~LZC0.io.z
  zeroHeadDivisor := ~LZC1.io.z
  // sub = zeroHeadDivider - zeroHeadDividend
  val sub = Wire(UInt(6.W))
  sub := (-zeroHeadDividend) +& zeroHeadDivisor
  // needComputerWidth: Int = zeroHeadDivider - zeroHeadDividend + 2
  val needComputerWidth = Wire(UInt(7.W))
  needComputerWidth := sub +& 2.U
  // noguard: Boolean =  needComputerWidth % 4 == 0
  val noguard = !needComputerWidth(0) && !needComputerWidth(1)
  // guardWidth: Int =  if (noguard) 0 else 4 - needComputerWidth % 4
  val guardWidth = Wire(UInt(2.W))
  guardWidth := Mux(noguard, 0.U, 4.U + -needComputerWidth(1, 0))
  // counter: Int = (needComputerWidth + guardWidth) / radixLog2
  val counter = ((needComputerWidth +& guardWidth) >> 2).asUInt
  // leftShiftWidthDividend: Int = zeroHeadDividend - (if (noguard) 0 else 1)
  val leftShiftWidthDividend = Wire(UInt(6.W))
  val leftShiftWidthDivisor = Wire(UInt(6.W))

  leftShiftWidthDividend := zeroHeadDividend +& -Cat(0.U(4.W), guardWidth)
  leftShiftWidthDivisor := zeroHeadDivisor(4, 0)

  val rightshiftNumber = Mux(leftShiftWidthDividend(5), -leftShiftWidthDividend, 0.U)
  val rightshiftBits = abs.io.aOut(1, 0)

  // keep mutiple cycles for SRT
  val negativeSRT = RegEnable(negative, srt.input.fire)
  val zeroHeadDivisorSRT = RegEnable(zeroHeadDivisor, srt.input.fire)
  val dividendSignSRT = RegEnable(abs.io.aSign, srt.input.fire)
  val rightshiftNumberSRT = RegEnable(rightshiftNumber, srt.input.fire)
  val rightshiftBitsSRT = RegEnable(rightshiftBits, srt.input.fire)

  // keep for one cycle
  val divideZeroReg = RegEnable(divideZero, false.B, input.fire)
  val biggerdivisorReg = RegEnable(biggerdivisor, false.B, input.fire)
  val bypassSRTReg = RegNext(bypassSRT, false.B)
  val dividendReg = RegEnable(dividend, 0.U, input.fire)
  val dividendSignReg = RegEnable(abs.io.aSign, false.B, input.fire)

  // do SRT

  srt.input.bits.dividend := Mux(
    leftShiftWidthDividend(5),
    dividend >> -leftShiftWidthDividend,
    dividend << leftShiftWidthDividend
  )
  srt.input.bits.divider := abs.io.bOut << leftShiftWidthDivisor
  srt.input.bits.counter := counter
  // if dividezero or biggerdivisor, bypass SRT
  srt.input.valid := input.valid && !bypassSRT
  srt.fixValue := dividend(2,0)
  // copy srt ready to top
  input.ready := srt.input.ready

  // post-process for sign
  // todo: remove bias
  val quotientAbs = Wire(UInt(32.W))
  val remainderAbsBias = Wire(UInt(32.W))
  quotientAbs := srt.output.bits.quotient
  remainderAbsBias := srt.output.bits.reminder >> zeroHeadDivisorSRT

  val dividendRestore = Wire(UInt(32.W))
  dividendRestore := Mux(dividendSignReg, -dividendReg(31, 0), dividendReg(31, 0))

  output.valid := srt.output.valid | bypassSRTReg
  // the quotient of division by zero has all bits set, and the remainder of division by zero equals the dividend.
  output.bits.quotient := Mux(
    divideZeroReg,
    "hffffffff".U(32.W),
    Mux(biggerdivisorReg, 0.U, Mux(negativeSRT, -quotientAbs, quotientAbs))
  ).asSInt
  output.bits.reminder := Mux(
    divideZeroReg,
    dividendRestore,
    Mux(biggerdivisorReg, dividendRestore, Mux(dividendSignSRT, -remainderAbsBias, remainderAbsBias))
  ).asSInt

}
