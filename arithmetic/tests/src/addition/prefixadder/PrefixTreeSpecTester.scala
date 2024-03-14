package addition.prefixadder

import addition.prefixadder.common.CommonPrefixSum
import addition.prefixadder.graph.{HasPrefixSumWithGraphImp, PrefixGraph, PrefixNode}
import arithmetic._
import chiseltest.formal.BoundedCheck
import formal.FormalSuite
import utest._

/** This adder is same with BrentKungSum8*/
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

object BrentKungSum33ByGraph extends HasPrefixSumWithGraphImp with CommonPrefixSum {
  val zeroLayer: Seq[PrefixNode] = Seq.tabulate(33)(PrefixNode(_))
  val node101: PrefixNode = PrefixNode(zeroLayer(0), zeroLayer(1))
  val node102: PrefixNode = PrefixNode(zeroLayer(0), zeroLayer(1), zeroLayer(2))
  val node104: PrefixNode = PrefixNode(zeroLayer(3), zeroLayer(4))
  val node105: PrefixNode = PrefixNode(zeroLayer(3), zeroLayer(4), zeroLayer(5))
  val node107: PrefixNode = PrefixNode(zeroLayer(6), zeroLayer(7))
  val node108: PrefixNode = PrefixNode(zeroLayer(6), zeroLayer(7), zeroLayer(8))
  val node110: PrefixNode = PrefixNode(zeroLayer(9), zeroLayer(10))
  val node111: PrefixNode = PrefixNode(zeroLayer(9), zeroLayer(10), zeroLayer(11))
  val node113: PrefixNode = PrefixNode(zeroLayer(12), zeroLayer(13))
  val node114: PrefixNode = PrefixNode(zeroLayer(12), zeroLayer(13), zeroLayer(14))
  val node116: PrefixNode = PrefixNode(zeroLayer(15), zeroLayer(16))
  val node117: PrefixNode = PrefixNode(zeroLayer(15), zeroLayer(16), zeroLayer(17))
  val node119: PrefixNode = PrefixNode(zeroLayer(18), zeroLayer(19))
  val node120: PrefixNode = PrefixNode(zeroLayer(18), zeroLayer(19), zeroLayer(20))
  val node122: PrefixNode = PrefixNode(zeroLayer(21), zeroLayer(22))
  val node123: PrefixNode = PrefixNode(zeroLayer(21), zeroLayer(22), zeroLayer(23))
  val node125: PrefixNode = PrefixNode(zeroLayer(24), zeroLayer(25))
  val node126: PrefixNode = PrefixNode(zeroLayer(24), zeroLayer(25), zeroLayer(26))

  val node203: PrefixNode = PrefixNode(node102, zeroLayer(3))
  val node204: PrefixNode = PrefixNode(node102, node104)
  val node205: PrefixNode = PrefixNode(node102, node105)
  val node206: PrefixNode = PrefixNode(node102, node105, zeroLayer(6))
  val node207: PrefixNode = PrefixNode(node102, node105, node107)
  val node208: PrefixNode = PrefixNode(node102, node105, node108)

  val node212: PrefixNode = PrefixNode(node111, zeroLayer(12))
  val node213: PrefixNode = PrefixNode(node111, node113)
  val node214: PrefixNode = PrefixNode(node111, node114)
  val node215: PrefixNode = PrefixNode(node111, node114, zeroLayer(15))
  val node216: PrefixNode = PrefixNode(node111, node114, node116)
  val node217: PrefixNode = PrefixNode(node111, node114, node117)

  val node221: PrefixNode = PrefixNode(node120, zeroLayer(21))
  val node222: PrefixNode = PrefixNode(node120, node122)
  val node223: PrefixNode = PrefixNode(node120, node123)
  val node224: PrefixNode = PrefixNode(node120, node123, zeroLayer(24))
  val node225: PrefixNode = PrefixNode(node120, node123, node125)
  val node226: PrefixNode = PrefixNode(node120, node123, node126)

  val node309: PrefixNode = PrefixNode(node208, zeroLayer(9))
  val node310: PrefixNode = PrefixNode(node208, node110)
  val node311: PrefixNode = PrefixNode(node208, node111)
  val node312: PrefixNode = PrefixNode(node208, node212)
  val node313: PrefixNode = PrefixNode(node208, node213)
  val node314: PrefixNode = PrefixNode(node208, node214)
  val node315: PrefixNode = PrefixNode(node208, node215)
  val node316: PrefixNode = PrefixNode(node208, node216)
  val node317: PrefixNode = PrefixNode(node208, node217)

