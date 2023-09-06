package addition

import chisel3._
import chiseltest._
import utest._
import csa._

import scala.util.Random

object Compressor42Tester extends TestSuite with ChiselUtestTester {
  def tests: Tests = Tests {
    test("CSACompressor4_2 should pass") {
      def testcase(width: Int): Unit = {

        val in0 = Random.nextInt(2)
        val in1 = Random.nextInt(2)
        val in2 = Random.nextInt(2)
        val in3 = Random.nextInt(2)
        val in4 = Random.nextInt(2)

        val Sum = in0 + in1 + in2 + in3 + in4

        var sum,carry,cout = 0

        val sele = if(in0 == in1) 0 else 1
        val cout_expect = if(sele==1) in2 else in0

        val result = Sum match {
          case 5 => (1,1,1)
          case 4 => (1,1,0)
          case 3 => (cout_expect, 1 - cout_expect, 1)
          case 2 => (cout_expect, 1 - cout_expect, 0)
          case 1 => (0, 0, 1)
          case 0 => (0, 0, 0)
          case _ => (0, 0, 0)
        }




        val in0Input = "b" + in0.toBinaryString
        val in1Input = "b" + in1.toBinaryString
        val in2Input = "b" + in2.toBinaryString
        val in3Input = "b" + in3.toBinaryString
        val in4Input = "b" + in4.toBinaryString



        // test
        testCircuit(
          new CSACompressor4_2,
          Seq(chiseltest.internal.NoThreadingAnnotation, chiseltest.simulator.WriteVcdAnnotation)
        ) { dut:  CSACompressor4_2 =>
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
          dut.in(0).poke(in0Input.U)
          dut.in(1).poke(in1Input.U)
          dut.in(2).poke(in2Input.U)
          dut.in(3).poke(in3Input.U)
          dut.cin.poke(in4Input.U)
          dut.clock.step(1)
          dut.out(0).expect(result._2.U)
          dut.out(1).expect(result._3.U)
          dut.cout.expect(result._1.U)


        }
      }


//      for (i <- 1 to 10) {
//        testcase(4)
//      }

    }
  }
}