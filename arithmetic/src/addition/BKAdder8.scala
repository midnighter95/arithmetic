package addition

import chisel3._
import chisel3.util._

import prefixadder.common._

/**
  * IO formula
  * {{{
  * sew = 8 => Input.sew = b001.U,
  * cin = c3,c2,c1,c0
  * Output cout = cout
  *
  *
  * sew = 16 => input.sew = b010.U
  * Input.sew = 010,
  * cin = c_high,c_high,c_low,c_low
  * example :
  * cin = 1 and 0, Input cin should be b1100.U
  * cout = 10, Output cout = b10.U
  *
  *
  *
  * sew = 32 Input.sew = 010,
  * cin = cin,cin,cin,cin
  * example: cin = 1, Input cin should be 1111.U
  * cout = 1, Output cout = b1.U
  *}}}
  *
  */
class BKAdder8 extends Module {
  val width = 8
  val a: UInt = IO(Input(UInt(width.W)))
  val b: UInt = IO(Input(UInt(width.W)))
  val z: UInt = IO(Output(UInt(width.W)))

  val cin = IO(Input(Bool()))
  val cout = IO(Output(Bool()))

  // Split up bit vectors into individual bits and reverse it
  val as: Seq[Bool] = a.asBools
  val bs: Seq[Bool] = b.asBools

  def zeroLayer(a: Seq[Bool], b: Seq[Bool]): Seq[(Bool, Bool)] = a.zip(b).map { case (a, b) => GeneratePG(a, b) }

  def prefixadd(a: (Bool, Bool), b: (Bool, Bool)) = PrefixAdd2(b._1, b._2,a._1, a._2)


  def cgen(pg: (Bool, Bool), cin: Bool) = Cgen(pg._1, pg._2, cin)

  def bk8(leaf: Seq[(Bool, Bool)]): Seq[(Bool, Bool)] = leaf match {
    /** match to 8 bits fan-in */
    case Seq((p0, g0), (p1, g1), (p2, g2), (p3, g3), (p4, g4), (p5, g5), (p6, g6), (p7, g7)) => {
      val layer0 = Seq(prefixadd((p7, g7), (p6, g6)), prefixadd((p5, g5), (p4, g4)), prefixadd((p3, g3), (p2, g2)), prefixadd((p1, g1), (p0, g0)))
      val layer1 = Seq(prefixadd(layer0(0), layer0(1)), prefixadd(layer0(2), layer0(3)))

      val t0 = (p0, g0)
      val t1 = layer0(3)
      val t2 = prefixadd((p2, g2), t1)
      val t3 = layer1(1)
      val t4 = prefixadd((p4, g4), t3)
      val t5 = prefixadd(layer0(1), layer1(1))
      val t6 = prefixadd((p6, g6), t5)
      val t7 = prefixadd(layer1(0), layer1(1))
      Seq(t0, t1, t2, t3, t4, t5, t6, t7)
    }
  }

  val pairs: Seq[(Bool, Bool)] = zeroLayer(as, bs)


  val tree = bk8(pairs)

  /** Each 8 bits pg tree will be combined with their own cin to get carryResult in each bit.
    *
    * It's why 16sew input cin should be xxyy instead of 0x0y.
    */
  def buildCarry(tree: Seq[(Bool, Bool)], cin: Bool): UInt = {
    VecInit(tree.map(pg => cgen(pg, cin))).asUInt
  }

  /** if carry generated in each bit , in order */
  val carryResult = buildCarry(tree, cin)


  val cs = Cat(carryResult(6, 0),   cin)


  val ps = VecInit(pairs.map(_._1)).asUInt
  z := ps ^ cs
  cout := carryResult(7)
}


