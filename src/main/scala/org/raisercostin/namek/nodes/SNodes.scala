package org.raisercostin.namek.nodes

import scala.reflect.runtime.{ universe => ru }
import ru._
import scala.util.Try
import scala.xml.Node
import scala.xml.NodeSeq

import org.raisercostin.jedi.InputLocation
import org.raisercostin.jedi.Locations
import org.raisercostin.syaml.InputLocationSyamlSource
import org.raisercostin.syaml.Syaml

import com.networknt.schema.JsonSchema
import com.networknt.schema.JsonSchemaFactory

import rapture.json.Json
import rapture.xml.Xml
import scala.language.dynamics
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory

object SNodes {
  def parseYaml(data: String): Try[RaptureJsonANode] = loadYaml(Locations.memory("a").writeContent(data))
  def parseYamlViaJson(data: String): Try[RaptureJsonANode] = loadYamlViaJson(Locations.memory("a").writeContent(data))
  def parseYamlViaSyaml(data: String): Try[SyamlANode] = loadYamlViaSyaml(Locations.memory("a").writeContent(data))
  def parseJson(data: String): Try[RaptureJsonANode] = loadJson(Locations.memory("a").writeContent(data))
  def parseXml(data: String): Try[SNode] = parseXmlViaRapture(data)
  def parseXmlViaRapture(data: String): Try[SNode] = loadXmlViaRapture(Locations.memory("a").writeContent(data))
  def parseXmlViaJava(data: String): Try[SNode] = loadXmlViaJava(Locations.memory("a").writeContent(data))
  def parseXmlViaScala(data: String): Try[SNode] = loadXmlViaRapture(Locations.memory("a").writeContent(data))
  def parseFreemind(data: String): Try[SNode] = loadFreemind(Locations.memory("a").writeContent(data))

  def loadYaml(location: InputLocation): Try[RaptureJsonANode] = loadYamlViaJson(location)
  def loadYamlViaSyaml(location: InputLocation): Try[SyamlANode] = {
    location.readContentAsText.map(x => Syaml.parse(x)(InputLocationSyamlSource(location))).map(x => SyamlANode(x))
  }
  def loadYamlViaJson(location: InputLocation): Try[RaptureJsonANode] = {
    location.readContentAsText.map(x => Syaml.parse(x)(InputLocationSyamlSource(location))).map(x => SyamlANode(x)).flatMap(yamlToJson)
  }
  def loadJson(location: InputLocation): Try[RaptureJsonANode] = {
    import rapture.core._
    import rapture.json._
    import rapture.json.jsonBackends.spray._
    location.readContentAsText.map(x => RaptureJsonANode(Json.parse(x)))
  }
  def loadXml(location: InputLocation): Try[SNode] = loadXmlViaRapture(location)
  def loadXmlViaRapture(location: InputLocation): Try[SNode] = {
    import rapture.core._
    import rapture.xml._
    import rapture.xml.xmlBackends.stdlib._
    //location.readContentAsText.map(x => RaptureJsonANode(Xml.parse(x)))
    location.readContentAsText.map(x => RaptureXmlNode(Xml.parse(x)))
  }
  def loadXmlViaJava(location: InputLocation): Try[SNode] = {
    Try { JavaXmlNode(loadJavaXml(location)) }
  }
  def loadXmlViaScala(location: InputLocation): Try[SNode] = {
    Try { ScalaElemNode(loadScalaXml(location)) }
  }
  def loadFreemind(location: InputLocation): Try[SNode] = {
    Try { MindMapJavaXmlNode(JavaXmlNode(loadJavaXml(location))) }
  }
  def loadJavaXml(file: InputLocation): org.w3c.dom.Document = {
    import javax.xml.parsers.DocumentBuilderFactory
    val docBuilderFactory = DocumentBuilderFactory.newInstance();
    val docBuilder = docBuilderFactory.newDocumentBuilder();
    file.usingInputStream(docBuilder.parse)
  }
  def loadScalaXml(file: InputLocation): scala.xml.Elem = {
    val content = file.readContent
    scala.xml.XML.loadString(content)
  }

  def yamlToJson(node: JNode): Try[RaptureJsonANode] = {
    val yamlReader = new ObjectMapper(new YAMLFactory())
    val obj = yamlReader.readValue(node.asInstanceOf[SyamlANode].syaml.toYamlString, classOf[Object])
    val jsonWriter = new ObjectMapper()
    val jsonString = jsonWriter.writeValueAsString(obj)
    parseJson(jsonString)
  }
}

