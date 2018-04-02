package jsky.app.ot.viewer.planner

import java.awt.{BasicStroke, Color, Graphics, Graphics2D, RenderingHints}
import java.awt.event.{MouseEvent, MouseWheelEvent, MouseWheelListener, MouseMotionListener}
import java.awt.geom.{AffineTransform, Area, Ellipse2D, Line2D, Point2D, Rectangle2D}
import java.awt.event.InputEvent._
import javax.swing.JPanel
import scala.collection.mutable.ListBuffer
import scala.math._
import java.awt.RenderingHints._
import edu.gemini.spModel.target.offset.OffsetPosBase
import edu.gemini.spModel.target.offset.OffsetPointingOrigin
import java.awt.{Font, FontMetrics}
import java.awt.Dimension

import edu.gemini.spModel.target.SPTarget

import edu.gemini.skycalc.{Angle, Offset, Coordinates}

import scala.collection.JavaConversions._

import jsky.image.graphics.MeasureBand
import jsky.app.ot.tpe.feat.TpeTargetPosFeature

import jsky.image.gui.DivaGraphicsImageDisplay
import jsky.coords.{CoordinateConverter, HMS, DMS, WorldCoordinateConverter, WorldCoords}

import jsky.coords.WCSTransform
import jsky.image.gui.ImageCoordinateConverter

import scala.collection.mutable.ArrayBuffer

import java.awt.image.BufferedImage

// I accept full blame for mutable state values
class Plot(planner: Planner) extends DivaGraphicsImageDisplay with MouseWheelListener { 

  // Define a WCS transform.  See also:  WCSTransform.java
  var cra: Double     = 0.0  // Center right ascension in degrees
  var cdec: Double    = 0.0  // Center declination in degrees
  var xsecpix: Double = 1.0  // Number of arcseconds per pixel along x-axis
  var ysecpix: Double = 1.0  // Number of arcseconds per pixel along y-axis
  var xrpix: Double = 100.0  // Reference pixel X coordinate
  var yrpix: Double = 100.0  // Reference pixel Y coordinate
  var nxpix: Int      = 200  // Number of pixels along x-axis
  var nypix: Int      = 200  // Number of pixels along y-axis
  var rotate: Double  = 0.0  // Rotation angle (clockwise positive) in degrees
  var equinox: Int   = 2000  // Equinox of coordinates, 1950 and 2000 supported
  var epoch: Double   = 0.0  // Epoch of coordinates, used for FK4/FK5 conversion no effect if 0
  var proj: String  = "-SIN" // Projection 

  var wcst = wcsTransform

  var localScale = 1.0
  var annotate = true

  var preset: Option[Preset] = None
  def presetId:Int = if(preset.isDefined)preset.get.presetId else planner.NO_PRESET
  def mountPos():WorldCoords = if(preset.isDefined)preset.get.mountPos else new WorldCoords
  def seqsSx():List[InstSequence] = if(preset.isDefined)preset.get.seqsSx else Nil
  def seqsDx():List[InstSequence] = if(preset.isDefined)preset.get.seqsDx else Nil

  /** this gets used by Diva processes to orient themselves in our coordinate system */
  val icc = new ImageCoordinateConverter(this)

  /** background text rarely changes, keep on separate layer */
  var plotAnnotations = new PlotAnnotations(100, 100, mountPos)

  lazy val measureBand = new MeasureBand(this) 

  /** create a new transform based on our state */
  def wcsTransform(): WCSTransform = 
    new WCSTransform(cra,cdec,xsecpix,ysecpix,xrpix,yrpix,nxpix,nypix,rotate,equinox,epoch,proj)

  override def getWCS(): WorldCoordinateConverter = wcsTransform

  /** we will can support a WCS only after we have been drawn at least once */
  override def isWCS(): Boolean = true

  /** Set the sequences that will be plotted.  */
  def setPreset(p: Preset): Unit = {
    preset = Some(p)
    repaint()
  }

  // If this dead preset happens to be in use on the Plot, free its resources */
  def notifyOfDeath(dead: Preset): Unit = preset.foreach(p => if (p == dead) {
    preset = None
    repaint()
  })

  var dragAnchor = new Point2D.Double
  var dragOffset = new Point2D.Double

