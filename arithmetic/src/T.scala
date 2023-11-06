package addition

import chisel3._
import addition.prefixadder.common._
import multiplier._


object T extends App{
  val testRunDir = os.pwd / "test_run_dir"
  val name = "KSAdder32L.sv"
  println("Generating "+ name)
  os.makeDir.all(testRunDir)
  os.remove(testRunDir / name)
  os.write(testRunDir / name, chisel3.getVerilogString(new KSAdder32L))

}

class AdderLatency(width: Int) extends Module{
  val a: UInt = IO(Input(UInt(width.W)))
  val b: UInt = IO(Input(UInt(width.W)))
  val cin = IO(Input(UInt(1.W)))
  val z: UInt = IO(Output(UInt(width.W)))
  val cout = IO(Output(UInt(1.W)))

  val aReg = RegNext(a, 0.U(width.W))
  val bReg = RegNext(b, 0.U(width.W))
  val cReg = RegNext(cin, 0.U(1.W))

  val sum = aReg +& bReg +& cReg

  val zReg = RegNext(sum(31,0), 0.U(32.W))
  val coutReg = RegNext(sum(32), 0.U(1.W))

  z := zReg
  cout := coutReg
}

class AdderArea(width: Int) extends Module{
  val a: UInt = IO(Input(UInt(width.W)))
  val b: UInt = IO(Input(UInt(width.W)))
  val cin = IO(Input(UInt(1.W)))
  val z: UInt = IO(Output(UInt(width.W)))
  val cout = IO(Output(UInt(1.W)))

//  val Adder = Module(new BrentKungAdder(32))
//  Adder.a := a
//  Adder.b := b
//  Adder.cin := cin

  val sum = a +& b +& cin

  z := sum(31,0)
  cout := sum(32)
}

class Multiplier32L extends Module{
  val a: UInt = IO(Input(UInt(32.W)))
  val b: UInt = IO(Input(UInt(32.W)))
  val z: UInt = IO(Output(UInt(64.W)))

  val aReg = RegNext(a,0.U(32.W))
  val bReg = RegNext(b,0.U(32.W))

  val Mul = Module(new Multiplier32)
  Mul.a := aReg
  Mul.b := bReg
  Mul.sew := 4.U(3.W)

  val zReg = RegNext(Mul.z, 0.U(64.W))

  z := zReg
}

class Abs(n: Int) extends Module {
  val io = IO(new Bundle() {
    val aIn = Input(SInt(n.W))
    val bIn = Input(SInt(n.W))
    val signIn = Input(Bool())
    val aOut = Output(UInt(n.W))
    val bOut = Output(UInt(n.W))
    val aSign = Output(Bool())
    val bSign = Output(Bool())
  })
  val a = Wire(SInt(n.W))
  val b = Wire(SInt(n.W))
  val aSign = io.aIn(n - 1)
  val bSign = io.bIn(n - 1)
  a := io.aIn
  b := io.bIn
  io.aOut := Mux(io.signIn && aSign, -a, a).asUInt
  io.bOut := Mux(io.signIn && bSign, -b, b).asUInt
  io.aSign := io.signIn && aSign
  io.bSign := io.signIn && bSign
}
class BKAdder32A extends Module{
  val a    = IO(Input(UInt(32.W)))
  val b    = IO(Input(UInt(32.W)))
  val cin  = IO(Input(Bool()))
  val z    = IO(Output(UInt(32.W)))
  val cout = IO(Output(Bool()))


  val m = Module(new BrentKungAdder(32))
  m.a := a
  m.b := b
  m.cin := cin

  z := m.z
  cout := m.cout
}

class KSAdder32A extends Module{
  val a    = IO(Input(UInt(32.W)))
  val b    = IO(Input(UInt(32.W)))
  val cin  = IO(Input(Bool()))
  val z    = IO(Output(UInt(32.W)))
  val cout = IO(Output(Bool()))


  val m = Module(new KoggeStoneAdder(32))
  m.a := a
  m.b := b
  m.cin := cin

  z := m.z
  cout := m.cout
}

class BKAdder32L extends Module{
  val a    = IO(Input(UInt(32.W)))
  val b    = IO(Input(UInt(32.W)))
  val cin  = IO(Input(Bool()))
  val z    = IO(Output(UInt(32.W)))
  val cout = IO(Output(Bool()))

  val aReg = RegNext(a)
  val bReg = RegNext(a)
  val cinReg = RegNext(a)

  val m = Module(new BrentKungAdder(32))
  m.a := aReg
  m.b := bReg
  m.cin := cinReg

  val zReg = RegNext(m.z)
  val coutReg = RegNext(m.cout)

  z := zReg
  cout := coutReg
}

class KSAdder32L extends Module{
  val a    = IO(Input(UInt(32.W)))
  val b    = IO(Input(UInt(32.W)))
  val cin  = IO(Input(Bool()))
  val z    = IO(Output(UInt(32.W)))
  val cout = IO(Output(Bool()))

  val aReg = RegNext(a)
  val bReg = RegNext(a)
  val cinReg = RegNext(a)

  val m = Module(new KoggeStoneAdder(32))
  m.a := aReg
  m.b := bReg
  m.cin := cinReg

  val zReg = RegNext(m.z)
  val coutReg = RegNext(m.cout)

  z := zReg
  cout := coutReg
}




