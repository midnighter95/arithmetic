package arithmetic

import addition.prefixadder.PrefixAdderWithAssert
import addition.prefixadder.common.KoggeStoneAdder
import chisel3.stage.ChiselGeneratorAnnotation
import chisel3.stage.phases.Convert
import firrtl.AnnotationSeq
import firrtl.stage.FirrtlCircuitAnnotation
import mainargs._

object Main extends App
  {
    var topName: String = null
    println("Generating firrtl")
    val pwd = os.pwd / "output"
    os.remove.all(pwd)
    os.makeDir(pwd)
    val annos: AnnotationSeq = Seq(
      new chisel3.stage.phases.Elaborate,
      new Convert
    ).foldLeft(
      Seq(
        ChiselGeneratorAnnotation(() => new AdderFromJsonWithWrapper)
      ): AnnotationSeq
    ) { case (annos, stage) => stage.transform(annos) }
      .flatMap {
        case FirrtlCircuitAnnotation(circuit) =>
          topName = circuit.main
          os.write.over(pwd /  s"$topName.fir", circuit.serialize)
          None
        case _: chisel3.stage.DesignAnnotation[_] => None
        case a => Some(a)
      }
    os.write.over(os.pwd / "output" / s"$topName.anno.json", firrtl.annotations.JsonProtocol.serialize(annos))
  }
