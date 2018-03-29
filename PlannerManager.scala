package jsky.app.ot.viewer.planner

import edu.gemini.pot.sp.ISPNode
import jsky.app.ot.viewer.SPTree


/** Maintains the singleton Planner instance, and wraps methods callable from the OT. */
object PlannerManager {
  lazy val planner = new Planner

  /** Edit this to control whether the Planner is available on the OT  */
  val enabled = true

  /** Displays a message on the planner message area
  @param s Message that will be added to the Planner message display on a new line
  @returns nothing
  */
  def message(s: String): Unit = if(enabled) planner.message(s)

  /** Replaces the Planned nodes with the selected nodes
  @param t The current Science Program on the Observation Tool (OT)
  @param a An array of program nodes selected on the Science Program
  @returns nothing
  */
  def setSelectedNodes(t: SPTree, a: Array[ISPNode]): Unit = enabled match {
    case true => t match {
      case tree: SPTree => a match {
        case array: Array[ISPNode] => planner.setSelectedNodes(tree, array)
        case _ => 
      }
      case _ => 
    }
    case false => 
  }
    
  /** Update all planned nodes
  @param t The current Science Program on the Observation Tool (OT)
  @param n A node on the Science Program that has changed state
  @returns nothing
  */
  def dataObjectChanged(t: SPTree, n: ISPNode): Unit =  enabled match {
    case true => t match {
      case tree: SPTree => n match {
        case node: ISPNode => planner.dataObjectChanged(tree, node)
        case _ => 
      }
      case _ => 
    }
    case false =>
  }

  /** Controls visibility of the Planner frame
  @param state Component visibility setting
  @returns nothing
  */
  def setVisible(state: Boolean): Unit = if(enabled) planner.setVisible(state)
}

