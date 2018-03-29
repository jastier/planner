package jsky.app.ot.viewer.planner

import edu.gemini.pot.sp.ISPObservation
import java.awt.{Color, Dimension, GridBagConstraints, GridBagLayout, Insets}
import java.awt.event.MouseEvent
import java.awt.GridBagConstraints._
import javax.swing.border.LineBorder
import javax.swing.{JLabel, JPanel}
import scala.math._


/** A SequenceInfoPanel provides notes the preset sequences for one side of the telescope
    @param id unique ID number of the parent preset
    @param scale Used to size steps on the graphical timeline
    @param tStart start time, seconds
    @param tPreset the time needed to complete this prese
*/
class ImPresetSequenceInfo(id: Int, scale: Double, tStart: Int, tPreset: Int, seqs: List[Sequence]) 
extends MousePanel {

  // TODO do these have to be vals?  Why not just inline them?
  val accumulatedTime = tStart + tPreset
  val titleLabel = new JLabel("Preset " + id)
  val timeLabel = new JLabel("Time = " + accumulatedTime + " s")

  override def mouseEntered (e: MouseEvent): Unit = {} //osp.plotActive(seq.steps)

  override def mouseExited (e: MouseEvent): Unit = {} //sp.plotActive(List.empty)

  override def mouseClicked (e: MouseEvent): Unit = {} //seq.mouseClicked

  setBorder(new LineBorder(Color.black, 1))
  setLayout(new GridBagLayout)

  // use the same popup menu as the preset
  setInheritsPopupMenu(true)

  add (new JPanel {
    setBackground(Color.white)
    setLayout(new GridBagLayout)
    add (titleLabel)
  }, new GridBagConstraints {
    weightx = 1.0
    weighty = 1.0
    fill = BOTH
  })

  add (new JPanel {
    setBackground(Color.white)
    setLayout(new GridBagLayout)
    add (timeLabel)
  }, new GridBagConstraints {
    gridx = 0
    weightx = 1.0
    fill = HORIZONTAL
  })
}

