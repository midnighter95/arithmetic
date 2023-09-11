package multiplier

import chisel3._
import chiseltest._
import utest._

import scala.util.Random

object M32S32Tester extends TestSuite with ChiselUtestTester {
  def tests: Tests = Tests {
    test("M32S32 should pass") {
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

        val a = BigInt(31,Random) * (if (Random.nextInt(2) == 1) 1 else -1)
        val b = BigInt(31,Random) * (if (Random.nextInt(2) == 1) 1 else -1)
        val z = a * b

        val aInput   = "b" + getCircuitValue(a, 32)
        val bInput   = "b" + getCircuitValue(b, 32)
        val z_expect = "b" + getCircuitValue(z, 64)

        // test
        testCircuit(
          new VectorMultiplier32,
          Seq(chiseltest.internal.NoThreadingAnnotation, chiseltest.simulator.WriteVcdAnnotation)
        ) { dut: VectorMultiplier32 =>
//                    println("a = " + a)
//                    println("aInput=" + aInput)
//                    println("b = " + b)
//                    println("bInput=" + bInput)
//
//                    println("z_expect = " + z)
//                    println("z_expectUInt = " + z_expect)

          dut.clock.setTimeout(0)
          dut.a.poke(aInput.U)
          dut.b.poke(bInput.U)
          dut.unsign.poke(false.B)
          dut.sew.poke("b100".U)
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
