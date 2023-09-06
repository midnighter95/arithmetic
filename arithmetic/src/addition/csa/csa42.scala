package addition.csa

import chisel3._
import chisel3.util._

import math._

class csa42(width: Int) extends Module{
  val in = IO(Input(Vec(4, UInt(width.W))))
  val out = IO(Output(Vec(2, UInt((width+1).W))))
  val result = IO(Output(UInt((width+2).W)))

  val compressor: Seq[CSACompressor4_2] = Seq.fill(width)(Module(new CSACompressor4_2))

  /** cout in order
    *
    * coutUInt(i) represents cout for compressor(i)
    */
  val coutUInt = VecInit(compressor.map(_.cout)).asUInt
  val cinVec: Seq[Bool] = Cat(coutUInt(width-2, 0), 0.U(1.W)).asBools
  val compressorAssign = compressor
    .zip(cinVec)
    .zipWithIndex
    .map{
      case ((c, cin), i) => {
        c.in(0) := in(0)(i)
        c.in(1) := in(1)(i)
        c.in(2) := in(2)(i)
        c.in(3) := in(3)(i)
        c.cin   := cin
      }
    }
  /** sum */
  out(1) := VecInit(compressor.map(_.out(1)) :+ coutUInt(width-1)).asUInt
  /** carry */
  out(0) := VecInit(compressor.map(_.out(0))).asUInt

  result := out(1) +& (out(0)<<1.U).asUInt
}