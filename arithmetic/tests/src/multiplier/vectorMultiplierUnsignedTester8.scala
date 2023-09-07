package multiplier

import chisel3._
import chiseltest._
import utest._

import scala.util.Random

object vectorMultiplierUnsignedTester8 extends TestSuite with ChiselUtestTester {
  def tests: Tests = Tests {
    test("VectorMultiplier8unsigned should pass") {
      def testcase(width: Int): Unit = {

        val n = 2 ^ 8 - 1

//        val a: Int = Random.nextInt(n) * (if(Random.nextInt(2)==0) -1 else 1)
//        val b = Random.nextInt(n) * (if(Random.nextInt(2)==0) -1 else 1)
        val a: Int = Random.nextInt(n)
        val b      = Random.nextInt(n)

        val z = a * b

        def complementNine(x : Int) = {
          if(x>=0){
            "b" + x.toBinaryString
          } else{
            val comple = 2^9 + x
            "b" + comple.toBinaryString
          }
        }

        val aInput = complementNine(a)
        val bInput = complementNine(b)
        val z_expect = complementNine(z)

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
