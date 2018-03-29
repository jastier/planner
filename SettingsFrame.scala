package jsky.app.ot.viewer.planner

import java.awt.{Color, Dimension, Font, GridBagConstraints, GridBagLayout, Rectangle, Insets}
import javax.swing.{JComponent, JFrame, JLabel, JPanel, JTextField, JTree}
import javax.swing.tree.{DefaultMutableTreeNode, DefaultTreeModel, TreeNode}
import jsky.util.Preferences
import scala.collection.JavaConversions._
import java.io.IOException
import javax.xml.parsers.{DocumentBuilder, DocumentBuilderFactory, ParserConfigurationException}

import org.w3c.dom._
import org.w3c.dom.Node._
import org.xml.sax.SAXException

class SettingsFrame extends JFrame{

  setBounds(100,100,500,500)


  override def getTitle(): String = "LBTO Binocular Planner Settings"

  // build components
  val resource = System.getProperty("user.home") + "/.lbtocs/plannerOverheads.xml"
  val tree = new SettingsTree(resource)

  // build components
  val controls = new JPanel(){
    setLayout(new GridBagLayout)
    setBackground(Color.gray)
    add(new JLabel("Controls"), new GridBagConstraints() {
      insets = new Insets(4, 4, 10, 4)
    })
  }

  // assemble UI
  setContentPane(new JPanel(){ 
    setLayout(new GridBagLayout)
    add(tree, new GridBagConstraints() {
      fill = GridBagConstraints.BOTH
      weightx = 1.0
      weighty = 1.0
      insets = new Insets(4, 4, 10, 4)
    })
    add(controls, new GridBagConstraints() {
      gridx = 0
      gridy = 1
      fill = GridBagConstraints.HORIZONTAL
      insets = new Insets(0, 4, 4, 4)
    })
  })
  pack
}


class SettingsTree(filePath: String) extends JTree {

  Option(filePath).foreach(validPath => setPath(validPath))

  def setPath(filePath: String): Unit = try {
    val factory=DocumentBuilderFactory.newInstance()
    val builder = factory.newDocumentBuilder()
    val doc = builder.parse(filePath)
    val root = doc.getDocumentElement.asInstanceOf[Node]
    val treeRoot = new DefaultMutableTreeNode("Binocular Planner Settings")
    walk(root, treeRoot, "")
    setModel(new DefaultTreeModel(treeRoot))
  } catch {
    case e: Throwable => println(" Exception: " + e.toString)
  }

  def walk(maybeNode: Node, parent: DefaultMutableTreeNode, indent: String): Unit = {
    Option(maybeNode).foreach(node => node.getNodeType match { 
      case ELEMENT_NODE =>{
        val s = node.getNodeName + " = " + node.getTextContent
//        println(indent + "ELEMENT_NODE, " + s)
        val childNode = new DefaultMutableTreeNode(s)
        parent.add(childNode)
        getOffspring(node).foreach(child => walk(child, childNode, indent + "  "))
      }
//      case TEXT_NODE => {
//        println(indent + "TEXT_NODE, " + node.getNodeValue)
//        val childNode = new DefaultMutableTreeNode(node.getNodeValue)
//        parent.add(childNode)
//      }
      case _ =>  // println(indent + "UNHANDLED node type (" + node.getNodeType + ")")
    })
  }

  def getOffspring(node: Node): List[Node] = {
    val nodeList = node.getChildNodes
    val indices: Range = 1 to nodeList.getLength
    indices.map(n => nodeList.item(n)).toList
  }
}


