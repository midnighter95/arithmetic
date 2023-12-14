package addition.prefixadder

import addition.FullAdder
import chisel3._
import chisel3.util._
import logger.LazyLogging

trait PrefixSum extends LazyLogging {

  /** Generate the final prefix tree
    * @param summands zero layer of prefix tree
    * @return final prefix tree
    */
  def apply(summands: Seq[(Bool, Bool)]): Vector[(Bool, Bool)]

  /** associate summands, generate node in next layer
    * @param leaf summands need to be associated
    * @return associated node in next layer
    */
  def associativeOp(leaf: Seq[(Bool, Bool)]): (Bool, Bool)

  /** function to generate zero layer, normally the P and G definition
    * @param a input of adder
    * @param b input of adder
    * @return zero layer of prefix tree
    */
  def zeroLayer(a: Seq[Bool], b: Seq[Bool]): Seq[(Bool, Bool)]
}

class AdderBundle(val w: Int) extends Bundle {
  val a: UInt = Input(UInt(w.W))
  val b: UInt = Input(UInt(w.W))
  val z: UInt = Output(UInt((w + 1).W))
}

class AdderInputBundle(val w: Int) extends Bundle {
  val a: UInt = UInt(w.W)
  val b: UInt = UInt(w.W)
}

class AdderOutputBundle(val w: Int) extends Bundle {
  val z: UInt = UInt((w + 1).W)
}

class PrefixAdder(val width: Int, prefixSum: PrefixSum) extends FullAdder {
  override val desiredName: String = this.getClass.getSimpleName + width.toString

  // Split up bit vectors into individual bits
  val as: Seq[Bool] = cin +: a.asBools
  val bs: Seq[Bool] = cin +: b.asBools

  /** Type of pair is P and G
    * @todo How to abstract this with Ling Adder?
    */
  val pairs: Seq[(Bool, Bool)] = prefixSum.zeroLayer(as, bs)
  pairs.map{
    p => {
      dontTouch(p._1)
      dontTouch(p._2)
    }
  }

  /** Final prefix tree generated by [[prefixSum]] */
  val pgs: Vector[(Bool, Bool)] = prefixSum(pairs)

  // Carries are Generates from end of prefix sum
  val cs: Vector[Bool] = false.B +: pgs.map(_._2)

  // Sum requires propagate bits from first step
  val ps: Seq[Bool] = pairs.map(_._1) :+ false.B // Include P for overflow
  // Si = Pi xor Ci-1
  val sum: Seq[Bool] = ps.zip(cs).map { case (p, c) => p ^ c }

  // Recombine bits into bitvector
  val s: UInt = VecInit(sum).asUInt

  cout := s.head(1)
  z := s(width, 1)
}
