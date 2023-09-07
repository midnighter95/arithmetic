package addition.csa

import chisel3._
import chisel3.util._

class csa52(width: Int) extends Module{
  val in = IO(Input(Vec(5, UInt(width.W))))
  val out = IO(Output(Vec(2, UInt((width+2).W))))
  val result = IO(Output(UInt((width+2).W)))

  val csa53 = addition.csa.c53(in)
  val csa32 = addition.csa.c32(VecInit(csa53(0)<<1, csa53(1)<<1, csa53(2)))
  result := csa32(1) +& (csa32(0)<<1.U).asUInt
  /** carry */
  out(0) := csa32(0)
  /** sum */
  out(1) := csa32(1)
}

object csa52 {
  def apply(
             width:     Int,
           )(in:         Vec[UInt]
           ): (UInt, UInt) = {
    val csa52 = Module(new csa52(width))
    // This need synthesis tool to do constant propagation
    csa52.in := in
    (csa52.out(0), csa52.out(1))
  }
}