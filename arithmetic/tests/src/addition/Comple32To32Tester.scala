package addition

import chisel3._
import chiseltest._
import utest._

import scala.util.Random

object Comple32To32Tester extends TestSuite with ChiselUtestTester {
  def tests: Tests = Tests {
    test("complement32_32 should pass") {
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

        def getAbsCircuitValue(x: BigInt, width: Int) = {
          val abs = if (x >= 0) {
            x
          } else {
            -x
          }
          Seq.fill(width - abs.toString(2).length)("0").mkString("") + abs.toString(2)
        }

        val a0 = BigInt(31, Random) * (if (Random.nextInt(2) == 1) 1 else -1)


        val aInput = "b" +
          getCircuitValue(a0, 32)
        val z_expect = "b" +
          getAbsCircuitValue(a0, 32)

        // test
        testCircuit(
          new Abs32,
          Seq(chiseltest.internal.NoThreadingAnnotation, chiseltest.simulator.WriteVcdAnnotation)
        ) { dut: Abs32 =>
          //          println("a0 = " + a0)
          //          println("a1 = " + a1)
          //          println("aInput=" + aInput)
          dut.clock.setTimeout(0)
          dut.a.poke(aInput.U)
          dut.sew.poke("b100".U)
          dut.clock.step(1)
          dut.z.expect(z_expect.U)


        }
      }


//      for (i <- 1 to 20) {
//        testcase(8)
//      }

    }
  }
}
