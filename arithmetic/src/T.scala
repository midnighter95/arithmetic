package arithmetic

import addition.prefixadder.common._
import addition._
import addition.prefixadder.{PrefixAdder, PrefixAdderNetlist, PrefixAdderWithAssert, PrefixAdderWithWrapper}
import addition.prefixadder.graph._
import chisel3._
import float._
import os._

object GraphFromJson extends CommonPrefixSum with HasPrefixSumWithGraphImp{
  println("This is playgorund")

  val matirx = Seq(
    Seq(1, 3, 2, 1),
    Seq(2, 1, 1, 1)
  )

  val matirx1 = Seq(
    Seq(2, 1, 2, 1, 2, 1, 2, 1),
    Seq(2, 1, 1, 1, 2, 2, 1, 1),
    Seq(2, 1, 1, 1, 2, 2, 1, 1),
    Seq(2, 1, 2, 2, 1, 1, 1, 1),
    Seq(1, 2, 1, 1, 1, 1, 1, 1)
  )

  val matirx2 = Seq(
    Seq(2, 1, 2, 1, 2, 1, 2, 1),
    Seq(2, 2, 1, 1, 2, 2, 1, 1),
    Seq(2, 2, 2, 2, 2, 2, 1, 1)
  )

  val matirx3 = Seq(
    Seq(4, 3, 2, 1, 4, 3, 2, 1),
    Seq(2, 2, 2, 2, 1, 1, 1, 1),
  )

  val matrix32 = Seq(
    Seq(4,3,2,1,  4,3,2,1,  4,3,2,1,   4,3,2,1,      4,3,2,1,  4,3,2,1,  4,1,2,1,  4,1,2,1),
    //                      完成大组内 depth = n-16
    Seq(4,4,4,4,  3,3,3,3,  2,2,2,2,   1,1,1,1,      4,4,4,4,  3,3,3,3,  1,2,1,1,  1,2,1,1), // 下半组计算完成
    //                                 结合上大组
    Seq(2,2,2,2,  2,2,2,2,  2,2,2,2,   2,2,2,2,      1,1,1,1,  1,1,1,1,  2,2,2,2,  1,1,1,1)
    //
  )


  val dotgraph: Seq[Node] = MatrixToGraph.elabroate(matrix32)

  val graphJson = GraphToJson.elaborate(dotgraph)
  os.write.over(os.pwd / "output" / "graph.graphml", Graphml(dotgraph).toString)
  val prefixGraph: PrefixGraph = PrefixGraph(graphJson)
}



class AdderFromJsonNetlist extends PrefixAdderNetlist(GraphFromJson.prefixGraph.width - 1, GraphFromJson)
class AdderFromJsonWithAssert extends PrefixAdderWithAssert(GraphFromJson.prefixGraph.width - 1, GraphFromJson)
class AdderFromJsonWithWrapper extends PrefixAdderWithWrapper(GraphFromJson.prefixGraph.width - 1, GraphFromJson)



