package addition

import chisel3._
import chiseltest._
import utest._

import scala.util.Random

object vectorAdder32Tester16 extends TestSuite with ChiselUtestTester {
  def tests: Tests = Tests {
    test("vectorAdder16 should pass") {
      def testcase(width: Int): Unit = {

        val a:Seq[Int] = Seq(Random.nextInt(65535),Random.nextInt(65535))
        val b:Seq[Int] = Seq(Random.nextInt(65535),Random.nextInt(65535))
        val c:Seq[Int] = Seq(Random.nextInt(2),Random.nextInt(2))

        def toHex(a:Int)={
            Seq.fill(4-a.toHexString.length)("0").mkString("") + a.toHexString
}
        def add(a:Tuple2[Int,Int]) :Int = (a._1 + a._2)
        val z = a.zip(b).map(add(_)).zip(c).map(add(_))


        val overflow = z.map(a => {
          if(a > 65535) "1" else "0"
        })

        val z_expectSeq = z.map(a =>{
          val ov = a > 65535
          if(ov) a-65536 else a
        })

        def toCircuit16(a:Seq[Int])={
          "h" + toHex(a(0)) + toHex(a(1))
        }

        val aInput = toCircuit16(a)
        val bInput = toCircuit16(b)
        val cInput = "b"+c(0).toString+c(0).toString+c(1).toString+c(1).toString
        val z_expect = toCircuit16(z_expectSeq)
        val ov_expect = "b"+overflow.map(_.toString).mkString("")

        // test
        testCircuit(
          new vectorAdder32,
          Seq(chiseltest.internal.NoThreadingAnnotation, chiseltest.simulator.WriteVcdAnnotation)
        ) { dut: vectorAdder32 =>
//          println("a = " + a)
//          println("aInput=" + aInput)
//          println("b = " + b)
//          println("bInput=" + bInput)
//          println("c = " + c)
//          println("cInput=" + cInput)
//          println("z_expectSeq = " + z_expectSeq.toString)
//          println("z_expect = " + z_expect)
//          println("overflow = " + overflow)
          dut.clock.setTimeout(0)
          dut.a.poke(aInput.U)
          dut.b.poke(bInput.U)
          dut.cin.poke(cInput.U)
          dut.sew.poke("b010".U)
          dut.clock.step(1)
          dut.z.expect(z_expect.U)
          dut.cout.expect(ov_expect.U)


        }
      }


            for (i <- 1 to 50) {
              testcase(32)
            }

    }
  }
}
