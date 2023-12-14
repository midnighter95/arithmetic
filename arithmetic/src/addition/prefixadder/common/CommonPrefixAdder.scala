package addition.prefixadder.common

import addition.prefixadder.PrefixSum
import chisel3._

/** 2 inputs Prefix sum is has implementation of
  * [[RippleCarrySum]], [[KoggeStoneSum]], [[BrentKungSum]]
  * You should read those code in sequence for better understanding.
  */
trait CommonPrefixSum extends PrefixSum {
  def associativeOp(leaf: Seq[(Bool, Bool)]): (Bool, Bool) = leaf match {
    /** match to 2 bits fan-in */
    case Seq((p0, g0), (p1, g1)) =>
      PrefixAdd2(p0, g0, p1, g1)

    /** match to 3 bits fan-in */
    case Seq((p0, g0), (p1, g1), (p2, g2)) =>
      PrefixAdd3(p0, g0, p1, g1, p2, g2)

    /** match to 4 bits fan-in */
    case Seq((p0, g0), (p1, g1), (p2, g2), (p3, g3)) =>
      PrefixAdd4(p0, g0, p1, g1, p2, g2, p3, g3)

    /** match to 5 bits fan-in */
    case Seq((p0, g0), (p1, g1), (p2, g2), (p3, g3), (p4, g4)) =>
      (
        p0 && p1 && p2 && p3 && p4,
        (g0 && p1 && p2 && p3 && p4) || (g1 && p2 && p3 && p4) || (g2 && p3 && p4) || (g3 && p4) || g4
      )

    /** match to 6 bits fan-in */
    case Seq((p0, g0), (p1, g1), (p2, g2), (p3, g3), (p4, g4), (p5, g5)) =>
      (
        p0 && p1 && p2 && p3 && p4 && p5,
        (g0 && p1 && p2 && p3 && p4 && p5) || (g1 && p2 && p3 && p4 && p5) || (g2 && p3 && p4 && p5) || (g3 && p4 && p5) || (g4 && p5) || g5
      )

    /** match to 7 bits fan-in */
    case Seq((p0, g0), (p1, g1), (p2, g2), (p3, g3), (p4, g4), (p5, g5), (p6, g6)) =>
      (
        p0 && p1 && p2 && p3 && p4 && p5 && p6,
        (g0 && p1 && p2 && p3 && p4 && p5 && p6) || (g1 && p2 && p3 && p4 && p5 && p6) || (g2 && p3 && p4 && p5 && p6) || (g3 && p4 && p5 && p6) || (g4 && p5 && p6) || (g5 && p6) || g6
      )

    /** match to 8 bits fan-in */
    case Seq((p0, g0), (p1, g1), (p2, g2), (p3, g3), (p4, g4), (p5, g5), (p6, g6), (p7, g7)) =>
      (
        p0 && p1 && p2 && p3 && p4 && p5 && p6 && p7,
        (g0 && p1 && p2 && p3 && p4 && p5 && p6 && p7) || (g1 && p2 && p3 && p4 && p5 && p6 && p7) || (g2 && p3 && p4 && p5 && p6 && p7) || (g3 && p4 && p5 && p6 && p7) || (g4 && p5 && p6 && p7) || (g5 && p6 && p7) || (g6 && p7) || g7
      )

    /** match to 9 bits fan-in */
    case Seq((p0, g0), (p1, g1), (p2, g2), (p3, g3), (p4, g4), (p5, g5), (p6, g6), (p7, g7), (p8, g8)) =>
      (
        p0 && p1 && p2 && p3 && p4 && p5 && p6 && p7 && p8,
        (g0 && p1 && p2 && p3 && p4 && p5 && p6 && p7 && p8) || (g1 && p2 && p3 && p4 && p5 && p6 && p7 && p8) || (g2 && p3 && p4 && p5 && p6 && p7 && p8) || (g3 && p4 && p5 && p6 && p7 && p8) || (g4 && p5 && p6 && p7 && p8) || (g5 && p6 && p7 && p8) || (g6 && p7 && p8) || (g7 && p8) || g8
      )

    /** match to 10 bits fan-in */
    case Seq((p0, g0), (p1, g1), (p2, g2), (p3, g3), (p4, g4), (p5, g5), (p6, g6), (p7, g7), (p8, g8), (p9, g9)) =>
      (
        p0 && p1 && p2 && p3 && p4 && p5 && p6 && p7 && p8 && p9,
        (g0 && p1 && p2 && p3 && p4 && p5 && p6 && p7 && p8 && p9) || (g1 && p2 && p3 && p4 && p5 && p6 && p7 && p8 && p9) || (g2 && p3 && p4 && p5 && p6 && p7 && p8 && p9) || (g3 && p4 && p5 && p6 && p7 && p8 && p9) || (g4 && p5 && p6 && p7 && p8 && p9) || (g5 && p6 && p7 && p8 && p9) || (g6 && p7 && p8 && p9) || (g7 && p8 && p9) || (g8 && p9) || g9
      )
  }

