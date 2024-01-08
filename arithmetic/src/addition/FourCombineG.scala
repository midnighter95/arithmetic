package addition

import chisel3._

class FourCombineG extends Module {
  val g0 = IO(Input(Bool()))
  val p1 = IO(Input(Bool()))
  val g1 = IO(Input(Bool()))
  val p2 = IO(Input(Bool()))
  val g2 = IO(Input(Bool()))
  val p3 = IO(Input(Bool()))
  val g3 = IO(Input(Bool()))

  val gOut = IO(Output(Bool()))

  gOut :=  (g0 && p1 && p2 && p3) || (g1 && p2 && p3) || (g2 && p3) || g3
}

object FourCombineG {
  def apply(g0:Bool, p1:Bool, g1:Bool, p2:Bool, g2:Bool,p3:Bool, g3:Bool): Bool = {
    val fourCombineG = Module(new FourCombineG)
    fourCombineG.g0 := g0
    fourCombineG.p1 := p1
    fourCombineG.g1 := g1
    fourCombineG.p2 := p2
    fourCombineG.g2 := g2
    fourCombineG.p3 := p3
    fourCombineG.g3 := g3
    fourCombineG.gOut
  }
}