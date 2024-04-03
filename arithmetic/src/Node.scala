package arithmetic

import scala.language.postfixOps
import scala.math.Ordered.orderingToOrdered

case class Node(level: Int, position:Int, associativeNum:Int, copy:Int) extends Ordered[Node]{

  var fathers:Set[Node] = Set()


  var index:Int = 0

  var depth:Int = 1

  var fanout:Int = 0

  def fanoutInc():Unit = {
    fanout = fanout + 1
  }

  def getNextPos = position - depth

  def isFinish: Boolean = position == depth

  override def toString: String = s"Node$level-${position-1}-$index"

  def getReadableString:String = s"Node$level-${position-1}-$associativeNum-$depth"

  override def compare(that: Node): Int = (this.level, this.position).compare(that.level, that.position)

}

object Node  {
  var index:Int = 0

  def apply(position:Int, fathers: Set[Node]): Node = {
    if(fathers.size == 1) return fathers.head

    val newLevel = fathers.toSeq.map(_.level).max + 1

    val node = new Node(newLevel, position, fathers.size, 0)
    node.fathers = fathers
    node.depth = fathers.toSeq.map(_.depth).sum
    node
  }

  def apply(node:Node, index:Int): Node = {
    node.index = index
    node
  }

  def apply(position: Int): Node = Node(0, position, 1, 0)

  def apply(node:Node):Node = {
    val output = Node(node.level, node.position, node.associativeNum, node.copy+1)
    output.depth = node.depth
    output.fathers = node.fathers
    output
  }


}