package jsky.app.ot.viewer.planner

import edu.gemini.spModel.target.offset.OffsetPosBase
import edu.gemini.spModel.target.offset.OffsetPointingOrigin
import jsky.app.ot.tpe.TpeContext

import scala.collection.JavaConversions._

import scala.math._

import java.awt.geom.Point2D

import jsky.coords.WorldCoords

// Take a WCS mount position for a recursive walk through the presets
object SequenceWalker {

  def allSelected(ctx: TpeContext): List[OffsetPosBase] = {
    val allSolc = ctx.offsets.allJava.toList
    val allPos = allSolc.map(solc => solc.posList).flatten
    // get thee absorbed
    OffsetPointingOrigin.updateOffsetBases(allPos)
    allPos
  }

  // wc start off as the mount position in WCS. 
  def walkSeqs(seqs: List[InstSequence]): List[Point2D.Double] = walkSeqs(seqs, Nil)

  def walkSeqs(seqs: List[InstSequence], points: List[Point2D.Double]): List[Point2D.Double] = 
    if(seqs.isEmpty) points
    else walkSeqs(seqs.tail, points ++ seqPoint(seqs.head))

  def seqPoint(seq: InstSequence): List[Point2D.Double] = 
    List(new Point2D.Double(seq.target.getXAxis, seq.target.getYAxis))
}


