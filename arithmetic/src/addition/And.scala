package addition

import chisel3._

class And extends Module{
  val a = IO(Input(Bool()))
  val b = IO(Input(Bool()))
  val z = IO(Output(Bool()))

  z := a && b
}

object And {
  def apply(a:Bool, b:Bool):Bool = {
    val and = Module(new And)
    and.a := a
    and.b := b
    and.z
  }

  def apply(a: Bool, b: Bool, index:Int): Bool = {
    val and = Module(new And).suggestName(s"g$index")
    and.a := a
    and.b := b
    and.z
  }
}