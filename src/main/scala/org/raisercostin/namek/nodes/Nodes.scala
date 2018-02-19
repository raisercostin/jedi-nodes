package org.raisercostin.namek.nodes

import org.raisercostin.jedi.InputLocation
import org.raisercostin.syaml.Syaml
import scala.util.Try
import org.raisercostin.syaml.InputLocationSyamlSource
import org.raisercostin.jedi.Locations
import rapture.json.Json
import rapture.data.DynamicData
import rapture.json.JsonAst

object Nodes {
  import rapture.data.MutableCell
  def loadYaml(location: InputLocation): Try[ANode] = {
    location.readContentAsText.map(x => Syaml.parse(x)(InputLocationSyamlSource(location))).map(x=>SyamlANode(MutableCell(x)))
  }
  def parseYaml(data:String): Try[ANode] = loadYaml(Locations.memory("a").writeContent(data))
  
  def parseJsonRapture(data:String):Try[ANode] = loadJsonRapture(Locations.memory("a").writeContent(data))
  def loadJsonRapture(location:InputLocation):Try[ANode] = {
    import rapture.json._
    import rapture.core._
    import rapture.json.formatters.humanReadable._
    import rapture.json.jsonBackends.spray._
    import modes.returnTry
    import JodaAndJdkTimeConverters.implicits._
    import JodaAndJdkTimeConverters._
    location.readContentAsText.map(x=>RaptureJsonANode(MutableCell(Json.parse(x))))
  }

//    override def write(items: Iterable[CalendarEvent]) = Try {
//      file.writeContent(Json.format(Json(items.toSeq)))
//    }
//    override def read(): Iterable[CalendarEvent] = {
//      file.readContentAsText.map(Json.parse(_).as[Seq[CalendarEvent]]).get
//    }
}
import scala.language.dynamics
import rapture.data.MutableCell
import rapture.data.DataType
import rapture.data.DataAst
import rapture.data.Extractor
import rapture.core.Mode
import rapture.data.TypeMismatchException
import rapture.data.DataCompanion
import rapture.data.MissingValueException
import rapture.data.DataTypes
import rapture.data.MutableCell
import rapture.data.MutableCell

trait DynamicData2 extends Dynamic {self2=>
  type T = ANode
  /** Assumes the Json object wraps a `Map`, and extracts the element `key`. */
  def selectDynamic(key: String): T = $deref(Right(key) +: $path)
  def self = selectDynamic("self")
  def $deref($path: Vector[Either[Int, String]]): T
  def $path: Vector[Either[Int, String]]
  def $extract(sp: Vector[Either[Int, String]]): T =
    if (sp.isEmpty) self
    else
      sp match {
        case Left(i) +: tail => ???//apply(i).$extract(tail)
        case Right(e) +: tail => selectDynamic(e).$extract(tail)
      }
}
trait DataType2{ self2=>
  type T
  type AstType = DataAst
  val $root: MutableCell
  implicit def $ast: AstType
  def $path: Vector[Either[Int, String]]
  def $normalize: Any = doNormalize(false)
  def $wrap(any: Any, $path: Vector[Either[Int, String]] = Vector()): T
  def $deref($path: Vector[Either[Int, String]] = Vector()): T
  def $extract($path: Vector[Either[Int, String]]): T

  def \(key: String): T = $deref(Right(key) +: $path)

  def \\(key: String): T = $wrap($ast.fromArray(derefRecursive(key, $normalize)))

  def toBareString: String

  private def derefRecursive(key: String, any: Any): List[Any] =
    if (!$ast.isObject(any)) Nil
    else
      $ast.getKeys(any).to[List].flatMap {
        case k if k == key => List($ast.dereferenceObject(any, k))
        case k => derefRecursive(key, $ast.dereferenceObject(any, k))
      }

  protected def doNormalize(orEmpty: Boolean): Any = {
    rapture.core.yCombinator[(Any, Vector[Either[Int, String]]), Any] { fn =>
      {
        case (j, Vector()) => j: Any
        case (j, t :+ e) =>
          fn(({
            if (e.bimap(x => $ast.isArray(j), x => $ast.isObject(j))) {
              try e.bimap($ast.dereferenceArray(j, _), $ast.dereferenceObject(j, _))
              catch {
                case TypeMismatchException(exp, fnd) => throw TypeMismatchException(exp, fnd)
                case exc: Exception =>
                  if (orEmpty) DataCompanion.Empty
                  else {
                    e match {
                      case Left(e) => throw MissingValueException(s"[$e]")
                      case Right(e) => throw MissingValueException(e)
                    }
                  }
              }
            } else
              throw TypeMismatchException(
                  if ($ast.isArray(j)) DataTypes.Array else DataTypes.Object,
                  e.bimap(l => DataTypes.Array, r => DataTypes.Object)
              )
          }, t))
      }
    }($root.value -> $path)
  }

