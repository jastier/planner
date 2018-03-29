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

import jsky.app.ot.tpe.TpeContext
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
class Plot(planner: Planner) extends DivaGraphicsImageDisplay with MouseWheelListener with MouseMotionListener{

  // keep some extra layers around just in case
  var plotLayer: PlotLayer = new PlotLayer(this, 10, 10)

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
  var writer = new PlotWriter(this)

  var dragStart = new Point2D.Double
  var offset = new Point2D.Double
  var localScale = 1.0
  var annotate = false

  var preset: Option[Preset] = None
  def presetId:Int = if(preset.isDefined)preset.get.presetId else planner.NO_PRESET
  def mountPos():WorldCoords = if(preset.isDefined)preset.get.mountPos else new WorldCoords
  def seqsSx():List[InstSequence] = if(preset.isDefined)preset.get.seqsSx else Nil
  def seqsDx():List[InstSequence] = if(preset.isDefined)preset.get.seqsDx else Nil

  /** this gets used by Diva processes to orient themselves in our coordinate system */
  val icc = new ImageCoordinateConverter(this)

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

  override def processMouseEvent(e: MouseEvent){
    if(e.getID == MouseEvent.MOUSE_PRESSED) dragStart.setLocation(e.getX, e.getY)

    super.processMouseEvent(e)
  }

  /** Translates the display */
  override def mouseDragged (e: MouseEvent): Unit = {
    if((e.getModifiersEx & BUTTON1_DOWN_MASK) == BUTTON1_DOWN_MASK) {
      val xPix = e.getX
      val yPix = e.getY
      offset.setLocation(offset.x +(xPix - dragStart.x), offset.y - (yPix - dragStart.y))
      dragStart.setLocation(xPix, yPix)
      repaint()
    }
  }

  override def mouseMoved (e: MouseEvent): Unit = {
    super.processMouseEvent(e)  // for lack of anything better to do
  }

  override def processMouseMotionEvent(e: MouseEvent){
    if(e.getID == MouseEvent.MOUSE_DRAGGED)mouseDragged(e)
    val xPix = e.getX
    val yPix = getHeight - e.getY
    val radec = wcst.pix2wcs(xPix, yPix)
    if(radec != null) {
      planner.plotFrame.reportCursorPosition(new WorldCoords(radec.x, radec.y))
    }
    super.processMouseMotionEvent(e)
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
    offset.setLocation(0.0, 0.0)
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
    xrpix = offset.x + getWidth / 2.0
    yrpix = offset.y + getHeight / 2.0
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

  def checkLayers(): Unit = {
    println("plot.checkLayers")
    println("  width  = " + getWidth)
    println("  height = " + getHeight)
    val w = getWidth
    val h = getHeight
    println("  plot width  = " + w)
    println("  plot height = " + h)
    println("  checking plot layer...")
    println("    plotLayer width  = " + plotLayer.getWidth)
    println("    plotLayer height = " + plotLayer.getHeight)
    if((plotLayer.getWidth == w) && (plotLayer.getHeight == h)) {
      println("    Plot layer OK")
    } else {
      println("    Plot layer needs updated")
      plotLayer = new PlotLayer(this, w, h)
    } 
  }

  /** DivaGraphics override  */
  override def paintLayer(g2d: Graphics2D, region: Rectangle2D) = synchronized  {

    // resize layers if necessary
    checkLayers()
    plotLayer.draw

    // update the transform before we do anything else
    computeWcsTransform
    val trans = wcsTransform

    measureBand.setEnabled(true)
    
    // set the rendering hints now so the measuring tool doesn't draw like a 1979 video game
    g2d.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON)
    g2d.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON)
    super.paintLayer(g2d, region)

    // draw the highest priority objects last so they are not overwritten
    drawMount(g2d)

    // draw sequences
    val seqs = seqsSx ++ seqsDx
    seqs.foreach(seq => drawSeq(g2d, seq))

    // draw annotations if selected
    if(annotate) writer.drawAnnotations(g2d, mountPos)

    // report errors if present
    if((seqs.filter(seq => !seq.isCopointed)).nonEmpty) writer.copointAdvisory(g2d)
  
    // The draw (at least over a VPN connection at home) was much faster without the
    // buffered image copy.

    // draw layers  (keep in mind these do NOT have to be full size images)
    g2d.drawImage(plotLayer, offset.x.toInt, -offset.y.toInt, plotLayer.getWidth, plotLayer.getHeight, null)
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
    val ctx = TpeContext(seq.observation)
    val pad = ctx.instrument.get.getPosAngleDegrees
    val par = toRadians(pad)

    val wc = CopointMath.sequencePosWorldCoords(seq.target)
    seq.isCopointed = CopointMath.isCopointed(mountPos, wc)
    if(seq.isCopointed) g2d.setColor(Color.yellow) else g2d.setColor(Color.red)
    worldCoordsToPixels(wc).foreach (pix => TpeTargetPosFeature.drawTargetCrosshair(g2d, pix))

    val offsetCopoint = allOffsets(ctx).map(opb => drawOffset(g2d, opb, seq.target, par))

    val badOffsets = offsetCopoint.filter(o => (o == false))

    if(badOffsets.nonEmpty) seq.isCopointed = false
  }

  def allOffsets(ctx: TpeContext): List[OffsetPosBase] = {
    val allSolc = ctx.offsets.allJava.toList
    val allPos = allSolc.map(solc => solc.posList).flatten
    OffsetPointingOrigin.updateOffsetBases(allPos)
    allPos
  }

  private def drawOffset(
      g2d: Graphics2D,
      opb: OffsetPosBase,
      target: SPTarget,
      par: Double): Boolean = {
    val wc = CopointMath.offsetPosWorldCoords(target, opb, par)
    opb.setIsCopointed(CopointMath.isCopointed(mountPos, wc))
    
    worldCoordsToPixels(wc).foreach (pix => drawPoint(g2d, pix, opb.getIsCopointed))

    opb.getIsCopointed
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


