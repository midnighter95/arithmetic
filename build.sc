import mill._
import mill.scalalib._
import mill.scalalib.scalafmt._
import $file.common

object v {
  val scala = "2.13.10"
  val spire = ivy"org.typelevel::spire:0.18.0"
  val evilplot = ivy"io.github.cibotech::evilplot:0.9.0"
  val oslib =  ivy"com.lihaoyi::os-lib:0.9.1"
  val chiselCrossVersions = Map(
    "5.0.0" -> (ivy"org.chipsalliance::chisel:5.0.0", ivy"org.chipsalliance:::chisel-plugin:5.0.0"),
  )

  val scalatest = ivy"org.scalatest::scalatest:3.2.0"
  val scalapar = ivy"org.scala-lang.modules::scala-parallel-collections:1.0.4"
  val mainargs = ivy"com.lihaoyi::mainargs:0.5.0"
}

object arithmetic extends Cross[Arithmetic](v.chiselCrossVersions.keys.toSeq)

object arithmetictest extends Cross[ArithmeticTest](v.chiselCrossVersions.keys.toSeq)

trait Arithmetic
  extends common.ArithmeticModule
    with ScalafmtModule
    with Cross.Module[String] {

  override def scalaVersion = T(v.scala)

  override def millSourcePath = os.pwd / "arithmetic"

  def spireIvy = v.spire

  def evilplotIvy = v.evilplot

  def mainargsIvy = v.mainargs

  def chiselModule = None

  def chiselPluginJar = None

  def chiselIvy = Some(v.chiselCrossVersions(crossValue)._1)

  def chiselPluginIvy = Some(v.chiselCrossVersions(crossValue)._2)

  def elaborate = T {
    // class path for `moduleDeps` is only a directory, not a jar, which breaks the cache.
    // so we need to manually add the class files of `moduleDeps` here.
    upstreamCompileOutput()
    mill.util.Jvm.runLocal(
      finalMainClass(),
      runClasspath().map(_.path),
      Seq(
        "--dir", T.dest.toString,
      ),
    )
    println("output to"+T.dest.toString)
    PathRef(T.dest)
  }

  def chirrtl = T {
    os.walk(elaborate().path).collectFirst { case p if p.last.endsWith("fir") => p }.map(PathRef(_)).get
  }

  def chiselAnno = T {
    os.walk(elaborate().path).collectFirst { case p if p.last.endsWith("anno.json") => p }.map(PathRef(_)).get
  }

  def topName: Target[String] = T{
    chirrtl().path.last.split('.').head
  }

  def mfccompile = T {
    os.proc("firtool",
      chirrtl().path,
      s"--annotation-file=${chiselAnno().path}",
      "--disable-annotation-unknown",
      "--strip-fir-debug-info",
      "--strip-debug-info",
      "-dedup",
      "-O=debug",
      "--verilog",
      "--preserve-values=named",
      "--output-annotation-file=mfc.anno.json",
      "--lowering-options=verifLabels",
      s"-o=${T.dest/topName()}"
    ).call(T.dest)
    PathRef(T.dest)
  }

}

trait ArithmeticTest
  extends common.ArithmeticTestModule
    with Cross.Module[String] {

  override def scalaVersion = T(v.scala)

  override def millSourcePath = os.pwd / "tests"

  def arithmeticModule = arithmetic(crossValue)

  def scalatestIvy = v.scalatest

  def scalaparIvy = v.scalapar

  def spireIvy = v.spire

  def evilplotIvy = v.evilplot

  def oslibIvy = v.oslib

  def chiselModule = None

  def chiselPluginJar = None

  def chiselIvy = Some(v.chiselCrossVersions(crossValue)._1)

  def chiselPluginIvy = Some(v.chiselCrossVersions(crossValue)._2)
}