import com.fasterxml.jackson.databind.ObjectMapper
import org.raisercostin.syaml.SyamlError
import rapture.data.Extractor
import scala.reflect.ClassTag
import scala.runtime.ScalaRunTime
trait SNode extends Dynamic with SNodeNoDynamic with Iterable[SNode] { self =>
  override def isEmpty: Boolean = isFailure
  override def iterator: Iterator[self.type] = asIterable.iterator

  //implement Iterable
  /**Adding `with Iterable[SNode]` breaks toString on case classes. So we redefine it.*/
  override def toString(): String = self.getClass match {
    case t if classOf[Product].isAssignableFrom(t) => ScalaRunTime._toString(self.asInstanceOf[Product])
    case _ => ???
  }

}
trait SNodeNoDynamic extends JNode { self =>

  //type NodeSelector = String
  //type NodeId = String
  //type ChildNodeType
  def selectDynamic(key: String): self.type = child(key).asInstanceOf[self.type]
  //def nonEmpty: Boolean = isSuccess
  def isFailure: Boolean = !isSuccess
  def isSuccess: Boolean
  def id: String /*NodeId*/ = "@" + hashCode

  override def child(key: String): self.type
  override def addChildToJNode(key: String, value: Any): JNode = addChild(key, value)
  //TODO key is not added
  def addChild(key: String, value: Any): SNode = self.asInstanceOf[SNode]

  //see more https://medium.com/@sinisalouc/overcoming-type-erasure-in-scala-8f2422070d20
  def as[T](implicit tag: WeakTypeTag[T]): T = ???
  override def asClass[T](clazz: Class[T]): T = {
    val t: ru.Type = getType(clazz)
    asType(t)
  }
  def getType[T](clazz: Class[T]): ru.Type = {
    val runtimeMirror = ru.runtimeMirror(clazz.getClassLoader)
    runtimeMirror.classSymbol(clazz).toType
  }
  def asType[T](ruType: ru.Type): T = this.asInstanceOf[T]
  def query(path: String /*NodeSelector*/ *): self.type = {
    path.foldLeft[self.type](self) {
      case (x: self.type, key) =>
        println("search " + key + " on " + x.id)
        x.child(key)
    }
  }

  def asOptionString: Option[String] = as[Option[String]]
  def asOptionBoolean: Option[Boolean] = as[Option[Boolean]]
  def asOptionInt: Option[Int] = as[Option[Int]]
  /**As scala node. Can be used from java for more powerful interface.*/
  override def asSNode(): SNode = this.asInstanceOf[SNode]
  /**Iterate over child nodes.*/
  def asIterable: Iterable[self.type] = ???
  /**Switch node to a statically checked type.*/
  def asStatic: SNodeNoDynamic = this
  override def asOptionalString(): java.util.Optional[String] = java.util.Optional.ofNullable(asOptionString.getOrElse(null))
  //  @deprecated def get(key: String): self.type = child(key)
  //  def or(key: String, value: =>self.type): self.type = {
  //    val res = child(key)
  //    if (res.isFailure)
  //      value
  //    else
  //      res
  //  }
  def getOr[T](key: String, value: => T)(implicit tag: WeakTypeTag[T]): T = child(key).as[Option[T]].getOrElse(value)
}
case class ANodeError(ex: Throwable, val path: Vector[Either[Int, String]] = Vector()) extends SNode { self =>
  //type ChildNodeType = ANodeError
  override def isSuccess: Boolean = false
  def child(key: String): self.type = this
  //def ex: Throwable = $root.value.asInstanceOf[Throwable]
  //  override def id = "ANodeError" + hashCode()
  //  override def isSuccess: Boolean = false
  //override def query(path: NodeSelector*): ANode = this
  //  def child(key: NodeSelector): ANodeList = this
  //  def children: ANodeList = this
  def all: Stream[SNode] = ???

  //def $deref($path: scala.collection.immutable.Vector[scala.util.Either[Int,String]]): T = this
  def $path: Vector[Either[Int, String]] = path
  override def asType[T](t: ru.Type): T = {
    this.asInstanceOf[T]
  }
}

//trait SimpleANode extends SNode {
//  override def query(path: NodeSelector*): ANode = {
//    path.foldLeft[ANode](this) {
//      case (x: ANode, key) =>
//        println("search " + key + " on " + x.id)
//        x.queryOne(key)
//    }
//  }
//}
//
//case class SimpleNodeList(all: Stream[ANode]) extends ANode {
//  override def isSuccess: Boolean = true
//  override def query(path: NodeSelector*): ANode = ???
////  def child(key: NodeSelector): ANodeList = ???
////  def children: ANodeList = ???
//  def $deref($path: scala.collection.immutable.Vector[scala.util.Either[Int,String]]): ANode = ???
//  def $path: Vector[Either[Int,String]] = ???
//}

