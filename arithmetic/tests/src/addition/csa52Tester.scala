package addition

import addition.csa._
import chisel3._
import chiseltest._
import utest._

import scala.util.Random

object csa52Tester extends TestSuite with ChiselUtestTester {
  def tests: Tests = Tests {
    test("csa42 should pass") {
      def testcase(width: Int): Unit = {

        val n = 2^width -1


        val in0 = Random.nextInt(n)
        val in1 = Random.nextInt(n)
        val in2 = Random.nextInt(n)
        val in3 = Random.nextInt(n)
        val in4 = Random.nextInt(n)

        val z = in0 + in1 + in2 + in3 + in4

        val in0Input = "b" + in0.toBinaryString
        val in1Input = "b" + in1.toBinaryString
        val in2Input = "b" + in2.toBinaryString
        val in3Input = "b" + in3.toBinaryString
        val in4Input = "b" + in4.toBinaryString

        val result_expect = "b" + z.toBinaryString

        // test
        testCircuit(
          new csa52(width),
          Seq(chiseltest.internal.NoThreadingAnnotation, chiseltest.simulator.WriteVcdAnnotation)
        ) { dut:  csa52 =>
          dut.clock.setTimeout(0)
          dut.in(0).poke(in0Input.U)
          dut.in(1).poke(in1Input.U)
          dut.in(2).poke(in2Input.U)
          dut.in(3).poke(in3Input.U)
          dut.in(4).poke(in4Input.U)
          dut.clock.step(1)
          dut.result.expect(result_expect.U)

        }
      }


      for (i <- 1 to 50) {
        testcase(8)
      }

    }
  }
}