package multiplier

import addition.prefixadder.PrefixSum
import addition.prefixadder.common.BrentKungSum
import chisel3._
import chisel3.util._
import chisel3.experimental.FixedPoint
import utils.extend
import addition.csa._

class VectorMultiplier(width: Int) extends Module {
  val Width = width + 1
  val outWidth = width * 2
  val radixLog2 = 2
  val signed = true

  val a = IO(Input(UInt(width.W)))
  val b = IO(Input(UInt(width.W)))
  val z = IO(Output(UInt(outWidth.W)))
  val sign = IO(Input(Bool()))

  val aIn = Mux(sign, extend(a,Width), extend(a, Width,false))
  val bIn = Mux(sign, extend(b,Width), extend(b, Width,false))


  val recMultiplerVec: Vec[SInt] = VecBooth
    .recode(Width)(radixLog2, signed = true)(Cat(aIn,0.U(1.W)).asUInt)

  /** */
  val encodedWidth = (radixLog2 + 1).W
  /** do b * recode a (don't shift)
    * output is 10bits
    * */
  val partialProductLookupTable: Seq[(UInt, Bits)] = Seq(
      0.U(3.W)                    -> 0.U(10.W),
      (-1).S(encodedWidth).asUInt -> ~extend(bIn(8, 0), 10),
      (-2).S(encodedWidth).asUInt -> ~(bIn(8, 0) << 1),
      1.S(encodedWidth).asUInt    -> extend(bIn(Width - 1, 0), 10),
      2.S(encodedWidth).asUInt    -> (bIn(8, 0) << 1)
  )

  /**
    *
    * sign : pp sign
    * doshift
    * complementAddOne is true when recoded < 0
    *
    * */
  def make10BitsPartialProduct(weight: Int, recoded: SInt): (Bool, UInt, Bool) = { // Seq[(weight, value)]
    val partialProductOrigin: UInt = MuxLookup(recoded.asUInt, 0.U(10.W), partialProductLookupTable).asUInt
    val doShift = weight match {
      case 0 => partialProductOrigin(8,0)
      case c => Cat(partialProductOrigin(8,0),0.U((2*c).W))
    }
    val sign = partialProductOrigin(9)
    val complementAddOne = recoded(2)
    (sign, doShift, complementAddOne)
  }

  val partialProducts: IndexedSeq[(Bool, UInt, Bool)] = recMultiplerVec.zipWithIndex.map { case (x, i) => make10BitsPartialProduct(i, x) }

  val pp0 = partialProducts(0)._2
  val pp1 = partialProducts(1)._2
  val pp2 = partialProducts(2)._2
  val pp3 = partialProducts(3)._2
  val pp4 = partialProducts(4)._2
  dontTouch(pp0)
  dontTouch(pp1)
  dontTouch(pp2)
  dontTouch(pp3)
  dontTouch(pp4)

  val sign0 = partialProducts(0)._1
  val sign1 = partialProducts(1)._1
  val sign2 = partialProducts(2)._1
  val sign3 = partialProducts(3)._1
  val sign4 = partialProducts(4)._1
  dontTouch(sign0)
  dontTouch(sign1)
  dontTouch(sign2)
  dontTouch(sign3)
  dontTouch(sign4)

  val ulp0 = partialProducts(0)._3
  val ulp1 = partialProducts(1)._3
  val ulp2 = partialProducts(2)._3
  val ulp3 = partialProducts(3)._3
  val ulp4 = partialProducts(4)._3
  dontTouch(ulp0)
  dontTouch(ulp1)
  dontTouch(ulp2)
  dontTouch(ulp3)
  dontTouch(ulp4)

  /** output width = 19 */
  val csa52: (UInt, UInt) = addition.csa.csa52(17)(VecInit(pp0,pp1,pp2,pp3,pp4))

  val signAndCinCol = {
    partialProducts(4)._1 ## false.B ##
    !partialProducts(3)._1 ## false.B ##
    !partialProducts(2)._1 ## false.B ##
    !partialProducts(1)._1 ## false.B ##
    !partialProducts(0)._1 ##
    partialProducts(4)._3  ## false.B ##
    partialProducts(3)._3  ## false.B ##
    partialProducts(2)._3  ## false.B ##
    partialProducts(1)._3  ## false.B ##
    partialProducts(0)._3
  }
  dontTouch(signAndCinCol)
  val compensateCol = Cat("b110101011".U, 0.U(9.W))

  val csa52Carry = csa52._1 << 1
  val csa52Sum = csa52._2

  val csa42 = addition.csa.csa42(16)(VecInit(csa52Sum(15,0), csa52Carry(15,0), compensateCol(15,0), signAndCinCol(15,0)))

  val carry = (csa42._1 << 1).asUInt
  val sum = csa42._2
  dontTouch(sum)
  dontTouch(carry)

  z := carry(15,0) + sum(15,0)

}