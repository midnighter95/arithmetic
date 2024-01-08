package addition

import chisel3._

class Xor extends Module {
  val a = IO(Input(Bool()))
  val b = IO(Input(Bool()))
  val z = IO(Output(Bool()))

  z := a ^ b
}

object Xor {
  def apply(a: Bool, b: Bool): Bool = {
    val xor = Module(new Xor)
    xor.a := a
    xor.b := b
    xor.z
  }
}