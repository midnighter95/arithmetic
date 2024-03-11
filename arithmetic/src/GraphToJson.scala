package arithmetic

import ujson.{Arr, Obj}

object GraphToJson {
  def elaborate(dotgraph: Seq[Node]) = {

    val dotNodes: Seq[Obj] = dotgraph
      .zipWithIndex
      .map { case (c, objectIndex) =>
        Obj("_gvid" -> ujson.Num(objectIndex),
          "name" -> ujson.Str(c.toString))
      }

    val dotEdges: Seq[Obj] = dotgraph.filter { _.fathers != Set() }
      .flatMap(c => c.fathers.zip(Seq.fill(c.fathers.size)(c)))
      .zipWithIndex
      .map { case ((father, son), edgeIndex) =>
        Obj("_gvid" -> ujson.Num(edgeIndex), "tail" -> ujson.Num(father.index), "head" -> ujson.Num(son.index))
      }

    val output: Obj = Obj(
      "name" -> ujson.Str("G"),
      "objects" -> Arr(dotNodes: _*),
      "edges" -> Arr(dotEdges: _*)
    )

    os.write.over(os.pwd / "graph.json", ujson.write(output, indent = 4))
  }
}

