package addition

import chisel3._
import chiseltest._
import org.bouncycastle.util.test.FixedSecureRandom.BigInteger
import utest._

import scala.util.Random

object vectorAdder64Tester64 extends TestSuite with ChiselUtestTester {
  def tests: Tests = Tests {
    test("should pass") {
      def testcase(width: Int): Unit = {

        val a = Seq(BigInt(64, Random))
        val b = Seq(BigInt(64, Random))
        val c = Seq(BigInt(1, Random))

        def toHex(a: BigInt) = {
          Seq.fill(16 - a.toString(16).length)("0").mkString("") + a.toString(16)
        }

        def add(a: Tuple2[BigInt, BigInt]) = (a._1 + a._2)

        val z = a.zip(b).map(add).zip(c).map(add)


        val z_exp: Seq[BigInt] = z.map {
          c => {
            val overflow = c > BigInt("ffffffffffffffff", 16)
            if (overflow) c - BigInt("10000000000000000", 16) else c
          }
        }
        val overflowSeq = z.map {
          c => {
            val overflow = c > BigInt("ffffffffffffffff", 16)
            if (overflow) "1" else "0"
          }
        }

        val aInput = "h" + toHex(a(0))
        val bInput = "h" + toHex(b(0))
        val cInput = "b" + Seq.fill(8)(c(0)).mkString("")

        val z_expect = "h" + toHex(z_exp(0))


        val ov_expect = "b" + overflowSeq(0)

        // test
        testCircuit(
          new vectorAdder64,
          Seq(chiseltest.internal.NoThreadingAnnotation, chiseltest.simulator.WriteVcdAnnotation)
        ) { dut: vectorAdder64 =>
//          println("a = " + a)
//          println("b = " + b)
//          println("c = " + c)
//          println("z_exp = " + z_exp)
//          println("z_expect = " + z_expect)
          dut.clock.setTimeout(0)
          dut.a.poke(aInput.U)
          dut.b.poke(bInput.U)
          dut.cin.poke(cInput.U)
          dut.sew.poke("b1000".U)
          dut.clock.step(1)
          dut.z.expect(z_expect.U)
          dut.cout.expect(ov_expect.U)


        }
      }


//      for (i <- 1 to 100) {
//        testcase(32)
//      }

    }
  }
}
