package jsky.app.ot.viewer.planner

import java.awt.{BorderLayout, Color, GridBagConstraints, GridBagLayout, GridLayout, Insets}
import javax.swing.{BorderFactory, BoxLayout, JButton, JCheckBox, JLabel, JPanel}

import java.awt.event.{ActionEvent, ActionListener}
import jsky.coords.WorldCoords


/** the column of controls to the left of the Plot display */
class PlotControl(planner: Planner, pf: PlotFrame) extends JPanel {

  /** Scale and translate the Plot so all offsets are visible  */
  val buttonFind = new JButton("FIND") {
    setToolTipText("Place all offsets on the display")
    addActionListener(new ActionListener {
      override def actionPerformed(e: ActionEvent) = pf.plot.find
    })
  }

  /** Center the mount and set the scale to 1 */
  val buttonReset = new JButton("RESET") {
    setToolTipText("Center the mount and set the scale to 1.0")
    addActionListener(new ActionListener {
      override def actionPerformed(e: ActionEvent) = pf.plot.reset
    })
  }

  /** Configures the plot to show or hide optional text details */
  val checkBoxInfo = new JCheckBox("INFO") {
    setToolTipText("Include annotations on the display")
    setSelected(true)
    addActionListener(new ActionListener {
      override def actionPerformed(e: ActionEvent) = pf.plot.enableInfo(isSelected)
    })
  }

  /** Configures the plot to show or hide optional text details */
  val buttonPrev = new JButton("PREV") {
    setToolTipText("Show the previous preset")
    addActionListener(new ActionListener {
      override def actionPerformed(e: ActionEvent) = planner.timeline.prevPreset(pf.plot.preset)
    })
  }

  /** Configures the plot to show or hide optional text details */
  val buttonNext = new JButton("NEXT") {
    setToolTipText("Show the next preset")
    addActionListener(new ActionListener {
      override def actionPerformed(e: ActionEvent) = planner.timeline.nextPreset(pf.plot.preset)
    })
  }


  setLayout(new GridBagLayout)

  add(new JPanel{
    setLayout(new GridLayout(7,1))
    add(buttonReset)
    add(buttonFind)
    add(new JPanel)
    add(buttonPrev)
    add(buttonNext)
    add(new JPanel)
    add(checkBoxInfo)
  }, new GridBagConstraints{
    gridx = 0
    insets = new Insets(10,10,10,10)
  })

}


