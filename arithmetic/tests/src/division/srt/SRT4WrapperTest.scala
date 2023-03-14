package division.srt

import chisel3._
import utils._
import chiseltest.{ChiselUtestTester, _}
import utest.{TestSuite, _}

import scala.util.Random

object SRT4WrapperTest extends TestSuite with ChiselUtestTester {
  def tests: Tests = Tests {
    test("SRT4Wrapper should pass") {
      def testcase(width: Int): Unit ={
        // parameters
        val radixLog2: Int = 2
        val n: Int = width
        val m: Int = n - 1
        val p: Int = Random.nextInt(m)
        val q: Int = Random.nextInt(m)
        val signRandom1: Int = Random.nextInt(2)
        val signRandom2: Int = Random.nextInt(2)
        val sign1: Int = if(signRandom1==0) -1 else 1
        val sign2: Int = if(signRandom2==0) -1 else 1
        val dividend: BigInt = BigInt(p, Random) * sign1
        val divisor: BigInt = BigInt(q, Random) * sign2

        if(divisor == 0) return
        val quotient_ex = dividend / divisor
        val remainder_ex = dividend % divisor

//        println("dividend = %8x, dividend = %d ".format(dividend, dividend))
//        println("divider  = %8x, divider  = %d".format(divider, divider))
//        println("zeroHeadDividend_ex  = %d".format(zeroHeadDividend))
//        println("zeroHeadDivider_ex   = %d".format(zeroHeadDivider))
//        println("noguard = "+ noguard)
//        println("quotient   = %d,  remainder  = %d".format(quotient, remainder))
//        println("counter_ex   = %d, needComputerWidth_ex = %d".format(counter, needComputerWidth))
        // test
        testCircuit(new SRT4Wrapper,
          Seq(chiseltest.internal.NoThreadingAnnotation,
            chiseltest.simulator.WriteVcdAnnotation)) {
          dut: SRT4Wrapper =>
            dut.clock.setTimeout(0)
            dut.input.bits.dividend.poke(dividend.asSInt)
            dut.input.bits.divisor.poke(divisor.asSInt)
            dut.input.bits.signIn.poke(true.B)
            dut.input.valid.poke(true.B)
            dut.clock.step()
            dut.input.valid.poke(false.B)
            var flag = false
            for (a <- 1 to 1000 if !flag) {
              if (dut.output.valid.peek().litValue == 1) {
                flag = true
//                println("%d / %d = %d --- %d".format(dividend,divisor,quotient_ex,remainder_ex))
//                println("%d / %d = %d --- %d".format(dividend,divisor,dut.output.bits.quotient.peek().litValue,dut.output.bits.reminder.peek().litValue))
                //                println("%d / %d ".format(dut.debug.dividend.peek().litValue,dut.debug.divisor.peek().litValue))
                //                println("bigdivisor = %d".format(dut.debug.bigdivisor.peek().litValue))
                utest.assert(dut.output.bits.quotient.peek().litValue == quotient_ex)
                utest.assert(dut.output.bits.reminder.peek().litValue == remainder_ex)
              }
              dut.clock.step()
            }
            utest.assert(flag)
            dut.clock.step(scala.util.Random.nextInt(10))


//            println("zeroHeadDividend  = %d".format(dut.io.zeroHeadDividend.peek().litValue))
//            println("zeroHeadDivider   = %d".format(dut.io.zeroHeadDivisor.peek().litValue))
//            println("sub   = "  +  (dut.io.sub.peek()))
//            println("needComputerWidth = "+  dut.io.needComputerWidth.peek().litValue)
//            println("counter = " + dut.io.counter.peek().litValue )
//            println("left = " + dut.io.leftShiftWidthDividend.peek().litValue)
        }
      }


      for( i <- 1 to 20){
        testcase(32)
      }
    }
  }
}