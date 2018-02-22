package org.raisercostin.syaml

import java.io.Writer
import java.util.Date
import scala.annotation.tailrec
import scala.reflect.ClassTag
import scala.util.Failure
import scala.util.Try
import org.joda.time.DateTime
import scala.collection.Seq
import scala.util.Success
import scala.collection.immutable.ListMap
import scala.language.dynamics
import org.raisercostin.syaml.Syaml.WrappedMap
import org.raisercostin.jedi.InputLocation
import org.raisercostin.jedi.Locations
import org.raisercostin.jedi.impl.SlfLogger
import scala.runtime.ScalaRunTime
import scala.collection.mutable.LinkedHashMap

object Syaml extends org.raisercostin.jedi.impl.SlfLogger {
  type WrappedMap[K, V] = ListMap[K, V]
  def WrappedMap() = ListMap()

  def apply(original: => Any)(implicit source: SyamlSource): Syaml = Try {
    original match {
      case m: WrappedMap[AnyRef, _] =>
        SyamlMap(m)
      case m: LinkedHashMap[AnyRef, _] =>
        SyamlMap(ListMap() ++ m)
      case l: Seq[AnyRef] =>
        SyamlList(l)
      case (x, y) =>
        SyamlPair(x, y)
      case e: Throwable =>
        SyamlError(e)
      case s: String   => SyamlValue(s)
      case n: Number   => SyamlValue(n)
      case b: Boolean  => SyamlValue(b)
      case d: DateTime => SyamlValue(d)
      //case null        => null
      //case other =>
      //  Logger.warn("Unexpected YAML object of type " + other.getClass)
      //  other.toString
      case _: Unit =>
        SyamlEmpty
      case _ =>
        //logger.warn(s"found ${original.getClass.getName} for $original")
        SyamlError(new IllegalArgumentException(s"found ${original.getClass.getName} for $original"))
    }
  }.recover {
    case e: Throwable =>
      SyamlError(e)
    case x =>
      ???
  }.get

  def apply(original: Any, strict: Boolean = true)(implicit source: SyamlSource): Syaml = Syaml(original)

  def write(yaml: Syaml, writer: Writer) = {
    new org.yaml.snakeyaml.Yaml().dump(yaml.value, writer);
  }

  def parse(yaml: String)(implicit source: SyamlSource): Syaml = {
    import scala.collection.JavaConverters._
    def yamlToScala(obj: AnyRef): AnyRef = obj match {
      case map: java.util.Map[String, AnyRef] => WrappedMap() ++ map.asScala.toMap.mapValues(yamlToScala)
      case list: java.util.List[AnyRef]       => list.asScala.toList.map(yamlToScala)
      case s: String                          => s
      case n: Number                          => n
      case b: java.lang.Boolean               => b
      case d: Date                            => new DateTime(d)
      case null                               => null
      case other =>
        logger.warn("Unexpected YAML object of type " + other.getClass)
        other.toString
    }

    Syaml(yamlToScala(new org.yaml.snakeyaml.Yaml().load(yaml)))
  }
}

//case class DynamicSyaml(f: Syaml) {
//  def selectDynamic(name: String): DynamicSyaml = DynamicSyaml(f.get(name))
//}

trait Syaml extends org.raisercostin.jedi.impl.SlfLogger with Dynamic with Iterable[Syaml] {
  type YamlString = String
  implicit def source: SyamlSource
  def name: Option[Any] = None

  //dynamic part
  /**In case some attributes are the same with methods you can escape them with a `$` prefix like in `syaml.$children.image*/
  def selectDynamic(name: String): Syaml = get(name.stripPrefix("$"))

  //container part
  override def iterator: Iterator[Syaml] = children.iterator
  def empty: Boolean = isFailure
  override def nonEmpty: Boolean = isSuccess
  def isSuccess: Boolean = true
  def isFailure: Boolean = !isSuccess
  
  //navigability
  @deprecated("use get(key).asString") def getString(key: String): Try[String] = get(key).asString
  def children: Iterable[Syaml]
  def get(key: String): Syaml
  def or(key: String, value: =>Syaml): Syaml = {
    val res = get(key)
    if (res.isFailure)
      value
    else
      res
  }
  def getOr(key: String, value: =>Any): Syaml = or(key,Syaml(value))
  def query[T](keys: String): Syaml = query(keys.split("\\."): _*)
  def query[T](keys: String*): Syaml = {
    logger.debug(s"keys: [${keys.mkString(",")}]")
    @tailrec def queryRec(res: Syaml, param: Seq[String], full: String): Syaml = {
      if (res.isSuccess) {
        logger.debug(s"search with [${param.mkString(",")}] in ${res.toString.take(50)}... at $full")
        param match {
          case Seq()                => res
          case Seq(head)            => res.get(head)
          case Seq(head, tail @ _*) => queryRec(res.get(head), tail, full + "." + head.toString.take(20))
        }
      } else {
        res
      }
    }
    queryRec(this, keys.toSeq, "")
  }

  //just type conversion - shouldn't be needed
  def asString = as[String]
  def asInt = as[Int]
  def asBoolean:Try[Boolean] = as[Boolean]
  def asDate = as[DateTime]
  def asMap = as[Map[_, _]]
  def asList[T]: Try[List[T]] = as[List[T]]
  def as[T](implicit ct: ClassTag[T]): Try[T] = Try { value.asInstanceOf[T] }
  def asSyamlPair: SyamlPair = this.asInstanceOf[SyamlPair]
  def asSyamlMap: SyamlMap = this.asInstanceOf[SyamlMap]
  def get = this

