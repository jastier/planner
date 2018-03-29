package jsky.app.ot.viewer.planner

import javax.swing.ScrollPaneConstants
import java.awt.{BorderLayout, Color,Component, Dimension, GridBagConstraints, GridBagLayout}
import java.awt.GridBagConstraints._
import javax.swing.{BoxLayout, JPanel, JMenuItem, JPopupMenu, JScrollPane}
import scala.annotation.tailrec
import scala.math._
import edu.gemini.pot.sp.ISPObservation



/** display a column of selected presets for this tree */
class Timeline(val planner: Planner) extends JPanel {

  // never ending clicker
  private var presetId: Int = 0

  def newPresetId(): Int = {
    presetId += 1
    presetId
  }

  /** Removes preset panel from the timeline.  The plan must be recomputed */
  final def remove(p: Preset): Unit = {
    // inform the Plot
    planner.plotFrame.notifyOfDeath(p)
    // recover keys
    planner.selectors.addKeys(p.keys)
    super.remove(p)
    plan
    revalidate
    repaint()
  }

  /** returns the preset in the list at the direction relative to the passed-in preset */
  def navigate(p: Preset, direction: Int): Unit = {
    val list: List[Preset] = presets
    if(list.size > 1) {  // a list with less than 2 elements has no room in which to move.
      val idx = list.indexOf(p) + direction
      val idxHead = 0
      val idxTail = list.size-1
      if(idx < idxHead) plot(list(idxTail))
      else if(idx > idxTail) plot(list(idxHead))
      else plot(list(idx))
    }
  }

  /** Returns the preset before this one, using the presets as a circular list */
  def prevPreset(o: Option[Preset]): Unit = o.foreach(preset => navigate(preset, -1))

  /** Returns the preset after this one, using the presets as a circular list */
  def nextPreset(o: Option[Preset]): Unit = o.foreach(preset => navigate(preset, 1))

  /** Adds a preset to the end of this timeline */
  final def addPreset(): Unit = {
    add(new Preset(this, newPresetId))
    plan
  }

  /** Adds a preset above the argument preset */
  final def addPresetAbove(pp: Preset): Unit = {
    add(new Preset(this, newPresetId), presets.indexOf(pp))
    plan
  }

  /** Adds a preset below the argument preset */
  final def addPresetBelow(pp: Preset): Unit = {
    add(new Preset(this, newPresetId), presets.indexOf(pp) + 1)
    plan
  }

  /** display this preset on the plot frame */
  def plot(p: Preset): Unit = planner.plotFrame.setPreset(p)

  /** update each preset with the latest Key definition */
  def update(keys: List[Key]): Unit = presets.foreach(p => p.update(keys))

  /** Returns a list of all the keys on the Timeline */
  def keys(): List[Key] = presets.map(p => p.keys).toList.flatten

  /** Returns a set of all the Sequences on the Timeline */
  def seqs(): List[Sequence] = presets.map(p => p.seqs).toList.flatten

  /** Returns a list of all the Steps on the Timeline */
  def steps(): List[Step] = seqs.map(seq => seq.steps).toList.flatten

  /** Returns a type-safe Array of all the Presets on the Timeline */
  def presets(): List[Preset] = getComponents.flatMap {
    case pp: Preset => Some(pp)
    case _ => None
  }.toList


  /** Plan the timeline using the the list of presets and a scaling factor */
  def plan(): Int = {
    planner.plotFrame.repaint()
    TimelinePlanner.plan(presets, planner.userScale)
  }

  /** Set the visibility of all the header panels in the Timeline
   @param state the visibility setting */
  def setHeadersVisible(state: Boolean): Unit = {
    presets.foreach(p => p.setHeaderVisible(state))
    revalidate
    repaint()
  }

  /** Set the visibility of all the sequence panels in the Timeline
   @param state the visibility setting */
  def setSequencesVisible(state: Boolean): Unit = {
    presets.foreach(p => p.setSequenceVisible(state))
    revalidate
    repaint()
  }

  // The timeline is displayed as a single column of Presets
  setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
}


class TimelinePanel(val timeline: Timeline) extends JPanel {
  setLayout(new BorderLayout)
  add(timeline, BorderLayout.NORTH)
  add(new PopupMenuPanel{
    addMenuItem("Add Preset", "Add a new Preset to this Plan", timeline.addPreset)
  }, BorderLayout.CENTER)
}


object TimelinePlanner {

  /** Compute a plan for this preset list */
  @tailrec
  final def plan(scale: Double, t0: Int, list: List[Preset]): Int = 
  if(list.isEmpty) t0 
  else plan(scale, t0 + list.head.plan(scale, t0), list.tail)

  /** Compute a plan for this preset list
  @param presets a list of all the presets on the timeline
  @param userScale A scale factor selected by the user from the main menu 
         "scale" submenu group of radio check button menu items.  */
  def plan(presets: List[Preset], userScale: Double): Int = {

    /** This is the value by which all steps in the timeline are automatically
        scaled such that the smallest step is visible, typically 30 pixels high  */
    val dataScale = DataScale(30, presets)

    /** The scale factor used to compute the height in pixels of each step in the
        Timeline is the product of the data scale and the user scale chosen.  */
    val scale = dataScale * userScale

    /** HiiiYA!  */
    plan(scale, 0, presets)
  }
}

