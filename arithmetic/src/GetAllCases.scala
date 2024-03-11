package arithmetic

import scala.collection.mutable.ListBuffer
import scala.language.postfixOps

object GetAllCases {
  val width = 8
  val maxAssociativeNum = 4
  val finalLevel = 3

  def getInc(level:Int) :Int = {
    (math.pow(maxAssociativeNum, level) * (maxAssociativeNum-1)).toInt
  }

  val listForGap : List[Int]= 1 to (finalLevel-1) toList

  val listforquery = listForGap.map(c=>getInc(c))



  def test4() = {

    val layer0 = Seq(Seq(
      Node(4),
      Node(3),
      Node(2),
      Node(1),
    ))

    val layer1: Seq[Seq[Node]] = iterLayer(layer0, 1)
    println("cases in layer1=" + layer1.length)

    val layer2: Seq[Seq[Node]] = iterLayer(layer1, 2)
    println("cases in layer2=" + layer2.length)

//    layer2.map{c => println(PrefixGraph(c.distinct.sorted))}

  }

  def test8() = {

    val layer0 = Seq(Seq(
      Node(8),
      Node(7),
      Node(6),
      Node(5),
      Node(4),
      Node(3),
      Node(2),
      Node(1),
    ))

    val layer1: Seq[Seq[Node]] = iterLayer(layer0, 1)
    println("cases in layer1=" + layer1.length)

    val layer2: Seq[Seq[Node]] = iterLayer(layer1, 2)
    println("cases in layer2=" + layer2.length)

    val graph = PrefixGraph(layer2.head.distinct.sorted)
    println(graph)

    val layer3 = iterLayer(layer2, 3)
    println("cases in layer3=" + layer3.length)
    println(layer3.head.takeRight(8).map(c => c.associativeNum))
    println(layer3.head.slice(16, 24).map(c => c.associativeNum))
    println(layer3.head.slice(8, 16).map(c => c.associativeNum))
    println(layer3.head.take(8).map(c => c.associativeNum))

    val graph3 = PrefixGraph(layer3.head.distinct.sorted)
    println(graph3)

  }

  def iterLayer(layer: Seq[Seq[Node]], level: Int): Seq[Seq[Node]] = {
    val targetLayer = new ListBuffer[Seq[Node]]
    layer.foreach { i => {
      val oneLayercase: Seq[Seq[Node]] = getNextLayerCasesWithFormer(level, i)
      oneLayercase.foreach(targetLayer.append(_))
    }
    }
    targetLayer.toSeq
  }

  private def getNextLayerCasesWithFormer(level: Int, nodes: Seq[Node]): Seq[Seq[Node]] = {
    mergeNodesToCases(getNextLayerNodes(level, nodes)).map {
      _ ++ nodes
    }
  }

  /** Get the combination of those nodes, the all cases
    *
    * @return A Seq of Seq[Node], each element represents a possible case in this layer.
    */
  def mergeNodesToCases(nodes: Seq[Seq[Node]]): Seq[Seq[Node]] = for {
    t1 <- nodes(0)
    t2 <- nodes(1)
    t3 <- nodes(2)
    t4 <- nodes(3)
    t5 <- nodes(4)
    t6 <- nodes(5)
    t7 <- nodes(6)
    t8 <- nodes(7)
  } yield Seq(t8, t7, t6, t5, t4, t3, t2, t1)

  /** Get all nodes cases in next layer
    * In each postion, generate all possible nodes according to config.
    *
    * @return A Seq of ListBuffer[Node], each element represents all the possible Nodes in this position.
    */
  private def getNextLayerNodes(level:Int, nodes: Seq[Node]): Seq[Seq[Node]] = {
    val formerLayerNodes = nodes.take(width)
    (1 to width).map{
      c => getPossibleNodesFromOnePostion(level, c, formerLayerNodes)
    }
  }

  /**
    *
    * @param position the position where evaluation happens.
    * @param nodes    all nodes from the former level
    *
    * activated check:
    * node in final level need position==depth
    * node before needs sum>= (position - finalLevel * (maxAssociativeNum-1)
    * @todo add more check */
  def getPossibleNodesFromOnePostion(level: Int, position: Int, nodes: Seq[Node]): Seq[Node] = {
    var list = new ListBuffer[Node]
    val availableNodes = getAvailableNodes(maxAssociativeNum, nodes.takeRight(position))

    /** make sure there are enough Nodes in avableNodes list */
    val associativeNum = availableNodes.length.min(maxAssociativeNum)
    val minDepth = (position - listforquery.take(finalLevel-level).sum)

    /** See if this associative number satisfy verification */
    for (i <- 1 to associativeNum) {
      val father: Set[Node] = availableNodes.take(i).toSet
      val newDepth = availableNodes.take(i).map(_.depth).sum
      if (finalLevel == level && newDepth == position)
        list.append(Node(position, father))
      if (finalLevel > level && newDepth >= minDepth)
        list.append(Node(position, father))
    }
    list.toSeq
  }

  /** Get all nodes to satisfy associative condition
    *
    * @param nodes: all nodes which can be found(lower position)
    * @param maxAssociativeNum constainss
    *
    * @return all the availableNodes (satisfy nearby condition)
    */
  private def getAvailableNodes(maxAssociativeNum:Int, nodes:Seq[Node]): Seq[Node] = {
    var list = new ListBuffer[Node]
    /** the first node is it self */
    var startpoint = nodes.head.position
    for(i <- 1 to maxAssociativeNum;if nodes.exists(_.position == startpoint)){
      val node = nodes.filter(_.position == startpoint).head
      startpoint = node.getNextPos
      list.append(node)
    }
    list.toSeq
  }
}