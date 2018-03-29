package jsky.app.ot.viewer.planner

import jsky.app.ot.tpe.TpeImageFeature
import jsky.app.ot.tpe.feat.TpeTargetPosFeature
import jsky.app.ot.gemini.tpe.EdIterOffsetFeature

import jsky.app.ot.tpe.TpeImageInfo
import java.awt.{BasicStroke, Color, Graphics, Graphics2D, RenderingHints}
import java.awt.geom.Point2D

import jsky.app.ot.tpe.TpeContext
import scala.math._

import edu.gemini.spModel.target.offset.OffsetPosBase
import edu.gemini.spModel.target.offset.OffsetPointingOrigin

import scala.collection.JavaConversions._


class PlannerOffsetFeature extends EdIterOffsetFeature("PlannerOffsetFeature", "Fun!") {

  var seqs: List[InstSequence] = List.empty

  def setSeqs(all: List[InstSequence]): Unit = {
    seqs = all
  }

  def allSelected(ctx: TpeContext): List[OffsetPosBase] = {
    val allSolc = ctx.offsets.allJava.toList
    val allPos = allSolc.map(solc => solc.posList).flatten

    // get thee absorbed
    OffsetPointingOrigin.updateOffsetBases(allPos)

    allPos
  }


  override def draw(g: Graphics, tii: TpeImageInfo): Unit = {
    println("PlannerOffsetFeature.draw")
    seqs.foreach(seq => drawSeq(seq))
  }
    

  def drawSeq(seq: InstSequence): Unit = {
    val ctx = TpeContext(seq.observation)
    val pad = ctx.instrument.get.getPosAngleDegrees 
    val par = toRadians(pad)
    println("  Sequence: " + seq.title)
    println("  Pos angle:" + pad)
    println("  Target:   " + seq.target.toString)
    println("  Offsets:")

    allSelected(ctx).map(off => showOffset(off, par))
  }

  def showOffset(opb: OffsetPosBase, par: Double): Unit = {

    val radecOffset = OffsetPointingOrigin.offsetPosToRadecOffset(opb, par);
//    val screenPix = _iw.offsetToScreenCoords(radecOffset.x, radecOffset.y)

    println("    Offset")
    println("      OffsetPosBase.XAxis: " + opb.getXAxis)
    println("      OffsetPosBase.YAxis: " + opb.getYAxis)
    println("      radecOffset.x: " + radecOffset.x)
    println("      radecOffset.y: " + radecOffset.y)
//    println("      screenPix.x: " + screenPix.x)
//    println("      screenPix.y: " + screenPix.y)
  }



}

