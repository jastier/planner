package jsky.app.ot.viewer.planner

import edu.gemini.pot.sp.ISPObservation
import java.awt.{Color, Component, Dimension, Font, GridBagConstraints, GridBagLayout}
import java.awt.event.{ActionListener, ActionEvent, MouseEvent}
import java.awt.geom.Point2D
import java.awt.{BorderLayout, GridLayout, Insets}
import java.awt.GridBagConstraints._
import javax.swing.{BoxLayout, JButton, JCheckBox, JComponent, JLabel, JTextField}
import javax.swing.{JCheckBoxMenuItem, JPanel}
import scala.math._
import javax.swing.event.{PopupMenuEvent, ListDataListener, ListDataEvent}
import javax.swing.{Box, BorderFactory, JMenuItem}
import javax.swing.{DefaultListModel, TransferHandler, ListSelectionModel, DropMode}
import scala.collection.JavaConversions._
import javax.swing.border.LineBorder
import jsky.coords.WorldCoords


/** A UI representation of a Preset - a single movement of the telescope mount and then a
   concurrent series of SX and DX observations.   The UI is a title bar, a status panel with 
   observations and summary, and a sequence steps panel.   The status and steps panel visibilities
   are user controlled 

   The mount position is calculated as the midpoint between the first SX and first DX
   observation.   This mount position is the location of the 40" radius copointing limit.

   @param timeline Owner of this preset
   @param presetId Unique identification for this preset

*/   
class Preset (val timeline: Timeline, val presetId: Int) extends PopupMenuPanel {

  var mountPos: WorldCoords = new WorldCoords

  /** Show the mount position as it would be calculated by the TCS */
  val labelMountPos = new JLabel(mountPos.toString)
  val textFieldTitle = new JTextField("Synchronous Preset")

  val buttonPlot = new JButton("Plot") {
    addActionListener(new ActionListener {
      override def actionPerformed(e: ActionEvent): Unit = {
        timeline.planner.plotFrame.setVisible(true)
        plot
      }
    })
  }

  val checkBoxObs = new JCheckBox(presetId.toString){
    setToolTipText("Display the observations used in this preset")
    setSelected(true)
    addActionListener(new ActionListener {
      override def actionPerformed(e: ActionEvent): Unit = setHeaderVisible(isSelected())
    })
  }

  val checkBoxSteps = new JCheckBox("Steps"){
    setToolTipText("Display the sequence of steps used in this preset")
    setSelected(false)
    addActionListener(new ActionListener {
      override def actionPerformed(e: ActionEvent): Unit = setSequenceVisible(isSelected())
    })
  }

  val panelTimeStatus = new PresetTimeStatus
  val panelCopointStatus = new PresetCopointStatus
  val panelHeader = new PresetHeader(this)
  val panelSequence = new TransientComponentPanel


  def showTimelineOnly(state: Boolean): Unit = {
    if(state) {
      panelPreset.setVisible(false)
      panelHeader.setVisible(false)
      panelSequence.setVisible(true)
    }
    else {
      panelPreset.setVisible(true)
      panelHeader.setVisible(checkBoxObs.isSelected)
      panelSequence.setVisible(checkBoxSteps.isSelected)
    }

    revalidate
    repaint()
  }

  /** Returns the title for this Preset */
  def title(): String = textFieldTitle.getText

  /** Removes this preset from the parent timeline */
  def remove(): Unit = timeline.remove(this)

  /** Add a new preset above this preset on the parent timeline */
  def addPresetAbove(): Unit = timeline.addPresetAbove(this)

  /** Add a new preset below this preset on the parent timeline */
  def addPresetBelow(): Unit = timeline.addPresetBelow(this)

  /** Show this preset in the Plot frame */
  def plot(): Unit = timeline.plot(this)

  /** turn a component visibility on or off and update the UI
   @param c The component for which visibility is being set
   @param state The visibility setting */
  def setComponentVisible(c: JComponent, state: Boolean): Unit = {
    c.setVisible(state)
    if(state) {
      c.revalidate
      c.repaint()
    }
    revalidate
    repaint()
  }

  /** Set the visbility of the header panel 
   @param state The visiblity setting */
  def setHeaderVisible(state: Boolean): Unit = {
    setComponentVisible(panelHeader, state)
    checkBoxObs.setSelected(state)
  }

