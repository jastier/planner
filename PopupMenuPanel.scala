package jsky.app.ot.viewer.planner

import java.awt.event.{MouseAdapter, MouseEvent, ActionListener, ActionEvent}
import javax.swing.{JPanel, JPopupMenu, JMenuItem}
import javax.swing.event.{PopupMenuEvent, PopupMenuListener}

/** A base class for panels that support a popup menu */
trait PopupMenuPanel extends MousePanel with PopupMenuListener{

  def showPopup(x: Int, y: Int): Unit = getComponentPopupMenu.show(this, x, y)

  override def popupMenuCanceled(e: PopupMenuEvent): Unit = {}

  override def popupMenuWillBecomeInvisible(e: PopupMenuEvent): Unit = {}

  override def popupMenuWillBecomeVisible(e: PopupMenuEvent): Unit = {}

  /** add a separator to the popup menu */
  def addSeparator(): Unit = getComponentPopupMenu.addSeparator

  /** Add a predefined menu item to the popup menu */
  def addMenuItem(item: JMenuItem): Unit = getComponentPopupMenu.add(item)

  /** add a menu item that simply calls a unit function without arguments */
  def addMenuItem(label: String, tip: String, fn: () => Unit): Unit = 
    addMenuItem(MenuBuilder.menuItem(label, tip, fn))

  /** add a checkbox menu item that calls the passed-in function with the checkbox state */
  def addMenuToggleItem(label: String, tip: String, fn: (Boolean) => Unit, state: Boolean): Unit =
    addMenuItem(MenuBuilder.menuToggleItem(label, tip, fn, state))

  setComponentPopupMenu(new JPopupMenu)

  addMouseListener(new MouseAdapter() {
    override def mouseClicked(e: MouseEvent): Unit = {
      if (e.getButton != MouseEvent.BUTTON1) showPopup(e.getX, e.getY)
      else {}
    }
  })
}

