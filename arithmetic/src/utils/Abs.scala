package utils

import chisel3._

// when signIn = 1, do abs; else do nothing
class Abs(n: Int) extends Module {
  val io = IO(new Bundle() {
    val aIn = Input(SInt(n.W))
    val bIn = Input(SInt(n.W))
    val signIn = Input(Bool())
    val aOut = Output(UInt(n.W))
    val bOut = Output(UInt(n.W))
    val aSign = Output(Bool())
    val bSign = Output(Bool())
    //debug
  })
  val a = Wire(SInt(n.W))
  val b = Wire(SInt(n.W))
  val aSign = io.aIn(n-1)
  val bSign = io.bIn(n-1)
  a := io.aIn
  b := io.bIn
  io.aOut := Mux(io.signIn, Mux(aSign, -a, a), a).asUInt
  io.bOut := Mux(io.signIn, Mux(bSign, -b, b), b).asUInt
  io.aSign := Mux(io.signIn,aSign,false.B)
  io.bSign := Mux(io.signIn,bSign,false.B)
}
