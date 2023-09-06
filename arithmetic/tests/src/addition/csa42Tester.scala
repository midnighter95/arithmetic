package addition

import chisel3._
import chiseltest._
import utest._
import addition.csa._
import addition.csa.common._

import scala.util.Random

object csa42Tester extends TestSuite with ChiselUtestTester {
  def tests: Tests = Tests {
    test("csa42 should pass") {
      def testcase(width: Int): Unit = {

        val n = 2^width -1


        val in0 = Random.nextInt(n)
        val in1 = Random.nextInt(n)
        val in2 = Random.nextInt(n)
        val in3 = Random.nextInt(n)

        val z = in0 + in1 + in2 + in3

        val in0Input = "b" + in0.toBinaryString
        val in1Input = "b" + in1.toBinaryString
        val in2Input = "b" + in2.toBinaryString
        val in3Input = "b" + in3.toBinaryString

        val result_expect = "b" + z.toBinaryString

        // test
        testCircuit(
          new csa42(width),
          Seq(chiseltest.internal.NoThreadingAnnotation, chiseltest.simulator.WriteVcdAnnotation)
        ) { dut:  csa42 =>
          dut.clock.setTimeout(0)
          dut.in(0).poke(in0Input.U)
          dut.in(1).poke(in1Input.U)
          dut.in(2).poke(in2Input.U)
          dut.in(3).poke(in3Input.U)
          dut.clock.step(1)
          dut.result.expect(result_expect.U)

        }
      }


      for (i <- 1 to 20) {
        testcase(9)
      }

    }
  }
}