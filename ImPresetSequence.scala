package jsky.app.ot.viewer.planner

import java.awt.{Color, Component, Dimension, GridLayout}
import javax.swing.{JLabel, JPanel}
import scala.math._
import edu.gemini.pot.sp.ISPObservation
import javax.swing.border.LineBorder
import jsky.util.gui.NumberBoxWidget
import jsky.coords.WorldCoords


  /** Presets are planned using the index, scale, start time, and keys */
// Immutable
class ImPresetSequence(
  val id: Int,
  val targetPos: WorldCoords,
  val scale: Double,
  val tStart: Int,
  val seqsSx: List[InstSequence],
  val seqsDx: List[InstSequence]) extends JPanel {

  lazy val seqs = List(seqsSx, seqsDx).flatten
  lazy val isEmpty =seqs.isEmpty

  lazy val stepsSx: List[Step] = seqsSx.map(seq => seq.steps).flatten
  lazy val stepsDx: List[Step] = seqsDx.map(seq => seq.steps).flatten
  lazy val steps = List(stepsSx, stepsDx).flatten

  // filter exposure steps, these get plotted
  lazy val exposureStepsSx: List[ExposureStep] = stepsSx.flatMap {
    case es: ExposureStep => Some(es)
    case _ => None
  }
  lazy val exposureStepsDx: List[ExposureStep] = stepsDx.flatMap {
    case es: ExposureStep => Some(es)
    case _ => None
  }

  // Compute the time required to perform all of the sequence steps for each side.
  lazy val tSx: Int = stepsSx.map(step => step.seconds).sum
  lazy val tDx: Int = stepsDx.map(step => step.seconds).sum

  // The preset time is the greatest of the two sequence times
  lazy val tPreset: Int = max(tSx, tDx)

  // the unused time is the shortest sequence time subtracted from the longest sequence time
  lazy val tUnusedSx: Int = tPreset - tSx
  lazy val tUnusedDx: Int = tPreset - tDx

  // panel height is the the preset time multiplied by a scaling factor
  val dim = new Dimension (-1, rint(scale * tPreset).toInt)
  override def getMinimumSize(): Dimension = dim
  override def getPreferredSize(): Dimension = dim

  // four equal-width columns, one row
  setLayout(new GridLayout(1,4))

  // use the same popup menu as the preset
  setInheritsPopupMenu(true)

  // Display the information on panels
  add(new ImPresetSequenceInfo(id, scale, tStart, tPreset, seqsSx))
  add(new ImPresetSequenceSteps(scale, tUnusedSx, seqsSx))
  add(new ImPresetSequenceSteps(scale, tUnusedDx, seqsDx))
  add(new ImPresetSequenceInfo(id, scale, tStart, tPreset, seqsDx))
}

