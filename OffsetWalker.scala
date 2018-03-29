package jsky.app.ot.viewer.planner

import edu.gemini.spModel.target.offset.OffsetPosBase
import edu.gemini.spModel.target.offset.OffsetPointingOrigin
import jsky.app.ot.tpe.TpeContext

import scala.collection.JavaConversions._

import scala.math._

import java.awt.geom.AffineTransform

import jsky.coords.WorldCoords

// recursivly take a WCS mount position for a walk through the presets
object OffsetWalker {

  def allSelected(ctx: TpeContext): List[OffsetPosBase] = {
    val allSolc = ctx.offsets.allJava.toList
    val allPos = allSolc.map(solc => solc.posList).flatten
    // get thee absorbed
    OffsetPointingOrigin.updateOffsetBases(allPos)
    allPos
  }

  // wc start off as the mount position in WCS. 
  def walkSeqs(wc: WorldCoords, seqs: List[InstSequence]): Unit = {
    println("Mount WCS: " + wc.toString)

    seqs.foreach(seq => walkSeq(wc, seq))
  }

  // Sequence targets are defined in degrees
  def walkSeq(mountPos: WorldCoords, seq: InstSequence): Unit = {
    val ctx = TpeContext(seq.observation)
    val pad = ctx.instrument.get.getPosAngleDegrees
    val par = toRadians(pad)


    // mount position in absolute degrees
    val mountX = mountPos.getRaDeg
    val mountY = mountPos.getDecDeg

    // sequence position in absolute degrees
    val seqX  = seq.target.getXAxis
    val seqY  = seq.target.getYAxis
    val seqPos = new WorldCoords(seqX, seqY)
    println("  Seq WCS: " + seqPos.toString)
    val dis: Array[Double] = mountPos.dispos(seqPos)
  
    println("  dis[0] = " + dis(0))
    println("  dis[1] = " + dis(1))

    // distance from mount to sequence
    val dx = dis(0)
    val dy = seqY + mountY

    allSelected(ctx).map(opb => if(opb.isDetxy) walkDetxyOffset(mountPos, seqPos, opb, par))
//       else if(opb.isRadec) walkRadecOffset(wc, opb, par))
  }

  def arcsecToDegrees(arcsec: Double) = {
    val arcsecToDegrees = 0.000277778
    arcsec * arcsecToDegrees
  }

  def walkDetxyOffset(
      mountPos: WorldCoords,
      targetPos: WorldCoords,
      opb: OffsetPosBase,
      par: Double): Unit = {

    val radecOffset = OffsetPointingOrigin.offsetPosToRadecOffset(opb, par);
    val offsetX = arcsecToDegrees(radecOffset.x)
    val offsetY = -arcsecToDegrees(radecOffset.y)
   
    val wcX = targetPos.getRaDeg + offsetX
    val wcY = targetPos.getDecDeg + offsetY

    val wc = new WorldCoords(wcX, wcY)
    println("    Offset WCS: " + wc.toString)
  }

  def walkRadecOffset(
      mountPos: WorldCoords,
      targetPos: WorldCoords,
      opb: OffsetPosBase,
      par: Double): Unit = {

    val radecOffset = OffsetPointingOrigin.offsetPosToRadecOffset(opb, par);
    val offsetX = arcsecToDegrees(radecOffset.x)
    val offsetY = arcsecToDegrees(radecOffset.y)
   
    val wcX = targetPos.getRaDeg + offsetX
    val wcY = targetPos.getDecDeg + offsetY

    val wc = new WorldCoords(wcX, wcY)
    println("    Offset WCS: " + wc.toString)
  }
}

