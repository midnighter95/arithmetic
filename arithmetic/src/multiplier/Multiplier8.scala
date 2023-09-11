package multiplier

import chisel3._
import chisel3.util._

class Multiplier8 extends Module{
  val a = IO(Input(UInt(8.W)))
  val b = IO(Input(UInt(8.W)))
  val z = IO(Output(UInt(16.W)))

  def make8BitsPartialProduct(a : Tuple2[Bool, Int]): UInt = { // Seq[(weight, value)]
    val exist = Mux(a._1, b, 0.U(8.W))
    val doShift = a._2 match {
      case 0 => exist
      case c => Cat(exist, 0.U(c.W))
    }
    doShift
  }

  val aVec = a.asBools
  val partialProducts: Seq[UInt] = aVec.zipWithIndex.map(make8BitsPartialProduct)

  val pp0 = partialProducts(0)
  val pp1 = partialProducts(1)
  val pp2 = partialProducts(2)
  val pp3 = partialProducts(3)
  val pp4 = partialProducts(4)
  val pp5 = partialProducts(5)
  val pp6 = partialProducts(6)
  val pp7 = partialProducts(7)


  dontTouch(pp0)
  dontTouch(pp1)
  dontTouch(pp2)
  dontTouch(pp3)
  dontTouch(pp4)
  dontTouch(pp5)
  dontTouch(pp6)
  dontTouch(pp7)

  def compress82(in: Seq[UInt]): (UInt, UInt) = {
    val layer0   = addition.csa.csa42(12)(VecInit(in.dropRight(4)))
    val layer1   = addition.csa.csa42(12)(VecInit(in(4)(11,4), in(5)(12,4), in(6)(13,4), in(7)(14,4)))
    val layerOut = addition.csa.csa42(16)(VecInit(layer0._1 << 1, layer0._2, layer1._1 << 5, layer1._2<<4))
    ((layerOut._1(14,0)<<1).asUInt, layerOut._2(15,0))
  }

  def add82(in: Seq[UInt]): UInt = {
    val compress = compress82(in)
    compress._1.asUInt + compress._2.asUInt
  }

  z := add82(partialProducts)


}