package jsky.app.ot.viewer.planner

import jsky.app.ot.tpe.{TpeImageWidget, InstrumentContext, TpeContext}
import jsky.app.ot.gemini.tpe.EdIterOffsetFeature
import jsky.app.ot.tpe.feat.TpeTargetPosFeature
import jsky.app.ot.tpe.TpeImageInfo
import java.awt.{BasicStroke, Color, Graphics, Graphics2D, RenderingHints}
import java.awt.geom.{Point2D, Rectangle2D}
import scala.collection.JavaConversions._
import jsky.coords.WorldCoords
import edu.gemini.spModel.target.offset.OffsetPosBase
import edu.gemini.spModel.target.offset.OffsetPointingOrigin


// TODO enable drag mode
class PlannerImageWidget(p: Planner) extends TpeImageWidget(p) {

  var sxSeqs: List[InstSequence] = List.empty
  var dxSeqs: List[InstSequence] = List.empty

  val tpeImageInfo = new TpeImageInfo
  val offsetFeature = new PlannerOffsetFeature
  val targetPosFeature = new TpeTargetPosFeature

  addFeature(targetPosFeature)
  addFeature(offsetFeature)


  // I accept full blame for mutable state values
  def setSeqs(sx: List[Sequence], dx: List[Sequence]): Unit = {

    sxSeqs = sx.flatMap{
      case instSeq: InstSequence => Some(instSeq)
      case _ => None
    }

    dxSeqs = dx.flatMap{
      case instSeq: InstSequence => Some(instSeq)
      case _ => None
    }

    val all = (sxSeqs ++ dxSeqs)


    offsetFeature.reinit(this, _imgInfo)
    targetPosFeature.reinit(this, _imgInfo)

    offsetFeature.setSeqs(all)

    repaint()
  }


  override def paintLayer(g2d: Graphics2D, region: Rectangle2D): Unit = synchronized {
    println("PlannerImageWidget.paintLayer")

    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)

    super.paintLayer(g2d, region)

    _featureList.toList.foreach(feat => feat.draw(g2d, _imgInfo))

  }

  def oneSeq(seq: InstSequence): Unit = {
    setBasePos(new WorldCoords(seq.target.getXAxis, seq.target.getYAxis))
    reset(TpeContext(seq.observation))
    repaint()
  }

  def llSelected(ctx: TpeContext): List[OffsetPosBase] = {
    val allSolc = ctx.offsets.allJava.toList
    val allPos = allSolc.map(solc => solc.posList).flatten

    // get thee absorbed
    OffsetPointingOrigin.updateOffsetBases(allPos)

    allPos
  }

  
  override def reset(ctx: TpeContext): Unit = {
     if(_ctx.instrument.isDefined) {
       _ctx.instrument.get.removePropertyChangeListener(this)
     }

     if(_ctx.targets.base.isDefined) {
       _ctx.targets.base.get.deleteWatcher(this)
     } else {
        // There is no target to view, but we need to update the image widgets with new WCS info.
       clear
     }

     _ctx = ctx

     if(_ctx.instrument.isDefined) {
       _ctx.instrument.get.addPropertyChangeListener(this)
       setPosAngle(_ctx.instrument.get.getPosAngleDegrees)
     }
  }





}

