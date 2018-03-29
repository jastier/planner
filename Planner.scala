package jsky.app.ot.viewer.planner

import javax.swing.{BoxLayout, JComponent, JFrame, JLabel, JPanel, JScrollPane, JSplitPane}
import javax.swing.{ScrollPaneConstants}
import java.awt.{Component, Dimension, Frame, GridBagConstraints, GridBagLayout, Insets}
import java.awt.GridBagConstraints._
import edu.gemini.pot.sp.{ISPNode, ISPObservation}
import jsky.app.ot.viewer.SPTree
import scala.collection.JavaConversions._


/** Main coordinator class for the Binocular Planner.  This class exists as a 
 *  singleton instance owned by a PlannerManager object.  */
class Planner extends JFrame("LBTO Binocular Planner") {
  // indicates no preset currently in use
  val NO_PRESET = -1

  // define location and size of this frame
  val BOUNDS_X = 100
  val BOUNDS_Y = 100
  val BOUNDS_WIDTH = 900
  val BOUNDS_HEIGHT = 700

  // define locations of splitter panes holding UI components
  val SPLIT_2 = 200  // divides SPLIT_3 and Timeline
  val SPLIT_1 = 550  // divides SPLIT_2 and message display at bottom of screen

  // UI elements that comprise a complete Planner
  val messageDisplay = new MessageDisplay
  val plotFrame = new PlotFrame(this)
  val timeline = new Timeline(this)
  val selectors = new Selectors(this)
  val overheads = new Overheads
  val settings = new SettingsFrame
  val mainMenuBar = new MainMenuBar(this)

  /** retrieve the currently selected user scale option from the main menu */
  def userScale(): Double = mainMenuBar.scale

  /** Write something on the message display */
  def message(s: String): Unit = messageDisplay.append(s)

  /** Updates the plan if a tree node changes */
  def dataObjectChanged(t: SPTree, n: ISPNode): Unit = setSelectedNodes(t, Array(n))

  /** Places the selected tree nodes (and any other nodes in the same group) on the planner. */
  def setSelectedNodes(t: SPTree, a: Array[ISPNode]): Unit = {
    // find all the keys in the tree
    val keys = findKeys(t)

    // the timeline should remove any that have vanished.
    timeline.update(keys)

    // the keys actually used by the timeline are then collected.
    val usedKeys = timeline.keys
    val remainingKeys = keys.filterNot(k => usedKeys.contains(k))
    selectors.setKeys(remainingKeys)
    timeline.plan
    plotFrame.repaint()
  }

  /** return keys for all observations in the tree */
  def findKeys(t: SPTree): List[Key] = {
    val observations = t.getRoot.getAllObservationsRecursive.toList
    val sequences = observations.map(obs => Sequence(obs))
    val instSequences = sequences.flatMap{
      case is: InstSequence => Some(is)
      case _ => None }
    instSequences.map(seq => new Key(t, seq))
  }

  /** Reset the size and location of the Planner to defaults.  Also reset the slider
     bar positions between UI elements to their default positions */
  def resetGeometry(): Unit = {
    setBounds(BOUNDS_X, BOUNDS_Y, BOUNDS_WIDTH, BOUNDS_HEIGHT)
    getContentPane.removeAll
    initUI
  }


  override def getMinimumSize(): Dimension = new Dimension(BOUNDS_WIDTH,BOUNDS_HEIGHT)
  override def getPreferredSize(): Dimension = new Dimension(BOUNDS_WIDTH,BOUNDS_HEIGHT)

  /** set the Planner frame geometry and splitter panes to their default states */
  def initUI(): Unit = {
    getContentPane.add(new JSplitPane(JSplitPane.VERTICAL_SPLIT) {
      setTopComponent(new JSplitPane(JSplitPane.VERTICAL_SPLIT) {
        setLeftComponent(selectors)
        setBottomComponent(add(new JScrollPane(new TimelinePanel(timeline))))
        setDividerLocation(SPLIT_2)
      })
      setBottomComponent(messageDisplay)
      setDividerLocation(SPLIT_1)
    })
    revalidate
    repaint()
  }

  setJMenuBar(mainMenuBar)

  setBounds(BOUNDS_X, BOUNDS_Y, BOUNDS_WIDTH, BOUNDS_HEIGHT)

  setExtendedState(Frame.NORMAL)

  initUI
}

// companion
object Planner extends Planner