  def dragStart(e: MouseEvent): Unit = {
    dragAnchor.setLocation(e.getX, e.getY)
  }

  def drag(e: MouseEvent): Unit = {
    val xPix = e.getX
    val yPix = e.getY
    val dxPix = xPix - dragAnchor.x
    val dyPix = yPix - dragAnchor.y
    
    dragOffset.setLocation(dragOffset.x + dxPix, dragOffset.y - dyPix)
    dragAnchor.setLocation(xPix, yPix)
  }


  override def processMouseEvent(e: MouseEvent){
    super.processMouseEvent(e)
    if(e.getID == MouseEvent.MOUSE_PRESSED) dragStart(e)
    repaint()
  }

  override def processMouseMotionEvent(e: MouseEvent){
    super.processMouseMotionEvent(e)
    if((e.getModifiersEx & BUTTON1_DOWN_MASK) == BUTTON1_DOWN_MASK) drag(e)
    val xPix = e.getX
    val yPix = getHeight - e.getY
    val radec = wcst.pix2wcs(xPix, yPix)
    if(radec != null) {
      planner.plotFrame.reportCursorPosition(new WorldCoords(radec.x, radec.y))
    }
    repaint()
  }

  /** Scales the display */
  def mouseWheelMoved (e: MouseWheelEvent): Unit = {
    val scaleStep = 1 - e.getWheelRotation * -0.1
    localScale *= scaleStep
    repaint()
  }

  override def getCoordinateConverter(): CoordinateConverter = icc

  def enableInfo(state: Boolean): Unit = {
    annotate = state
    repaint()
  }

  /** clears the plot points, sets the translation to 0.0 and the scale to 1.0 */
  def reset(): Unit = {
    localScale = 1.0
    dragOffset.setLocation(0.0, 0.0)
    repaint()
  }


  /** put all steps on the display */
  def find(): Unit = {
    reset // for now
  }

  // Without those events, we want the biggest circle we can get to represent our
  // 40" copointing limit.  
  // when setting the transform, we take into consideration the effects of local events.
  // these are user inputs that have translated or scaled the plot.
  private def computeWcsTransform(): Unit = {

    // pixels from the edge of the display
    val margin = 30

    // resize the transform for the display width
    nxpix = getWidth
    nypix = getHeight

    // place the mount position at the center of the display
    xrpix = dragOffset.x + getWidth / 2.0
    yrpix = dragOffset.y + getHeight / 2.0
    cra   = mountPos.getX
    cdec  = mountPos.getY

    // find the shortest display axis, it is the limiting factor in scaling
    val shortDisplayAxis = min(nxpix, nypix) - 2.0 * margin

    // scale the copointing radius into the shortest axis 
    val secpix = CopointMath.arcsecondsPerPixel(shortDisplayAxis.toInt)

    // For some reason this transform increases ra the wrong direction...
    xsecpix = -secpix * localScale
    ysecpix =  secpix * localScale

    wcst = wcsTransform
  }

  def drawAnnotations(g2d: Graphics2D): Unit = {
    if((plotAnnotations.getWidth != getWidth) || 
     (plotAnnotations.getHeight != getHeight)  ||
     (plotAnnotations.mountPos != mountPos)) {
       plotAnnotations = new PlotAnnotations(getWidth, getHeight, mountPos)
    }
    g2d.drawImage(plotAnnotations, 0, 0, getWidth, getHeight, null)
  }


  var copointOK: Boolean = true

