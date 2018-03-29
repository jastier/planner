package jsky.app.ot.viewer.planner

import edu.gemini.pot.sp.{ISPNode,ISPObservation,ISPObservationContainer, SPComponentType}
import edu.gemini.pot.sp.SPComponentBroadType._
import edu.gemini.spModel.config2.{Config, ConfigSequence, ItemEntry, ItemKey}
import edu.gemini.spModel.config.ConfigBridge
import edu.gemini.spModel.config.map.ConfigValMapInstances
import edu.gemini.spModel.util.SPTreeUtil
import java.util.HashMap
import scala.collection.JavaConversions._
import jsky.app.ot.viewer.{NodeData,SPTree}
import scala.annotation.tailrec


// one stop shopping for Config values
object ConfigUtil {
  def toInt(s: String, fallback: Int): Int = try {
    s.toInt
  } catch {
    case e: Exception => fallback
  }

  def toDouble(s: String, fallback: Double): Double = try {
    s.toDouble
  } catch {
    case e: Exception => fallback
  }

  def string(c: Config, key: ItemKey): String = Option(c.getItemValue(key)) match {
    case Some(any) => any.toString
    case None => ""
  }

  def int(c: Config, key: ItemKey): Int = toInt(string(c, key), 0)

  def double(c: Config, key: ItemKey): Double = toDouble(string(c, key), 0.0)

  private def dumpConfig(c: Config, buf: StringBuilder): String = {
    c.itemEntries.foreach(item => buf ++= (configItemToText(item) + "\n"))
    buf.toString
  }

  def configToText(c: Config): String = dumpConfig(c, new StringBuilder("Config entries:\n"))

  private def configItemToText(ie: ItemEntry): String = 
    ie.getKey.toString + " = " + configValueToText(ie.getItemValue)

  private def configValueToText(a: Any): String = a.getClass.getName + ", " + a.toString

  def configs(node: ISPObservation): List[Config] = configSequence(node).getAllSteps.toList

  def configSequence(node: ISPObservation): ConfigSequence =
    ConfigBridge.extractSequence(node, new HashMap, ConfigValMapInstances.IDENTITY_MAP)
}


// String map munger with fallback return value if mapping fails
trait MapUtil {  

  /** Return the int value for the key, or the fallback value if that fails */
  def int(map: Map[String, String], key: String, fallback: Int): Int = try {
    map(key).toInt
  } catch {
    case e: Exception => fallback
  }

  /** Return the double value for the key, or the fallback value if that fails */
  def double(map: Map[String, String], key: String, fallback: Double): Double = try {
    map(key).toDouble
  } catch {
    case e: Exception => fallback
  }

  /** Return the string value for the key, or the fallback value if that fails */
  def string(map: Map[String, String], key: String, fallback: String): String = try {
    map(key)
  } catch {
    case e: Exception => fallback
  }
}

