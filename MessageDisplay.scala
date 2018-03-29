package jsky.app.ot.viewer.planner

import java.awt.{Color, Dimension, GridBagConstraints, GridBagLayout}
import javax.swing.{JPanel, JScrollPane, JTextArea}


/** A class to keep a running event log at the bottom of the Planner frame */
class MessageDisplay extends PopupMenuPanel {

  val textArea = new JTextArea {
    setEditable(false)
    setLineWrap(true)
    setInheritsPopupMenu(true)
    setBackground(Color.white)
  }

  /** erase all the text on the message panel */
  def clear(): Unit = textArea.setText("")

  /** erase all the text on the message panel */
  def copy(): Unit = textArea.copy

  /** select all text in preparation for copying */
  def selectAll(): Unit = textArea.selectAll

  /** append the message to the text on the message panel */
  def append(text: String): Unit = {
    textArea.append(text + "\n")
    println(text)
  }

  /** append an informative message to the message panel */
  def info(text: String): Unit = append(text)

  /** append a warning message to the message panel */
  def warning(text: String): Unit = append("WARN: " + text + "\n")

  /** append an error message to the message panel */
  def error(text: String): Unit = append("ERROR: " + text + "\n")

  /** Allow the layout manager to reduce the size of this component to nothing */
  override def getMinimumSize(): Dimension = {new Dimension (-1, -1)}

  setLayout(new GridBagLayout)
  setInheritsPopupMenu(true)

  val scrollPane =  new JScrollPane(new JPanel{
    setInheritsPopupMenu(true)
    setLayout(new GridBagLayout)
    add(textArea, new GridBagConstraints {
      weightx = 1.0
      weighty = 1.0
      fill =  GridBagConstraints.BOTH
    })
    addMenuItem("Clear", "Erase all text", clear)
    addSeparator
    // TODO make these menu items local variables.   The select all should only be
    // enabled if text is present.   The copy should only be enabled if there is text selected
    addMenuItem("Select All", "Select all log text", selectAll)
    addMenuItem("Copy", "Copy text selection to the clipboard", copy)
  })

  scrollPane.setInheritsPopupMenu(true)

  add(scrollPane, new GridBagConstraints() {
    weightx = 1.0
    weighty = 1.0
    fill =  GridBagConstraints.BOTH
  })

  info("LBTO Binocular Planner 2018A semester")
}

//class MessageDisplayLayout extends JPanel with PopupMenuTrait {

