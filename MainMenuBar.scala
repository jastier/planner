package jsky.app.ot.viewer.planner

import java.awt.event.{ActionListener, ActionEvent}
import javax.swing.{Box, ButtonGroup, JCheckBoxMenuItem, JMenu, JMenuItem, JMenuBar}

/** The top menu bar for the Binocular Planner frame  */
class MainMenuBar(p: Planner) extends JMenuBar {

  val scaleMenu = new ScaleMenu(p)

  add(new JMenu("File") {
    add(MenuBuilder.menuItem("Export as Image (*.png)", "Save as a .png image", saveAsPng))
    add(MenuBuilder.menuItem("Export as Plan (*.pln)", "Save Timeline a Binocular Plan", saveAsPlan))
    addSeparator
    add(MenuBuilder.menuItem("Hide this Window", "Close the Planner window", plannerOff))
  })
  add(new JMenu("Sequencing") {
    add (MenuBuilder.menuItem("Add Preset", "Create a new preset", addPreset))
  })
  add(new JMenu("Settings") {
    List("LBC", "LUCI", "MODS").foreach(s => add (new JMenuItem(s)))  // fake news
  })
  add(new JMenu("View") {
    add(new JMenu("Presets") {
      add(MenuBuilder.menuItem("Headers On", "Show the preset headers", headersOn))
      add(MenuBuilder.menuItem("Headers Off","Hide the preset headers", headersOff))
    })
    add(new JMenu("Sequences") {
      add(MenuBuilder.menuItem("Steps On", "Show the Preset Sequence Steps", stepsOn))
      add(MenuBuilder.menuItem("Steps Off","Hide the Preset Sequence Steps", stepsOff))
      addSeparator
      add(scaleMenu)
    })
    add(MenuBuilder.menuItem("Copoint Visualizer", "Show the Copointing Display", plotterOn))
    add(MenuBuilder.menuItem("Reset Window Geometry", "Restore default layout", p.resetGeometry))
  })
  add(Box.createHorizontalGlue)
  add(new JMenu("Help") {
    add (new JMenuItem("OT help page link goes here"))
  })

  def plannerOff(): Unit = p.setVisible(false)
  def scale(): Double = scaleMenu.scale
  def plotterOn(): Unit = p.plotFrame.setVisible(true)
  def headersOn(): Unit = p.timeline.setHeadersVisible(true)
  def headersOff(): Unit = p.timeline.setHeadersVisible(false)
  def stepsOn(): Unit = p.timeline.setSequencesVisible(true)
  def stepsOff(): Unit = p.timeline.setSequencesVisible(false)
  def addPreset(): Unit = p.timeline.addPreset
  def saveAsPng(): Unit = FileIo.saveAsPng(p.timeline)
  def saveAsPlan(): Unit = println("Save as a binocular plan")
}

