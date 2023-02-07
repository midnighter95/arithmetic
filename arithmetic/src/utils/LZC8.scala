package utils

import division.srt._
import addition.csa.CarrySaveAdder
import addition.csa.common.CSACompressor3_2
import chisel3._
import chisel3.internal.firrtl.Width
import chisel3.util._
import spire.math
import utils.leftShift
/** ~z = zerocount
  * if allzero, v = 0 z = b000
  */
class LZC8 extends Module{
  val io = IO(new Bundle{
    val a = Input(UInt(8.W))
    val z = Output(UInt(3.W))
    val v = Output(UInt(1.W))
  })
  //val a = Wire(UInt(8.W))
  val a = io.a
  val z0 : UInt = (!(a(7) | (!a(6)) & a(5))) & ((a(6) | a(4)) | !(a(3) | (!a(2) & a(1))))
  val z1 : UInt = !(a(7) | a(6)) & ((a(5) | a(4)) | !(a(3) | a(2)))
  val z2 : UInt = !(a(7) | a(6)) & !(a(5) | a(4))
  val _v : UInt = !(!(a(7) | a(6)) & !(a(5) | a(4))) | !(!(a(3) | a(2)) & !(a(1) | a(0)))

  io.z := Cat(~z2,~z1,~z0)
  io.v := _v
}

