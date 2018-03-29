package jsky.app.ot.viewer.planner

import java.awt.{BorderLayout, Color, Font, GridLayout}
import javax.swing.{JLabel, JPanel, JScrollPane}
import javax.swing.border.LineBorder
import edu.gemini.pot.sp.ISPObservation


/** A panel with a Selector for Sx and Dx keys'
 @param p A reference to the singleton Planner instance
*/
class Selectors (p: Planner) extends JPanel {
  val selectors = List(new SelectorPanel(Sx, p), new SelectorPanel(Dx, p))

  def setKeys(keys: List[Key]): Unit = selectors.foreach(s => s.setKeys(keys))

  def addKeys(keys: List[Key]): Unit = selectors.foreach(s => s.addKeys(keys))

  setLayout(new GridLayout(1,2,4,4))

  selectors.foreach(s => add(s))
}


/** Observation key selector for one side of the telescope
 @param s Telescope side, eithr Sx or Dx
 @param p A reference to the singleton Planner instance
*/
class SelectorPanel(s: Side, p: Planner) extends JPanel {
  val keyList = new KeyList(s, p)

  /** replaces the keys with a new list */
  def setKeys(keys : List[Key]): Unit = keyList.setKeys(keys)

  /** adds the new keys to the existing key list */
  def addKeys(keys : List[Key]): Unit = keyList.addKeys(keys)

  setLayout(new BorderLayout(4, 4))

  // add the title in bold text
  add(new JLabel(s.title + " Mirror"){
    setFont(new Font(getFont().getName(), Font.BOLD, 20))
  }, BorderLayout.NORTH)

  // add a list for the model
//  add(new JScrollPane(keyList){setBorder(new LineBorder(Color.black, 1))},  BorderLayout.CENTER)
  add(new KeyListPane(keyList), BorderLayout.CENTER)
}

