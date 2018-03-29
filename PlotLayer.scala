package jsky.app.ot.viewer.planner

import java.awt.{Color, Font, FontMetrics, Graphics, Graphics2D}
import jsky.coords.{WorldCoords}
import java.awt.image.BufferedImage

// Draw text on the Planner Plot object
class PlotLayer(plot: Plot, width: Int, height: Int) 
extends BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB) {

  val g2d: Graphics2D = createGraphics

  def draw(): Unit = {

    g2d.setColor(Color.magenta)
    var x0 = 0
    var y0 = 0
    var x1 = getWidth
    var y1 = getHeight

    g2d.drawLine(x0,y0,x1,y1)
    g2d.drawLine(x0,y1,x1,y0)
  } 
}

