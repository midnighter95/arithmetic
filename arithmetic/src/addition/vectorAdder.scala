package addition

import chisel3._
import chisel3.util._

/**
  * sew = 8: Input.sew = 001, cin = c3,c2,c1,c0
  * sew = 16 Input.sew = 010, cin = c_high,c_high,c_low,c_low
  * sew = 32 Input.sew = 010, cin = cin,cin,cin,cin
  *
  *
  */
class vectorAdder(val width: Int) extends Module{
  require(width > 0)
  val a: UInt = IO(Input(UInt(width.W)))
  val b: UInt = IO(Input(UInt(width.W)))
  val z: UInt = IO(Output(UInt(width.W)))
  val sew = IO(Input(UInt(3.W)))

  val cin  = IO(Input(UInt(4.W)))
  val cout = IO(Output(UInt(4.W)))



  // Split up bit vectors into individual bits
  val as: Seq[Bool] = a.asBools
  val bs: Seq[Bool] = b.asBools

  def zeroLayer(a: Seq[Bool], b: Seq[Bool]): Seq[(Bool, Bool)] = a.zip(b).map { case (a, b) => (a ^ b, a & b) }

  /** Type of pair is P and G
    *
    * @todo How to abstract this with Ling Adder?
    */


  def prefixadd(a:(Bool, Bool), b:(Bool, Bool)) = (a._1 & b._1, a._2 | (a._1 & b._2))
  def cgen(pg:(Bool, Bool), cin:Bool) = pg._2 || (pg._1 && cin)

  def bk8(leaf: Seq[(Bool, Bool)]): Seq[(Bool, Bool)] = leaf match {
    /** match to 8 bits fan-in */
    case Seq((p0, g0), (p1, g1), (p2, g2), (p3, g3), (p4, g4), (p5, g5), (p6, g6), (p7, g7)) => {
      val layer0 = Seq(prefixadd((p7, g7),(p6, g6)),prefixadd((p5, g5),(p4, g4)),prefixadd((p3, g3),(p2, g2)),prefixadd((p1, g1),(p0, g0)))
      val layer1 = Seq(prefixadd(layer0(0),layer0(1)),prefixadd(layer0(2),layer0(3)))

      val s0 = (p0, g0)
      val s1 = layer0(3)
      val s2 = prefixadd((p2, g2), s1)
      val s3 = layer1(1)
      val s4 = prefixadd((p4, g4), s3)
      val s5 = prefixadd(layer0(1), layer1(1))
      val s6 = prefixadd((p6, g6), s5)
      val s7 = prefixadd(layer1(0), layer1(1))
      Seq(s0, s1, s2, s3, s4, s5, s6, s7)
    }
  }

  val pairs: Seq[(Bool, Bool)] = zeroLayer(as, bs)
  val paire8: Seq[Seq[(Bool, Bool)]] = Seq(pairs.slice(0,8), pairs.slice(8,16), pairs.slice(16,24), pairs.slice(24,32))
  val tree8 = bk8(pairs.slice(0,8)) ++ bk8(pairs.slice(8,16))++ bk8(pairs.slice(16,24))++ bk8(pairs.slice(24,32))

  def buildCarry(tree: Seq[(Bool, Bool)], pg:Seq[(Bool, Bool)], cin: UInt): UInt ={
    val ci0 = VecInit(tree.slice(0,8).map(pg => cgen(pg, cin(0)))).asUInt
    val ci1 = VecInit(tree.slice(8,16).map(pg => cgen(pg, cin(1)))).asUInt
    val ci2 = VecInit(tree.slice(16,24).map(pg => cgen(pg, cin(2)))).asUInt
    val ci3 = VecInit(tree.slice(24,32).map(pg => cgen(pg, cin(3)))).asUInt
    Cat(ci3,ci2,ci1,ci0)
  }

  val carry = buildCarry(tree8, pairs, cin)
  val cout8 = carry(31) ## carry(23) ## carry(15) ## carry(7)
  val ps = VecInit(pairs.map(_._1)).asUInt
  val carrySele = cin
  val cs = Cat(carry(30,24), carrySele(3),
               carry(22, 16), carrySele(2),
               carry(14, 8),  carrySele(1),
               carry(6,0),    carrySele(0))
  cout := cout8
  dontTouch(carry)
  dontTouch(ps)


  cout := 0.U
  z := ps ^ cs

}

