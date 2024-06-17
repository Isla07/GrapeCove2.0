package coincreekDCache

import chisel3._
import chisel3.util._

import MemoryOpConstants._

class DataExchangeReq extends Bundle {
  val paddr  = UInt(paddrWidth.W)
  val source = UInt(srcWidth.W)
  val cmd    = UInt(M_SZ.W)
  // 64 -> 2, 512 -> 3
  val size   = UInt(log2Up(log2Up(dataBytes)).W)
  val signed = Bool()
  val wdata  = UInt(dataWidth.W)
  val wmask  = UInt(dataBytes.W)

  // for replace & test fill
  // operate a specific cache way
  val specifyValid = Bool()
  val specifyWay   = UInt(log2Up(nWays).W)

}

class DataExchangeResp extends Bundle {
  val source = UInt(srcWidth.W)
  val hit    = Bool()
  val data   = UInt(dataWidth.W)
}

class DataExchangeIO extends Bundle {
  val req  = Flipped(Decoupled(new DataExchangeReq))
  val resp = Decoupled(new DataExchangeResp)
}
