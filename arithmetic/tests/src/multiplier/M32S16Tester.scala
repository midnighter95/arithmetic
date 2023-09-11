package multiplier

import chisel3._
import chiseltest._
import utest._

import scala.util.Random

object M32S16Tester extends TestSuite with ChiselUtestTester {
  def tests: Tests = Tests {
    test("M32S16 should pass") {
      def testcase(width: Int): Unit = {
        def getCircuitValue(x: BigInt, width: Int) = {
          val complement = if (x >= 0) {
            x
          } else {
            width match {
              case 8 => BigInt(256) + x
              case 16 => BigInt(65536) + x
              case 32 => (BigInt(2).pow(32) + x)
              case 64 => (BigInt(2).pow(64) + x)
            }
          }
          Seq.fill(width - complement.toString(2).length)("0").mkString("") + complement.toString(2)
        }

        val a0 = BigInt(15, Random) * (if (Random.nextInt(2) == 1) 1 else -1)
        val b0 = BigInt(15, Random) * (if (Random.nextInt(2) == 1) 1 else -1)
        val a1 = BigInt(15, Random) * (if (Random.nextInt(2) == 1) 1 else -1)
        val b1 = BigInt(15, Random) * (if (Random.nextInt(2) == 1) 1 else -1)
        val z0 = a0 * b0
        val z1 = a1 * b1

        val aInput = "b"   + getCircuitValue(a1, 16) + getCircuitValue(a0, 16)
        val bInput = "b"   + getCircuitValue(b1, 16) + getCircuitValue(b0, 16)
        val z_expect = "b" + getCircuitValue(z1, 32) + getCircuitValue(z0, 32)

        // test
        testCircuit(
          new VectorMultiplier32,
          Seq(chiseltest.internal.NoThreadingAnnotation, chiseltest.simulator.WriteVcdAnnotation)
        ) { dut: VectorMultiplier32 =>
//          println("a = " + a)
//          println("aInput=" + aInput)
//          println("b = " + b)
//          println("bInput=" + bInput)
//
//          println("z_expect = " + z)
//          println("z_expectUInt = " + z_expect)

          dut.clock.setTimeout(0)
          dut.a.poke(aInput.U)
          dut.b.poke(bInput.U)
          dut.unsign.poke(false.B)
          dut.sew.poke("b010".U)
          dut.clock.step(1)
          dut.z.expect(z_expect.U)
        }
      }


      for (i <- 1 to 20) {
        testcase(32)
      }

    }
  }
}
