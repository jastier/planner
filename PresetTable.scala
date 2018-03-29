package jsky.app.ot.viewer.planner

import javax.swing.ScrollPaneConstants
import javax.swing.table.{AbstractTableModel, DefaultTableModel}
import javax.swing.table.{DefaultTableCellRenderer, TableCellRenderer, TableModel}
import java.awt.{BorderLayout, Color,Component, Dimension}
import java.awt.GridBagConstraints._
import java.awt.event.{MouseEvent, MouseListener}
import javax.swing.{BoxLayout, JPanel, JMenuItem, JPopupMenu, JScrollPane, JLabel, JTable}
import scala.annotation.tailrec
import scala.math._


class PresetTable(val planner: Planner) extends JTable with MouseListener {

  val model = new PresetTableModel(planner)
  setModel(model)

  val defaultRenderer = getDefaultRenderer(classOf[Preset])
  setDefaultRenderer(classOf[Preset], new PresetTableCellRenderer(defaultRenderer))

  def addPreset(): Unit = {
    val tl = planner.timeline
    val rows: Array[Object] =Array(new Preset(tl, tl.newPresetId){ setFocusable(true)})
    model.addRow(rows)
  }

  override def mouseClicked (e: MouseEvent): Unit = {
    println("PresetTable.mouseClicked")
    println(" Column = " + getSelectedColumn)
    println(" Row = " + getSelectedRow)
  }

  override def mouseEntered (e: MouseEvent): Unit = {println("PresetTable.mouseEntered")}

  override def mouseExited  (e: MouseEvent): Unit = {println("PresetTable.mouseExited")}

  override def mousePressed (e: MouseEvent): Unit = {println("PresetTable.mousePressed")}

  override def mouseReleased(e: MouseEvent): Unit = {println("PresetTable.mouseReleased")}

  addMouseListener(this)

  setShowHorizontalLines(true)

  setDragEnabled(true)

  setBackground(Color.green)

  setFocusable(true)

  setIntercellSpacing(new Dimension (4,4))

  setRowHeight (150)

  setModel(model)
}



class PresetTableModel(val planner: Planner) extends DefaultTableModel{

  override def getColumnCount(): Int = 3

  override def getColumnClass(col: Int) = classOf[Preset]

//  def addRow(p: Preset): Unit = println("PresetTableModel.addRow with Preset")

//  override def addRow(pa: Array[Object]):Unit = {
//    pa.foreach(a  => println("PresetTableModel.addRow with Any"))
//  }

}


class PresetTableCellRenderer(val renderer: TableCellRenderer) extends DefaultTableCellRenderer {

  override def getTableCellRendererComponent(
      table: JTable,
      obj: Object,
      isSelected: Boolean,
      hasFocus: Boolean,
      row: Int,
      col: Int) = obj match {
    case c: Preset => c
    case _ => renderer.getTableCellRendererComponent(table, obj, isSelected, hasFocus, row, col)
  }

  
}

