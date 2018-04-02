package jsky.app.ot.viewer.planner

import edu.gemini.pot.sp.{ISPObservation, SPComponentType}
import edu.gemini.pot.sp.SPComponentType._
import edu.gemini.spModel.config2.{Config, ConfigSequence}
import edu.gemini.spModel.config.ConfigBridge
import edu.gemini.spModel.config.map.ConfigValMapInstances
import edu.gemini.spModel.gemini.luci.Luci
import edu.gemini.spModel.target.SPTarget
import java.awt.geom.Point2D
import java.util.HashMap
import scala.math._


class LuciOverheads (map: Map[String, String]) extends MapUtil{
  def alignLongslit = int(map, "LuciAlignLongslit", 74)
  def presetMos = int(map, "LuciPresetMos", 360)
  def confirmLongslit = int(map, "LuciConfirmLongslit", 60)
  def confirmMos = int(map, "LuciConfirmMos", 90)
  def mechanismCamera = int(map, "LuciMechanismCamera", 15)
  def mechanismFilter = int(map, "LuciMechanismFilter", 15)
  def mechanismGrating = int(map, "LuciMechanismGrating", 20)
  def mechanismMaskPos = int(map, "LuciMechanismMaskPos", 72)
  def presetImaging = int(map, "LuciPresetImaging", 180)
  def presetLongslit = int(map, "LuciPresetLongslit", 360)
  def alignMos = int(map, "LuciAlignMos", 194)
  def readoutLir = double(map, "LuciReadoutLir", 2.51)
  def readoutMer = double(map, "LuciReadoutMer", 6.26)
}


case class LuciSequence(node: ISPObservation) extends InstSequence (node) {

  // we must read this, not build for it.
  val map = Overheads.apply
  val oh = new LuciOverheads(map)

  def tReadout(s: LuciStep): Int = s.readout match {  // never concurrent
    case Luci.READOUT_LIR_NAME => rint(s.nExpo * (s.nDit * s.dit) + oh.readoutLir).toInt
    case Luci.READOUT_MER_NAME => rint(s.nExpo * (s.nDit * s.dit) + oh.readoutMer).toInt
    case _ => rint(s.nExpo * (s.nDit * s.dit)).toInt
  }

  def tMechanism(s0: LuciStep, s1: LuciStep): Int = List (
    (if(s1.camera != s0.camera) oh.mechanismCamera else 0),
    (if(s1.filter != s0.filter) oh.mechanismFilter else 0),
    (if(s1.grating != s0.grating) oh.mechanismGrating else 0),
    (if(s1.maskPos != s0.maskPos) oh.mechanismMaskPos else 0)).max

  // First step in the sequence.  Ignore mechanism overheads
  def firstStep(s: LuciStep): LuciStep = {
    val time = tReadout(s) + tOffset(s)
    new LuciStep(s.node, time, stepText(s), s.config, posAngleRadians)
  }

  def stepText(s: LuciStep): String = {
    "Text!"
  }

  def walk(s0: LuciStep, tail: List[LuciStep], res: List[LuciStep]): List[LuciStep] = 
    if(tail.nonEmpty) {
      val s1 = tail.head
      val time = tReadout(s1) + max(tMechanism(s0, s1), tOffset(s0, s1))
      val newStep = new LuciStep(s1.node, time, s1.text, s1.config, posAngleRadians)
      walk(s1, tail.tail, res.:+(newStep))
    } else res

  def timeSteps(steps: List[LuciStep]): List[LuciStep] = 
    if(steps.nonEmpty) 
      walk(steps.head, steps.tail, List(firstStep(steps.head)))
    else List.empty

  // weaksauce, use extractor pattern
  override def steps = {
    val timedSteps = 
      timeSteps(ConfigUtil.configs(node).map(c => LuciStep(node, 1, "", c, posAngleRadians)))
    if(timedSteps.nonEmpty) {
      timedSteps.head.mask match {
        case Luci.MASK_NONE_NAME => ImagingSequence(target, timedSteps, oh)
        case Luci.MASK_CUSTOM_NAME => MosSequence(target, timedSteps, oh)
        case _ => LongslitSequence(target, timedSteps, oh)
      }
    } else List.empty
  }
}

object ImagingSequence {
  def apply(target: SPTarget, steps: List[LuciStep], oh: LuciOverheads): List[Step] = 
    if(steps.nonEmpty) steps.+:(preset(target, oh.presetImaging)) else steps

  def preset(target: SPTarget, time: Int): Step = 
    new PresetStep("Preset (Imaging)", time, target)
}

trait Spectroscopy {
  def maskInFpu(head: LuciStep, tail: List[LuciStep]): List[LuciStep] = 
  if(tail.nonEmpty) {
    if((head.maskPos == Luci.MASK_POS_TURNOUT_NAME) && 
      (tail.head.maskPos == Luci.MASK_POS_FPU_NAME)) tail.tail
    else maskInFpu(tail.head, tail.tail)
  } 
  else List.empty

  def apply(target: SPTarget, steps: List[LuciStep], oh: LuciOverheads): List[Step] = 
    if(steps.head.isAcquisition) preset(target, steps, oh) 
    else steps

  def preset(target: SPTarget, steps: List[LuciStep], oh: LuciOverheads): List[Step] = 
    if(steps.length > 3) acquisition(target, steps, oh)
    else steps

  def align(time: Int, name: String): Step = 
    new NonExposureStep("Align (" + name + ")", time)

  def confirm(time: Int, name: String): Step = 
    new NonExposureStep("Confirm (" + name + ")", time)

  def preset(time: Int, name: String, target: SPTarget): Step = 
    new PresetStep("Preset (" + name + ")", time, target)

  def acquisition(target: SPTarget, steps: List[LuciStep], oh: LuciOverheads): List[Step]
}


object LongslitSequence extends Spectroscopy {
  val name = "Longslit"
  def acquisition( target: SPTarget, steps: List[LuciStep], oh: LuciOverheads): List[Step] =  {
    val fpuSteps = maskInFpu(steps.head, steps.tail)
    if(fpuSteps.nonEmpty) {
      List(preset(oh.presetLongslit, name, target)) ++
      steps.dropRight(fpuSteps.length) ++
      List(align(oh.alignLongslit, name)) ++ 
      fpuSteps ++
      List(confirm(oh.confirmLongslit, name))
    } else steps
  }
}
    

object MosSequence extends Spectroscopy {
  val name = "MOS"
  def acquisition( target: SPTarget, steps: List[LuciStep], oh: LuciOverheads): List[Step] =  {
    val fpuSteps = maskInFpu(steps.head, steps.tail)
    if(fpuSteps.nonEmpty) {
      List(preset(oh.presetMos, name, target)) ++
      steps.dropRight(fpuSteps.length) ++
      List(align(oh.alignMos, name)) ++ 
      fpuSteps ++
      List(confirm(oh.confirmMos, name))
    } else steps
  }
}