  /** DivaGraphics override  */
  override def paintLayer(g2d: Graphics2D, region: Rectangle2D) = synchronized  {

    // resize layers if necessary
    // update the transform before we do anything else
    computeWcsTransform
    val trans = wcsTransform

    measureBand.setEnabled(true)
    
    // set the rendering hints now so the measuring tool doesn't draw like a 1979 video game
    g2d.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON)
    g2d.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON)
    super.paintLayer(g2d, region)

    // draw annotations if selected
    if(annotate) drawAnnotations(g2d)

    copointOK = true

    // draw the highest priority objects last so they are not overwritten
    drawMount(g2d)

    // draw sequences
    val seqs = seqsSx ++ seqsDx

    seqs.foreach(seq => drawSeq(g2d, seq))


    // report copoint error if anything isn't copointed 
    if(!copointOK) {
      val margin = 10 //pixels
      val oldFont = g2d.getFont
      val fontSize = 16 // points
      g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, fontSize))
      val text = "COPOINT"
      val bounds = g2d.getFontMetrics.getStringBounds(text, g2d)
      val x: Int = getWidth - bounds.getWidth.toInt - margin
      val y: Int = getHeight - margin

      // fill in a black background several extending several pixels around the text
      g2d.setColor(Color.black)
      g2d.fillRect(x-2, 
        y-bounds.getHeight.toInt+2, 
        bounds.getWidth.toInt+4, 
        bounds.getHeight.toInt +2)

      g2d.setColor(Color.red)
      g2d.drawString(text, x, y)
      g2d.setFont(oldFont)
    }
  }



  /** Return the display pixel coordinates for the world coordinates, or None if the
    WCSTransform can't figure it out.  Flip the Y axis because Java draws inverted.  */
  private def worldCoordsToPixels(wc: WorldCoords): Option[Point2D.Double] = {
    val pix = wcst.wcs2pix(wc.getX, wc.getY)
    if(pix != null) {
      val xPix = pix.x
      val yPix = getHeight - pix.y
      Some(new Point2D.Double(xPix, yPix))
    }
    else None
  }


  /** position the mount at the current translation */
  private def drawMount(g2d: Graphics2D): Unit = worldCoordsToPixels(mountPos).foreach (pix => {

    // draw mount center as a '+'
    val mountR = 15 
    val mx  = pix.x.toInt
    val mx0 = mx - mountR
    val mx1 = mx + mountR

    val my  = pix.y.toInt
    val my0 = my - mountR
    val my1 = my + mountR

    g2d.setColor(Color.cyan)
    g2d.setStroke(new BasicStroke(1))
    g2d.drawLine(mx, my0, mx, my1)
    g2d.drawLine(mx0, my, mx1, my)

    // draw copoint radius 
    val r = abs(CopointMath.COPOINT_RADIUS_ARCSEC / ysecpix)
    val x = pix.x - r
    val y = pix.y - r
    val width = r*2
    val height = r*2
    g2d.draw(new Area(new Ellipse2D.Double(x, y, width, height)))
  })

  // return true if the sequence target was within the copointing distance of the mount
  private def drawSeq(g2d: Graphics2D, seq: InstSequence): Unit = {
    val wc = CopointMath.targetWorldCoords(seq.target)
    println("Plot.drawSeq")
    println("  target = " + wc.toString)
    println("  steps = " + seq.steps.length)
    seq.isCopointed = CopointMath.isCopointed(mountPos, wc)

    if(!seq.isCopointed)copointOK = false;

    if(seq.isCopointed) g2d.setColor(Color.yellow) else g2d.setColor(Color.red)
    worldCoordsToPixels(wc).foreach (pix => TpeTargetPosFeature.drawTargetCrosshair(g2d, pix))

    // draw the exposure steps as offsets for this sequence
    drawSeqSteps(g2d, mountPos, seq.target, seq.posAngleRadians, 0, 0, exposureSteps(seq.steps))
  }

  private def exposureSteps(steps: List[Step]): List[ExposureStep] = steps.flatMap {
    case es: ExposureStep => Some(es)
    case _ => None
  }

  /** draw the offsets.  Keep absorb base current 
  @param g2d Draw context
  @param posAngleRadians instrument rotation about focal plane
  @param xBase the absorbed pointing origin base X term
  @param yBase the absorbed pointing origin base Y term
  @param steps all of the steps for this sequence  */
  private def drawSeqSteps(
      g2d: Graphics2D, 
      mountPos: WorldCoords,
      seqTarget: SPTarget,
      posAngleRadians: Double, 
      baseX: Double, 
      baseY: Double, 
      steps: List[ExposureStep]): Unit = if(steps.nonEmpty) { 

    val step: ExposureStep = steps.head
    val xAxis = step.xAxis
    val yAxis = step.yAxis
    val src = new Point2D.Double(xAxis, yAxis)
    val dst = new Point2D.Double(xAxis, yAxis)
    if(step.isDetxy) {
      AffineTransform.getRotateInstance(posAngleRadians).transform(src, dst)
      dst.y *= -1.0
    }
    val x = baseX + dst.x
    val y = baseY + dst.y

    val distance = sqrt(x*x + y*y)

/*
    println("Plot.drawSeqSteps")
    println("  baseX  = " + baseX)
    println("  baseY  = " + baseY)
    println("  xAxis  = " + xAxis)
    println("  yAxis  = " + yAxis)
    println("  x      = " + x)
    println("  y      = " + y)
    println("  coords = " + step.coords)
    println("  absorb = " + step.absorb)
    println("  angle  = " + toDegrees(posAngleRadians))
    println("  dist   = " + distance)

*/

    val targetCoordinates = seqTarget.getSkycalcCoordinates
    val xDeg = targetCoordinates.getRaDeg
    val yDeg = targetCoordinates.getDecDeg

    val stepPos = new WorldCoords(xDeg, yDeg)
    
    worldCoordsToPixels(stepPos).foreach(pix => {
      val xPix = pix.x + x/xsecpix
      val yPix = pix.y - y/ysecpix

      val offsetPosPoint = wcst.pix2wcs(xPix, yPix)
      val offsetPos= new WorldCoords(offsetPosPoint.x, offsetPosPoint.y)
      step.setCopointed(CopointMath.isCopointed(mountPos, offsetPos))

      if(!step.isCopointed) copointOK = false
      
      // we now take the distance between the mount and the offset world coordinates,
      // this is our copointing distance

      drawPoint(g2d, new Point2D.Double(xPix, yPix), step.isCopointed)
    })


/*
    val mountRaDeg = mountPos.getRaDeg
    val mountDecDeg = mountPos.getDecDeg

    val targetCoordinates = seqTarget.getSkycalcCoordinates
    val targetRaDeg = targetCoordinates.getRaDeg
    val targetDecDeg = targetCoordinates.getRaDeg

    val dRaDeg = targetRaDeg - mountRaDeg
    val dDecDeg = targetDecDeg - mountDecDeg

    val stepRaDeg = x * CopointMath.DEGREES_PER_ARCSECOND
    val stepDecDeg = y * CopointMath.DEGREES_PER_ARCSECOND

    val dRaDeg = (targetRaDeg + stepRaDeg)-mountRaDeg
    val dDecDeg = (targetDecDeg + stepDecDeg)-mountDecDeg

    

    worldCoordsToPixels(mountPos).foreach(pix => {
      val xPix = pix.x + dRaDeg/CopointMath.DEGREES_PER_ARCSECOND/xsecpix
      val yPix = pix.y + dRaDeg/CopointMath.DEGREES_PER_ARCSECOND/ysecpix
      drawPoint(g2d, new Point2D.Double(xPix, yPix), step.isCopointed)
    })


    val targetCoordinates = seqTarget.getSkycalcCoordinates
    val xDeg = targetCoordinates.getRaDeg - (x * CopointMath.DEGREES_PER_ARCSECOND)
    val yDeg = targetCoordinates.getDecDeg - (y * CopointMath.DEGREES_PER_ARCSECOND)

    val stepPos = new WorldCoords(xDeg, yDeg)

//    step.isCopointed = CopointMath.isCopointed(mountPos, stepPos)

    worldCoordsToPixels(stepPos).foreach (pix => {
      val xPix = pix.x + x/xsecpix
      val yPix = pix.y - y/ysecpix
      //drawPoint(g2d, new Point2D.Double(xPix, yPix), step.isCopointed)
      drawPoint(g2d, pix, step.isCopointed)
    })
*/

    if(step.absorb) drawSeqSteps(g2d, mountPos, seqTarget, posAngleRadians, x, y, steps.tail)
    else  drawSeqSteps(g2d, mountPos, seqTarget, posAngleRadians, baseX, baseY, steps.tail)
  }

  private def drawPoint(g2d: Graphics2D, pixelPos: Point2D.Double, isCopointed: Boolean): Unit = {
    val r = 3
    val x: Int = pixelPos.x.toInt - r
    val y: Int = pixelPos.y.toInt - r
    val width: Int = r * 2
    val height: Int = r * 2
    if(isCopointed) g2d.setColor(Color.green) else g2d.setColor(Color.red)
    g2d.drawOval(x, y, width, height)
  }

  setBackground(Color.black)

  addMouseWheelListener(this)
}


