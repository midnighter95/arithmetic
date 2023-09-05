package multiplier

import chisel3._
import chiseltest._
import utest._

import scala.util.Random

object vectorMultiplierTester8 extends TestSuite with ChiselUtestTester {
  def tests: Tests = Tests {
    test("VectorMultiplier8 should pass") {
      def testcase(width: Int): Unit = {

        val a = "b1011110"
        val b = "b1011110"

        val aInput = "b1011110"
        val bInput = "b1011110"

        // test
        testCircuit(
          new VectorMultiplier,
          Seq(chiseltest.internal.NoThreadingAnnotation, chiseltest.simulator.WriteVcdAnnotation)
        ) { dut: VectorMultiplier =>
          //          println("a = " + a)
          //          println("aInput=" + aInput)
          //          println("b = " + b)
          //          println("bInput=" + bInput)
          //          println("c = " + c)
          //          println("cInput=" + cInput)
          //          println("z_expectSeq = " + z_expectSeq.toString)
          //          println("z_expect = " + z_expect)
          //          println("overflow = " + overflow)
          dut.clock.setTimeout(0)
          dut.a.poke(aInput.U)
          dut.b.poke(bInput.U)
          dut.clock.step(1)
          dut.z.expect(0.U)


        }
      }


      for (i <- 1 to 2) {
        testcase(8)
      }

    }
  }
}
