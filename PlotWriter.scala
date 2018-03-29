package jsky.app.ot.viewer.planner

import java.awt.{Color, Font, FontMetrics, Graphics2D}
import jsky.coords.{WorldCoords}
import java.awt.image.BufferedImage

// Draw text on the Planner Plot object
class PlotWriter(plot: Plot) {

  val margin = 10 // pixels

  def drawAnnotations(g2d: Graphics2D, mountPos: WorldCoords): Unit = {
//    drawDirectionLabel(g2d)
    drawRaAxisLabel(g2d)
    drawDecAxisLabel(g2d)
    drawMountLabel(g2d, mountPos)
  }

  def drawDirectionLabel(g2d: Graphics2D): Unit = {
    val oldFont = g2d.getFont
    val fontSize = 14 // points

    g2d.setFont(new Font(plot.getFont().getName(), Font.PLAIN, fontSize))

    val text = "NE"
    val bounds = g2d.getFontMetrics.getStringBounds(text, g2d)
    val width: Int = bounds.getWidth.toInt
    val height: Int =  bounds.getHeight.toInt
    val x: Int = 2
    val y: Int = height
    g2d.setColor(Color.cyan)
    g2d.drawString(text, x, y)
    g2d.setFont(oldFont)
  }

  def drawRaAxisLabel(g2d: Graphics2D): Unit = {
    val oldFont = g2d.getFont
    val fontSize = 10 // points

    g2d.setFont(new Font(plot.getFont().getName(), Font.PLAIN, fontSize))

    val text = "+RA"
    val bounds = g2d.getFontMetrics.getStringBounds(text, g2d)
    val width: Int = bounds.getWidth.toInt
    val height: Int =  bounds.getHeight.toInt
    val x: Int = 2
    val y: Int = (plot.getHeight + height)/2
    g2d.setColor(Color.cyan)
    g2d.drawString(text, x, y)
    g2d.setFont(oldFont)
  }


  def drawDecAxisLabel(g2d: Graphics2D): Unit = {
    val oldFont = g2d.getFont
    val fontSize = 10 // points

    g2d.setFont(new Font(plot.getFont().getName(), Font.PLAIN, fontSize))

    val text = "+Dec"
    val bounds = g2d.getFontMetrics.getStringBounds(text, g2d)
    val width: Int = bounds.getWidth.toInt
    val height: Int =  bounds.getHeight.toInt
    val x: Int = (plot.getWidth - width)/2
    val y: Int = height
    g2d.setColor(Color.cyan)
    g2d.drawString(text, x, y)
    g2d.setFont(oldFont)
  }


  /** Lower left corner:  Mount position  */
  def drawMountLabel(g2d: Graphics2D, mountPos: WorldCoords): Unit =  {
    val oldFont = g2d.getFont
    val fontSize = 14 // points

    g2d.setFont(new Font(plot.getFont().getName(), Font.PLAIN, fontSize))

//    val text = "Mount: " + mountPos.toString
    val text = mountPos.toString
    val bounds = g2d.getFontMetrics.getStringBounds(text, g2d)
    val width: Int = bounds.getWidth.toInt
    val height: Int =  bounds.getHeight.toInt
    val x: Int = margin
    val y: Int = plot.getHeight - margin

    g2d.setColor(Color.cyan)
    g2d.drawString(text, x, y)
    g2d.setFont(oldFont)
  }

  /** Lower Right corner:  Errors  */
  def copointAdvisory(g2d: Graphics2D): Unit = {
    val oldFont = g2d.getFont
    val fontSize = 16 // points

    g2d.setFont(new Font(plot.getFont().getName(), Font.BOLD, fontSize))

    val text = "COPOINT"
    val bounds = g2d.getFontMetrics.getStringBounds(text, g2d)
    val width: Int = bounds.getWidth.toInt
    val height: Int =  bounds.getHeight.toInt
    val x: Int = plot.getWidth - width - margin
    val y: Int = plot.getHeight - margin
    
    g2d.setColor(Color.red)
    g2d.drawString(text, x, y)
    g2d.setFont(oldFont)
  }
}


