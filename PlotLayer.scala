package jsky.app.ot.viewer.planner

import java.awt.{Color, Font, FontMetrics, Graphics, Graphics2D}
import jsky.coords.{WorldCoords}
import java.awt.image.BufferedImage

// Draw text on the Planner Plot object
class PlotLayer(plot: Plot, width: Int, height: Int) 
extends BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB) {
  val g2d = createGraphics
  g2d.setColor(Color.magenta)
  g2d.drawLine(0,0,getWidth,getHeight)
  g2d.drawLine(0,getHeight,getWidth,0)
}

