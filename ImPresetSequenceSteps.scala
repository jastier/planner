package jsky.app.ot.viewer.planner

import edu.gemini.pot.sp.ISPObservation
import java.awt.{Color, Dimension, GridBagConstraints, GridBagLayout, GridLayout, Insets}
import java.awt.event.MouseEvent
import java.awt.GridBagConstraints._
import javax.swing.border.LineBorder
import javax.swing.{BoxLayout, JLabel, JPanel}
import scala.math._


/** Vertical column of step panels for one side of the telescope
    @param scale Used to size steps on the graphical timeline
    @param time the time needed for the steps in this sequence
    @param unusedTime the time needed by the preset that is not used by this sequence
    @param seqs All the sequences on this preset
*/
class ImPresetSequenceSteps(scale: Double, unusedTime: Int, seqs: List[Sequence]) extends JPanel{

  val steps = seqs.map(seq => seq.steps).flatten

  // use the same popup menu as the preset
  setInheritsPopupMenu(true)

  setLayout(new GridBagLayout)

  // if this side of the telescope has any steps, plan them
  if(!steps.isEmpty) {
    add(new JPanel {
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
      steps.foreach(step => add(new StepPanel(step, scale)))
    }, new GridBagConstraints {
      gridx = 0
      weightx = 1.0
      fill = HORIZONTAL
    })
  }

  // if the sum of the times of the planned steps is less than that of the other side of the
  // telescope, fill in the remaining space with a warning showing the missing time.
  if(unusedTime > 0) add(new JPanel{
    setBorder(new LineBorder(Color.black, 1))
    setBackground(Color.yellow)
    setLayout(new GridBagLayout)
    add(new JLabel("Unused time = " + unusedTime + " s"))
  }, new GridBagConstraints {
    gridx = 0
    weightx = 1.0
    weighty = 1.0
    fill = BOTH
  })
}


/**
 * A graphical representation of a Step object.   The width is derived from the width of the
 * Planner window and is somewhat arbitrary.  The height is the time needed to complete the Step,
 * multiplied by a scaling factor.  All steps in a binocular plan are drawn to the same scale with
 * respect to time.
 */
class StepPanel(step: Step, scale: Double) extends MousePanel {
  override def getPreferredSize = new Dimension(-1, rint(step.seconds * scale).toInt)

  override def getMinimumSize = new Dimension(-1, rint(step.seconds * scale).toInt)

//  override def mouseEntered (e: MouseEvent): Unit = sp.plotActive(List(step))

//  override def mouseExited (e: MouseEvent): Unit = sp.plotActive(List.empty)

//  override def mouseClicked (e: MouseEvent): Unit = step.mouseClicked

  // use the same popup menu as the preset
  setInheritsPopupMenu(true)

  setBorder(step.border)
  setBackground(step.color)
  setLayout(new GridBagLayout)
  setToolTipText(step.toolTipText)
  add(new JLabel(step.text),new GridBagConstraints {
    weighty = 1.0
    weightx = 1.0
    fill = BOTH
    insets = new Insets(0, 4, 0, 4)
  })
}
