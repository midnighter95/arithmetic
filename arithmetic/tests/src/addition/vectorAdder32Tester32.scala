package addition

import chisel3._
import chiseltest._
import utest._

import scala.util.Random

object vectorAdder32Tester32 extends TestSuite with ChiselUtestTester {
  def tests: Tests = Tests {
    test("vectorAdder32 should pass") {
      def testcase(width: Int): Unit = {

        val a = BigInt(32, Random)
        val b = BigInt(32, Random)
        val c = BigInt(1)

        def toHex(a: BigInt) = {
          Seq.fill(8 - a.toString(16).length)("0").mkString("") + a.toString(16)
        }


        val z = a + b + c
        val overflow = z >= BigInt("ffffffff",16)

        val z_expect = if(overflow) z - BigInt("100000000",16) else z

        val aInput = "h"+toHex(a)
        val bInput = "h"+toHex(b)
        val cInput = "b" + c.toString + c.toString + c.toString + c.toString
        val ov_expect= if(overflow) "b1" else "b0"

        // test
        testCircuit(
          new vectorAdder32,
          Seq(chiseltest.internal.NoThreadingAnnotation, chiseltest.simulator.WriteVcdAnnotation)
        ) { dut: vectorAdder32 =>
//                    println("a = " + a.toString(10))
//                    println("aInput=" + aInput)
//                    println("b = " + b.toString(10))
//                    println("bInput=" + bInput)
//                    println("c = " + c)
//                    println("cInput=" + cInput)
//                    println("z_expect = " + z_expect)
//                    println("overflow = " + overflow)
          dut.clock.setTimeout(0)
          dut.a.poke(aInput.U)
          dut.b.poke(bInput.U)
          dut.cin.poke(cInput.U)
          dut.sew.poke("b100".U)
          dut.clock.step(1)
          dut.z.expect(z_expect.U)
          dut.cout.expect(ov_expect.U)


        }
      }


//      for (i <- 1 to 50) {
//        testcase(32)
//      }

    }
  }
}
