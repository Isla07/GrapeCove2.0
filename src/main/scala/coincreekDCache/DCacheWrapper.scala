package coincreekDCache
import chisel3._
import chisel3.util._
import org.chipsalliance.cde.config.{Parameters, Field}
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.devices.tilelink._
import _root_.circt.stage.ChiselStage

class TLCManager()(
    implicit p: Parameters
) extends LazyModule {
  val access = TransferSizes(1, blockBytes)
  val xfer   = TransferSizes(blockBytes, blockBytes)
  val atom   = TransferSizes(1, beatBytes)

  val node = TLManagerNode(Seq(TLManagerPortParameters(
    managers = Seq(TLManagerParameters(
      address = Seq(AddressSet(0x80000000L, 0xffffL)),
      resources = (new SimpleDevice("tlc-manager", Seq("example,tlcmanager"))).reg,
      regionType = RegionType.CACHED,
      supportsAcquireB = xfer,
      supportsAcquireT = xfer,
      supportsGet = atom,
      supportsPutFull = atom,
      supportsPutPartial = atom,
      supportsArithmetic = atom,
      supportsLogical = atom,
      supportsHint = access,
      mayDenyGet = true,
      mayDenyPut = true,
      alwaysGrantsT = true,
      fifoId = None,
    )),
    beatBytes = beatBytes,
    endSinkId = nMSHRs,
    minLatency = 1,
  )))

  lazy val module = new LazyModuleImp(this) {
    implicit val edge = node.edges.in(0)
    val (in, _)       = node.in(0)

    val (c_first, c_last, _) = edge.firstlast(in.c)

    in.a.ready := false.B
    in.b.valid := false.B
    in.e.ready := false.B
    in.c.ready := true.B

    val s1_cValid = RegNext(in.c.fire && c_last)
    val s1_cReq   = RegEnable(in.c.bits, in.c.fire && c_last)

    in.b.valid := RegNext(RegNext(s1_cValid))
    in.b.bits := edge.Probe(
      fromAddress = s1_cReq.address,
      toSource = 0.U,
      lgSize = log2Ceil(blockBytes).U,
      capPermissions = TLPermissions.toN,
    )._2

    in.d.valid := s1_cValid && s1_cReq.opcode === TLMessages.Release
    in.d.bits  := edge.ReleaseAck(in.c.bits)

  }
}

class DCacheWrapper()(
    implicit p: Parameters
) extends LazyModule {

  val dcacheClient = LazyModule(new CCDCache()(p))
  val manager      = LazyModule(new TLCManager()(p))
  val tlXbar       = LazyModule(new TLXbar)

  tlXbar.node  := TLWidthWidget(beatBytes) := dcacheClient.node
  manager.node := tlXbar.node

  lazy val module = new DCacheWrapperImp(this)
}

class DCacheWrapperImp(outer: DCacheWrapper) extends LazyModuleImp(outer) {
  val io = IO(new DataExchangeIO())
  outer.dcacheClient.module.io <> io
}

object Main extends App {

  val firtoolOptions = Array(
    "--lowering-options=" + List(
      "disallowLocalVariables",
      "disallowPackedArrays",
      "locationInfoStyle=wrapInAtSquareBracket",
    ).reduce(_ + "," + _)
  )

  lazy val dcacheWrapper = LazyModule(new DCacheWrapper()(Parameters.empty))
  ChiselStage.emitSystemVerilogFile(dcacheWrapper.module, args, firtoolOptions)
}
