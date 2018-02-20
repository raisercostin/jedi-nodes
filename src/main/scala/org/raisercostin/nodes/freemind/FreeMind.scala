package org.raisercostin.nodes.freemind

import scala.util.Try
import scala.util.Failure
import org.raisercostin.jedi.InputLocation
import org.raisercostin.namek.nodes.SNodes

trait MindMapEntity {
  def text: String
}

case class MindNode(id: String, created: Long, modified: Long, text: String, attributes: Seq[MindAttr], children: Seq[MindNode]) extends MindMapEntity {
  override def toString(): String = s"MindNode($id,$text,${children.mkString(",", ",", "")})"
}
case class MindMap(children: Seq[MindNode]) extends MindMapEntity {
  def text = ""
}
case class MindAttr(key: String, value: String) extends MindMapEntity {
  def text = ???
}
object FreeMindLoader {
  def load(file: InputLocation): MindMap = fromXml(SNodes.loadScalaXml(file))

  private def fromXml(node: scala.xml.Node): MindMap = {
    MindMap((node \ "node").map(fromXmlNode))
  }
  private def fromXmlNode(node: scala.xml.Node): MindNode = Try {
    val id = (node \ "@ID").text
    val created = (node \ "@CREATED").text.toLong
    val modified = (node \ "@MODIFIED").text.toLong
    val text = (node \ "@TEXT").text
    MindNode(id, created, modified, text, (node \ "attribute").map(fromXmlAttr), (node \ "node").map(fromXmlNode))
  }.recoverWith { case x: Throwable => Failure(new RuntimeException(s"When parsing ${(node \ "id").text}", x)) }.get

  private def fromXmlAttr(node: scala.xml.Node): MindAttr = {
    MindAttr((node \ "@NAME").text, (node \ "@VALUE").text)
  }
}
