package addition.csa

import chisel3._
import chisel3.util._

class CSACompressor4_2 extends Module{
  val in = IO(Input(Vec(4,UInt(1.W))))
  val cin = IO(Input(UInt(1.W)))
  val out = IO(Output(Vec(2,UInt(1.W))))
  val cout = IO(Output(UInt(1.W)))

  val ab = in(0) ^ in(1)
  val cd = in(2) ^ in(3)
  val abcd = ab ^ cd
  // sum
  out(1) := abcd ^ cin
  // carry
  out(0) := Mux(abcd.asBool, cin, in(3))
  cout := Mux(ab.asBool, in(2), in(0))
}