  //extract value
  def value: Any
  def valueOrElse[T](alternative: =>T)(implicit ct: ClassTag[T]): T = if (isSuccess) value.asInstanceOf[T] else alternative
  def valueToOption: Option[Any] = if (isSuccess) Some(value) else None
  @deprecated def descriptionIfNotExists: String = if (isSuccess) "" else s"You can add a value in $source"
  @deprecated("use valueToOption") def toOption: Option[Any] = valueToOption
  @deprecated("use value.toString") def toStringValue: String = value.toString

  //tostring
  @deprecated("use toYamlString") def dump:YamlString = toYamlString
  def toYamlString: YamlString = {
    val yamlString = new org.yaml.snakeyaml.Yaml().dump(toJavaValue(value))
    //yamlString
    prettyPrint(yamlString)
  }
  //todo bug: destroys order of maps
  private def prettyPrint(yamlString: YamlString): YamlString = {
    import net.jcazevedo.moultingyaml._
    yamlString.parseYaml.prettyPrint
  }
  private def toJavaValue(obj: Any): Any = {
    import scala.collection.JavaConverters._
    def yamlToJava(obj: Any): Any = obj match {
      case map: WrappedMap[_, _] => map.mapValues(yamlToJava).asJava
      case list: Seq[AnyRef]     => list.map(yamlToJava).asJava
      case s: String             => s
      case n: Number             => n
      case b: java.lang.Boolean  => b
      case d: DateTime           => new Date(d.getMillis)
      case null                  => null
      case other =>
        logger.warn("Unexpected YAML object of type " + other.getClass)
        other.toString
    }

    yamlToJava(obj)
  }
}
case class SyamlValue(value: Any)(implicit val source: SyamlSource) extends Syaml {
  override def get(key: String): Syaml = Syaml(new RuntimeException(s"This is a value of type ${getClass.getName}. Cannot get something by key [$key]. The value is ${value.toString.take(100)}"))
  override def children: Iterable[Syaml] = Seq()
}
case class SyamlPair(key: Any, value: Any)(implicit val source: SyamlSource) extends Syaml {
  override def get(key: String): Syaml = //Syaml(new RuntimeException(s"This is a value of type ${getClass.getName}. Cannot get something by key [$key]. The value is ${value.toString.take(100)}"))
    Syaml(value).get(key)
  override def children: Iterable[Syaml] = Syaml(value).children
  override def name: Option[Any] = Some(key)
  def valueAsYaml: Syaml = Syaml(value)
  override def toString: String = s"SyamlPair($key,$value)"
}

/**Defines the source of syaml class to give better explanations about what it didn't worked.*/
trait SyamlSource
case class InputLocationSyamlSource(src: InputLocation) extends SyamlSource
case class ChangedSyamlSource(source: SyamlSource, key: AnyRef, value: Any) extends SyamlSource
case class EmptySyamlSource() extends SyamlSource
case class StringSyamlSource(explanation: String) extends SyamlSource {
  val place: Throwable = new RuntimeException("place of creation")
  //  override def toString(): String = {
  //    val sw = new java.io.StringWriter
  //    place.printStackTrace(new java.io.PrintWriter(sw))
  //    s"StringSyamlSource($explanation, ${sw.toString})"
  //  }
}
case class FromParentSyaml(parent: SyamlSource, location: String) extends SyamlSource

case class SyamlMap(value: ListMap[AnyRef, _])(implicit val source: SyamlSource) extends Syaml {
  override def get(key: String): Syaml = Syaml {
    Try { value.get(key).get }.
      recoverWith {
        case t: Throwable =>
          Failure(new IllegalArgumentException(s"Couldn't find value for key [$key] in [$source]. The keys are [${value.keys.map(_.toString.take(20)).mkString(",")}]"
          , t))
      }.get
  }
  override def children: Iterable[Syaml] = value.map { case (key, y) => Syaml(key -> y) }
  def withChild(key: AnyRef, newValue: Any): SyamlMap = SyamlMap(value + (key -> newValue))(ChangedSyamlSource(source, key, newValue))
}
case class SyamlList(value: Seq[_])(implicit val source: SyamlSource) extends Syaml {
  override def get(key: String): Syaml = Syaml(
    Try { key.toInt }.
      recover { case e: Throwable => throw new IllegalArgumentException(s"[$key] is not a index value.", e) }.
      map { index => value(index) }.
      recoverWith { case e: Throwable => Try { findChild(key) } }.
      get)

  def findChild(key: String): Any = {
    value.collectFirst {
      case s: WrappedMap[String, _] if s.size == 1 && s.contains(key) =>
        s.get(key).get
    }.get
  }
  override def children: Iterable[Syaml] = value.map(x => Syaml(x))
}
object SyamlEmpty extends Syaml {
  implicit val source: SyamlSource = EmptySyamlSource()
  override def getOr(key: String, value: =>Any): Syaml =
    Syaml(value)
  override def get(key: String): Syaml = this
  override def value: Any = ""
  override def asMap = Success(Map())
  override def children: Iterable[Syaml] = Seq()
}
case class SyamlError(error: Throwable)(implicit val source: SyamlSource) extends Syaml {
  override def isSuccess: Boolean = false
  override def get(key: String): Syaml = this
  override def value: Any = error
  override def children: Iterable[Syaml] = Seq()
  override def as[T](implicit ct: ClassTag[T]): Try[T] = Failure{error}
  override def toString: String = ScalaRunTime._toString(this)
  override def get = throw error
}
