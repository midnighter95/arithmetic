package arithmetic

case class Graphml(nodes: Seq[Node]){

  override def toString: String =
    "<graphml>\n" +
    "  <key id=\"label\" for=\"node\" attr.name=\"label\" attr.type=\"string\"/> <!--Define custom label property-->\n" +
    "  <graph id=\"G\" edgedefault=\"directed\">\n"+
      (nodes.map { node =>
      s"""    <node id="${node.level}-${node.position-1}">\n""" +
      s"""      <data key="label">${node.level}-${node.position-1}</data>      <!--Using custom label property-->\n""" +
      "    </node>"
    } ++ nodes.filter { c => c.fathers !=  Set() }
      .flatMap(c => c.fathers.zip(Seq.fill(c.fathers.size)(c)))
      .map { case (father, son) =>
        s"""    <edge source="${father.level}-${father.position-1}"  target="${son.level}-${son.position-1}"/>"""
      }).mkString("\n") + "\n" +
      "  </graph>\n" +
      "</graphml>"

}