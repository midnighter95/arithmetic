package float

import chisel3._
import chisel3.util._
import sqrt._

/**
  *
  * @todo Opt for zero
  *       input is Subnormal!
  *
  * */
class SqrtFloat(expWidth: Int, sigWidth: Int) extends Module{
  class FloatSqrtInput(expWidth: Int, sigWidth: Int) extends Bundle() {
    val oprand = UInt((expWidth + sigWidth).W)
  }

  /** add 2 for rounding */
  class FloatSqrtOutput(expWidth: Int, sigWidth: Int) extends Bundle() {
    val result = UInt((expWidth + sigWidth).W)
    val sig = UInt((sigWidth + 2).W)
    val exp = UInt(expWidth.W)

    //  val exceptionFlags = UInt(5.W)
  }

  val input = IO(Flipped(DecoupledIO(new FloatSqrtInput(expWidth, sigWidth))))
  val output = IO(ValidIO(new FloatSqrtOutput(expWidth, sigWidth)))
  val rawFloatIn = rawFloatFromFN(expWidth,sigWidth,input.bits.oprand)

  /** Control path */
  val isNegaZero = rawFloatIn.isZero && rawFloatIn.sign
  val isPosiInf  = rawFloatIn.isInf  && rawFloatIn.sign

  val fastWorking = RegInit(false.B)
  val fastCase = Wire(Bool())

  /** negative or NaN*/
  val invalidExec = (rawFloatIn.sign && !isNegaZero) || rawFloatIn.isNaN
  /** positive inf */
  val infinitExec = isPosiInf

  fastCase := invalidExec || infinitExec
  fastWorking := input.fire && fastCase



  /** Data path
    *
    * {{{
    * expLSB   rawExpLSB    Sig             SigIn     expOut
    *      0           1    1.xxxx>>2<<1    1xxxx0    rawExp/2 +1 + bias
    *      1           0    1.xxxx>>2       01xxxx    rawExp/2 +1 + bias
    * }}}
    *
    */

  val adjustedExp = Cat(rawFloatIn.sExp(expWidth-1), rawFloatIn.sExp(expWidth-1, 0))
  val expStore = RegEnable(adjustedExp(expWidth,1), 0.U(expWidth.W), input.fire)
  val expToRound = expStore

  val sqrtExIsEven = input.bits.oprand(sigWidth - 1)
  val fractIn = Mux(sqrtExIsEven, Cat("b0".U(1.W),rawFloatIn.sig(sigWidth-1, 0),0.U(1.W)),
    Cat(rawFloatIn.sig(sigWidth-1, 0),0.U(2.W)))

  val SqrtModule = Module(new SquareRoot(2, 2, 26, 26))
  SqrtModule.input.valid := input.valid && !fastCase
  SqrtModule.input.bits.operand := fractIn

  val rbits = SqrtModule.output.bits.result(1) ## (!SqrtModule.output.bits.zeroRemainder || SqrtModule.output.bits.result(0))
  val sigforRound = SqrtModule.output.bits.result(24,2)


  input.ready := SqrtModule.input.ready
  val roundresult = RoundingUnit(
    input.bits.oprand(expWidth + sigWidth-1) ,
    expToRound.asSInt,
    sigforRound,
    rbits,
    consts.round_near_even,
    invalidExec,
    infinitExec,
    false.B,
    false.B,
    false.B
  )
  output.bits.result := roundresult(0)
  output.bits.sig := output.bits.result(sigWidth-2, 0)
  output.bits.exp := output.bits.result(30,23)
  output.valid := SqrtModule.output.valid || fastWorking

}


