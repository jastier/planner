package jsky.app.ot.viewer.planner

import java.awt.event.{ActionEvent, ActionListener}
import javax.swing.{JCheckBoxMenuItem, JMenuItem}

/** A couple static methods that were being duplicated in some Planner classes, so 
  I put them in this static definition for any class to use */
object MenuBuilder {

  /** Creates a menu item that calls a unitary function when clicked
   @param label The text you see on the menu item UI element
   @param tip The ToolTipText that shows if the mouse cursor hovers over the menu item
   @param fn A function signature that is called when the menu item is activated
   @returns A MenuItem widget with the params
 */
  def menuItem(label: String, tip: String, fn: () => Unit): JMenuItem = new JMenuItem(label) {
    setToolTipText(tip)
    addActionListener(new ActionListener { 
      override def actionPerformed(e: ActionEvent): Unit = fn()
    })
  }

  /** Return a checkbox menu item that calls the passed-in function with the checkbox state.
    A function call is generated for both states.
   @param label The text you see on the menu item UI element
   @param tip The ToolTipText that shows if the mouse cursor hovers over the menu item
   @param fn A function signature that is called when the menu item is activated
   @param state The initial state for this checkbox
   @returns A MenuItem checkbox widget that calls back a user-defined function with its state. 
 */
  def menuToggleItem(
    text: String,
    tip: String,
    fn: (Boolean) => Unit,
    state: Boolean): JCheckBoxMenuItem = 
  new JCheckBoxMenuItem(text) {
    setToolTipText(tip)
    setState(state)
    addActionListener(new ActionListener {
      override def actionPerformed(e: ActionEvent) = fn(isSelected)
    })
  }

}
