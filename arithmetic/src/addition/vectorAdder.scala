package addition

import chisel3._
import chisel3.util._

class vectorAdder(val width: Int) extends Module{
  require(width > 0)
  val a: UInt = IO(Input(UInt(width.W)))
  val b: UInt = IO(Input(UInt(width.W)))
  val z: UInt = IO(Output(UInt(width.W)))

  val cin: Bool = IO(Input(Bool()))
  val cout: Bool = IO(Output(Bool()))



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

  val tree = bk8(pairs)
  val cs =  cin +: tree.map(pg => cgen(pg, cin))

  val ps = pairs.map(_._1) :+ false.B
  val psVec = VecInit(ps).asUInt
  val sum = ps.zip(cs).map { case (p, c) => p ^ c }

  val reuslt = Wire(UInt(9.W))
  reuslt := VecInit(sum).asUInt
  z := reuslt

  cout := reuslt(8)
  assert(a +& b + cin === Cat(cout, z))
  

}

