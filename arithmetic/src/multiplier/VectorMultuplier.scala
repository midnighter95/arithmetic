package multiplier

import addition.prefixadder.PrefixSum
import addition.prefixadder.common.BrentKungSum
import chisel3._
import chisel3.util._
import chisel3.experimental.FixedPoint
import utils.extend
import addition.csa._

class VectorMultiplier(Width: Int) extends Module {
  val outWidth = Width * 2
  val radixLog2 = 2
  val signed = true

  val a: UInt = IO(Input(UInt(Width.W)))
  val b: UInt = IO(Input(UInt(Width.W)))
  val z: UInt = IO(Output(UInt(outWidth.W)))

  val recMultiplerVec: Vec[SInt] = Booth
    .recode(Width)(radixLog2, signed = true)(a)

  // produce Seq(b, 2 * b, ..., 2^digits * b), output width = width + radixLog2 - 1
  val bMultipleWidth = (Width - 1).W

  /** */
  val encodedWidth = (radixLog2 + 1).W
  /** */
  val partialProductLookupTable: Seq[(UInt, Bits)] = Seq(
      0.U(3.W)                    -> 0.U(Width.W),
      (-1).S(encodedWidth).asUInt -> ~extend(b(Width - 1, 0), Width+1),
      (-2).S(encodedWidth).asUInt -> ~(b(Width - 1, 0) << 1),
      1.S(encodedWidth).asUInt    -> extend(b(Width - 1, 0), Width+1),
      2.S(encodedWidth).asUInt    -> (b(Width - 1, 0) << 1)
  )

  /**
    *
    * sign : pp sign
    * doshift
    * complementAddOne is true when recoded < 0
    *
    * */
  def make10BitsPartialProduct(weight: Int, recoded: SInt): (Bool, UInt, Bool) = { // Seq[(weight, value)]
    val partialProductOrigin: UInt = MuxLookup(recoded.asUInt, 0.U(bMultipleWidth), partialProductLookupTable).asUInt
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

  val csa52: (UInt, UInt) = addition.csa.csa52(17)(VecInit(pp0,pp1,pp2,pp3,pp4))

  val signAndCinCol = {
    partialProducts(4)._3 ## false.B ##
    !partialProducts(3)._3 ## false.B ##
    !partialProducts(2)._3 ## false.B ##
    !partialProducts(1)._3 ## false.B ##
    !partialProducts(0)._3 ##
    partialProducts(4)._1  ## false.B ##
    partialProducts(3)._1  ## false.B ##
    partialProducts(2)._1  ## false.B ##
    partialProducts(1)._1  ## false.B ##
    partialProducts(0)._1
  }
  val compensateCol = Cat("b110101011".U, 0.U(9.W))

  val csa52Carry = csa52._1 << 1
  val csa52Sum = csa52._2

  val csa42 = addition.csa.csa42(19)(VecInit(csa52Sum, csa52Carry, compensateCol, signAndCinCol))

  val carry = (csa42._1 << 1).asUInt
  val sum = csa42._2
  dontTouch(sum)
  dontTouch(carry)

  val finalsum = carry +& sum

  z := finalsum(17, 0)

}