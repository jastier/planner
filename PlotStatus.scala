package jsky.app.ot.viewer.planner

import java.awt.{BorderLayout, Dimension, GridBagLayout}
import javax.swing.{BorderFactory, JLabel, JPanel}
import jsky.coords.WorldCoords


/** This is the small panel below the plot display that shows the cursor position */
class PlotStatus extends JPanel {

  val labelCursorWc = new JLabel
  val labelPresetId = new JLabel

  /** Post the mouse cursor position in WCS coordinates */
  def reportCursorPosition(wc: WorldCoords): Unit = {
    labelCursorWc.setText(wc.toString)
  }

  def setPresetId(id: Int): Unit = {
    if(id == Planner.NO_PRESET) labelPresetId.setText("")
    else labelPresetId.setText("Sync. Preset " + id)
  }

  setLayout(new BorderLayout)

  add(new JPanel {
    val WIDTH = 120  // pixels
    val HEIGHT = 28  // pixels
    val dim = new Dimension(WIDTH, HEIGHT)
    override def getPreferredSize(): Dimension = dim
    override def getMinimumSize(): Dimension = dim

    setLayout(new GridBagLayout)
    setBorder(BorderFactory.createLoweredBevelBorder)
    add(labelPresetId)
  }, BorderLayout.WEST)

  add(new JPanel {
    setLayout(new GridBagLayout)
    setBorder(BorderFactory.createLoweredBevelBorder)
    add(labelCursorWc)
  }, BorderLayout.CENTER)
}

