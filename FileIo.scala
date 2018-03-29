package jsky.app.ot.viewer.planner

import java.awt.Component
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.filechooser.FileNameExtensionFilter
import javax.swing.{JFileChooser, JOptionPane}


/** All file operations of the Planner are done here */
object FileIo {

  /** saves an image of the component to a user-chosen filename */
  def saveAsPng(c: Component): Unit = {
    val fc = fileChooser(c, "Save as .png", "*.png", ".png") 

    if(JFileChooser.APPROVE_OPTION.equals(fc.showSaveDialog(c))) write(c, fc.getSelectedFile())
  }

  /** create a file chooser based on file type */
  private def fileChooser(
      c: Component,
      name: String, 
      filter: String, 
      extensions: String): JFileChooser = {
    new JFileChooser {
      addChoosableFileFilter(new FileNameExtensionFilter(filter, extensions))
      setName(name)
      override def approveSelection(): Unit = {
        if(getSelectedFile().exists()) JOptionPane.showConfirmDialog (
          c,
          "The file exists, overwrite?",
          "Existing file",
          JOptionPane.YES_NO_CANCEL_OPTION
        ) match {
          case JOptionPane.YES_OPTION => super.approveSelection
          case _ => cancelSelection
        } else super.approveSelection
      }
    }
  } 

  /** Returns a buffered image of the timeline if width and height are both positive, else None */
  private def image(c: Component): Option[BufferedImage] = {
    val w = c.getWidth
    val h = c.getHeight
    if((w > 0) && (h > 0)) {
      val img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB)
      c.paint(img.getGraphics)
      Some(img)
    } else None
  }

  /** Writes a png image of the component to the file */
  private def write(c: Component, f: File) = {
    image(c).foreach(img => ImageIO.write(img, "png", f))
  }
}
   

