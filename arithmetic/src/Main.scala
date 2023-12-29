package arithmetic

import chisel3.stage.ChiselGeneratorAnnotation
import chisel3.stage.phases.Convert
import firrtl.AnnotationSeq
import firrtl.stage.FirrtlCircuitAnnotation
import mainargs._

object Main {
  @main def elaborate(
                       @arg(name = "dir") dir: String,
                     ) = {
    val dir_ = os.Path(dir, os.pwd)
    var topName: String = null
    val annos: AnnotationSeq = Seq(
      new chisel3.stage.phases.Elaborate,
      new Convert
    ).foldLeft(
      Seq(
        ChiselGeneratorAnnotation(() => new DemoAdderWithGraph)
      ): AnnotationSeq
    ) { case (annos, stage) => stage.transform(annos) }
      .flatMap {
        case FirrtlCircuitAnnotation(circuit) =>
          topName = circuit.main
          os.write(dir_ / s"$topName.fir", circuit.serialize)
          None
        case _: chisel3.stage.DesignAnnotation[_] => None
        case a => Some(a)
      }
    os.write(dir_ / s"$topName.anno.json", firrtl.annotations.JsonProtocol.serialize(annos))
  }

  def main(args: Array[String]): Unit = ParserForMethods(this).runOrExit(args)
}
