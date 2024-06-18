package coincreekDCache

import chisel3._
import chisel3.util._

object AddrDecoder {
  def getBankIdx(addr: UInt): UInt =
    addr(bankIdxBits + rowOffBits - 1, rowOffBits)

  def getSetIdx(addr: UInt): UInt =
    addr(setIdxBits + blockOffBits - 1, blockOffBits)

  def getRowIdx(addr: UInt): UInt =
    addr(rowIdxBits + bankIdxBits + rowOffBits - 1, bankIdxBits + rowOffBits)

  def getTag(addr: UInt): UInt =
    addr(addr.getWidth - 1, untagBits)
}