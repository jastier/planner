package jsky.app.ot.viewer.planner

import edu.gemini.spModel.target.offset.OffsetPosBase
import edu.gemini.spModel.target.offset.OffsetPointingOrigin
import jsky.app.ot.tpe.TpeContext

import scala.collection.JavaConversions._

import scala.math._


/** a Debug class that does not actually get used in the Planner */
object Reporter {

  def reportSeqs(p: Planner, seqs: List[InstSequence]): Unit = {
    p.message("INSTRUMENT SEQUENCES, n = " + seqs.size + ":")
    seqs.foreach(seq => reportSeq(p, seq))
  }

  def allSelected(ctx: TpeContext): List[OffsetPosBase] = {
    val allSolc = ctx.offsets.allJava.toList
    val allPos = allSolc.map(solc => solc.posList).flatten

    // get thee absorbed
    OffsetPointingOrigin.updateOffsetBases(allPos)

    allPos
  }

  def reportSeq(p: Planner, seq: InstSequence): Unit = {
    val ctx = TpeContext(seq.observation)
    val pad = ctx.instrument.get.getPosAngleDegrees
    val par = toRadians(pad)
    p.message("  Sequence: " + seq.title)
    p.message("  Pos angle:" + pad)
    p.message("  Target:   " + seq.target.toString)
    p.message("  Offsets:")

    allSelected(ctx).map(off => reportOffset(p, off, par))
  }

  def reportOffset(p: Planner, opb: OffsetPosBase, par: Double): Unit = {

    val radecOffset = OffsetPointingOrigin.offsetPosToRadecOffset(opb, par);

    p.message("    Offset")
    if(opb.isDetxy) p.message("      DETXY")
    if(opb.isRadec) p.message("      RADEC")
    p.message("      OffsetPosBase.XAxis: " + opb.getXAxis)
    p.message("      OffsetPosBase.YAxis: " + opb.getYAxis)
    p.message("      radecOffset.x: " + radecOffset.x)
    p.message("      radecOffset.y: " + radecOffset.y)
  }
}

