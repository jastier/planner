package jsky.app.ot.viewer.planner

import javax.swing.{JLabel, JPanel}
import java.awt.{Color, Component, GridBagConstraints, GridBagLayout}


/** a panel hosting a single changable component */
class TransientComponentPanel extends JPanel {

  setLayout(new GridBagLayout)

  setBackground(Color.cyan)

  setInheritsPopupMenu(true)

  /** Replace the entire contents of this panel with a new component */
  def apply(c: Component):Unit = {
    if(!matches(c, getComponents)) {
      removeAll
      add(c, new GridBagConstraints{
        gridx = 0
        gridy = 0
        weightx = 1.0
        weighty = 1.0
        fill = GridBagConstraints.BOTH
      })
      c.revalidate
      c.repaint()
      revalidate()
      repaint()
    }
  }

  /** Return the first component in the getComponents array, or None if the array is empty */
  def component(): Option[Component] = firstComponent(getComponents)

  /** return the first element in the component array, or None if the array is empty */
  def firstComponent(a:  Array[Component]): Option[Component] = if(a.isEmpty) None else Some(a(0))

  /** test the component against our array of existing components.  We are only interested
   in the first element of the array, it should never grow beyone 1 element long */
  def matches(c: Component, a: Array[Component]): Boolean = if(a.isEmpty) false else (a(0) == c)
}

