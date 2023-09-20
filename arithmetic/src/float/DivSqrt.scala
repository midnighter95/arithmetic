package float

import chisel3._
import chisel3.util._
import division.srt.srt16._
import sqrt._

/** DIV
  * input
  * {{{
  * dividend = 0.1f  -> 1f +"00000" right extends to 32
  * divisor  = 0.1f  -> 1f +"00000" right extends to 32
  * }}}
  *
  * output = 0.01f or 0.1f, LSB 28bits effective
  * {{{
  * 0.01f: 28bits=01f f=sig=select(25,3)
  * 0.1f : 28bits=1f  f=sig=select(26,4)
  * }}}
  *
  * SQRT
  * {{{
  * expLSB   rawExpLSB    Sig             SigIn     expOut
  *      0           1    1.xxxx>>2<<1    1xxxx0    rawExp/2 +1 + bias
  *      1           0    1.xxxx>>2       01xxxx    rawExp/2 +1 + bias
  * }}}
  */
class DivSqrt(expWidth: Int, sigWidth: Int) extends Module{
  val fpWidth = expWidth + sigWidth
  val calWidth = 28
  val input = IO(Flipped(DecoupledIO(new DivSqrtInput(expWidth, sigWidth))))
  val output = IO(ValidIO(new DivSqrtOutput(expWidth, sigWidth)))

  val opSqrtReg       = RegEnable(input.bits.sqrt        , false.B, input.fire)
  val roundingModeReg = RegEnable(input.bits.roundingMode, 0.U    , input.fire)

  val rawA = rawFloatFromFN(expWidth, sigWidth, input.bits.a)
  val rawB = rawFloatFromFN(expWidth, sigWidth, input.bits.b)

  // Exceptions

  /** inf/inf and 0/0  => NaN out */
  val divNotSigNaNInButInvalidExc =
    (rawA.isZero && rawB.isZero) || (rawA.isInf && rawB.isInf)
  /** -Inf + -normal => NaN out */
  val sqrtNotSigNaNInButInvalidExc =
    !rawA.isNaN && !rawA.isZero && rawA.sign
  /** isSigNaNRawFloat detect signaling NaN */
  val majorExc =
    Mux(input.bits.sqrt,
      isSigNaNRawFloat(rawA) || sqrtNotSigNaNInButInvalidExc,
      isSigNaNRawFloat(rawA) || isSigNaNRawFloat(rawB) ||
        divNotSigNaNInButInvalidExc ||
        (!rawA.isNaN && !rawA.isInf && rawB.isZero)
    )

  /** all cases result in NaN output */
  val isNaN =
    Mux(input.bits.sqrt,
      rawA.isNaN || sqrtNotSigNaNInButInvalidExc,
      rawA.isNaN || rawB.isNaN || divNotSigNaNInButInvalidExc
    )
  val isInf  = Mux(input.bits.sqrt, rawA.isInf, rawA.isInf || rawB.isZero)
  val isZero = Mux(input.bits.sqrt, rawA.isZero, rawA.isZero || rawB.isInf)

  val majorExcReg = RegEnable(majorExc, false.B, input.fire)
  val isNaNReg    = RegEnable(isNaN   , false.B, input.fire)
  val isInfReg    = RegEnable(isInf   , false.B, input.fire)
  val isZeroReg   = RegEnable(isZero  , false.B, input.fire)

  /** invalid operation flag */
  val invalidExec = majorExcReg &&  isNaNReg

  /** DivideByZero flag */
  val infinitExec = majorExcReg && !isNaNReg

  val specialCaseA = rawA.isNaN || rawA.isInf || rawA.isZero
  val specialCaseB = rawB.isNaN || rawB.isInf || rawB.isZero
  val normalCaseDiv = !specialCaseA && !specialCaseB
  val normalCaseSqrt = !specialCaseA && !rawA.sign
  val normalCase = Mux(input.bits.sqrt, normalCaseSqrt, normalCaseDiv)
  val specialCase = !normalCase

  val fastValid = RegInit(false.B)
  fastValid := specialCase && input.fire

  // sign
  val signNext = Mux(input.bits.sqrt, rawA.isZero && rawA.sign, rawA.sign ^ rawB.sign)
  val signReg = RegEnable(signNext, input.fire)

  /** sqrt exp logic
    *
    * {{{
    * sExp first 2 bits
    * 00 -> 10 (subnormal)
    * 01 -> 11 (true exp negative)
    * 10 -> 00 (true exp positive)
    * }}}
    */
  val expfirst2 = UIntToOH(rawA.sExp(expWidth, expWidth-1))
  /** expfirst2(3) never happens */
  val expstart  = Mux1H(
    Seq(
      expfirst2(0) -> "b10".U,
      expfirst2(1) -> "b11".U,
      expfirst2(2) -> "b00".U
    )
  )

