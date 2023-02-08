package division.srt
import Chisel.PriorityEncoder
import chiseltest.ChiselUtestTester
import utest.TestSuite
import utils.LZC8
import chisel3._
import chiseltest._
import utest._

import scala.util.Random

object LZC8Test extends TestSuite with ChiselUtestTester{
  def tests: Tests = Tests {
    test("LZC8 should pass") {
      def testcase() = {
        val r: Int = Random.nextInt(8)
        val p:Int = BigInt(r,Random).toInt
        def zeroCount(x: BigInt): Int = {
          var flag = false
          var a: Int = 7
          while (!flag && (a >= -1)) {
            flag = ((BigInt(1) << a) & x) != 0
            a = a - 1
          }
          7 - (a + 1)
        }
        val z_ex = if (p==0) 0 else 7-zeroCount(p)
        val v_ex = if (p==0) 0 else 1
//        println("Input = " + p.toBinaryString)
//        println("zeroCount = " + zeroCount(p))
//        println("z_ex=" + z_ex)
//        println("v_ex=" + v_ex)
        testCircuit(new LZC8,
          Seq(chiseltest.internal.NoThreadingAnnotation,
            chiseltest.simulator.WriteVcdAnnotation)) {
          dut : LZC8 =>
            dut.io.a.poke(p.U)
//            println("Z = " + dut.io.z.peek().litValue.toString())
//            println("V = " + dut.io.v.peek().litValue.toString())
            dut.io.z.expect(z_ex.U)
            dut.io.v.expect(v_ex.U)

        }
      }
      for( i <- 1 to 8){
        testcase()
      }
    }
  }
}
