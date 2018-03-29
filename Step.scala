package jsky.app.ot.viewer.planner

import edu.gemini.pot.sp.{ISPNode, ISPObsComponent, ISPObservation}
import edu.gemini.skycalc.Coordinates
import edu.gemini.spModel.config2.{Config, ItemEntry, ItemKey}
import edu.gemini.spModel.target.SPTarget
import java.awt.Color
import java.awt.geom.Point2D
import javax.swing.border.LineBorder
import jsky.app.ot.viewer.SPTree
import scala.collection.JavaConversions._
import scala.math._


/** A step is an atomic unit of a Science Plan. */
abstract class Step {
  val seconds: Int
  val color: Color = Color.white
  val border: LineBorder = new LineBorder(Color.black, 1)

  /** Shown on the step label */
  val text: String

  /** shown on the tool tip popup */
  def toolTipText = text

  val isAcquisition: Boolean = false
  val isExposure: Boolean = false
  val isScience: Boolean = false

  def mouseClicked(): Unit = {}
}


/** An open shutter step */
abstract class ExposureStep(node: ISPNode, c: Config) extends Step {
  println("\nSTEP:")
    println(ConfigUtil.configToText(c))

  def instrument = ConfigUtil.string(c, new ItemKey("instrument:instrument"))

  def x = ConfigUtil.double(c, new ItemKey("telescope:xaxis"))

  def y = ConfigUtil.double(c, new ItemKey("telescope:yaxis"))

  def coords = ConfigUtil.string(c,new ItemKey("telescope:coords"))  // RADEC or DETXY

  def stepNumber = ConfigUtil.int(c, new ItemKey("metadata:stepcount"))

  def obsClass = ConfigUtil.string(c, new ItemKey("observe:class"))

  def obsClassText = if(obsClass == "") "" else obsClass + ", "

  override def mouseClicked(): Unit = PlannerManager.message(ConfigUtil.configToText(c))

  override val text = "Step " + stepNumber + ": " + obsClassText + seconds + " s"
  override val isExposure = true
  override val isScience = List("tscience", "scal", "science").contains(obsClass)
  override val isAcquisition = List("acq", "tacq", "sacq").contains(obsClass)
  override val color = if(isAcquisition) Color.cyan else Color.lightGray
}


case class NonExposureStep(name: String, override val seconds: Int) extends Step {
  override val text = (name + ", " + seconds + " s")
  override val color = new Color(0.9F, 1.0F, 0.9F) //Color.green
}


/** A Preset is an observation, with n offsets from the same target location */
class PresetStep (
    override val name: String,
    override val seconds: Int,
    target: SPTarget) 
  extends NonExposureStep(name, seconds) {
  val line1 = name + ", " + seconds + " s<br>"
  val line2 = "RA:  " + target.getXAxisAsString + "<br>"
  val line3 = "Dec: " + target.getYAxisAsString + "<br>"
  override val text = "<html>" + line1 + line2 + line3 + "</html>"
}


case class LbcStep (
    node: ISPNode,
    override val seconds: Int,
    toolTip: String,
    config: Config)
  extends ExposureStep (node, config) {
  lazy val exposureTime = ConfigUtil.double(config,
    new ItemKey("instrument:exposureTime")).toInt
  lazy val camera: String = ConfigUtil.string(config, new ItemKey("instrument:camera"))
  lazy val filter: String = ConfigUtil.string(config, new ItemKey("instrument:filter"))
}


case class LuciStep (
    node: ISPNode,
    override val seconds: Int,
    toolTip: String,
    config: Config)
  extends ExposureStep (node, config) {
  val mask: String = ConfigUtil.string(config, new ItemKey("instrument:mask")) 
  val maskPos: String = ConfigUtil.string(config, new ItemKey("instrument:maskPos")) 
  val nDit: Double = ConfigUtil.double(config, new ItemKey("instrument:nDit"))
  val dit: Double = ConfigUtil.double(config, new ItemKey("instrument:dit"))
  val nExpo: Double = ConfigUtil.double(config, new ItemKey("instrument:nExpo"))
  val readout: String = ConfigUtil.string(config, new ItemKey("instrument:readout"))
  val camera: String = ConfigUtil.string(config, new ItemKey("instrument:camera"))
  val filter: String = ConfigUtil.string(config, new ItemKey("instrument:filter"))
  val grating: String = ConfigUtil.string(config, new ItemKey("instrument:grating"))
  val head = "<html>"
  val body1 = "Step " + stepNumber + ": " + obsClass + ", " + seconds + " s<br>"
  val body2 = mask + ", " +  maskPos
  val tail = "</html>"

  override val text = head + body1 + body2 + tail
}


case class ModsStep (
    node: ISPNode, 
    override val seconds: Int,
    config: Config) 
  extends ExposureStep (node, config) {
  val redExposureTime = ConfigUtil.double(config, new ItemKey("instrument:redExpTime"))
  val blueExposureTime = ConfigUtil.double(config, new ItemKey("instrument:blueExpTime"))
}

