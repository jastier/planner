package jsky.app.ot.viewer.planner

import edu.gemini.pot.sp.{ISPObservation, SPComponentType}
import edu.gemini.pot.sp.SPComponentType._
import edu.gemini.spModel.config2.{Config, ConfigSequence}
import edu.gemini.spModel.config.ConfigBridge
import edu.gemini.spModel.config.map.ConfigValMapInstances
import edu.gemini.spModel.gemini.mods.Mods
import edu.gemini.spModel.target.SPTarget
import java.awt.geom.Point2D
import java.util.HashMap
import scala.math._


case class ModsSequence (node: ISPObservation) extends InstSequence(node) {

  def tReadout(s: ModsStep): Int = max(s.blueExposureTime, s.redExposureTime).toInt

  def tMechanism(s0: ModsStep, s1: ModsStep): Int = 0
    
  // First step in the sequence.  Ignore mechanism overheads
  def firstStep(s: ModsStep): ModsStep = {
    val time = tReadout(s) + tOffset(s)
    new ModsStep(s.node, time, s.config)
  }

  def walk(s0: ModsStep, tail: List[ModsStep], res: List[ModsStep]): List[ModsStep] = 
    if(tail.nonEmpty) {
      val s1 = tail.head
      val time = tReadout(s1) + max(tMechanism(s0, s1), tOffset(s0, s1))
      val newStep = new ModsStep(s1.node, time, s1.config)
      walk(s1, tail.tail, res.:+(newStep))
    } else res

  def timeSteps(steps: List[ModsStep]): List[ModsStep] = if(steps.nonEmpty) 
    walk(steps.head, steps.tail, List(firstStep(steps.head)))
    else List.empty

  def preset(target: SPTarget): Step = new PresetStep("Preset (Imaging)", 180, target)

  override def steps = timeSteps(ConfigUtil.configs(node).map(c => ModsStep(node, 1, c)))
    if(steps.nonEmpty) steps.+:(preset(target)) else steps
}