  val expForSqrt = Cat(expstart, rawA.sExp(expWidth - 2, 0)) >> 1
  val sqrtExpIsOdd = !rawA.sExp(0)
  val sqrtFractIn = Mux(sqrtExpIsOdd, Cat(0.U(1.W), rawA.sig(sigWidth - 1, 0), 0.U(1.W)),
    Cat(rawA.sig(sigWidth - 1, 0), 0.U(2.W)))

  val SqrtModule = Module(new SquareRoot(2, 2, sigWidth+2, sigWidth+2))
  SqrtModule.input.bits.operand := sqrtFractIn
  SqrtModule.input.valid := input.valid && input.bits.sqrt && normalCaseSqrt

  val rbitsSqrt      = SqrtModule.output.bits.result(1) ## (!SqrtModule.output.bits.zeroRemainder || SqrtModule.output.bits.result(0))
  val sigToRoundSqrt = SqrtModule.output.bits.result(24, 2)


  // divInput
  val fractDividendIn = Wire(UInt((fpWidth).W))
  val fractDivisorIn = Wire(UInt((fpWidth).W))
  fractDividendIn := Cat(1.U(1.W), rawA.sig(sigWidth - 2, 0), 0.U(expWidth.W))
  fractDivisorIn  := Cat(1.U(1.W), rawB.sig(sigWidth - 2, 0), 0.U(expWidth.W))

  val divModule = Module(new SRT16(fpWidth, fpWidth, fpWidth))
  divModule.input.bits.dividend := fractDividendIn
  divModule.input.bits.divider := fractDivisorIn
  divModule.input.bits.counter := 8.U
  divModule.input.valid := input.valid && !input.bits.sqrt && normalCaseDiv


  /** collect div sig result
    *
    * {{{
    * when B_sig > A_sig
    * divout = 0000,01xxx
    * exp need decrease by 1
    * }}}
    */
  val needRightShift = !divModule.output.bits.quotient(27)
  val sigToRoundDiv = Mux(needRightShift,
    divModule.output.bits.quotient(calWidth - 3, calWidth - sigWidth - 1),
    divModule.output.bits.quotient(calWidth - 2, calWidth - sigWidth))
  val rbitsDiv = Mux(needRightShift, divModule.output.bits.quotient(calWidth - sigWidth - 2) ## divModule.output.bits.reminder.orR,
    divModule.output.bits.quotient(calWidth - sigWidth - 1) ## divModule.output.bits.reminder.orR)

  // collect sig result
  val sigToRound   = Mux(opSqrtReg, sigToRoundSqrt, sigToRoundDiv)
  val rbitsToRound = Mux(opSqrtReg, rbitsSqrt, rbitsDiv)

  // exp logic
  val expStoreNext,expToRound = Wire(UInt((expWidth+2).W))
  /** expStore is 10bits SInt
    *
    * for sqrt
    * {{{
    * expForSqrt(7,0) effective is 8bits, MSB is sign
    * extends 2 sign bit in MSB
    * expStoreNext = 10bits
    * }}}
    *
    * for div
    * rawA_S.sExp - rawB_S.sExp
    */
  expStoreNext := Mux(input.bits.sqrt,
    Cat(expForSqrt(7),expForSqrt(7),expForSqrt(7,0)),
    (rawA.sExp-rawB.sExp).asUInt)
  val expStore = RegEnable(expStoreNext, 0.U((expWidth+2).W), input.fire)
  expToRound := Mux(opSqrtReg, expStore, expStore - needRightShift)

  val roundresult = RoundingUnit(
    signReg,
    expToRound.asSInt,
    sigToRound,
    rbitsToRound,
    roundingModeReg,
    invalidExec,
    infinitExec,
    isNaNReg,
    isInfReg,
    isZeroReg)

  output.bits.result := roundresult(0)
  output.bits.exceptionFlags := roundresult(1)

  input.ready  := divModule.input.ready  && SqrtModule.input.ready
  output.valid := divModule.output.valid || SqrtModule.output.valid || fastValid
}


class DivSqrtInput(expWidth: Int, sigWidth: Int) extends Bundle() {
  val a = UInt((expWidth + sigWidth).W)
  val b = UInt((expWidth + sigWidth).W)
  val sqrt = Bool()
  val roundingMode = UInt(3.W)
}


class DivSqrtOutput(expWidth: Int, sigWidth: Int) extends Bundle() {
  val result = UInt((expWidth + sigWidth).W)
  val exceptionFlags = UInt(5.W)
}
