package division.srt

import chisel3._
import utils._
import chiseltest.{ChiselUtestTester, _}
import utest.{TestSuite, _}

import scala.util.Random

object FinalWrapperTest extends TestSuite with ChiselUtestTester {
  def tests: Tests = Tests {
    test("FinalWrapper should pass") {
      def testcase(): Unit ={
        // parameters
        val radixLog2: Int = 2
        val n: Int = 6
        val m: Int = 5
        val p: Int = Random.nextInt(8)
        val q: Int = Random.nextInt(8)
        val dividend: BigInt = BigInt(p, Random) 
        val divisor: BigInt = BigInt(q, Random)
        if ((divisor == 0) || (divisor.abs > dividend.abs)) return
        val quotient_ex = dividend / divisor
        val remainder_ex = dividend % divisor

        if ((divisor == 0) )
          return

        println("%d / %d = %d --- %d".format(dividend,divisor,quotient_ex,remainder_ex))
//        println("zeroHeadDividend_ex  = %d".format(zeroHeadDividend))
//        println("zeroHeadDivider_ex   = %d".format(zeroHeadDivider))
//        println("noguard = "+ noguard)
//        println("quotient   = %d,  remainder  = %d".format(quotient, remainder))
//        println("counter_ex   = %d, needComputerWidth_ex = %d".format(counter, needComputerWidth))
        // test
        testCircuit(new finalWrapper,
          Seq(chiseltest.internal.NoThreadingAnnotation,
            chiseltest.simulator.WriteVcdAnnotation)) {
          dut: finalWrapper =>


//            println("zeroHeadDividend  = %d".format(dut.io.zeroHeadDividend.peek().litValue))
//            println("zeroHeadDivider   = %d".format(dut.io.zeroHeadDivisor.peek().litValue))
//            println("sub   = "  +  (dut.io.sub.peek()))
//            println("needComputerWidth = "+  dut.io.needComputerWidth.peek().litValue)
//            println("counter = " + dut.io.counter.peek().litValue )
//            println("left = " + dut.io.leftShiftWidthDividend.peek().litValue)
            dut.clock.setTimeout(0)
            dut.input.valid.poke(true.B)
            dut.input.bits.dividend.poke(dividend.S)
            dut.input.bits.divisor.poke(divisor.S)
            dut.signIn.poke(true.B)
            dut.clock.step()
            dut.input.valid.poke(false.B)
            var flag = false
            for (a <- 1 to 1000 if !flag) {
              if (dut.output.valid.peek().litValue == 1) {
                flag = true
                println("%d / %d = %d --- %d".format(dividend,divisor,dut.output.bits.quotient.peek().litValue,dut.output.bits.reminder.peek().litValue))
              }
              dut.clock.step()
            }
            utest.assert(flag)
            dut.clock.step(scala.util.Random.nextInt(10))
        }
      }


      for( i <- 1 to 20){
        testcase()
      }
    }
  }
}