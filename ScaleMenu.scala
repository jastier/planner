package jsky.app.ot.viewer.planner

import java.awt.event.{ActionListener, ActionEvent}
import javax.swing.{ButtonGroup, JMenu, JRadioButtonMenuItem}

/** The top menu bar for the Binocular Planner frame  */
class ScaleMenu(p: Planner) extends JMenu("Scale") {

  /** an menu radio button that takes an action and can return a double */
  class scaleRadioMenuItem(c: Tuple3[String, Double, Boolean]) extends JRadioButtonMenuItem(c._1) {

    /** return the scale value if selected, else zero */
    def scale(): Double = if(isSelected) c._2 else 0
    
    setSelected(c._3)

    addActionListener(new ActionListener{
      override def actionPerformed(e: ActionEvent): Unit = if(isSelected)p.timeline.plan
    })
  }

  /** some completely arbitrarily defined scale factors.  Feel free to add more  */
  val choices = List[Tuple3[String, Double, Boolean]](
    ("100%", 1.0, true),
    ("75%", 0.75, false),
    ("50%", 0.5, false),
    ("25%", 0.25, false),
    ("10%", 0.1, false),
    ("5%", 0.05, false),
    ("1%", 0.01, false),
    ("0.5%", 0.005, false),
    ("0.1%", 0.001, false),
    ("0.05%", 0.0005, false),
    ("0.01%", 0.0001, false))

  /** The choices, mapped into unrelated radio buttons */
  val buttons = choices.map(c => new scaleRadioMenuItem(c))

  /** The buttons, grouped to work like radio buttons */
  val group = new ButtonGroup

  /** add each button to the menu and to the radio button group */
  buttons.foreach(b => {
    add(b)
    group.add(b)
  })

  /** the scale() method returns the scale if selected, otherwise zero.  Map to list and sum */
  def scale(): Double = buttons.map(b => b.scale).sum
}

