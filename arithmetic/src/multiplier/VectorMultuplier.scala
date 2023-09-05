package multiplier

import addition.prefixadder.PrefixSum
import addition.prefixadder.common.BrentKungSum
import chisel3._
import chisel3.util._
import chisel3.experimental.FixedPoint
import utils.extend

class VectorMultiplier extends Module {
  val aWidth = 9
  val bWidth = aWidth
  val outWidth = bWidth * 2
  val radixLog2 = 2
  val signed = true

  val a: UInt = IO(Input(UInt(aWidth.W)))
  val b: UInt = IO(Input(UInt(aWidth.W)))
  val z: UInt = IO(Output(UInt(outWidth.W)))

  val recMultiplerVec: Vec[SInt] = Booth
    .recode(aWidth)(radixLog2, signed = true)(a)


  val recMultiplerWire = recMultiplerVec.asUInt
  dontTouch(recMultiplerWire)

  // produce Seq(b, 2 * b, ..., 2^digits * b), output width = width + radixLog2 - 1
  val bMultipleWidth = (bWidth + radixLog2 - 1).W

  /**
    * -2
    * -1
    * 0
    * 1
    * 2
    *
    *
    * */
  def prepareBMultiples: Seq[UInt] = {

      val lowerMultiples = b
      val higherMultiples = b<<2
    lowerMultiples ++ higherMultiples

    }

  val bMultiples = prepareBMultiples(radixLog2 - 1)
  val encodedWidth = (radixLog2 + 1).W

  val b0 = bMultiples(0).asUInt
  dontTouch(b0)


  z := 0.U
}