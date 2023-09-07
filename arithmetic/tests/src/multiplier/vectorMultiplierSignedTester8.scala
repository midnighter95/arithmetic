package multiplier

import chisel3._
import chiseltest._
import utest._
import math._

import scala.util.Random

object vectorMultiplierSignedTester8 extends TestSuite with ChiselUtestTester {
  def tests: Tests = Tests {
    test("VectorMultiplier8Signed should pass") {
      def testcase(width: Int): Unit = {

        val n = 2 ^ 7 - 1

        val a: Int = Random.nextInt(n) * (if(Random.nextInt(2)==0) -1 else 1)
        val b = Random.nextInt(n) * (if(Random.nextInt(2)==0) -1 else 1)

//        val a = -2
//        val b = -3


        val z = a * b

        def complement(x : Int, width:Int) = {
          if(x>=0){
            x
          } else{
            if(width==8)256 + x
            else 65536 + x
          }
        }

        def getInput(x:Int) = {
          "b" + x.toBinaryString
        }


        val aInput   = getInput(complement(a, 8))
        val bInput   = getInput(complement(b, 8))
        val z_expect = getInput(complement(z, 16))

        // test
        testCircuit(
          new VectorMultiplier(width),
          Seq(chiseltest.internal.NoThreadingAnnotation, chiseltest.simulator.WriteVcdAnnotation)
        ) { dut: VectorMultiplier =>
//          println("a = " + a)
//          println("aInput=" + aInput)
//          println("b = " + b)
//          println("bInput=" + bInput)
//          println("z_expect = " + z)
//          println("z_expectUInt = " + z_expect)

          dut.clock.setTimeout(0)
          dut.a.poke(aInput.U)
          dut.b.poke(bInput.U)
          dut.sign.poke(true.B)
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
