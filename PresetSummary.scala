package jsky.app.ot.viewer.planner

import java.awt.{BorderLayout, Color, GridLayout}
import javax.swing.{JLabel, JPanel, JTextField}


class PresetSummary extends JPanel {

  val textFieldStart = new JTextField
  val textFieldPreset = new JTextField
  val textFieldEnd = new JTextField

  val labelStart = new JLabel
  val labelPreset = new JLabel
  val labelEnd = new JLabel

  def apply(ps: ImPresetSequence): Unit = report(ps.tStart, ps.tPreset)

  def report(tStart: Int, tPreset: Int): Unit = {
    val tEnd = tStart + tPreset
    labelStart.setText(tStart + " s")
    labelPreset.setText(tPreset + " s")
    labelEnd.setText(tEnd + " s")
  }

  def paramPanel(text: String, label: JLabel): JPanel = new JPanel{
    setLayout(new GridLayout(1, 2, 8, 0))
    add(new JPanel{
      setLayout(new BorderLayout)
      add(new JPanel, BorderLayout.CENTER)
      add(new JLabel(text), BorderLayout.EAST)
    })
    add(new JPanel{
      setLayout(new BorderLayout)
      add(label, BorderLayout.WEST)
      add(new JPanel, BorderLayout.CENTER)
    })
  }

  setInheritsPopupMenu(true)

  setLayout(new GridLayout(3, 1))

  add(paramPanel("Start:", labelStart))
  add(paramPanel("Length:", labelPreset))
  add(paramPanel("End:", labelEnd))

  report(0, 0)
}

