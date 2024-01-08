package addition

import chisel3._

class TwoCombineG extends Module {
  val g0 = IO(Input(Bool()))
  val p1 = IO(Input(Bool()))
  val g1 = IO(Input(Bool()))

  val gOut = IO(Output(Bool()))

  gOut := (g0 && p1) || g1
}

object TwoCombineG {
  def apply(g0:Bool, p1:Bool, g1:Bool): Bool = {
    val twoCombineG = Module(new TwoCombineG)
    twoCombineG.p1 := p1
    twoCombineG.g0 := g0
    twoCombineG.g1 := g1
    twoCombineG.gOut
  }
}