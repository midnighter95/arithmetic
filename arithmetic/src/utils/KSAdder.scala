package utils

import chisel3._
import chisel3.internal.firrtl.Width
import chisel3.util._

class KSAdder(n:Int) extends Module {
  val io = IO(new Bundle() {
    val a = Input(SInt(n.W))
    val b = Input(SInt(n.W))
    val s = Output(SInt((n+1).W))
  })
  val sum = Wire(SInt((n+1).W))
  sum := addition.prefixadder.koggeStone(io.a.asUInt, io.b.asUInt, false.B).asSInt
  io.s := sum
}
