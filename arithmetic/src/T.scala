package arithmetic

import addition.prefixadder.common.{BrentKungSum, CommonPrefixSum, KoggeStoneAdder}
import addition._
import addition.prefixadder.PrefixAdder
import addition.prefixadder.graph._
import chisel3._
import float._

object T extends App{
  println("T work fine")

  val name = "DemoAdder"
  val testRunDir = os.pwd / "test_run_dir"
  os.makeDir.all(testRunDir)
  os.remove(testRunDir / s"$name.sv")
  os.write(testRunDir / s"$name.sv", chisel3.getVerilogString(new DemoAdderWithGraph))
}

object Adder8ByGraph extends HasPrefixSumWithGraphImp with CommonPrefixSum {
  val zeroLayer: Seq[PrefixNode] = Seq.tabulate(8)(PrefixNode(_))

  val node17 = PrefixNode(zeroLayer(7), zeroLayer(6), zeroLayer(5), zeroLayer(4))
  val node16 = PrefixNode(zeroLayer(6), zeroLayer(5), zeroLayer(4))
  val node15 = PrefixNode(zeroLayer(5), zeroLayer(4))
  val node13 = PrefixNode(zeroLayer(3), zeroLayer(2), zeroLayer(1), zeroLayer(0))
  val node12 = PrefixNode(zeroLayer(2), zeroLayer(1), zeroLayer(0))
  val node11 = PrefixNode(zeroLayer(1), zeroLayer(0))

  val node27 = PrefixNode(node17, node13)
  val node26 = PrefixNode(node16, node13)
  val node25 = PrefixNode(node15, node13)
  val node24 = PrefixNode(zeroLayer(4), node13)


  val prefixGraph = PrefixGraph(
    zeroLayer.toSet +
      node17 + node16 + node15 + node13 + node12 + node11 +
      node27 + node26 + node25 + node24
  )
}

object BrentKungSum8ByGraph extends HasPrefixSumWithGraphImp with CommonPrefixSum {
  val zeroLayer: Seq[PrefixNode] = Seq.tabulate(8)(PrefixNode(_))
  val node11: PrefixNode = PrefixNode(zeroLayer(0), zeroLayer(1))
  val node13: PrefixNode = PrefixNode(zeroLayer(2), zeroLayer(3))
  val node15: PrefixNode = PrefixNode(zeroLayer(4), zeroLayer(5))
  val node17: PrefixNode = PrefixNode(zeroLayer(6), zeroLayer(7))
  val node22: PrefixNode = PrefixNode(node11, zeroLayer(2))
  val node23: PrefixNode = PrefixNode(node11, node13)
  val node27: PrefixNode = PrefixNode(node15, node17)
  val node35: PrefixNode = PrefixNode(node23, node15)
  val node37: PrefixNode = PrefixNode(node23, node27)
  val node34: PrefixNode = PrefixNode(node23, zeroLayer(4))
  val node46: PrefixNode = PrefixNode(node35, zeroLayer(6))

  val prefixGraph = PrefixGraph(
    zeroLayer.toSet +
      node11 + node13 + node15 + node17 +
      node22 + node23 + node27 +
      node35 + node37 + node34 +
      node46
  )
}

class DemoPrefixAdderWithGraph extends PrefixAdder(BrentKungSum8ByGraph.prefixGraph.width - 1, BrentKungSum8ByGraph)

class DemoAdderWithGraph extends PrefixAdder(Adder8ByGraph.prefixGraph.width - 1, Adder8ByGraph)

//class NormalAdder8Area extends Module{
//  val a = Input(UInt(8.W))
//  val b = Input(UInt(8.W))
//  val cin = Input(Bool())
//  val z = Output(UInt(8.W))
//  val cout = Output(Bool())
//
//  val sum = Wire(UInt(9.W))
//  sum := a + b + cin
//
//  z := sum(7,0)
//  cout := sum(8)
//}

class BKAdder8A extends Module{
  val a    = IO(Input(UInt(8.W)))
  val b    = IO(Input(UInt(8.W)))
  val cin  = IO(Input(Bool()))
  val z    = IO(Output(UInt(8.W)))
  val cout = IO(Output(Bool()))


  val m = Module(new KoggeStoneAdder(8))
  m.a := a
  m.b := b
  m.cin := cin

  z := m.z
  cout := m.cout

}

class KSAdder32A extends Module{
  val a    = IO(Input(UInt(32.W)))
  val b    = IO(Input(UInt(32.W)))
  val cin  = IO(Input(Bool()))
  val z    = IO(Output(UInt(32.W)))
  val cout = IO(Output(Bool()))


  val m = Module(new KoggeStoneAdder(32))
  m.a := a
  m.b := b
  m.cin := cin

  z := m.z
  cout := m.cout

}
