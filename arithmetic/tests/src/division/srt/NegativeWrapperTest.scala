package division.srt

import chisel3._
import utils._
import chiseltest.{ChiselUtestTester, _}
import utest.{TestSuite, _}

import scala.util.Random

object NegativeWrapperTest extends TestSuite with ChiselUtestTester {
  def tests: Tests = Tests {
    test("Negative should pass") {
      def testcase(): Unit ={
        // parameters
        val radixLog2: Int = 2
        val n: Int = 6
        val m: Int = 10
        val p: Int = Random.nextInt(m)
        val q: Int = Random.nextInt(m-2)
        val dividend: BigInt = BigInt(p, Random)  * (-1)
        val divisor: BigInt = BigInt(q, Random)
        if ((divisor == 0)) return
        val quotient_ex = dividend / divisor
        val remainder_ex = dividend % divisor
        // test
        testCircuit(new finalWrapper,
          Seq(chiseltest.internal.NoThreadingAnnotation,
            chiseltest.simulator.WriteVcdAnnotation)) {
          dut: finalWrapper =>
            dut.clock.setTimeout(0)
            dut.input.valid.poke(true.B)
            dut.input.bits.dividend.poke(dividend.S)
            dut.input.bits.divisor.poke(divisor.S)
            dut.input.bits.signIn.poke(true.B)
            dut.clock.step()
            dut.input.valid.poke(false.B)
            var flag = false
            for (a <- 1 to 1000 if !flag) {
              if (dut.output.valid.peek().litValue == 1) {
                flag = true
//                println("%d / %d = %d --- %d".format(dividend,divisor,quotient_ex,remainder_ex))
//                println("%d / %d = %d --- %d".format(dividend,divisor,dut.output.bits.quotient.peek().litValue,dut.output.bits.reminder.peek().litValue))
//                println("bigdivisor = %d".format(dut.debug.bigdivisor.peek().litValue))
//                println("bSign = %b".format(dut.debug.dividendSign))
//                println(("remainderAbs = %d".format(dut.debug.remainderAbs.peek().litValue)))
                utest.assert(dut.output.bits.quotient.peek().litValue == quotient_ex)
                utest.assert(dut.output.bits.reminder.peek().litValue == remainder_ex)
              }
              dut.clock.step()
            }
            //utest.assert(flag)
            dut.clock.step(scala.util.Random.nextInt(10))
        }
      }


      for( i <- 1 to 32){
        testcase()
      }
    }
  }
}