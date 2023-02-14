package division.srt

import chisel3._
import utils.Abs
import chiseltest.{ChiselUtestTester, _}
import utest.{TestSuite, _}

import scala.util.Random

object AbsTest extends TestSuite with ChiselUtestTester{
  def tests: Tests = Tests {
    test("Abs should pass") {
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
        testCircuit(new Abs(32),
          Seq(chiseltest.internal.NoThreadingAnnotation,
            chiseltest.simulator.WriteVcdAnnotation)) {
          dut : Abs =>
            dut.io.aIn.poke((-1).S)
            dut.io.bIn.poke((100).S)
            dut.io.signIn.poke(true.B)

//            println("a = " + dut.io.aOut.peek())
//            println("b = " + dut.io.bOut.peek())
//            println("aSign = " + dut.io.aSign.peek().litValue.toString())
//            println("bSign = " + dut.io.bSign.peek().litValue.toString())


        }
      }
      for( i <- 1 to 1){
        testcase()
      }
    }
  }
}
