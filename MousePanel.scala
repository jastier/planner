package jsky.app.ot.viewer.planner

import java.awt.event.{MouseEvent, MouseWheelEvent, MouseWheelListener}
import javax.swing.event.MouseInputListener
import javax.swing.JPanel

// a mouseadapter + mousewheel panel 
trait MousePanel extends JPanel with MouseInputListener with MouseWheelListener
{

  def mouse(state: Boolean): Unit = {}

  override def mouseEntered (e: MouseEvent): Unit = {}

  override def mouseExited  (e: MouseEvent): Unit = {}

  override def mousePressed (e: MouseEvent): Unit = {}

  override def mouseDragged (e: MouseEvent): Unit = {}

  override def mouseReleased(e: MouseEvent): Unit = {}

  override def mouseMoved   (e: MouseEvent): Unit = {}

  override def mouseClicked (e: MouseEvent): Unit = {}

  override def mouseWheelMoved (e: MouseWheelEvent): Unit = {}

  addMouseListener(this)
  addMouseMotionListener(this)
  addMouseWheelListener(this)
}
