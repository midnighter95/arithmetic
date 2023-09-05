package multiplier

import chisel3._
import chisel3.util.experimental.decode.{TruthTable, decoder}
import chisel3.util.{BitPat, Cat}
import utils.{extend, sIntToBitPat}

class VecBooth(width: Int)(radixLog2: Int, signed: Boolean = true) extends Module {
  val input = IO(Input(UInt((width+1).W)))
  val output = IO(
    Output(
      Vec(
        width / radixLog2 + 1, // = ceil(width / radixLog2)
        SInt((radixLog2 + 1).W)
      )
    )
  )

  val paddingLeftWidth = width + radixLog2 - width % radixLog2
  val paddedInput = Cat(extend(input(width,1), paddingLeftWidth, signed), input(0))

  val boothEncodingCoeff = Seq.tabulate(radixLog2 + 1) {
    case i if i == radixLog2 => -(1 << (radixLog2 - 1))
    case i if i == 0         => 1
    case i                   => 1 << (i - 1)
  }

  val boothEncodingTable = TruthTable(
    Seq
      .tabulate(1 << (radixLog2 + 1)) { i =>
        Seq
          .tabulate(radixLog2 + 1)((bit: Int) => if (BigInt(i).testBit(bit)) 1 else 0)
          .zip(boothEncodingCoeff)
          .map {
            case (a, b) => a * b
          }
          .sum
      }
      .zipWithIndex
      .map {
        case (o, i) =>
          val w = radixLog2 + 1
          (sIntToBitPat(i, w), sIntToBitPat(o, w))
      },
    BitPat.dontCare(radixLog2 + 1)
  )

  output := Seq
    .tabulate(output.size) { i =>
      decoder(paddedInput(radixLog2 * (i + 1), radixLog2 * i), boothEncodingTable)
    }
    .map(_.asSInt)
}

object VecBooth {
  def recode(width: Int)(radix: Int, signed: Boolean = false)(x: UInt): Vec[SInt] = {
    val recoder = Module(new Booth(width)(radix, signed))
    recoder.input := x
    recoder.output
  }
}
