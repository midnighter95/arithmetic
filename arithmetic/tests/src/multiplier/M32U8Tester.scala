package multiplier

import chisel3._
import chiseltest._
import utest._

import scala.util.Random

object M32U8Tester extends TestSuite with ChiselUtestTester {
  def tests: Tests = Tests {
    test("M32U8 should pass") {
      def testcase(width: Int): Unit = {
        def getCircuitValue(x: Int, width: Int) = {
          val complement = if (x >= 0) {
            x
          } else {
            if (width == 8) 256 + x
            else 65536 + x
          }
           Seq.fill(width - complement.toBinaryString.length)("0").mkString("") + complement.toBinaryString
        }

        val n = 255

        val a0 = Random.nextInt(n)
        val b0 = Random.nextInt(n)
        val a1 = Random.nextInt(n)
        val b1 = Random.nextInt(n)
        val a2 = Random.nextInt(n)
        val b2 = Random.nextInt(n)
        val a3 = Random.nextInt(n)
        val b3 = Random.nextInt(n)
        val z0 = a0 * b0
        val z1 = a1 * b1
        val z2 = a2 * b2
        val z3 = a3 * b3

        val aInput   = "b" + getCircuitValue(a3, 8) + getCircuitValue(a2, 8) + getCircuitValue(a1, 8) + getCircuitValue(a0, 8)
        val bInput   = "b" + getCircuitValue(b3, 8) + getCircuitValue(b2, 8) + getCircuitValue(b1, 8) + getCircuitValue(b0, 8)
        val z_expect = "b" + getCircuitValue(z3, 16) + getCircuitValue(z2, 16) + getCircuitValue(z1, 16) + getCircuitValue(z0, 16)

        // test
        testCircuit(
          new Multiplier32,
          Seq(chiseltest.internal.NoThreadingAnnotation, chiseltest.simulator.WriteVcdAnnotation)
        ) { dut: Multiplier32 =>
//          println("a0 = " + a0)
//          println("aInput=" + aInput)
//          println("b0 = " + b0)
//          println("bInput=" + bInput)
//          println("z0 = " + z0)
//          println("a1 = " + a1)
//          println("b1 = " + b1)
//          println("z1 = " + z1)
          dut.clock.setTimeout(0)
          dut.a.poke(aInput.U)
          dut.b.poke(bInput.U)
          dut.sew.poke("b001".U)
          dut.clock.step(1)
          dut.z.expect(z_expect.U)


        }
      }


      for (i <- 1 to 20) {
        testcase(8)
      }

    }
  }
}
