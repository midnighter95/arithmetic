package division.srt

import chisel3._
import utils._
import chiseltest.{ChiselUtestTester, _}
import utest.{TestSuite, _}

import scala.util.Random

object SRT16WrapperTest extends TestSuite with ChiselUtestTester {
  def tests: Tests = Tests {
    test("SRT16Wrapper should pass") {
      def testcase(width: Int,init:Int): Unit ={
        // parameters
        val radixLog2 = 4
        val n: Int = width
        val m: Int = n - 1
        val p: Int = Random.nextInt(m)
        val q: Int = Random.nextInt(m)
        val signRandom1: Int = Random.nextInt(2)
        val signRandom2: Int = Random.nextInt(2)
        val sign1: Int = if (signRandom1 == 0) -1 else 1
        val sign2: Int = if (signRandom2 == 0) -1 else 1
//        val dividend: BigInt = BigInt(p, Random) * sign1
//        val divisor: BigInt = BigInt(q, Random) * sign2
        val dividend: BigInt = 1227133513 + init
        val divisor: BigInt = 648422 + BigInt(5, Random)

        if(divisor == 0) return
        val quotient_ex = dividend / divisor
        val remainder_ex = dividend % divisor

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
        val zeroHeadDivider: Int = m - zeroCheck(divisor)
        val needComputerWidth: Int = zeroHeadDivider - zeroHeadDividend + 1 + 1
        val noguard: Boolean = needComputerWidth % 4 == 0
        val guardWidth: Int = if (noguard) 0 else 4 - needComputerWidth % 4
        val counter: Int = (needComputerWidth + guardWidth) / radixLog2
        val leftShiftWidthDividend: Int = zeroHeadDividend - guardWidth
        val leftShiftWidthDivider: Int = zeroHeadDivider
        if ((divisor == 0) || (divisor > dividend) || (needComputerWidth <= 0))
          return

        // test
        testCircuit(new SRT16Wrapper,
          Seq(chiseltest.internal.NoThreadingAnnotation,
            chiseltest.simulator.WriteVcdAnnotation)) {
          dut: SRT16Wrapper =>
            dut.clock.setTimeout(0)
            dut.input.bits.dividend.poke(dividend.asSInt)
            dut.input.bits.divisor.poke(divisor.asSInt)
            dut.input.bits.signIn.poke(false.B)
            dut.input.valid.poke(true.B)
            dut.clock.step()
            var flag = false
            for (a <- 1 to 1000 if !flag) {
              if (dut.output.valid.peek().litValue == 1) {
                flag = true
//                                println("zeroHeadDividend = %d".format(zeroHeadDividend))
//                                println("zeroHeadDivider = %d".format(zeroHeadDivider))
//                                println("needComputerWidth = %d".format(needComputerWidth))
//                                println("guardWidth = %d".format(guardWidth))
//                                println("dividend = %d".format(dividend))
//                                println("leftShiftWidthDividend = %d".format(leftShiftWidthDividend))
//                                println("dividend_shift = %x".format(dividend << leftShiftWidthDividend))
                println("%d / %d = %d --- %d".format(dividend, divisor, quotient_ex, remainder_ex))
                //                println("%d / %d = %d --- %d".format(dividend, divider, dut.output.bits.quotient.peek().litValue,
                //                  (dut.output.bits.reminder.peek().litValue >> zeroHeadDivider) + (if( dividend%2 == 1) 1 else 0 )))
                println("%d / %d = %d --- %d".format(dividend, divisor, dut.output.bits.quotient.peek().litValue,
                  dut.output.bits.reminder.peek().litValue))
                utest.assert(dut.output.bits.quotient.peek().litValue == quotient_ex)
                utest.assert(dut.output.bits.reminder.peek().litValue == remainder_ex)
              }
              dut.clock.step()
            }
            utest.assert(flag)
            dut.clock.step(scala.util.Random.nextInt(10))
        }
    }

//      for (i <- 0 to 20) {
//        testcase(32, i)
//      }
    }
  }
}