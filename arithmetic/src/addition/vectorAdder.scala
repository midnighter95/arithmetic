package addition

import chisel3._
import chisel3.util._

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
class vectorAdder(val width: Int) extends Module {
  require(width > 0)
  val a: UInt = IO(Input(UInt(width.W)))
  val b: UInt = IO(Input(UInt(width.W)))
  val z: UInt = IO(Output(UInt(width.W)))
  val sew = IO(Input(UInt(3.W)))
  
  val s0: Int = 7
  val s1: Int = 15
  val s2: Int = 23
  val s3: Int = 31

  val e0 = s0+1
  val e1 = s1+1
  val e2 = s2+1
  val e3 = s3+1
  
  val eSeq = Seq(0,8,16,24)
  

  val cin = IO(Input(UInt(4.W)))
  val cout = IO(Output(UInt(4.W)))

  // Split up bit vectors into individual bits and reverse it
  val as: Seq[Bool] = a.asBools
  val bs: Seq[Bool] = b.asBools

  def zeroLayer(a: Seq[Bool], b: Seq[Bool]): Seq[(Bool, Bool)] = a.zip(b).map { case (a, b) => (a ^ b, a & b) }

  def prefixadd(a: (Bool, Bool), b: (Bool, Bool)) = (a._1 & b._1, a._2 | (a._1 & b._2))

  def cgen(pg: (Bool, Bool), cin: Bool) = pg._2 || (pg._1 && cin)

  def bk8(leaf: Seq[(Bool, Bool)]): Seq[(Bool, Bool)] = leaf match {
    /** match to 8 bits fan-in */
    case Seq((p0, g0), (p1, g1), (p2, g2), (p3, g3), (p4, g4), (p5, g5), (p6, g6), (ps0, gs0)) => {
      val layer0 = Seq(prefixadd((ps0, gs0), (p6, g6)), prefixadd((p5, g5), (p4, g4)), prefixadd((p3, g3), (p2, g2)), prefixadd((p1, g1), (p0, g0)))
      val layer1 = Seq(prefixadd(layer0(0), layer0(1)), prefixadd(layer0(2), layer0(3)))

      val s0 = (p0, g0)
      val s1 = layer0(3)
      val s2 = prefixadd((p2, g2), s1)
      val s3 = layer1(1)
      val s4 = prefixadd((p4, g4), s3)
      val s5 = prefixadd(layer0(1), layer1(1))
      val s6 = prefixadd((p6, g6), s5)
      val ss0 = prefixadd(layer1(0), layer1(1))
      Seq(s0, s1, s2, s3, s4, s5, s6, ss0)
    }
  }

  val pairs: Seq[(Bool, Bool)] = zeroLayer(as, bs)

  val tree8Leaf = eSeq.map{
    case i => bk8(pairs.slice(i , i+8))
  }
  val tree8: Seq[(Bool, Bool)] = tree8Leaf.fold(Nil)(_++_)
  val tree16Leaf0 = tree8Leaf(0) ++ tree8Leaf(1).map(prefixadd(_, tree8Leaf(0)(7)))
  val tree16Leaf1 = tree8Leaf(2) ++ tree8Leaf(3).map(prefixadd(_, tree8Leaf(2)(7)))
  val tree16: Seq[(Bool, Bool)] = tree16Leaf0 ++ tree16Leaf1
  val tree32 = tree16Leaf0 ++ tree16Leaf1.map(prefixadd(_, tree16Leaf0(s1)))

  val tree8P  = VecInit(tree8.map(_._1)).asUInt
  val tree8G  = VecInit(tree8.map(_._2)).asUInt
  val tree16P = VecInit(tree16.map(_._1)).asUInt
  val tree16G = VecInit(tree16.map(_._2)).asUInt
  val tree32P = VecInit(tree32.map(_._1)).asUInt
  val tree32G = VecInit(tree32.map(_._2)).asUInt

  val treeP = Mux1H(Seq(
    sew(0) -> tree8P,
    sew(1) -> tree16P,
    sew(2) -> tree32P
  ))

  val treeG = Mux1H(Seq(
    sew(0) -> tree8G,
    sew(1) -> tree16G,
    sew(2) -> tree32G
  ))
  val tree = treeP.asBools.zip(treeG.asBools)

  /** Each 8 bits pg tree will be combined with their own cin to get carryResult in each bit.
    *
    * It's why 16sew input cin should be xxyy instead of 0x0y.
    */
  def buildCarry(tree: Seq[(Bool, Bool)], cin: UInt): UInt = {
    val ci0: UInt = VecInit(tree.slice(0 , e0).map(pg => cgen(pg, cin(0)))).asUInt
    val ci1: UInt = VecInit(tree.slice(e0, e1).map(pg => cgen(pg, cin(1)))).asUInt
    val ci2: UInt = VecInit(tree.slice(e1, e2).map(pg => cgen(pg, cin(2)))).asUInt
    val ci3: UInt = VecInit(tree.slice(e2, e3).map(pg => cgen(pg, cin(3)))).asUInt
    Cat(ci3, ci2, ci1, ci0)
  }

  /** if carry generated in each bit , in order */
  val carryResult = buildCarry(tree, cin)
  val cout8  = carryResult(s3) ## carryResult(s2) ## carryResult(s1) ## carryResult(s0)
  val cout16 = carryResult(s3) ## carryResult(s1)
  val cout32 = carryResult(s3)
  val ps = VecInit(pairs.map(_._1)).asUInt

  /** build cs for all cases
    *
    * {{{
    * cs for banked 8 :
    * bank3 ## cin3    ## bank2 ## cin2    ## bank1 ##  cin1   ## bank0 ## cin0
    *
    * cs for bank 16:
    * bank3 ## connect ## bank2 ## cin2    ## bank1 ## connect ## bank0 ## cin0
    *
    * cs for bank 32:
    * bank3 ## connect ## bank2 ## connect ## bank1 ## connect ## bank0 ## cin0
    *
    * banki is carry(i*8+6,i*8), 7bits
    * connect is carry(i*8+7)
    * }}}
    *
    * carryInSele is to append the holes in cs
    *
    *
    */
  val carryInSele = Mux1H(Seq(
    sew(0) -> cin,
    sew(1) -> carryResult(s2) ## cin(2)          ## carryResult(s0) ## cin(0),
    sew(2) -> carryResult(s2) ## carryResult(s1) ## carryResult(s0) ## cin(0),
  ))
  val cs = Cat(
    carryResult(30, 24), carryInSele(3),
    carryResult(22, 16), carryInSele(2),
    carryResult(14, 8),  carryInSele(1),
    carryResult(6, 0),   carryInSele(0))

  cout := Mux1H(Seq(
    sew(0) -> cout8,
    sew(1) -> cout16,
    sew(2) -> cout32,
  ))

  z := ps ^ cs
}

