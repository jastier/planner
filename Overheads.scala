package jsky.app.ot.viewer.planner

import java.net.URL
import scala.collection.breakOut
import scala.collection.immutable.VectorBuilder
import scala.collection.JavaConversions._
import scala.io.Source

// read business logic overheads from a config file
class Overheads {

  def map(filename: String): Map[String, String] = {
    val vb = new VectorBuilder[String]
    try {
      val bufferedSource = Source.fromFile(filename)
      val itr: Iterator[String] = bufferedSource.getLines
      vb ++= itr
      bufferedSource.close
    } catch {
      case e: Exception => // Planner.message.error("Exception: " + e.toString)
    }
    process(Map.empty, vb.result.toList.map(line => line.split("#").apply(0)))
  }

  def process(map: Map[String, String], lines: List[String]): Map[String, String] =
    if(lines.nonEmpty) process(readLine(map, lines.head.split(",").map(_.trim)), lines.tail)
    else map

  def readLine(map: Map[String, String], cols: Array[String]): Map[String, String] = 
    if(cols.length == 2) map + (cols.apply(0) -> cols.apply(1)) 
    else map
}


object Overheads extends Overheads {
  val resource = System.getProperty("user.home") + "/.lbtocs/plannerOverheads.csv"

  def apply(): Map[String, String] = map(resource)
}

