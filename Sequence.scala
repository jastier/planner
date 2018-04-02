package jsky.app.ot.viewer.planner

import edu.gemini.pot.sp.{ISPObsComponent, ISPObservation, SPComponentType}
import edu.gemini.pot.sp.SPComponentType._
import edu.gemini.spModel.config2.{Config, ConfigSequence}
import edu.gemini.spModel.config.ConfigBridge
import edu.gemini.spModel.config.map.ConfigValMapInstances
import edu.gemini.spModel.data.ISPDataObject
import edu.gemini.spModel.target.env.TargetEnvironment
import edu.gemini.spModel.target.obsComp.TargetObsComp
import edu.gemini.spModel.target.SPTarget
import edu.gemini.spModel.util.SPTreeUtil
import java.awt.geom.Point2D
import java.util.HashMap
import scala.collection.JavaConversions._
import edu.gemini.pot.sp.SPComponentBroadType
import edu.gemini.spModel.data.AbstractDataObject
import edu.gemini.spModel.obscomp.SPInstObsComp

// Immutable

/** A "Sequence", for planning purposes, is a single observation block */
object Target {
  def apply(node: ISPObservation): SPTarget = 
    Option(node.getObsComponents.toList) map searchObsComps getOrElse new SPTarget

  def searchObsComps(list: List[ISPObsComponent]): SPTarget = 
    list.find(oc => TargetObsComp.SP_TYPE == oc.getType) map target getOrElse new SPTarget

  def target(oc: ISPObsComponent): SPTarget =
    oc.getDataObject.asInstanceOf[TargetObsComp].getTargetEnvironment.getBase
}


object PosAngleRadians{

  def toInstruments(node: ISPObservation): List[SPInstObsComp] =
    node.getObsComponents.toList.map(oc => oc.getDataObject match {
      case obj: SPInstObsComp => Some(obj)
      case _ => None
    }).flatten

  def toPosAngleRadians(instruments: List[SPInstObsComp]): Double = if(instruments.isEmpty) 0.0
    else instruments.head.getPosAngleRadians

  def apply(node: ISPObservation): Double = toPosAngleRadians(toInstruments(node))
}


trait Sequence {
  def title: String

  def steps:List[Step]

  def seconds: Int = steps.map(step => step.seconds).sum

  def target: SPTarget = new SPTarget

  def mouseClicked(): Unit = {}

  def isEmpty: Boolean = steps.isEmpty

  override def toString: String = title

  override def equals (a: Any): Boolean = title == (a.toString)
}

case class GenericSequence(title: String, steps: List[Step]) extends Sequence 

abstract class InstSequence(node: ISPObservation) extends Sequence {
  var isCopointed = true

  def posAngleRadians: Double = PosAngleRadians(node)

  def observation(): ISPObservation = node

  def tOffset(x: Double, y: Double): Int = if((x != 0) || (y != 0)) 14 else 0

  def tOffset(s: ExposureStep): Int = tOffset(s.xAxis, s.yAxis)

  def tOffset(s0: ExposureStep, s1: ExposureStep): Int = tOffset(s1.xAxis - s0.xAxis, s1.yAxis - s0.yAxis)

  override def target = Target(node)

  override def title = "[" + node.getObservationNumber() + "] " + node.getDataObject().getTitle()
}


object Sequence {
  def apply() = GenericSequence("Empty", List[Step]())

  def apply(name: String, steps: List[Step]) = GenericSequence(name, steps)

  def apply(node: ISPObservation) = {
    Option(SPTreeUtil.findInstrument(node)).fold(UNKNOWN)(_.getType) match {
      case INSTRUMENT_LBCBLUE => new LbcSequence (node)
      case INSTRUMENT_LBCRED => new LbcSequence (node)
      case INSTRUMENT_LUCI1 => new LuciSequence (node)
      case INSTRUMENT_LUCI2 => new LuciSequence (node)
      case INSTRUMENT_MODS1 => new ModsSequence(node)
      case INSTRUMENT_MODS2 => new ModsSequence(node)
      case _ => GenericSequence("Unknown", List[Step]())
    }
  }
}
