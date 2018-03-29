package jsky.app.ot.viewer.planner

import java.awt.{BorderLayout, Color, Font, GridBagLayout}
import javax.swing.{JLabel, JPanel}
import javax.swing.border.LineBorder

class PresetCopointStatus extends JLabel {

  def apply(p: Preset): Unit = {

    val bad = p.seqs.filter(seq => !seq.isCopointed)

    setVisible(!bad.isEmpty)
  }

  setForeground(Color.red)
  setInheritsPopupMenu(true)
  setFont(new Font(getFont().getName(), Font.BOLD, 11))
  setText("COPOINT")
}

