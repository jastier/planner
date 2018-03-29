package jsky.app.ot.viewer.planner

import edu.gemini.skycalc.Angle.Unit._
import edu.gemini.skycalc.{Angle, Offset, Coordinates}

import edu.gemini.spModel.target.offset.OffsetPosBase
import edu.gemini.spModel.target.offset.OffsetPointingOrigin
import jsky.app.ot.tpe.TpeContext

import scala.collection.JavaConversions._

import edu.gemini.spModel.target.SPTarget
import edu.gemini.spModel.target.system.ICoordinate

import jsky.coords.{DMS, HMS, WorldCoords}

import edu.gemini.skycalc.CoordinateDiff


import java.awt.geom.{AffineTransform, Point2D, Rectangle2D}
import scala.math._


object CopointMath {

  // radius from pointing center in which targets can be seen without a preset
  val COPOINT_RADIUS_ARCSEC = 40.0
  val DEGREES_PER_ARCSECOND = 0.000277778

  def arcsecondsPerPixel(pixels: Int): Double = {
    if(pixels > 0) COPOINT_RADIUS_ARCSEC*2.0 / pixels
    else 1.0
  }

  def isCopointed(mount: WorldCoords, offset: WorldCoords): Boolean = {
    val diff = new CoordinateDiff(mount.getX, mount.getY, offset.getX, offset.getY)
    diff.getDistance.getMagnitude <= COPOINT_RADIUS_ARCSEC
  }
     

  /** return the Sequence target position in world coords  */
  def sequencePosWorldCoords(target: SPTarget): WorldCoords = {
    val targetCoordinates = target.getSkycalcCoordinates
    val raDeg = targetCoordinates.getRaDeg
    val decDeg = targetCoordinates.getDecDeg
    new WorldCoords(raDeg, decDeg)
  }

  /** return the offset position in world coords  */
  def offsetPosWorldCoords(target: SPTarget, opb: OffsetPosBase, par: Double): WorldCoords = {
    val baseRadec = opb.getBaseRadec
    val baseDetxy = opb.getBaseDetxy
    val poRadec = opb.getPoRadec
    val poDetxy = opb.getPoDetxy

    val pt = new Point2D.Double(poDetxy.x, poDetxy.y)

    AffineTransform.getRotateInstance(par).transform(pt,pt)
    val x = poRadec.x + pt.x
    val y = poRadec.y + pt.y

    val angleRa  = new Angle(x, ARCSECS)
    val angleDec = new Angle(y, ARCSECS)

    val angleRaDeg  = angleRa.convertTo(DEGREES).getMagnitude
    val angleDecDeg = angleDec.convertTo(DEGREES).getMagnitude

    val targetCoordinates = target.getSkycalcCoordinates

    if (opb.isDetxy){
      val raDeg = targetCoordinates.getRaDeg + angleRaDeg
      val decDeg = targetCoordinates.getDecDeg - angleDecDeg
      new WorldCoords(raDeg, decDeg)
    }

    else {
      val raDeg = targetCoordinates.getRaDeg + angleRaDeg
      val decDeg = targetCoordinates.getDecDeg + angleDecDeg
      new WorldCoords(raDeg, decDeg)
    } 
  }


  // check everything for copointing
  def status(mountPos: WorldCoords, sx: List[InstSequence], dx: List[InstSequence]): Unit = {
    // check sequence targets
  }


  // Decide how to split two mount positions
  def mountPosWorldCoords(sx: List[InstSequence], dx: List[InstSequence]): WorldCoords = {

    // both empty, default to zeros
    if((sx.isEmpty) && (dx.isEmpty)) 
      new WorldCoords(new HMS(0), new DMS(0))

    // sx empty, use dx
    else if (sx.isEmpty) {
      val raDeg  = dx.head.target.getXAxis
      val decDeg = dx.head.target.getYAxis
      new WorldCoords(raDeg, decDeg)
   }

    // dx empty, use sx
    else if (dx.isEmpty) {
      val raDeg  = sx.head.target.getXAxis
      val decDeg = sx.head.target.getYAxis
      new WorldCoords(raDeg, decDeg)
   }

    // average sx and dx.  Watch meridian crossings and negative zero Dec values
    else {
      // get coordinates of SX and DX sequences
      val sxCoordinates = sx.head.target.getSkycalcCoordinates
      val dxCoordinates = dx.head.target.getSkycalcCoordinates

      // get offset in degrees
      val diff: CoordinateDiff = new CoordinateDiff(sxCoordinates, dxCoordinates)
      val offset: Offset = diff.getOffset
      val pDeg = offset.p.convertTo(DEGREES).getMagnitude
      val qDeg = offset.q.convertTo(DEGREES).getMagnitude

      // Approach from both endpoints to the midpoint by half the distance
      val sxMidRaDeg  = sxCoordinates.getRaDeg  + pDeg/2.0
      val sxMidDecDeg = sxCoordinates.getDecDeg + qDeg/2.0
      val dxMidRaDeg  = dxCoordinates.getRaDeg  - pDeg/2.0
      val dxMidDecDeg = dxCoordinates.getDecDeg - qDeg/2.0

      // Bring negative meridian crossings into the positive axis if needed
      val posSxMidRaDeg = if(sxMidRaDeg < 0) sxMidRaDeg + 360.0 else sxMidRaDeg
      val posDxMidRaDeg = if(dxMidRaDeg < 0) dxMidRaDeg + 360.0 else dxMidRaDeg

      // create two coordinates that are the same but from different directions.
      val sxMidCoordinates = new Coordinates(posSxMidRaDeg, sxMidDecDeg)
      val dxMidCoordinates = new Coordinates(posDxMidRaDeg, dxMidDecDeg)

      // Avoid negative zero Dec term that may appear in at most one of the coordinates
      val posCoordinates = if(sxMidCoordinates.getDecDeg < 0) dxMidCoordinates else sxMidCoordinates
      
      new WorldCoords(posCoordinates.getRaDeg, posCoordinates.getDecDeg)
    }
  }
}