  val node318: PrefixNode = PrefixNode(node208, node217, zeroLayer(18))
  val node319: PrefixNode = PrefixNode(node208, node217, node119)
  val node320: PrefixNode = PrefixNode(node208, node217, node120)
  val node321: PrefixNode = PrefixNode(node208, node217, node221)
  val node322: PrefixNode = PrefixNode(node208, node217, node222)
  val node323: PrefixNode = PrefixNode(node208, node217, node223)
  val node324: PrefixNode = PrefixNode(node208, node217, node224)
  val node325: PrefixNode = PrefixNode(node208, node217, node225)
  val node326: PrefixNode = PrefixNode(node208, node217, node226)

  val node128: PrefixNode = PrefixNode(zeroLayer(28), zeroLayer(27))
  val node130: PrefixNode = PrefixNode(zeroLayer(30), zeroLayer(29))
  val node132: PrefixNode = PrefixNode(zeroLayer(32), zeroLayer(31))
  val node229: PrefixNode = PrefixNode(node128, zeroLayer(29))
  val node230: PrefixNode = PrefixNode(node128, node130)
  val node231: PrefixNode = PrefixNode(node128, node130, zeroLayer(31))
  val node232: PrefixNode = PrefixNode(node128, node130, node132)

  val node427: PrefixNode = PrefixNode(node326, zeroLayer(27))
  val node438: PrefixNode = PrefixNode(node326, node128)
  val node429: PrefixNode = PrefixNode(node326, node229)
  val node430: PrefixNode = PrefixNode(node326, node230)
  val node431: PrefixNode = PrefixNode(node326, node231)
  val node432: PrefixNode = PrefixNode(node326, node232)


  val prefixGraph = PrefixGraph(
    zeroLayer.toSet + node101 + node102 + node104 + node105 + node107 + node108 + node110 + node111 + node113 + node114 + node116 + node117 + node119 + node120 + node122 + node123 + node125 + node126 + node203 + node204 + node205 + node206 + node207 + node208 + node212 + node213 + node214 + node215 + node216 + node217 + node221 + node222 + node223 + node224 + node225 + node226 + node309 + node310 + node311 + node312 + node313 + node314 + node315 + node316 + node317 + node318 + node319 + node320 + node321 + node322 + node323 + node324 + node325 + node326 + node128 + node130 + node132 + node229 + node230 + node231 + node232 + node427 + node438 + node429 + node430 + node431 +
      node432
  )
}

class ThreeCombineAdder32 extends PrefixAdder(BrentKungSum33ByGraph.prefixGraph.width - 1, BrentKungSum33ByGraph)

class FourCombineAdder7 extends PrefixAdder(Adder8ByGraph.prefixGraph.width - 1, Adder8ByGraph)

object PrefixTreeSpecTester extends FormalSuite {

  val zeroLayer = Seq.tabulate(4)(PrefixNode(_))
  val node1 = PrefixNode(zeroLayer(0), zeroLayer(1))
  val node2 = PrefixNode(zeroLayer(2), zeroLayer(3))
  val node3 = PrefixNode(node1, node2)
  val node4 = PrefixNode(zeroLayer(2), node1)

  val tests: Tests = Tests {
    test("should serialize PrefixGraph") {
      assert(os.read(os.resource / "graph.dot") == BrentKungSum8ByGraph.prefixGraph.toString)
    }
    test("should deserialize PrefixGraph and generate correct adder.") {
      val d = new CommonPrefixSum with HasPrefixSumWithGraphImp {
        val prefixGraph: PrefixGraph = PrefixGraph(os.resource / "graph.json")
      }
      verify(new PrefixAdder(d.prefixGraph.width - 1, d), Seq(BoundedCheck(1)))
    }
    test("should json generate correct adder.") {
      verify(new AdderFromJsonWithAssert, Seq(BoundedCheck(1)))
    }
    test("should abort in PrefixNode generation") {
      try {
        PrefixNode(zeroLayer(2), node2)
      } catch {
        case _: java.lang.IllegalArgumentException => true
        case _: Throwable => false
      }
    }
    test("ThreeCombineAdder32 should pass test") {
      verify(new ThreeCombineAdder32, Seq(BoundedCheck(1)))
    }
    test("FourCombineAdder7 should pass test") {
      verify(new FourCombineAdder7, Seq(BoundedCheck(1)))
    }
    test("should generate graphML file") {
      os.write.over(os.pwd / "PrefixGraph.dot", PrefixGraph(zeroLayer.toSet + node1 + node2 + node3 + node4).toString)
    }
  }
}