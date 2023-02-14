package division.srt

import chisel3._
import utils._
import chiseltest.{ChiselUtestTester, _}
import utest.{TestSuite, _}

import scala.util.Random

object LZC16Test extends TestSuite with ChiselUtestTester{
  def tests: Tests = Tests {
    test("LZC16 should pass") {
      def testcase() = {
        val r: Int = Random.nextInt(16)
        val p = BigInt(r,Random)
        def zeroCount(x: BigInt): Int = {
          var flag = false
          var a: Int =15
          while (!flag && (a >= -1)) {
            flag = ((BigInt(1) << a) & x) != 0
            a = a - 1
          }
          15 - (a + 1)
        }
        val z_ex = if (p==0) 0 else 15-zeroCount(p)
        val v_ex = if (p==0) 0 else 1
//        println("Input = " + p.toBinaryString)
//        println("zeroCount = " + zeroCount(p))
//        println("z_ex=" + z_ex)
//        println("v_ex=" + v_ex)
        testCircuit(new LZC16,
          Seq(chiseltest.internal.NoThreadingAnnotation,
            chiseltest.simulator.WriteVcdAnnotation)) {
          dut : LZC16 =>
            dut.io.a.poke(p.U)
//            println("Z = " + dut.io.z.peek().litValue.toString())
//            println("V = " + dut.io.v.peek().litValue.toString())
            dut.io.z.expect(z_ex.U)
            dut.io.v.expect(v_ex.U)

        }
      }
      for (i <- 1 to 1) {
        testcase()
      }
    }
  }
}
