package utils

import chisel3._

// when signIn = 1, do abs; else do nothing
class Abs extends Module {
  val io = IO(new Bundle() {
    val aIn = Input(SInt(32.W))
    val bIn = Input(SInt(32.W))
    val signIn = Input(Bool())
    val aOut = Output(UInt(32.W))
    val bOut = Output(UInt(32.W))
    val aSign = Output(Bool())
    val bSign = Output(Bool())
    //debug
  })
  val a = Wire(SInt(32.W))
  val b = Wire(SInt(32.W))
  val aSign = io.aIn(31)
  val bSign = io.bIn(31)
  a := io.aIn
  b := io.bIn
  io.aOut := Mux(io.signIn, Mux(aSign, -a, a), a).asUInt
  io.bOut := Mux(io.signIn, Mux(bSign, -b, b), b).asUInt
  io.aSign := Mux(io.signIn,aSign,false.B)
  io.bSign := Mux(io.signIn,bSign,false.B)
}
