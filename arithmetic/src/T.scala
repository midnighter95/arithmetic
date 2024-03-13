package arithmetic

import addition.prefixadder.common._
import addition._
import addition.prefixadder.PrefixAdder
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


  val dotgraph: Seq[Node] = MatrixToGraph.elabroate(matirx3)


  GraphToJson.elaborate(dotgraph)
  os.write.over(os.pwd / "graph.graphml", Graphml(dotgraph).toString)
  val prefixGraph: PrefixGraph = PrefixGraph((os.pwd/"graph.json"))
}



class AdderFromJson extends PrefixAdder(GraphFromJson.prefixGraph.width - 1, GraphFromJson)



