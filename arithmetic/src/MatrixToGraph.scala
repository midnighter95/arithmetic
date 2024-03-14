package arithmetic

import scala.collection.mutable.ListBuffer

object MatrixToGraph {

  val maxAssociativeNum = 4

  def elabroate(matrix: Seq[Seq[Int]]): Seq[Node] = {
    assert(matrix.map(_.length).forall(_ == matrix.map(_.length).head), "Maxtrix row have different size")
    assert(matrix.map(_.max).max <= maxAssociativeNum, "Max associate number must less than 5")

    val width = matrix.head.length
    val layerNumber = matrix.length

    /*println("input matrix is")
    matrix.foreach(println(_))*/

    val indexList = Seq.tabulate(width)(n => width - n)
    val layer0: Seq[Node] = Seq.tabulate(width)(n => Node(width - n))

    var list = new ListBuffer[Node]
    layer0.foreach(list.append(_))

    Seq.tabulate(layerNumber)(_ + 1).foreach { level =>
      indexList.zip(matrix(level - 1))
        // _1 = position, _2 is all nodes in this level
        .map(c => Node(c._1, getFatherNodes(c._2, list.takeRight(width).takeRight(c._1).toSeq)))
        .foreach(list.append(_))
    }
    assert(!list.takeRight(width).exists(c => (c.position != c.depth)),
      "there is a final level node can't satisfy pos == depth" + s" , position = ${list.takeRight(width).filter(c => (c.position != c.depth)).map(_.position).mkString(" ")}")

    /*println("After resolving, output node(level-position-index) matrix is ")
    list.grouped(width).foreach(println(_))*/
    val distinctList: Seq[Node] = list.distinct.toSeq
    val listWithIndex: Seq[Node] = distinctList.sorted
      .zip(Seq.tabulate(distinctList.length)(n => n + 1))
      .map { case (node, index) => Node(node, index-1) }
    val nodes: Seq[Node] = listWithIndex
    val graph = PrefixGraphFix(listWithIndex)
//    println("Assigning index to distinct node, get output graph ")
//
//    println(
//      "Summary: number = "
//        + graph.nodes.length
//    )

//    println(graph)

    listWithIndex
  }

  /** get all father nodes
    *
    * @nodes all availNodes from this position, the first one is itself in the former level
    *
    */
  def getFatherNodes(combineNumber: Int, nodes: Seq[Node]): Set[Node] = {
    var list = new ListBuffer[Node]
    if(nodes.head.isFinish) return Set(nodes.head)
    /** the first node is it self */
    var startpoint = nodes.head.position
    for (i <- 1 to combineNumber) {
      assert(nodes.exists(_.position == startpoint), "cannot find FatherNodes when building next level " + nodes.head)
      val node = nodes.filter(_.position == startpoint).head
      startpoint = node.getNextPos
      list.append(node)
    }
    list.toSet
  }
}