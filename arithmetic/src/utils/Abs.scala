package utils

import chisel3._

// when signIn = 1, do abs; else do nothing
class Abs extends Module {
  val io = IO(new Bundle() {
    val aIn = Input(UInt(32.W))
    val bIn = Input(UInt(32.W))
    val signIn = Input(Bool())
    val aOut = Output(UInt(32.W))
    val bOut = Output(UInt(32.W))
    val aSign = Output(Bool())
    val bSign = Output(Bool())
  })
  val a = Wire(UInt(32.W))
  val b = Wire(UInt(32.W))
  val aSign = io.aIn(31)
  val bSign = io.bIn(31)
  a := io.aIn
  b := io.bIn

  io.aOut := Mux(io.signIn, Mux(aSign, addition.prefixadder.koggeStone((~a).asUInt,1.U,false.B), a), a)
  io.bOut := Mux(io.signIn, Mux(bSign, addition.prefixadder.koggeStone((~b).asUInt,1.U,false.B), b), b)
  io.aSign := aSign
  io.bSign := bSign
}
