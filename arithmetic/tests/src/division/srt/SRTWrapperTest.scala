package division.srt

import chisel3._
import utils._
import chiseltest.{ChiselUtestTester, _}
import utest.{TestSuite, _}

import scala.util.Random

object SRTWrapperTest extends TestSuite with ChiselUtestTester {
  def tests: Tests = Tests {
    test("SRTWrapper should pass") {
      def testcase(width: Int): Unit ={
        // parameters
        val radixLog2: Int = 2
        val n: Int = width
        val m: Int = n - 1
        val p: Int = Random.nextInt(m)
        val q: Int = Random.nextInt(m)
        val dividend: BigInt = BigInt(p, Random)
        val divider: BigInt = BigInt(q, Random)
//        val dividend: BigInt = BigInt("65")
//        val divider: BigInt = BigInt("1")
        def zeroCheck(x: BigInt): Int = {
          var flag = false
          var a: Int = m
          while (!flag && (a >= -1)) {
            flag = ((BigInt(1) << a) & x) != 0
            a = a - 1
          }
          a + 1
        }
        val zeroHeadDividend: Int = m - zeroCheck(dividend)
        val zeroHeadDivider: Int = m - zeroCheck(divider)
        val needComputerWidth: Int = zeroHeadDivider - zeroHeadDividend + 1 + radixLog2 - 1
        val noguard: Boolean = needComputerWidth % radixLog2 == 0
        val counter: Int = (needComputerWidth + 1) / 2
        if ((divider == 0) || (divider > dividend) || (needComputerWidth <= 0))
          return
        val quotient: BigInt = dividend / divider
        val remainder: BigInt = dividend % divider
        val leftShiftWidthDividend: Int = zeroHeadDividend - (if (noguard) 0 else 1)
        val leftShiftWidthDivider: Int = zeroHeadDivider

        println("dividend = %8x, dividend = %d ".format(dividend, dividend))
        println("divider  = %8x, divider  = %d".format(divider, divider))
        println("zeroHeadDividend_ex  = %d".format(zeroHeadDividend))
        println("zeroHeadDivider_ex   = %d".format(zeroHeadDivider))
//        println("quotient   = %d,  remainder  = %d".format(quotient, remainder))
//        println("counter   = %d, needComputerWidth = %d".format(counter, needComputerWidth))
        // test
        testCircuit(new SRTWrapper,
          Seq(chiseltest.internal.NoThreadingAnnotation,
            chiseltest.simulator.WriteVcdAnnotation)) {
          dut: SRTWrapper =>
            dut.io.dividendIn := dividend.asUInt
            dut.io.divisorIn := divider.asUInt
            println("zeroHeadDividend  = %d".format(dut.zeroHeadDividend.peek().litValue))
            println("zeroHeadDivider   = %d".format(dut.zeroHeadDivisor.peek().litValue))
        }
      }


      for( i <- 1 to 5){
        testcase(32)
      }
    }
  }
}