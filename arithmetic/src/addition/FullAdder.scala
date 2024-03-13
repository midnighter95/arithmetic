package addition

import chisel3._
import chisel3.util.Cat

trait FullAdder extends Module {
  val width: Int
//  require(width > 0)
  val a: UInt = IO(Input(UInt(width.W)))
  val b: UInt = IO(Input(UInt(width.W)))
  val z: UInt = IO(Output(UInt(width.W)))

  val cin:  Bool = IO(Input(Bool()))
  val cout: Bool = IO(Output(Bool()))

}

trait FullAdderWithAssert extends Module {
  val width: Int
  //  require(width > 0)
  val a: UInt = IO(Input(UInt(width.W)))
  val b: UInt = IO(Input(UInt(width.W)))
  val z: UInt = IO(Output(UInt(width.W)))

  val cin:  Bool = IO(Input(Bool()))
  val cout: Bool = IO(Output(Bool()))

  assert(a +& b + cin === Cat(cout, z))
}
trait FullAdderNetlist extends Module {
  val width: Int
  //  require(width > 0)
  val a: Seq[Bool] = Seq.tabulate(width)(i => IO(Input(Bool())).suggestName(s"a_${i}"))
  val b: Seq[Bool] = Seq.tabulate(width)(i => IO(Input(Bool())).suggestName(s"b_${i}"))

  val z: Seq[Bool] = Seq.tabulate(width)(i => IO(Output(Bool())).suggestName(s"z_${i}"))

  val cin:  Bool = IO(Input(Bool()))
  val cout: Bool = IO(Output(Bool()))
}
