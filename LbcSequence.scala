
    setComponentVisible(panelSequence, state)
    checkBoxSteps.setSelected(state)
package jsky.app.ot.viewer.planner

import edu.gemini.pot.sp.{ISPObservation, SPComponentType}
import edu.gemini.pot.sp.SPComponentType._
import edu.gemini.spModel.config2.{Config, ConfigSequence}
import edu.gemini.spModel.config.ConfigBridge
import edu.gemini.spModel.config.map.ConfigValMapInstances
import edu.gemini.spModel.gemini.lbc.Lbc
import edu.gemini.spModel.target.SPTarget
import java.awt.geom.Point2D
import java.util.HashMap
import scala.math._


// a single observation block of the LBC instrument
case class LbcSequence(node: ISPObservation) extends InstSequence(node) {

  def tReadout(s: LbcStep): Int = s.exposureTime

  def tMechanism(s0: LbcStep, s1: LbcStep): Int = List (
    (if(s1.camera  != s0.camera)  15 else 0),
    (if(s1.filter  != s0.filter)  15 else 0)).max

  // First step in the sequence.  Ignore mechanism overheads
  def firstStep(s: LbcStep): LbcStep = {
    val time = tReadout(s) + tOffset(s)
    new LbcStep(s.node, time, stepText(s), s.config, posAngleRadians)
  }

  def stepText(s: LbcStep): String = {
    "Text!"
  }

  def walk(s0: LbcStep, tail: List[LbcStep], res: List[LbcStep]): List[LbcStep] = 
    if(tail.nonEmpty) {
      val s1 = tail.head
      val time = tReadout(s1) + max(tMechanism(s0, s1), tOffset(s0, s1))
      val newStep = new LbcStep(s1.node, time, s1.text, s1.config, posAngleRadians)
      walk(s1, tail.tail, res.:+(newStep))
    } else res

  def timeSteps(steps: List[LbcStep]): List[LbcStep] = if(steps.nonEmpty) 
    walk(steps.head, steps.tail, List(firstStep(steps.head)))
    else List.empty

  def preset(target: SPTarget): Step = new PresetStep("Preset (Imaging)", 180, target)

  override def steps(): List[Step] = { 
    timeSteps(ConfigUtil.configs(node).map(c => LbcStep(node, 1, "", c, posAngleRadians)))
    if(steps.nonEmpty) steps.+:(preset(target)) else steps
  }
}


