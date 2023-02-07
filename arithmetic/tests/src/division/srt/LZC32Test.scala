package division.srt

import chisel3._
import utils._
import chiseltest.{ChiselUtestTester, _}
import utest.{TestSuite, _}

import scala.util.Random

object LZC32Test extends TestSuite with ChiselUtestTester{
  def tests: Tests = Tests {
    test("LZC32 should pass") {
      def testcase() = {
        val r: Int = Random.nextInt(32)
        val p:Int = BigInt(r,Random).toInt
        def zeroCount(x: BigInt): Int = {
          var flag = false
          var a: Int =31
          while (!flag && (a >= -1)) {
            flag = ((BigInt(1) << a) & x) != 0
            a = a - 1
          }
          31 - (a + 1)
        }

//        println("Input = " + p.toBinaryString)
//        println("Input = " + p.toString)
//        println("zeroCount = " + zeroCount(p))
        val z_ex = if (p==0) 0 else 31-zeroCount(p)
        val v_ex = if (p==0) 0 else 1
//        println("z_ex=" + z_ex)
//        println("v_ex=" + v_ex)

        testCircuit(new LZC32,
          Seq(chiseltest.internal.NoThreadingAnnotation,
            chiseltest.simulator.WriteVcdAnnotation)) {
          dut : LZC32 =>
            dut.io.a.poke(p.U)
//            println("Z = " + dut.io.z.peek().litValue.toString())
//            println("V = " + dut.io.v.peek().litValue.toString())
            dut.io.z.expect(z_ex.U)
            dut.io.v.expect(v_ex.U)

        }
      }

      for (i <- 1 to 32) {
        testcase()
      }


    }

  }
}
