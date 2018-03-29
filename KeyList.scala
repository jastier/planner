package jsky.app.ot.viewer.planner

import java.awt.{Color, GridBagLayout}
import java.awt.datatransfer.{DataFlavor, Transferable}
import javax.swing.{JComponent, JList, JPanel, JScrollPane}
import javax.swing.{DefaultListModel, TransferHandler, ListSelectionModel, DropMode}
import javax.swing.TransferHandler._
import scala.annotation.tailrec
import edu.gemini.pot.sp.ISPObservation

import scala.collection.JavaConversions._
import edu.gemini.spModel.util.SPTreeUtil
import javax.swing.border.LineBorder

import jsky.app.ot.viewer.SPTree


/** A single observation, with a human-readable unique ID
@param t The Science Program Tree with the observation
@param seq One instrument observation from a Science Plan Tree
*/
class Key(val t: SPTree, val seq: InstSequence) {
  lazy val obs = seq.observation
  lazy val title = "[" + obs.getObservationNumber() + "] " + obs.getDataObject().getTitle()

  // do we know the instrument is always found?
  lazy val instType = SPTreeUtil.findInstrument(obs).getType

  /** return true if the observation for this key is from the input side */
  def is(side: Side): Boolean = side(instType)

  // we test equality on title - it contains the observation number, which is sufficiently unique
  override def equals(o: Any): Boolean = o match {
    case that: Key => title.equals(that.title)
    case _ => false
  }
  
  override def toString(): String = title
}


/** A JList that accepts unique keys for one side of the telescope
@param side The telescope side using this keyset
*/
class KeyList(val side: Side, p: Planner) extends JList[Key] {

  val model = new KeyListModel(side, p)

  /** replaces the keys with a new set */
  def setKeys(list : List[Key]): List[Key] = {
    model.clear
    addKeys(list)
    list
  }

  /** add the new keys */
  def addKeys(keys : List[Key]): Unit =
    keys.foreach(key => if(side(key.instType)) model.addElement(key))

  /** remove all keys that are not in the latest key list, return a list of keys we are using */
  def updateKeys(living: List[Key]): Unit = setKeys(keys.filter(k => living.contains(k)))

  /** Returns a set of all selected keys in this list */
  def selectedKeys(): List[Key] = getSelectedValuesList.toList

  /** Returns a set of all keys in this list */
  def keys(): List[Key] = model.toArray.toList.flatMap{
    case k: Key => Some(k)
    case _ => None
  }

  setModel(model)

  setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION)
  setDragEnabled(true)
  setDropMode(DropMode.INSERT)
  setTransferHandler(new KeyListTransferHandler(side))

  setFocusable(true)
}

class KeyListPane(kl: KeyList) extends JScrollPane(kl) {
  setBorder(new LineBorder(Color.black, 1))
}


class KeyListTransferHandler(s: Side) extends TransferHandler {

  override def getSourceActions(c: JComponent): Int = MOVE

  override def canImport(info: TransferHandler.TransferSupport): Boolean =
    info.isDataFlavorSupported(s.flavor)

  override def createTransferable(c: JComponent): Transferable = c match {
    case ks: KeyList => new KeyListTransferable(s, ks.selectedKeys)
    case _ => super.createTransferable(c)
  }

  /** remove the transferred data from the source KeyList */
  override def exportDone(c: JComponent, t: Transferable, action: Int): Unit = c match {
    case ks: KeyList => t match {
      case kst: KeyListTransferable => action match {
        case MOVE => kst.isDataFlavorSupported(s.flavor) match {
          case true => ks.model.removeElements(kst.getTransferData(s.flavor))
          case false => 
        }
        case _ => 
      }
      case _ => 
    }
    case _ => 
  }

  /** Add the transferred data to the destination KeyList.  */
  override def importData(c: JComponent, t: Transferable): Boolean = c match {
    case ks: KeyList => t.isDataFlavorSupported(s.flavor) match {
      case true => {

        // this dynamic cast of the transferable data is lame, and I don't have a better idea.
        val data = t.getTransferData(s.flavor).asInstanceOf[List[Key]]

        ks.model.insertElements(ks.getDropLocation.getIndex, data)
        true
      }
      case false => false
    } 
    case _ => false
  }

}


/** Drag and Drop data carrier class for moving keys around */
final class KeyListTransferable(s: Side, data: List[Key]) extends Transferable {
  
  /** Returns true if the data flavor for this Side matches the argument value */
  override def isDataFlavorSupported(fl: DataFlavor): Boolean = getTransferDataFlavors.contains(fl)

  /** Return the data flavors for this Side. */
  override def getTransferDataFlavors(): Array[DataFlavor] = Array(s.flavor)

  /** Returns the data to be transferred, in this case a set of Observation Keys */
  override def getTransferData(fl: DataFlavor): List[Key] = 
    if(isDataFlavorSupported(fl)) data else List.empty
}


/** A list model that only takes keys from one side of the telescope */
final class KeyListModel(s: Side, p: Planner) extends DefaultListModel[Key] {

  def removeElements(keys: List[Key]) : Unit = {
    keys.foreach(key => removeElement(key))
    p.timeline.plan
  }

  def insertElements(targetIndex: Int, data: Object) : Unit = {
    insertElements(targetIndex, data.asInstanceOf[List[Key]])  // weak runtime cast
  }

  /** Insert the data at the drop target. The Java DefaultListModel does not have any
      insertElements(index: Int, nodes: Collection) method, so we have roll our own */
  @tailrec
  final def insertElements(targetIndex: Int, keys: List[Key]): Unit = if(!keys.isEmpty) {

    // the exportDone method will remove the first instance of this node it sees.
    val key = keys.head
    val keyIndex = indexOf(key)

    if((contains(key)) && (targetIndex < keyIndex)){
      // If we are inserting the new key ahead of a copy of itself in the same model, 
      // exportNode will get it wrong because it deletes the first instance of a key it finds,
      // which will be the key we are inserting.
      // In this case, delete the trailing key ourselves, and insert the new key twice.
      // The exportDone routine will delete the first copy.  The second will remain in the
      // correct place.
      removeElementAt(keyIndex)
      insertElementAt(key, targetIndex)
      insertElementAt(key, targetIndex)
      insertElements(targetIndex+2, keys.tail)
    } else {
      // we are either moving the key behind the existing key in the same model, or moving the
      // key to a different model.   ExportDone will remove the existing key properly.
      insertElementAt(key, targetIndex)
      insertElements(targetIndex+1, keys.tail)
    }
  }
}

