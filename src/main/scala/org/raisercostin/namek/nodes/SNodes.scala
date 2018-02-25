package org.raisercostin.namek.nodes

import scala.reflect.runtime.{ universe => ru }
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
  def parseYaml(data: String): Try[SNode] = loadYaml(Locations.memory("a").writeContent(data))
  def parseJson(data: String): Try[RaptureJsonANode] = loadJson(Locations.memory("a").writeContent(data))
  def parseXml(data: String): Try[SNode] = parseXmlViaRapture(data)
  def parseXmlViaRapture(data: String): Try[SNode] = loadXmlViaRapture(Locations.memory("a").writeContent(data))
  def parseXmlViaJava(data: String): Try[SNode] = loadXmlViaJava(Locations.memory("a").writeContent(data))
  def parseXmlViaScala(data: String): Try[SNode] = loadXmlViaRapture(Locations.memory("a").writeContent(data))
  def parseFreemind(data: String): Try[SNode] = loadFreemind(Locations.memory("a").writeContent(data))

  def loadYaml(location: InputLocation): Try[SNode] = {
    location.readContentAsText.map(x => Syaml.parse(x)(InputLocationSyamlSource(location))).map(x => SyamlANode(x))
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

trait SNode extends Dynamic with JNode with Iterable[SNode] {
  //type NodeSelector = String
  //type NodeId = String
  //type ChildNodeType
  def selectDynamic(key: String): SNode = child(key).asInstanceOf[SNode]
  override def isEmpty: Boolean = isFailure
  //def nonEmpty: Boolean = isSuccess
  def isFailure: Boolean = !isSuccess
  def isSuccess: Boolean = true
  def id: String /*NodeId*/ = "@" + hashCode
  override def iterator: Iterator[SNode] = children2.iterator
  def children2: Iterable[SNode] = ???

  override def child(key: String): SNode
  override def as[T](clazz: Class[T]): T = {
    val t: ru.Type = getType(clazz)
    as(t)
  }
  def getType[T](clazz: Class[T]): ru.Type = {
    val runtimeMirror = ru.runtimeMirror(clazz.getClassLoader)
    runtimeMirror.classSymbol(clazz).toType
  }
  def as[T](ruType: ru.Type): T = this.asInstanceOf[T]
  def query(path: String /*NodeSelector*/ *): SNode = {
    path.foldLeft[SNode](this) {
      case (x: SNode, key) =>
        println("search " + key + " on " + x.id)
        x.child(key)
    }
  }
}

//import rapture.data.MutableCell
//import rapture.data.DataType
//import rapture.data.DataAst
//import rapture.core.Mode
//import rapture.data.TypeMismatchException
//import rapture.data.DataCompanion
//import rapture.data.MissingValueException
//import rapture.data.DataTypes
//import rapture.data.MutableCell
//import rapture.data.MutableCell
//import scala.reflect.ClassTag
//import scala.reflect.api.TypeTags
//
//trait DynamicData2 extends Dynamic { self2 =>
//  type T = ANode2
//  /** Assumes the Json object wraps a `Map`, and extracts the element `key`. */
//  def selectDynamic(key: String): T = $deref(Right(key) +: $path)
//  def self = selectDynamic("self")
//  def $deref($path: Vector[Either[Int, String]]): T
//  def $path: Vector[Either[Int, String]]
//  def $extract(sp: Vector[Either[Int, String]]): T =
//    if (sp.isEmpty) self
//    else
//      sp match {
//        case Left(i) +: tail  => ??? //apply(i).$extract(tail)
//        case Right(e) +: tail => selectDynamic(e).$extract(tail)
//      }
//}
//trait DataType2 { self2 =>
//  type T
//  type AstType = DataAst
//  val $root: MutableCell
//  implicit def $ast: AstType
//  def $path: Vector[Either[Int, String]]
//  def $normalize: Any = doNormalize(false)
//  def $wrap(any: Any, $path: Vector[Either[Int, String]] = Vector()): T
//  def $deref($path: Vector[Either[Int, String]] = Vector()): T
//  def $extract($path: Vector[Either[Int, String]]): T
//
//  def \(key: String): T = $deref(Right(key) +: $path)
//
//  def \\(key: String): T = $wrap($ast.fromArray(derefRecursive(key, $normalize)))
//
//  def toBareString: String
//
//  private def derefRecursive(key: String, any: Any): List[Any] =
//    if (!$ast.isObject(any)) Nil
//    else
//      $ast.getKeys(any).to[List].flatMap {
//        case k if k == key => List($ast.dereferenceObject(any, k))
//        case k             => derefRecursive(key, $ast.dereferenceObject(any, k))
//      }
//
//  protected def doNormalize(orEmpty: Boolean): Any = {
//    rapture.core.yCombinator[(Any, Vector[Either[Int, String]]), Any] { fn =>
//      {
//        case (j, Vector()) => j: Any
//        case (j, t :+ e) =>
//          fn(({
//            if (e.bimap(x => $ast.isArray(j), x => $ast.isObject(j))) {
//              try e.bimap($ast.dereferenceArray(j, _), $ast.dereferenceObject(j, _))
//              catch {
//                case TypeMismatchException(exp, fnd) => throw TypeMismatchException(exp, fnd)
//                case exc: Exception =>
//                  if (orEmpty) DataCompanion.Empty
//                  else {
//                    e match {
//                      case Left(e)  => throw MissingValueException(s"[$e]")
//                      case Right(e) => throw MissingValueException(e)
//                    }
//                  }
//              }
//            } else
//              throw TypeMismatchException(
//                if ($ast.isArray(j)) DataTypes.Array else DataTypes.Object,
//                e.bimap(l => DataTypes.Array, r => DataTypes.Object))
//          }, t))
//      }
//    }($root.value -> $path)
//  }
//
//  import rapture.data.`Data#as`
//  import rapture.data.Extractor
//
//  /** Assumes the Json object is wrapping a `T`, and casts (intelligently) to that type. */
//  def as[S](implicit ext: Extractor[S, T], mode: Mode[`Data#as`]): mode.Wrap[S, ext.Throws] =
//    ext.extract(this.asInstanceOf[T], $ast, mode)
//
//  import rapture.core.modes
//  def is[S](implicit ext: Extractor[S, T]): Boolean =
//    try {
//      ext.extract(this.asInstanceOf[T], $ast, modes.throwExceptions())
//      true
//    } catch {
//      case e: Exception => false
//    }
//
//  def apply(i: Int = 0): T = $deref(Left(i) +: $path)
//
//  override def equals(any: Any) =
//    try {
//      any match {
//        case any: DataType[_, _] => $normalize == any.$normalize
//        case _                   => false
//      }
//    } catch { case e: Exception => false }
//
//  override def hashCode = $root.value.hashCode ^ 3271912
//  implicit class EitherExtras[L, R](either: Either[L, R]) {
//    def bimap[T](leftFn: L => T, rightFn: R => T) = either match {
//      case Left(left)   => leftFn(left)
//      case Right(right) => rightFn(right)
//    }
//  }
//}
//trait ANode2 extends DynamicData2 /* with DataType2 */ with JNode {
//  type NodeSelector = String
//  type NodeId = String
//  //type T = ANode
//  //  def empty: Boolean = isFailure
//  //  def nonEmpty: Boolean = isSuccess
//  //  def isFailure: Boolean = !isSuccess
//  //  def isSuccess: Boolean = true
//  //  def id: NodeId = "@" + hashCode
//  def query(path: NodeSelector): ANode2 = query(path.split("\\."): _*)
//  def query(path: NodeSelector*): ANode2 = ???
//  def queryOne(path: NodeSelector): ANode2 = ???
//  def child(key: NodeSelector): ANode2 = selectDynamic(key)
//  //def children: ANode = asList
//
//  def asStream: Stream[ANode2] = ???
//  final def asList: List[ANode2] = asStream.toList
//  override def as[T](clazz: Class[T]): T = {
//    println(s"convert $this to ${clazz}")
//    ???
//  }
//  //
//  //  //type AstType <: DataAst
//  //  //def as[T](implicit ext: Extractor[T,Json]):T = ???
//  //  implicit def $ast: DataAst = ???
//  //  //val $root: rapture.data.MutableCell = ???//val $root: MutableCell, val $path: Vector[Either[Int, String]] = Vector())(implicit val $ast: XmlAst)
//  //  def $wrap(any: Any,$path: scala.collection.immutable.Vector[scala.util.Either[Int,String]]): org.raisercostin.namek.nodes.ANode = ???
//  //  def toBareString: String = ???
//  //  import scala.reflect.runtime.universe._
//  //  override def as[T](clazz:Class[T]):T = {
//  //    println(s"convert $this to ${clazz}")
//  //    import rapture.core.modes.returnTry
//  //    import rapture.json._
//  //    import rapture.core._
//  //    import rapture.json.formatters.humanReadable._
//  //    import rapture.json.jsonBackends.spray._
//  //    import modes.returnTry
//  //    import JodaAndJdkTimeConverters.implicits._
//  //    import JodaAndJdkTimeConverters._
//  //    Extractor.anyExtractor.extract(any, ast, mode)
//  //    //super[DataType2].as[T].asInstanceOf[T]
//  //    //???(implicit ext: rapture.data.Extractor[T,ANode.this.T], implicit mode: rapture.core.Mode[rapture.data.Data#as])mode.Wrap[T,ext.Throws].  Unspecified value parameters ext, mode.
//  //
//  //  }
//  //  override def as[T]()(implicit typeTag:TypeTag[T]):T = {
//  //    println(s"convert $this to ${typeTag}")
//  //    ???
//  //  }
//}
//
//trait ANodeList extends ANode {
//  def one: ANode = all match {
//    case Stream()    => throw new IllegalArgumentException(s"No child node.")
//    case Stream(res) => res
//    case _           => throw new IllegalArgumentException(s"There are multiple child nodes.")
//  }
//  def all: Stream[ANode]
//}
case class ANodeError(ex: Throwable, val path: Vector[Either[Int, String]] = Vector()) extends SNode {
  //type ChildNodeType = ANodeError
  override def isSuccess: Boolean = false
  def child(key: String): ANodeError = this
  //def ex: Throwable = $root.value.asInstanceOf[Throwable]
  //  override def id = "ANodeError" + hashCode()
  //  override def isSuccess: Boolean = false
  //override def query(path: NodeSelector*): ANode = this
  //  def child(key: NodeSelector): ANodeList = this
  //  def children: ANodeList = this
  def all: Stream[SNode] = ???

  //def $deref($path: scala.collection.immutable.Vector[scala.util.Either[Int,String]]): T = this
  def $path: Vector[Either[Int, String]] = path
  override def as[T](t: ru.Type): T = {
    this.asInstanceOf[T]
  }
}

trait SimpleANode extends SNode {
  //  override def query(path: NodeSelector*): ANode = {
  //    path.foldLeft[ANode](this) {
  //      case (x: ANode, key) =>
  //        println("search " + key + " on " + x.id)
  //        x.queryOne(key)
  //    }
  //  }
}
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
  if (syaml.isInstanceOf[SyamlError])
    throw new RuntimeException(s"When reading [${syaml.asInstanceOf[SyamlError].source}]:" + syaml.asInstanceOf[SyamlError].error.getMessage, syaml.asInstanceOf[SyamlError].error)
  //require(!syaml.isInstanceOf[SyamlError], s"$syaml is an error")
  def child(key: String): SyamlANode = SyamlANode(syaml.selectDynamic(key))
  //def syaml: Syaml = $root.value.asInstanceOf[Syaml]
  //override type T = self2.type
  //  def child(key: String): ANodeList = new SimpleNodeList(Stream(new SyamlANode(syaml.key)))
  //  def children: ANodeList = new SimpleNodeList(syaml.children.toStream.map(new SyamlANode(_)))
  //def $deref($path: Vector[Either[Int,String]]): ANode = new SyamlANode($root,path ++ $path)
  //def $path: Vector[Either[Int,String]] = path
  override def as[T](t: ru.Type): T = {
    t match {
      case t if t =:= ru.typeOf[String] =>
        syaml.value.asInstanceOf[T]
      case _ =>
        ???
    }
  }
  override def children2: Iterable[SNode] = syaml.children.map(x => SyamlANode(x))
}

