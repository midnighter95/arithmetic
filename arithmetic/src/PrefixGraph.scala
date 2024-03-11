package arithmetic

case class PrefixGraph(nodes: Seq[Node]) {
  override def toString: String =
    "digraph G {\n" + (nodes.map { node =>
      s""""${node.toString}""""
    } ++ nodes.filter { c => c.fathers != Set() }
      .flatMap(c => c.fathers.zip(Seq.fill(c.fathers.size)(c)))
      .map { case (father, son) =>
        s""""${father.toString}" -> "${son.toString}""""
    }).mkString("\n") + "\n}"

}