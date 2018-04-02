package jsky.app.ot.viewer.planner

import java.awt.{Color, Font, FontMetrics, Graphics2D, RenderingHints}
import jsky.coords.{WorldCoords}
import java.awt.image.BufferedImage
import java.awt.RenderingHints._

// Draw non-moving text on the Planner Plot object
class PlotAnnotations(
    val width: Int, 
    val height: Int, 
    val mountPos: WorldCoords) 
  extends BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB) {

  val g2d = createGraphics

  val margin = 10 // pixels
  val fontSize = 10 // points

  g2d.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON)
  g2d.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON)
  g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fontSize))
  g2d.setColor(Color.cyan)

  drawRaAxisLabel
  drawDecAxisLabel
  drawMountLabel

  // the positive RA axis label on the middle left side of the canvas
  def drawRaAxisLabel(): Unit = {
    val text = "+RA"
    val bounds = g2d.getFontMetrics.getStringBounds(text, g2d)
    val x: Int = 2
    val y: Int = (height + bounds.getHeight.toInt)/2
    g2d.drawString(text, x, y)
  }

  // the positive Dec axis label on the middle left side of the canvas
  def drawDecAxisLabel(): Unit = {
    val text = "+Dec"
    val bounds = g2d.getFontMetrics.getStringBounds(text, g2d)
    val x: Int = (width - bounds.getWidth.toInt)/2
    val y: Int = bounds.getHeight.toInt
    g2d.drawString(text, x, y)
  }

  // the mount label
  def drawMountLabel(): Unit =  {
    val fontSize = 14 // points
    g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fontSize))
    val x: Int = margin
    val y: Int = height - margin
    g2d.setColor(Color.cyan)
    g2d.drawString("Mount: " + mountPos.toString, x, y)
  }
}

