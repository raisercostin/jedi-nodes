package org.raisercostin.namek.nodes

import org.raisercostin.jedi.OutputLocation
import org.raisercostin.jedi.Locations
import org.raisercostin.namek.nodes._
import rapture.data.MutableCell
import scala.Vector

case class JavaXmlNode(value:org.w3c.dom.Node) extends SNode {self=>
  import JavaXmlNode._
  import javax.xml.xpath.XPathFactory
  import org.w3c.dom.NodeList
  import javax.xml.xpath.XPathConstants
  import javax.xml.transform.TransformerFactory
  import javax.xml.transform.OutputKeys
  import javax.xml.transform.dom.DOMSource
  import javax.xml.transform.stream.StreamResult
  import java.io.OutputStreamWriter
  import org.w3c.dom.Node
  import javax.xml.xpath.XPathFactory
  val xPathfactory = XPathFactory.newInstance();

  def queryOne(path: String): SNode = {
    val xpath = xPathfactory.newXPath()
    val expr = xpath.compile(path)
    val nl: NodeList = expr.evaluate(value, XPathConstants.NODESET).asInstanceOf[NodeList]
    val stream: Stream[SNode] = Stream.from(0, nl.getLength).map(i => JavaXmlNode(nl.item(i)))
    stream.head
  }
//  override def queryOneList(path: NodeSelector): ANodeList = {
//    val xpath = xPathfactory.newXPath()
//    val expr = xpath.compile(path)
//    val nl: NodeList = expr.evaluate(value, XPathConstants.NODESET).asInstanceOf[NodeList]
//    val stream: Stream[ANode] = Stream.from(0, nl.getLength).map(i => JavaXmlNode(nl.item(i)))
//    SimpleNodeList(stream)
//  }
  override def child(key: String): self.type = queryOne(key).asInstanceOf[self.type]
  //override def children: ANode = queryOne("*")
  //override def id = value.Option(value.\@("id")).filter(_.nonEmpty).getOrElse(super.id)
  //  def child(key: NodeSelector): ANode = Try { one(value.\(key)) }.
  //    map(ScalaElemNode).recover { case x: Throwable => ANodeError(new IllegalArgumentException(s"When searching for child [$key]: " + x.getMessage, x)) }.get
  //  def children: Stream[ANode] = value.\\("_").toStream.map(x => ScalaElemNode(x))
  //
  private def one(seq: NodeList): Node = seq.getLength match {
    case 1          => seq.item(0)
    case 0          => throw new IllegalArgumentException(s"No child node.")
    case x if x > 1 => throw new IllegalArgumentException(s"There are multiple child nodes.")
  }

  override def toString: String = {
    val mem = Locations.memory("")
    print(mem)
    mem.readContent
  }
  def print(out: OutputLocation) = {
    val tf = TransformerFactory.newInstance();
    val transformer = tf.newTransformer();
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

    out.usingOutputStream(o => transformer.transform(new DOMSource(value),
      new StreamResult(new OutputStreamWriter(o, "UTF-8"))))
  }
  def all: Stream[SNode] = ???
//
//  def $deref($path: Vector[Either[Int,String]]): ANode = ???//new MindMapJavaXmlNode(node.key,path ++ $path)
//  def $path: Vector[Either[Int,String]] = path
}
