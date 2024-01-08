package addition

import chisel3._

class ThreeCombineP extends Module {
  val p0 = IO(Input(Bool()))
  val p1 = IO(Input(Bool()))
  val p2 = IO(Input(Bool()))

  val pOut = IO(Output(Bool()))

  pOut := p0 && p1 && p2
}

object ThreeCombineP {
  def apply(p0:Bool, p1:Bool, p2:Bool): Bool = {
    val threeCombineP = Module(new ThreeCombineP)
    threeCombineP.p0 := p0
    threeCombineP.p1 := p1
    threeCombineP.p2 := p2
    threeCombineP.pOut
  }
}