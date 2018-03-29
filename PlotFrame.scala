package jsky.app.ot.viewer.planner

import java.awt.{BorderLayout, Font, Frame, GridBagConstraints, GridBagLayout, Insets}
import javax.swing.{BorderFactory, JButton, JCheckBox, JFrame, JLabel, JPanel}

import java.awt.event.{ActionEvent, ActionListener}
import jsky.coords.WorldCoords

import javax.swing.{Box, JCheckBoxMenuItem, JMenu, JMenuItem, JMenuBar}


/** Arrange the layout of the Plot, the plot controls, and anything else needed to make
  a self-contained plot object */
class PlotFrame (var planner: Planner) extends JFrame {
  // define location and size of this frame
  val BOUNDS_X = 100
  val BOUNDS_Y = 100
  val BOUNDS_WIDTH = 600
  val BOUNDS_HEIGHT = 500

  val plot = new Plot (planner)
  val plotControls = new PlotControl(planner, this)
  val plotStatus = new PlotStatus

  // set preset by a direct action (user hit the "plot" button).
  def setPreset(p: Preset): Unit = {
    setTitle(p.title)
    plot.setPreset(p)
    plotStatus.setPresetId(plot.presetId)
  }

  /* This preset is being removed from the timeline.  Release resources if in use */
  def notifyOfDeath(p: Preset): Unit = {
    plot.notifyOfDeath(p)
    plotStatus.setPresetId(plot.presetId)
  }

  override def repaint(): Unit = {
    super.repaint()
    plot.repaint()
  }

  def find(): Unit = plot.find

  def reset(): Unit = plot.reset

  def reportCursorPosition(wc: WorldCoords): Unit = plotStatus.reportCursorPosition(wc)

  setExtendedState(Frame.NORMAL)

  setTitle("LBTO Binocular Plotter")
    
  setBounds(BOUNDS_X, BOUNDS_Y, BOUNDS_WIDTH, BOUNDS_HEIGHT)

  getContentPane.setLayout(new BorderLayout)

  getContentPane.add(new JPanel{
    setLayout(new BorderLayout)
    add(new JPanel{
      setLayout(new GridBagLayout)
      add(plot, new GridBagConstraints{
        weightx = 1.0
        weighty = 1.0
        fill = GridBagConstraints.BOTH
        insets = new Insets(10,10,10,0)
      })
    }, BorderLayout.CENTER)
    add(plotStatus, BorderLayout.SOUTH)
  }, BorderLayout.CENTER)
  getContentPane.add(plotControls, BorderLayout.EAST)


  setJMenuBar(new JMenuBar {
    def saveAsPng(): Unit = FileIo.saveAsPng(planner.plotFrame.plot)
    def hideFrame(): Unit = planner.plotFrame.setVisible(false)

    add(new JMenu("File") {
      add(MenuBuilder.menuItem("Save as image (*.png)", "Save this plot as an image", saveAsPng))
      addSeparator
      add(MenuBuilder.menuItem("Hide this Window", "Close the Planner window", hideFrame))
    })
    add(Box.createHorizontalGlue)
    add(new JMenu("Help") {
      add (new JMenuItem("OT help page link goes here"))
    })
  })

}

