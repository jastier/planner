package jsky.app.ot.viewer.planner

import java.awt.{BorderLayout, Color, Font, GridBagLayout}
import javax.swing.{JLabel, JPanel}
import javax.swing.border.LineBorder

class PresetTimeStatus extends JPanel {

  val label = new JLabel("Status not set")

  def apply(tStart: Int, tSx: Int, tDx: Int): Unit = {
    val tPreset = List(tSx, tDx).max
    val tUnusedSx = tPreset-tSx
    val tUnusedDx = tPreset-tDx

    if(tUnusedSx > 0) {
      setBackground(Color.orange)
      label.setText(Sx.title + " short " + tUnusedSx + " s")
    } else if(tUnusedDx > 0) {
      setBackground(Color.orange)
      label.setText(Dx.title + " short " + tUnusedDx + " s")
    } else {
      setBackground(Color.green)
      label.setText("OK")
    }
  }


  setInheritsPopupMenu(true)
  setBorder(new LineBorder(Color.black, 1))
  setLayout(new GridBagLayout)
  add(label)
}