case class RaptureXmlNode(xml: rapture.xml.Xml) extends SNode {
  println(s"loaded $this")
  def child(key: String): SNode = RaptureXmlNode(xml.selectDynamic(key))
  override def as[T](t: ru.Type): T = {
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
  def as2[T](implicit ext: Extractor[T, rapture.xml.Xml]): T = {
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

case class RaptureJsonANode(json: Json) extends SNode {
  def child(key: String): SNode = RaptureJsonANode(json.selectDynamic(key))
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
  override def as[T](t: ru.Type): T = {
    t match {
      case t if t =:= ru.typeOf[String] =>
        as2[String].asInstanceOf[T]
      case _ =>
        ???
    }
  }

  import rapture.data.Extractor
  def as2[T](implicit ext: Extractor[T, Json]): T = {
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
  import rapture.json.jsonBackends.spray._
  override def children2: Iterable[SNode] = json.as[Seq[Json]].map(x => RaptureJsonANode(x))
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
case class ScalaElemNode(value: Node) extends SimpleANode with SNode {
  //  override def id = Option(value.\@("id")).filter(_.nonEmpty).getOrElse(super.id)
  override def child(key: String /*NodeSelector*/ ): SNode = Try { one(value.\(key)) }.
    map(x => ScalaElemNode(x)).recover { case x: Throwable => ANodeError(new IllegalArgumentException(s"When searching for child [$key]: " + x.getMessage, x)) }.get
  //override def asStream: Stream[ANode] = value.\\("_").toStream.map(x => ScalaElemNode(x))

  private def one(seq: NodeSeq): Node = seq match {
    case a: NodeSeq if a.length == 1 => a.head
    case a: NodeSeq if a.length == 0 => throw new IllegalArgumentException(s"No child node.")
    case a: NodeSeq if a.length > 1  => throw new IllegalArgumentException(s"There are multiple child nodes.")
  }
}

/*-------------------------------------------------------------------------------------------------------*/

/*-------------------------------------------------------------------------------------------------------*/
case class MindMapJavaXmlNode(node: JavaXmlNode) extends SimpleANode with SNode {
  override def child(key: String): SNode = MindMapJavaXmlNode(node.queryOne("//node[@TEXT='" + key + "']").asInstanceOf[JavaXmlNode])
  def all: Stream[SNode] = ???
}