case class SyamlANode(syaml: Syaml) extends SNode { self2 =>
  def isSuccess: Boolean = syaml.isSuccess

  //  if (syaml.isInstanceOf[SyamlError])
  //    throw new RuntimeException(s"When reading [${syaml.asInstanceOf[SyamlError].source}]:" + syaml.asInstanceOf[SyamlError].error.getMessage, syaml.asInstanceOf[SyamlError].error)
  //require(!syaml.isInstanceOf[SyamlError], s"$syaml is an error")
  def child(key: String): self2.type = SyamlANode(syaml.selectDynamic(key)).asInstanceOf[self2.type]
  //def syaml: Syaml = $root.value.asInstanceOf[Syaml]
  //override type T = self2.type
  //  def child(key: String): ANodeList = new SimpleNodeList(Stream(new SyamlANode(syaml.key)))
  //  def children: ANodeList = new SimpleNodeList(syaml.children.toStream.map(new SyamlANode(_)))
  //def $deref($path: Vector[Either[Int,String]]): ANode = new SyamlANode($root,path ++ $path)
  //def $path: Vector[Either[Int,String]] = path
  //override def as[T](implicit tag: WeakTypeTag[T]): T =
  override def asType[T](tag: ru.Type): T = {
    tag match {
      case t if t =:= ru.typeOf[String] =>
        //syaml.value.asInstanceOf[T]
        syaml.asString.get.asInstanceOf[T]
      case _ =>
        ???
    }
  }

  override def as[T](implicit tag: WeakTypeTag[T]): T = tag.tpe match {
    case t @ TypeRef(utype, usymbol, args) if t =:= ru.typeOf[String] =>
      //println(List(utype, usymbol, args).mkString(","))
      //asRapture[String].asInstanceOf[T]
      syaml.valueToOption.get.asInstanceOf[T]
    case t @ TypeRef(utype, usymbol, args) if t =:= ru.typeOf[Option[String]] =>
      //println(List(utype, usymbol, args).mkString(","))
      syaml.valueToOption.asInstanceOf[T]
    case t @ TypeRef(utype, usymbol, args) if t =:= ru.typeOf[Option[Boolean]] =>
      //println(List(utype, usymbol, args).mkString(","))
      if (syaml.value.isInstanceOf[Boolean])
        syaml.asBoolean.toOption.asInstanceOf[T]
      else
        Option.empty[Boolean].asInstanceOf[T]
    case t @ TypeRef(utype, usymbol, args) if t =:= ru.typeOf[Option[Int]] =>
      //println(List(utype, usymbol, args).mkString(","))
      if (syaml.value.isInstanceOf[Int])
        syaml.asInt.toOption.asInstanceOf[T]
      else
        Option.empty[Boolean].asInstanceOf[T]
    case t @ TypeRef(utype, usymbol, args) =>
      //import com.twitter.bijection._
      //import purecsv.safe._
      //CSVReader[Boolean].readCSVFromString("alice,1")
      //println(List(utype, usymbol, args).mkString(","))
      throw new RuntimeException(s"Can't convert [$syaml] to " + List(utype, usymbol, args).mkString(","))
  }
  override def asIterable: Iterable[self2.type] = syaml.children.map(x => SyamlANode(x)).asInstanceOf[Iterable[self2.type]]

  override def addChildToJNode(key: String, value: Any): JNode = addChild(key, value)

  override def addChild(key: String, value: Any): SNode =
    SyamlANode(syaml.withChild(key, value))
}

