package multiplier

import chisel3._
import chiseltest._
import utest._

import scala.util.Random

object M32S8Tester extends TestSuite with ChiselUtestTester {
  def tests: Tests = Tests {
    test("M32S8 should pass") {
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

        val a0 = BigInt(7, Random) * (if (Random.nextInt(2) == 1) 1 else -1)
        val b0 = BigInt(7, Random) * (if (Random.nextInt(2) == 1) 1 else -1)
        val a1 = BigInt(7, Random) * (if (Random.nextInt(2) == 1) 1 else -1)
        val b1 = BigInt(7, Random) * (if (Random.nextInt(2) == 1) 1 else -1)
        val a2 = BigInt(7, Random) * (if (Random.nextInt(2) == 1) 1 else -1)
        val b2 = BigInt(7, Random) * (if (Random.nextInt(2) == 1) 1 else -1)
        val a3 = BigInt(7, Random) * (if (Random.nextInt(2) == 1) 1 else -1)
        val b3 = BigInt(7, Random) * (if (Random.nextInt(2) == 1) 1 else -1)
        val z0 = a0 * b0
        val z1 = a1 * b1
        val z2 = a2 * b2
        val z3 = a3 * b3


        val aInput = "b" +
          getCircuitValue(a3, 8) +
          getCircuitValue(a2, 8) +
          getCircuitValue(a1, 8) +
          getCircuitValue(a0, 8)
        val bInput = "b" +
          getCircuitValue(b3, 8) +
          getCircuitValue(b2, 8) +
          getCircuitValue(b1, 8) +
          getCircuitValue(b0, 8)
        val z_expect = "b" +
          getCircuitValue(z3, 16) +
          getCircuitValue(z2, 16) +
          getCircuitValue(z1, 16) +
          getCircuitValue(z0, 16)

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
          dut.sew.poke("b001".U)
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
