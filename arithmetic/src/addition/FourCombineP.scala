package addition

import chisel3._

class FourCombineP extends Module {
  val p0 = IO(Input(Bool()))
  val p1 = IO(Input(Bool()))
  val p2 = IO(Input(Bool()))
  val p3 = IO(Input(Bool()))

  val pOut = IO(Output(Bool()))

  pOut := p0 && p1 && p2 && p3
}

object FourCombineP {
  def apply(p0:Bool, p1:Bool, p2:Bool, p3:Bool): Bool = {
    val fourCombineP = Module(new FourCombineP)
    fourCombineP.p0 := p0
    fourCombineP.p1 := p1
    fourCombineP.p2 := p2
    fourCombineP.p3 := p3
    fourCombineP.pOut
  }
}