case class RaptureXmlNode(xml: rapture.xml.Xml) extends SNode { self =>
  def isSuccess: Boolean = true
  println(s"loaded $this")
  def child(key: String): self.type = RaptureXmlNode(xml.selectDynamic(key)).asInstanceOf[self.type]
  override def asType[T](t: ru.Type): T = {
    t match {
      case t if t =:= ru.typeOf[String] =>
        implicit val intExt = Xml.extractor[String].map(_.toInt)
        println(xml)
        xml.as[String].asInstanceOf[T]
      //as2[String].asInstanceOf[T]
      case _ =>
        ???
    }
  }

  import rapture.data.Extractor
  def asRapture[T](implicit ext: Extractor[T, rapture.xml.Xml]): T = {
    import rapture.core._
    import rapture.json._
    //    import rapture.core.modes.returnTry
    //json.child("","")
    //println("c2="+$extract(path).asInstanceOf[RaptureJsonANode].json)
    //$extract(path).asInstanceOf[RaptureJsonANode].json.as[String]
    //$extract(path).asInstanceOf[RaptureJsonANode].json.asInstanceOf[T]
    //val ext = implicitly(rapture.data.Extractor[String,Json])
    //println(json)
    xml.as[T]
  }
  override def as[T](implicit tag: WeakTypeTag[T]): T = tag.tpe match {
    case t @ TypeRef(utype, usymbol, args) if t =:= ru.typeOf[String] =>
      println(List(utype, usymbol, args).mkString(","))
      asRapture[String].asInstanceOf[T]
    case t @ TypeRef(utype, usymbol, args) if t =:= ru.typeOf[Option[String]] =>
      println(List(utype, usymbol, args).mkString(","))
      asRapture[Option[String]].asInstanceOf[T]
    case t @ TypeRef(utype, usymbol, args) =>
      println(List(utype, usymbol, args).mkString(","))
      throw new RuntimeException(s"Can't convert [$xml] to " + List(utype, usymbol, args).mkString(","))
  }
}
object YamlNodeValidator {
  def from(jsonValidator: JsonNodeValidator): YamlNodeValidator = YamlNodeValidator(jsonValidator)
}
case class YamlNodeValidator(jsonValidator: JsonNodeValidator) extends JNodeValidator {
  def validate(node: JNode) =
    jsonValidator.validate(SNodes.yamlToJson(node.asInstanceOf[SyamlANode]).get)
}
/**
 * Json Schema validation as defined at http://json-schema.org . Using the https://github.com/networknt/json-schema-validator library.
 */
object JsonNodeValidator {
  val factory = new JsonSchemaFactory()
  def fromString(schema: String): JsonNodeValidator = {
    JsonNodeValidator(factory.getSchema(schema))
  }

  //val schema = getJsonSchemaFromUrl("http://json-schema.org/calendar");
  def fromUrl(url: String): JsonNodeValidator = {
    JsonNodeValidator(factory.getSchema(new java.net.URL(url)))
  }

  def fromLocation(inputLocation: InputLocation): JsonNodeValidator = {
    JsonNodeValidator(inputLocation.usingInputStream(factory.getSchema))
  }
}
case class JsonNodeValidator(schema: JsonSchema) extends JNodeValidator {
  def validate(node: JNode) = {
    val result = schema.validate(getJsonNodeFromStringContent(node.asInstanceOf[RaptureJsonANode].json.toBareString))
    if (!result.isEmpty())
      throw new RuntimeException("Node invalid: " + result)
  }

  def getJsonNodeFromStringContent(content: String): JsonNode = {
    val mapper = new ObjectMapper()
    val result = mapper.readTree(content)
    println(result)
    result
  }
}

