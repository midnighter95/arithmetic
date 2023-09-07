package multiplier

import chisel3._
import chiseltest._
import utest._

import scala.util.Random

object vectorMultiplierUnsignedTester8 extends TestSuite with ChiselUtestTester {
  def tests: Tests = Tests {
    test("VectorMultiplier8unsigned should pass") {
      def testcase(width: Int): Unit = {
        def getCircuitValue(x: Int, width: Int) = {
          val complement = if (x >= 0) {
            x
          } else {
            if (width == 8) 256 + x
            else 65536 + x
          }
          "b" + Seq.fill(width - complement.toBinaryString.length)("0").mkString("") + complement.toBinaryString
        }

        val n = 2 ^ 8 - 1

        val a: Int = Random.nextInt(n)
        val b      = Random.nextInt(n)
        val z = a * b

        val aInput = getCircuitValue(a, 8)
        val bInput = getCircuitValue(b, 8)
        val z_expect = getCircuitValue(z,16)

        // test
        testCircuit(
          new VectorMultiplier(width),
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
          dut.z.expect(z_expect.U)


        }
      }


      for (i <- 1 to 10) {
        testcase(8)
      }

    }
  }
}
