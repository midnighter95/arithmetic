package addition

import chisel3._
import chiseltest._
import utest._
import scala.util.Random

object vectorAdderTester extends TestSuite with ChiselUtestTester {
  def tests: Tests = Tests {
    test("vectorAdder should pass") {
      def testcase(width: Int): Unit = {

        val a = Random.nextInt(255)
        val b = Random.nextInt(255)
        val cin = Random.nextInt(1)

        val z = a + b + cin
        val overflow = z > 255
        val z_expect = if(overflow) z-256 else z

        // test
        testCircuit(
          new vectorAdder(width),
          Seq(chiseltest.internal.NoThreadingAnnotation, chiseltest.simulator.WriteVcdAnnotation)
        ) { dut: vectorAdder =>
          dut.clock.setTimeout(0)
          dut.a.poke(a.U)
          dut.b.poke(b.U)
          dut.cin.poke(cin.U)
          dut.z.expect(z_expect.U)
          dut.cout.expect(overflow.B)
        }
      }


            for (i <- 1 to 30) {
              testcase(8)
            }

    }
  }
}