case class RaptureJsonANode(json: Json) extends SNode { self =>
  def isSuccess: Boolean = true
  def child(key: String): self.type = RaptureJsonANode(json.selectDynamic(key)).asInstanceOf[self.type]
  //def json:Json = $root.value.asInstanceOf[Json]
  //override type T = RaptureJsonANode
  //  def $deref($path: Vector[Either[Int, String]]): RaptureJsonANode = {
  //    ???
  //  }
  //def children: ANodeList = new SimpleNodeList(syaml.children.toStream.map(new SyamlANode(_)))
  //  def $deref(sp: Vector[Either[Int,String]]): T =
  //    if (sp.isEmpty) self
  //    else
  //      sp match {
  //        case Left(i) +: tail => ???//apply(i).$extract(tail)
  //        case Right(e) +: tail => new RaptureJsonANode(MutableCell(json.selectDynamic(e),tail))
  //      }

  //???//new RaptureJsonANode($root,path ++ $path)
  //def $path: Vector[Either[Int,String]] = path

  //  def as2[T](implicit ext: Extractor[T,Json]):T = {
  //    import rapture.core.modes.returnTry
  //    json.as[T]
  //  }
  //override def $root: rapture.data.MutableCell = MutableCell(node)
  //def $wrap(any: Any,$path: Vector[Either[Int,String]]): ANode = ???
  //import scala.reflect.runtime.universe._
  override def asType[T](t: ru.Type): T = {
    import rapture.json.jsonBackends.spray.implicitJsonAst
    import rapture.json.jsonBackends.spray._
    //implicit val ext:rapture.data.Extractor[T,rapture.json.Json] = Extractor.anyExtractor[Json]
    t match {
      case t if t =:= ru.typeOf[String] =>
        asRapture[String].asInstanceOf[T]
      case t if t =:= ru.typeOf[Option[String]] =>
        asRapture[Option[String]].asInstanceOf[T]
      case _ =>
        //as2[T].asInstanceOf[T]
        t.asInstanceOf[T]
    }
  }

  import rapture.data.Extractor
  def asRapture[T](implicit ext: Extractor[T, Json]): T = {
    import rapture.core._
    import rapture.json._
    //    import rapture.core.modes.returnTry
    //json.child("","")
    //println("c2="+$extract(path).asInstanceOf[RaptureJsonANode].json)
    //$extract(path).asInstanceOf[RaptureJsonANode].json.as[String]
    //$extract(path).asInstanceOf[RaptureJsonANode].json.asInstanceOf[T]
    //val ext = implicitly(rapture.data.Extractor[String,Json])
    //println(json)
    json.as[T]
  }
  import rapture.core._
  import rapture.json._
  import rapture.json.jsonBackends.spray.implicitJsonAst
  override def asIterable: Iterable[self.type] = json.as[Seq[Json]].map(x => RaptureJsonANode(x)).asInstanceOf[Iterable[self.type]]

  override def as[T](implicit tag: WeakTypeTag[T]): T = tag.tpe match {
    case t @ TypeRef(utype, usymbol, args) if t =:= ru.typeOf[String] =>
      println(List(utype, usymbol, args).mkString(","))
      asRapture[String].asInstanceOf[T]
    case t @ TypeRef(utype, usymbol, args) if t =:= ru.typeOf[Option[String]] =>
      println(List(utype, usymbol, args).mkString(","))
      asRapture[Option[String]].asInstanceOf[T]
    case t @ TypeRef(utype, usymbol, args) =>
      println(List(utype, usymbol, args).mkString(","))
      throw new RuntimeException(s"Can't convert [$json] to " + List(utype, usymbol, args).mkString(","))
  }
}

object AllExtractors {
  private case object NoConversion extends (Any => Nothing) {
    def apply(x: Any) = sys.error("No conversion")
  }

  // Just for convenience so NoConversion does not escape the scope.
  private def noConversion: Any => Nothing = NoConversion

  // and now some convenience methods that can be safely exposed:

  def canConvert[A, B]()(implicit f: A => B = noConversion) =
    (f ne NoConversion)

  def tryConvert[A, B](a: A)(implicit f: A => B = noConversion): Either[A, B] =
    if (f eq NoConversion) Left(a) else Right(f(a))

  def optConvert[A, B](a: A)(implicit f: A => B = noConversion): Option[B] =
    if (f ne NoConversion) Some(f(a)) else None
  //  import rapture.data.Extractor
  //  //def extract(ext: Extractor[T, D]) = ???
  //  def as[T](t: ru.Type): T = {
  //    import rapture.data.Extractor
  //    t match {
  //      case t if t =:= ru.typeOf[String] =>
  //        ??? //as2[String].asInstanceOf[T]
  //      case _ =>
  //        ???
  //    }
  //  }
}

/*-------------------------------------------------------------------------------------------------------*/
@deprecated("too simple xpath selections")
case class ScalaElemNode(value: Node) extends SNode { self =>
  def isSuccess: Boolean = true
  //  override def id = Option(value.\@("id")).filter(_.nonEmpty).getOrElse(super.id)
  override def child(key: String /*NodeSelector*/ ): self.type = Try { one(value.\(key)) }.
    map(x => ScalaElemNode(x)).recover { case x: Throwable => ANodeError(new IllegalArgumentException(s"When searching for child [$key]: " + x.getMessage, x)) }.get.asInstanceOf[self.type]

  //override def asStream: Stream[ANode] = value.\\("_").toStream.map(x => ScalaElemNode(x))

  private def one(seq: NodeSeq): Node = seq match {
    case a: NodeSeq if a.length == 1 => a.head
    case a: NodeSeq if a.length == 0 => throw new IllegalArgumentException(s"No child node.")
    case a: NodeSeq if a.length > 1 => throw new IllegalArgumentException(s"There are multiple child nodes.")
  }
}

/*-------------------------------------------------------------------------------------------------------*/

/*-------------------------------------------------------------------------------------------------------*/
case class MindMapJavaXmlNode(node: JavaXmlNode) extends SNode { self =>
  def isSuccess: Boolean = node.isSuccess
  override def child(key: String): self.type = MindMapJavaXmlNode(node.queryOne("//node[@TEXT='" + key + "']").asInstanceOf[JavaXmlNode]).asInstanceOf[self.type]
  def all: Stream[SNode] = ???
}
