package addition

import chisel3._

class ThreeCombineG extends Module {
  val g0 = IO(Input(Bool()))
  val p1 = IO(Input(Bool()))
  val g1 = IO(Input(Bool()))
  val p2 = IO(Input(Bool()))
  val g2 = IO(Input(Bool()))

  val gOut = IO(Output(Bool()))

  gOut := (g0 && p1 && p2) || (g1 && p2) || g2
}

object ThreeCombineG {
  def apply(g0:Bool, p1:Bool, g1:Bool, p2:Bool, g2:Bool): Bool = {
    val threeCombineG = Module(new ThreeCombineG)
    threeCombineG.g0 := g0
    threeCombineG.p1 := p1
    threeCombineG.g1 := g1
    threeCombineG.p2 := p2
    threeCombineG.g2 := g2
    threeCombineG.gOut
  }
}