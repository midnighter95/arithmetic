package addition

import chisel3._

class TwoCombineP extends Module{
  val a = IO(Input(Bool()))
  val b = IO(Input(Bool()))
  val z = IO(Output(Bool()))

  z := a && b
}

object TwoCombineP {
  def apply(a:Bool, b:Bool):Bool = {
    val twoCombineP = Module(new TwoCombineP)
    twoCombineP.a := a
    twoCombineP.b := b
    twoCombineP.z
  }
}