  import rapture.data.`Data#as`
  /** Assumes the Json object is wrapping a `T`, and casts (intelligently) to that type. */
  def as[S](implicit ext: Extractor[S, T], mode: Mode[`Data#as`]): mode.Wrap[S, ext.Throws] =
    ext.extract(this.asInstanceOf[T], $ast, mode)

  import rapture.core.modes
  def is[S](implicit ext: Extractor[S, T]): Boolean =
    try {
      ext.extract(this.asInstanceOf[T], $ast, modes.throwExceptions())
      true
    } catch {
      case e: Exception => false
    }

  def apply(i: Int = 0): T = $deref(Left(i) +: $path)

  override def equals(any: Any) =
    try {
      any match {
        case any: DataType[_, _] => $normalize == any.$normalize
        case _ => false
      }
    } catch { case e: Exception => false }

  override def hashCode = $root.value.hashCode ^ 3271912
  implicit class EitherExtras[L, R](either: Either[L, R]) {
    def bimap[T](leftFn: L => T, rightFn: R => T) = either match {
      case Left(left) => leftFn(left)
      case Right(right) => rightFn(right)
    }
  }
}
trait ANode extends DynamicData2 with DataType2{
  type NodeSelector = String
  type NodeId = String
  //type T = ANode
  def empty: Boolean = isFailure
  def nonEmpty: Boolean = isSuccess
  def isFailure: Boolean = !isSuccess
  def isSuccess: Boolean = true
  def id: NodeId = "@" + hashCode
  def query(path: NodeSelector): ANode = query(path.split("\\."): _*)
  def query(path: NodeSelector*): ANode = ???
  def queryOne(path: NodeSelector): ANode = ???
  def child(key: NodeSelector): ANode = selectDynamic(key)
  //def children: ANode = asList
  
  def asStream:Stream[ANode] = ???
  final def asList:List[ANode] = asStream.toList
  
  //type AstType <: DataAst
  //def as[T](implicit ext: Extractor[T,Json]):T = ???
  implicit def $ast: DataAst = ???
  //val $root: rapture.data.MutableCell = ???//val $root: MutableCell, val $path: Vector[Either[Int, String]] = Vector())(implicit val $ast: XmlAst)
  def $wrap(any: Any,$path: scala.collection.immutable.Vector[scala.util.Either[Int,String]]): org.raisercostin.namek.nodes.ANode = ???
  def toBareString: String = ???
}
//
//trait ANodeList extends ANode {
//  def one: ANode = all match {
//    case Stream()    => throw new IllegalArgumentException(s"No child node.")
//    case Stream(res) => res
//    case _           => throw new IllegalArgumentException(s"There are multiple child nodes.")
//  }
//  def all: Stream[ANode]
//}
case class ANodeError(val $root:MutableCell, val path: Vector[Either[Int, String]] = Vector()) extends ANode {
  def ex: Throwable = $root.value.asInstanceOf[Throwable]
  override def id = "ANodeError" + hashCode()
  override def isSuccess: Boolean = false
  override def query(path: NodeSelector*): ANode = this
//  def child(key: NodeSelector): ANodeList = this
//  def children: ANodeList = this
  def all: Stream[ANode] = ???
  
  def $deref($path: scala.collection.immutable.Vector[scala.util.Either[Int,String]]): T = this
  def $path: Vector[Either[Int,String]] = path
}

trait SimpleANode extends ANode {
  override def query(path: NodeSelector*): ANode = {
    path.foldLeft[ANode](this) {
      case (x: ANode, key) =>
        println("search " + key + " on " + x.id)
        x.queryOne(key)
    }
  }
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

case class SyamlANode(val $root: MutableCell, val path: Vector[Either[Int, String]] = Vector()) extends ANode {self2=>
  def syaml: Syaml = $root.value.asInstanceOf[Syaml]
    //override type T = self2.type
//  def child(key: String): ANodeList = new SimpleNodeList(Stream(new SyamlANode(syaml.key)))
//  def children: ANodeList = new SimpleNodeList(syaml.children.toStream.map(new SyamlANode(_)))
  def $deref($path: Vector[Either[Int,String]]): ANode = new SyamlANode($root,path ++ $path)
  def $path: Vector[Either[Int,String]] = path
}

case class RaptureJsonANode(val $root: MutableCell, val path: Vector[Either[Int, String]] = Vector()) extends ANode {
  def json:Json = $root.value.asInstanceOf[Json]
  //override type T = RaptureJsonANode
//  def $deref($path: Vector[Either[Int, String]]): RaptureJsonANode = {
//    ???
//  }
  //def children: ANodeList = new SimpleNodeList(syaml.children.toStream.map(new SyamlANode(_)))
  def $deref($path: Vector[Either[Int,String]]): T = new RaptureJsonANode($root,path ++ $path)
  def $path: Vector[Either[Int,String]] = path
  
//  def as2[T](implicit ext: Extractor[T,Json]):T = {
//    import rapture.core.modes.returnTry
//    json.as[T]
//  }
  //override def $root: rapture.data.MutableCell = MutableCell(node)
  //def $wrap(any: Any,$path: Vector[Either[Int,String]]): ANode = ???
}