  /** Set the visbility of the Sequence panel 
   @param state The visiblity setting */
  def setSequenceVisible(state: Boolean): Unit = {
    setComponentVisible(panelSequence, state)
    checkBoxSteps.setSelected(state)
  }

  /** returns the SX keys used in this preset */
  def keys(): List[Key] = keysSx ++ keysDx

  /** returns the SX keys used in this preset */
  def keysSx(): List[Key] = panelHeader.keysSx

  /** returns the DX keys used in this preset */
  def keysDx(): List[Key] = panelHeader.keysDx

  /** Returns the Sequences for this preset */
  def seqs(): List[InstSequence] = seqsSx ++ seqsDx

  /** Returns the SX Sequences for this preset */
  def seqsSx(): List[InstSequence] = keysSx.map(key => key.seq)

  /** Returns the SX Sequences for this preset */
  def seqsDx(): List[InstSequence] = keysDx.map(key => key.seq)

  /** Synchronize the execution of the keys to begin at the start time
  @param scale The scale at which to display the preset steps on the sequence steps panel
  @param tStart The time at which this preset begins
  @returns the start time plus the time required to exeture this preset */
  // This routine pulls double duty in that it also sets the step display scale.  I'm
  // wondering if that should not be its own call to keep things simple.
  def plan(scale: Double, tStart: Int): Int = {

    mountPos = CopointMath.mountPosWorldCoords(seqsSx, seqsDx)

    labelMountPos.setText(mountPos.toString)

    // create the step plan
    setSequence(new ImPresetSequence(presetId, mountPos, scale, tStart, seqsSx, seqsDx))
  }


  /** a fully completed preset sequence panel is placed on the sequence panel */
  private def setSequence(ps: ImPresetSequence): Int = {

    panelSequence(ps)
    panelTimeStatus(ps.tStart, ps.tSx, ps.tDx)
    panelCopointStatus(this)
    panelHeader.setSequence(ps)
    panelSequence.setVisible(checkBoxSteps.isSelected)
    panelHeader.setVisible(checkBoxObs.isSelected)
    revalidate
    repaint()
    ps.tPreset
  }


  /** The available keys have changed.  Remove any keys that are not among the living.
    Once all presets are updated, the timeline must then be planned with the start times and
    execution times of each Preset */
  def update(living: List[Key]):Unit = panelHeader.updateKeys(living)

  setLayout(new GridBagLayout)
  setFocusable(true)
  setInheritsPopupMenu(true)

  setBorder(new LineBorder(Color.black, 3))

  // topmost controls
  val panelPreset = new JPanel {
    setInheritsPopupMenu(true)
    setLayout(new GridLayout(1,4))

    // first quarter
    add(new JPanel{ 
      setInheritsPopupMenu(true)
      setLayout(new BorderLayout(10,4))
      add(checkBoxObs, BorderLayout.WEST) 
      add(textFieldTitle,  BorderLayout.CENTER)
    })

    // second quarter
    add(new JPanel{
      setLayout(new GridBagLayout)
      add(labelMountPos)
    })

    // third quarter
    add(new JPanel{
    })

    // fourth quearter
    add(panelTimeStatus)
  }

  add(panelPreset, new GridBagConstraints{
    weightx = 1.0
    fill = HORIZONTAL
  })

      
  // optional visibility observation panel
  add(panelHeader, new GridBagConstraints{
    gridx = 0
    gridy = 1
    weightx = 1.0
    fill = HORIZONTAL
  })

  // optional visibility sequence steps panel
  add(panelSequence, new GridBagConstraints{
    gridx = 0
    gridy = 2
    weightx = 1.0
    weighty = 1.0
    insets = new Insets(6,6,6,6)
    fill = BOTH
  })

  // populate menu
  addMenuItem("Add 1 Above", "Add one preset above this preset", addPresetAbove)
  addMenuItem("Add 1 Below", "Add one preset below this preset", addPresetBelow)
  addSeparator
  // maybe guard this with a confirmation dialog?
  addMenuItem("Delete this Preset", "Remove this Preset from the timeline", remove)

  setBorder(BorderFactory.createLoweredBevelBorder)
}

