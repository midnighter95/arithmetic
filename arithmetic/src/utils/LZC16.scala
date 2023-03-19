package utils

import chisel3._
import chisel3.util._

/** 16-bits Leading Zero Counter using 2 LZC8
  *
  * z for zero number
  * v for all-zero indicator
  * zero number = ~z
  */
class LZC16 extends Module{
  val io = IO(new Bundle{
    val a = Input(UInt(16.W))
    val z = Output(UInt(4.W))
    val v = Output(UInt(1.W))
  })
  val L0 = Module(new LZC8)
  val L1 = Module(new LZC8)
  L1.io.a := io.a(15,8)
  L0.io.a := io.a(7,0)

  val flag = L1.io.v.asBool
  val z3 = Mux(flag, 1.U, 0.U)
  val z2 = Mux(flag, L1.io.z(2), L0.io.z(2))
  val z1 = Mux(flag, L1.io.z(1), L0.io.z(1))
  val z0 = Mux(flag, L1.io.z(0), L0.io.z(0))


  io.z := Cat(z3,z2,z1,z0)
  io.v := L1.io.v | L0.io.v

}

