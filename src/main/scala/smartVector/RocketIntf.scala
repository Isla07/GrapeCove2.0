package smartVector

import chisel3._
import chisel3.util._

import SmartParam._
import freechips.rocketchip.rocket._

// RVU (Rocket Vector Interface)

class VInfo extends Bundle {
    val vl     = (UInt(bVL.W))
    val vstart = (UInt(bVstart.W))
    val vma    = (Bool())
    val vta    = (Bool())
    val vsew   = (UInt(3.W))
    val vlmul  = (UInt(3.W))
    val vxrm   = (UInt(2.W))
    val frm    = (UInt(3.W))
}

class RVUissue extends Bundle {
    val inst   = UInt(32.W)
    val rs1    = UInt(64.W)
    val rs2    = UInt(64.W)
    val vInfo  = new VInfo
}

class RVUMemoryReq extends Bundle {
    // ldst queue index
    val idx     = UInt(4.W)
    // memop
    val addr    = UInt(64.W)
    val signed  = Bool()
    // 0 for load; 1 for store
    val cmd     = Bool()
    // store info
    val data    = UInt(64.W)
    val mask    = UInt(8.W)
}

class RVUMemoryResp extends Bundle {
    // ldst queue index
    val idx      = UInt(4.W)
    // load result
    val data     = UInt(64.W)
    val mask     = UInt(8.W)
    // cache miss
    val nack     = Bool()

    val has_data = Bool()
}
class AlignmentExceptions extends Bundle {
    val ld = Bool()
    val st = Bool()
}

class HellaCacheExceptions extends Bundle {
    val ma = new AlignmentExceptions
    val pf = new AlignmentExceptions
    val gf = new AlignmentExceptions
    val ae = new AlignmentExceptions
}

class RVUMemory extends Bundle {
    val req  = Decoupled(new RVUMemoryReq)
    val resp = Flipped(Valid(new RVUMemoryResp))
    val xcpt = Input(new HellaCacheExceptions)
}

class RVUCommit extends Bundle {
    val commit_vld      = Output(Bool())
    val return_data_vld = Output(Bool()) // need to update scalar rf
    val return_data     = Output(UInt(64.W))
    val return_reg_idx  = Output(UInt(5.W))
    val exception_vld   = Output(Bool())
    val illegal_inst    = Output(Bool())
    val update_vl       = Output(Bool())
    val update_vl_data  = Output(UInt(5.W))
    val xcpt = Output(new HellaCacheExceptions)
}

class RVUExtra extends Bundle {
    //val vpu_ready = Output(Bool())
} 


