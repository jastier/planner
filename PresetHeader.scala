package jsky.app.ot.viewer.planner

import java.awt.{BorderLayout, Color, Dimension, GridLayout}
import java.awt.{GridBagLayout, GridBagConstraints}
import javax.swing.{JLabel, JPanel, JScrollPane}
import javax.swing.border.TitledBorder
import javax.swing.BorderFactory
import edu.gemini.pot.sp.ISPObservation
import javax.swing.border.LineBorder


import java.awt.event.{MouseAdapter, MouseEvent}


/** This panel goes between the Title Bar and the Sequence.  It has two key lists and
  a summary widget */
class PresetHeader(val p: Preset) extends MousePanel {
  val sxKeyList = new KeyList(Sx, p.timeline.planner)
  val dxKeyList = new KeyList(Dx, p.timeline.planner)
  val summaryDisplay = new PresetSummary

  setInheritsPopupMenu(true)

  override def getPreferredSize(): Dimension = new Dimension (-1, 100)

  override def getMinimumSize(): Dimension = new Dimension (-1, 100)

  /** send mouseEntered event to parent to extend parent event handling */
  override def mouseEntered (e: MouseEvent): Unit = p.mouseEntered(e)

  /** send mouseExited event to parent to extend parent event handling */
  override def mouseExited (e: MouseEvent): Unit = p.mouseExited(e)

  /** advise the summary panel of new sequence */
  def setSequence(ps: ImPresetSequence): Unit = summaryDisplay(ps)

  /** compare the incoming keys with those on our key lists.   Any keys on our key lists
    that are not on the incoming list are zombies and need to be handled accordingly */
  def updateKeys(living: List[Key]) :Unit = {
    sxKeyList.updateKeys(living)
    dxKeyList.updateKeys(living)
  }

  def keysSx(): List[Key] = sxKeyList.keys

  def keysDx(): List[Key] = dxKeyList.keys

  setLayout(new BorderLayout)

  add(new JPanel {
    setLayout(new GridLayout(1,3,4,4))
    add(new JPanel{add(new JLabel(sxKeyList.side.title + " Observations"))})
    add(new JPanel{add(new JLabel(dxKeyList.side.title + " Observations"))})
    add(new JPanel)
  }, BorderLayout.NORTH)

  add(new JPanel {
    setLayout(new GridLayout(1,3,4,4))
    add(new KeyListPane(sxKeyList))
    add(new KeyListPane(dxKeyList))
    add(new JPanel{
      setLayout(new BorderLayout)
      setInheritsPopupMenu(true)
      add(new JPanel {
        setLayout(new BorderLayout)
        setInheritsPopupMenu(true)
        add(p.buttonPlot, BorderLayout.NORTH)
        add(new JPanel, BorderLayout.CENTER) // spacer
        add(p.checkBoxSteps, BorderLayout.SOUTH)
      }, BorderLayout.WEST)
      add(summaryDisplay, BorderLayout.CENTER)
    })
  }, BorderLayout.CENTER)
}

