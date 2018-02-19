package org.raisercostn.syaml

import scala.xml.Node
import scala.util.Try
import scala.xml.NodeSeq
import org.raisercostin.jedi.OutputLocation
import org.raisercostin.jedi.Locations
import org.raisercostin.namek.nodes._
import rapture.data.MutableCell


/*-------------------------------------------------------------------------------------------------------*/
@deprecated("too simple xpath selections")
case class ScalaElemNode(val $root:MutableCell, val path: Vector[Either[Int, String]] = Vector()) extends SimpleANode with ANode {
  def value: Node = $root.value.asInstanceOf[Node]
  override def id = Option(value.\@("id")).filter(_.nonEmpty).getOrElse(super.id)
//  override def child(key: NodeSelector): ANode = Try { one(value.\(key)) }.
//    map(x=>ScalaElemNode(x,path :+ Right[Int,String](key))).recover { case x: Throwable => ANodeError(MutableCell(new IllegalArgumentException(s"When searching for child [$key]: " + x.getMessage, x))) }.get
  //override def asStream: Stream[ANode] = value.\\("_").toStream.map(x => ScalaElemNode(x))

  private def one(seq: NodeSeq): Node = seq match {
    case a: NodeSeq if a.length == 1 => a.head
    case a: NodeSeq if a.length == 0 => throw new IllegalArgumentException(s"No child node.")
    case a: NodeSeq if a.length > 1  => throw new IllegalArgumentException(s"There are multiple child nodes.")
  }
  def $deref($path: Vector[Either[Int,String]]): ANode = ???//new MindMapJavaXmlNode(node.key,path ++ $path)
  def $path: Vector[Either[Int,String]] = path
}

/*-------------------------------------------------------------------------------------------------------*/

/*-------------------------------------------------------------------------------------------------------*/
case class MindMapJavaXmlNode(val $root:MutableCell, val path: Vector[Either[Int, String]] = Vector()) extends SimpleANode with ANode {
  def node: JavaXmlNode = $root.value.asInstanceOf[JavaXmlNode] 
  //override def queryOne(path: NodeSelector): ANode = MindMapJavaXmlNode(node.queryOne("//node[@TEXT='" + path + "']").asInstanceOf[JavaXmlNode])
  override def child(key: String): ANode = ???
  def all: Stream[ANode] = ???
  def $deref($path: Vector[Either[Int,String]]): ANode = ???//new MindMapJavaXmlNode(node.key,path ++ $path)
  def $path: Vector[Either[Int,String]] = path
}