  def zeroLayer(a: Seq[Bool], b: Seq[Bool]): Seq[(Bool, Bool)] = a.zip(b).map { case (a, b) => GeneratePG(a, b) }
}

class GeneratePG extends Module{
  val a = IO(Input(Bool()))
  val b = IO(Input(Bool()))
  val p = IO(Output(Bool()))
  val g = IO(Output(Bool()))
  p := a ^ b
  g := a & b
}
object GeneratePG{
  def apply(a:Bool, b:Bool):(Bool,Bool)={
    val pg = Module(new GeneratePG)
    pg.a := a
    pg.b := b
    (pg.p, pg.g)
  }
}

class PrefixAdd2 extends Module{
  val p0 = IO(Input(Bool()))
  val g0 = IO(Input(Bool()))
  val p1 = IO(Input(Bool()))
  val g1 = IO(Input(Bool()))

  val pOut = IO(Output(Bool()))
  val gOut = IO(Output(Bool()))

  pOut := p0 && p1
  gOut := (g0 && p1) || g1
}
object PrefixAdd2{
  def apply(p0:Bool, g0:Bool, p1:Bool, g1:Bool):(Bool,Bool)={
    val add = Module(new PrefixAdd2)
    add.p0 := p0
    add.g0 := g0
    add.p1 := p1
    add.g1 := g1
    (add.pOut, add.gOut)
  }
}
class PrefixAdd3 extends Module{
  val p0 = IO(Input(Bool()))
  val g0 = IO(Input(Bool()))
  val p1 = IO(Input(Bool()))
  val g1 = IO(Input(Bool()))
  val p2 = IO(Input(Bool()))
  val g2 = IO(Input(Bool()))

  val pOut = IO(Output(Bool()))
  val gOut = IO(Output(Bool()))

  pOut := p0 && p1 && p2
  gOut := (g0 && p1 && p2) || (g1 && p2) || g2
}
object PrefixAdd3{
  def apply(p0:Bool, g0:Bool, p1:Bool, g1:Bool, p2:Bool, g2:Bool):(Bool,Bool)={
    val add = Module(new PrefixAdd3)
    add.p0 := p0
    add.g0 := g0
    add.p1 := p1
    add.g1 := g1
    add.p2 := p2
    add.g2 := g2
    (add.pOut, add.gOut)
  }
}

class PrefixAdd4 extends Module{
  val p0 = IO(Input(Bool()))
  val g0 = IO(Input(Bool()))
  val p1 = IO(Input(Bool()))
  val g1 = IO(Input(Bool()))
  val p2 = IO(Input(Bool()))
  val g2 = IO(Input(Bool()))
  val p3 = IO(Input(Bool()))
  val g3 = IO(Input(Bool()))

  val pOut = IO(Output(Bool()))
  val gOut = IO(Output(Bool()))

  pOut := p0 && p1 && p2 && p3
  gOut :=  (g0 && p1 && p2 && p3) || (g1 && p2 && p3) || (g2 && p3) || g3
}
object PrefixAdd4{
  def apply(p0:Bool, g0:Bool, p1:Bool, g1:Bool, p2:Bool, g2:Bool,p3:Bool, g3:Bool):(Bool,Bool)={
    val add = Module(new PrefixAdd4)
    add.p0 := p0
    add.g0 := g0
    add.p1 := p1
    add.g1 := g1
    add.p2 := p2
    add.g2 := g2
    add.p3 := p3
    add.g3 := g3
    (add.pOut, add.gOut)
  }
}

class Cgen extends Module{
  val p = IO(Input(Bool()))
  val g = IO(Input(Bool()))
  val cin = IO(Input(Bool()))
  val out = IO(Output(Bool()))

  out := g || (p & cin)
}

object Cgen{
  def apply(p:Bool, g:Bool, cin:Bool):Bool={
    val m = Module(new Cgen)
    m.p := p
    m.g := g
    m.cin := cin
    m.out
  }
}

