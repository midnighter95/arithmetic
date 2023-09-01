package addition

import chisel3._
import chiseltest._
import utest._
import scala.util.Random

object vectorAdderTester8 extends TestSuite with ChiselUtestTester {
  def tests: Tests = Tests {
    test("vectorAdder8 should pass") {
      def testcase(width: Int): Unit = {

        val a:Seq[Int] = Seq(Random.nextInt(255),Random.nextInt(255),Random.nextInt(255),Random.nextInt(255))
        val b:Seq[Int] = Seq(Random.nextInt(255),Random.nextInt(255),Random.nextInt(255),Random.nextInt(255))
        val c:Seq[Int] = Seq(Random.nextInt(2),Random.nextInt(2),Random.nextInt(2),Random.nextInt(2))

        def toHex(a:Int)={
            if(a>15) a.toHexString else "0"+a.toHexString
}
        def add(a:Tuple2[Int,Int]) :Int = (a._1 + a._2)
        val z = a.zip(b).map(add(_)).zip(c).map(add(_))


        val overflow = z.map(a => {
          if(a > 255) "1" else "0"
        })
        val z_expectSeq = z.map(a =>{
          val ov = a > 255
          if(ov) a-256 else a
        })

        def toCircuit(a:Seq[Int])={
          "h" + toHex(a(0)) + toHex(a(1)) + toHex(a(2)) + toHex(a(3))
        }

        val aInput = toCircuit(a)
        val bInput = toCircuit(b)
        val cInput = "b"+c.map(_.toString).mkString
        val z_expect = toCircuit(z_expectSeq)
        val ov_expect = "b"+overflow.map(_.toString).mkString("")

        // test
        testCircuit(
          new vectorAdder(width),
          Seq(chiseltest.internal.NoThreadingAnnotation, chiseltest.simulator.WriteVcdAnnotation)
        ) { dut: vectorAdder =>
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
          dut.sew.poke("b001".U)
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
