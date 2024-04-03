package arithmetic

import scala.collection.mutable.ListBuffer


object MatrixToGraphtestWithDuplicate {

  val maxAssociativeNum = 4

  def elabroate(matrix: Seq[Seq[Int]]): Seq[Node] = {
    assert(matrix.map(_.length).forall(_ == matrix.map(_.length).head), "Maxtrix row have different size")
    assert(matrix.map(_.max).max <= maxAssociativeNum, "Max associate number must less than 5")

    val width = matrix.head.length
    val layerNumber = matrix.length

    println("input matrix is")
    matrix.foreach(println(_))

    val indexList = Seq.tabulate(width)(n => width - n)
    val layer0: Seq[Node] = Seq.tabulate(width)(n => Node(width - n))

    var list = new ListBuffer[Node]
    layer0.foreach(list.append(_))


    Seq.tabulate(layerNumber)(_ + 1).foreach { level =>
      indexList.zip(matrix(level - 1))
        // _1 = position, _2 is combine number
        .map{c =>
          val nodes = list.filter(_.position <= c._1).groupBy(_.position).values.map { group =>
            group.maxBy(_.level)
          }.toSeq.sortBy(_.position).reverse

          Node(c._1, getFatherNodes(c._2, nodes, list, 3))

        }
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

    // reindex all fathers
    listWithIndex.foreach{ c=>
      c.fathers.foreach( i =>
        i.index = listWithIndex.filter(_.level == i.level).filter(_.position == i.position).filter(_.copy == i.copy).head.index
      )
    }

    val nodes: Seq[Node] = listWithIndex
    val graph: PrefixGraphFix = PrefixGraphFix(listWithIndex)

    listWithIndex
  }

  /** get all father nodes
    *
    * @nodes all availNodes from this position, the first one is itself in the former level
    *
    */
  def getFatherNodes(combineNumber: Int, nodes: Seq[Node], list:ListBuffer[Node], maxFanout:Int): Set[Node] = {

    var output = new ListBuffer[Node]
    if(nodes.head.isFinish) return Set(nodes.head)
    /** the first node is it self */
    var startpoint = nodes.head.position
    for (i <- 1 to combineNumber) {
//      println("nodes is "+ nodes.head + " times = " + i)
//      println("get father from "+ nodes)
//      println("Combinenumber=" + combineNumber)

      assert(nodes.exists(_.position == startpoint), "cannot find FatherNodes when building pos " + (nodes.head.position+1) + " in level " + (nodes.head.level + 1).toString)



      val mayebeNeedCopy = !nodes.filter(_.position == startpoint).exists(_.fanout < maxFanout)

      val goOn = nodes.filter(_.position == startpoint).head.level == 0

      val nodeMaybeFull = nodes.filter(_.position == startpoint).head

      if (mayebeNeedCopy && !goOn) {
//        println("start copy " + nodeMaybeFull)
        val copy: Node = Node(nodeMaybeFull)
        list.append(copy)


        return getFatherNodes(combineNumber, nodes :+ copy , list, maxFanout)
      }

      val node = if(goOn)
        nodes.filter(_.position == startpoint).head
      else
        nodes.filter(_.position == startpoint).filter(_.fanout < maxFanout).head



      startpoint = node.getNextPos

//      println("search " + node)
      output.append(node)
    }
    output.foreach(_.fanoutInc())
//    println("push " + output.toSet)
    output.toSet
  }
}