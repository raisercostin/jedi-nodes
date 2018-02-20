package org.raisercostin.nodes.freemind

import org.raisercostin.jedi.FileLocation
import java.io.StringReader
import scala.util.Try
import scala.util.Failure
import org.yaml.snakeyaml.{ Yaml => Yaml2 }
import org.raisercostin.jedi.InputLocation
import org.raisercostin.syaml.Syaml
import scala.xml.Elem
import scala.collection.mutable.LinkedHashMap
import org.raisercostin.jedi.OutputLocation
import org.raisercostin.nodes.freemind.MindAttr
import org.raisercostin.nodes.freemind.MindNode
import org.raisercostin.nodes.freemind.MindMap
import org.raisercostin.nodes.freemind.MindMapEntity
import org.raisercostin.nodes.freemind.FreeMindLoader
import org.joda.time.DateTime
//import org.raisercostin.nodes.freemind.FreeMindXmlLoader
import org.raisercostin.syaml.InputLocationSyamlSource
import org.raisercostin.syaml.StringSyamlSource
import org.raisercostin.syaml.SyamlSource
import org.raisercostin.jedi.impl.SlfLogger

trait FreeMindPickler {
  def load(file: InputLocation): Try[Syaml]
  def save(syaml: Syaml, location: OutputLocation): Try[Any]
}

object FreeMind {
  val yamlXslt: FreeMindPickler = FreeMindViaXslt
  val yaml: FreeMindPickler = FreeMindAsObject
  val hocon = FreeMindHoconPickler
  val tree = FreeMindLoader
  //val xml = FreeMindXmlLoader
}

object FreeMindHoconPickler {
  import com.typesafe.config._
  def load(file: InputLocation): Config = {
    ConfigFactory.parseString(toHoconString(FreeMindLoader.load(file)))
  }
  def toHoconString(mind: MindMap): String = {
    def toHoconOld(obj: MindMapEntity): String = obj match {
      case m: MindMap =>
        if (m.children.size != 1)
          ???
        else
          "[" + toHoconOld(m.children.head) + "]"
      case x =>
        throw new IllegalArgumentException(s"Cannot convert type ${x.getClass}: $x")
    }
    def toHocon(mind: MindMap): String = {
      toHoconStringFromPlainYaml(FreeMindAsObject.toPlainYaml(mind))
    }
    def toHoconStringFromPlainYaml(obj: Any): String = obj match {
      case map: LinkedHashMap[String, AnyRef] => map.map{case (k,v) => escape(k)+":"+toHoconStringFromPlainYaml(v)}.mkString("{",",","}")
      case list: Seq[AnyRef]                  => list.map(toHoconStringFromPlainYaml).mkString("[",",","]")
      case s: String                          => escape(s)
      case n: Number                          => n.toString
      case b: java.lang.Boolean               => b.toString
      case d: DateTime                        => d.toString
      case null                               => null
      case other =>
        throw new IllegalArgumentException(s"found ${other.getClass.getName} for $other")
    }
    
    def escape(value:String) = ConfigUtil.quoteString(value)
    
    toHocon(mind)
  }
}
object FreeMindAsObject extends FreeMindPickler with SlfLogger {
  override def save(syaml: Syaml, location: OutputLocation): Try[Any] = ???
  override def load(file: InputLocation): Try[Syaml] = Try {
    toYaml(FreeMindLoader.load(file))(InputLocationSyamlSource(file))
  }

  //def fromXmlToString(node: scala.xml.Node): String = toYaml(fromXml(node)).dump
  //  def xml2yaml(file: InputLocation): String = {
  //    val content = file.readContent
  //    val xml = scala.xml.XML.loadString(content)
  //    import net.jcazevedo.moultingyaml._
  //    import net.jcazevedo.moultingyaml.DefaultYamlProtocol._
  //    fromXmlToString(xml).parseYaml.prettyPrint
  //  }

  //type of end lines https://stackoverflow.com/questions/3790454/in-yaml-how-do-i-break-a-string-over-multiple-lines
  def cleanText(text: String, ident: String): String = text match {
    case ""                    => "_"
    case "?"                   => """"?""""
    case t if t.contains("&")  => s""""$t""""
    case t if t.contains("\n") => "|\n" + ident + t.replaceAll("\n", "\n" + ident) + "\n\n"
    case _                     => text
  }
  def ifText(condition: Boolean, thenText: => String, elseText: => String): String = {
    if (condition) thenText else elseText
  }

  def toYaml(obj: MindMapEntity)(implicit source:SyamlSource): Syaml = Syaml(toPlainYaml(obj.text, obj))
  def toPlainYaml(obj: MindMapEntity): Any = toPlainYaml(obj.text, obj)
  def toPlainYaml(text: String, obj: MindMapEntity, inMap: Boolean = false): Any = {
    logger.debug(s"convert ($text,$obj,inMap=$inMap)")
    obj match {
      case m: MindMap =>
        if (m.children.size != 1)
          ???
        else
          toPlainYaml(m.children.head.text, m.children.head)
      case n: MindNode if n.children.size == 0 =>
        if (text.isEmpty)
          ???
        else
          text
      case n: MindNode if n.children.size == 1 =>
        if (text.isEmpty)
          if (inMap || n.children.head.children.size == 0)
            //refer to last child don't put it in a seq
            toPlainYaml(n.children.head.text, n.children.head)
          else
            Seq(toPlainYaml(n.children.head.text, n.children.head))
        else
          LinkedHashMap(text -> toPlainYaml(n.children.head.text, n.children.head))
      case n: MindNode if n.children.size > 1 =>
        val allChildrenHaveTextAndChildrensSoCouldBeMap = n.children.forall(n => n.children.size >= 1 && n.text.nonEmpty)

        val all = if (allChildrenHaveTextAndChildrensSoCouldBeMap)
          //all children have text so a map can be constructed
          LinkedHashMap() ++ n.children.map { v => v.text -> toPlainYaml("", v, true) }
        //else if (n.children.size == 1 && n.children(0).children.size == 0)
        //  n.children.head.text
        else
          Seq(n.children.map(v => toPlainYaml(v.text, v, true)): _*)

        if (text.isEmpty)
          all
        else
          LinkedHashMap(text -> all)
      case x =>
        throw new RuntimeException(s"Unknown entyty ${x.getClass}")
    }
  }
}

object FreeMindViaXslt extends FreeMindPickler {
  val xslt = <xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:string="java.lang.String">
               <xsl:output method="text" indent="yes" version="1.0" encoding="UTF-8" standalone="yes"/>
               <xsl:template match="/">
                 <xsl:apply-templates/>
               </xsl:template>
               <xsl:template name="linebreak">
                 <xsl:text>
                 </xsl:text>
               </xsl:template>
               <xsl:template match="map">
                 <xsl:apply-templates select="child::node"/>
               </xsl:template>
               <xsl:template match="node">
                 <xsl:param name="commaCount">0</xsl:param>
                 <xsl:variable name="text_string" select="string:new(@TEXT)"/>
                 <xsl:if test="$commaCount > 0">
                   <xsl:call-template name="writeCommas">
                     <xsl:with-param name="commaCount" select="$commaCount"/>
                   </xsl:call-template>
                 </xsl:if>
                 -<xsl:value-of select="string:replaceAll($text_string, '\n', '\\n')"/>
                 <xsl:apply-templates select="child::attribute"/>
                 <xsl:call-template name="linebreak"/>
                 <xsl:apply-templates select="child::node">
                   <xsl:with-param name="commaCount" select="$commaCount + 1"/>
                 </xsl:apply-templates>
               </xsl:template>
               <xsl:template match="attribute">[<xsl:value-of select="@NAME"/>=<xsl:value-of select="@VALUE"/>]</xsl:template>
               <xsl:template name="writeCommas">
                 <xsl:param name="commaCount">0</xsl:param>
                 <xsl:if test="$commaCount > 0">
                   <![CDATA[,]]><xsl:call-template name="writeCommas">
                                  <xsl:with-param name="commaCount" select="$commaCount - 1"/>
                                </xsl:call-template>
                 </xsl:if>
               </xsl:template>
             </xsl:stylesheet>

  def transform(srcXml: String, srcXslt: String): String = {
    import java.io.FileNotFoundException
    import java.io.FileOutputStream
    import java.io.IOException
    import java.io.ByteArrayOutputStream

    import javax.xml.transform.Transformer
    import javax.xml.transform.TransformerConfigurationException
    import javax.xml.transform.TransformerException
    import javax.xml.transform.TransformerFactory
    import javax.xml.transform.Source
    import javax.xml.transform.stream.StreamSource
    import javax.xml.transform.Result
    import javax.xml.transform.stream.StreamResult
    import java.io.StringReader
    val outputStream = new ByteArrayOutputStream()
    //val outputStream = new FileOutputStream("birds.out")

    //val source: Source = new StreamSource(myXml)
    val xmlSource: Source = new StreamSource(new StringReader(srcXml))
    val result: Result = new StreamResult(outputStream)
    //val xslSource: Source = new StreamSource(myXsl)
    val xslSource: Source = new StreamSource(new StringReader(srcXslt))
    def buildTransformer(xslSource: Source): Transformer = {
      val transformerFactory = TransformerFactory.newInstance()
      transformerFactory.newTransformer(xslSource)
    }

    val transformer = buildTransformer(xslSource)
    transformer.transform(xmlSource, result)
    outputStream.toString
  }

  override def load(file: InputLocation): Try[Syaml] = Try {
    val yamlFromXml = transform(file.readContent, xslt.toString).replaceAll(",", "  ")
    //println(yamlFromXml)
    Syaml.parse(yamlFromXml)(InputLocationSyamlSource(file))
  }
  override def save(syaml: Syaml, location: OutputLocation): Try[Any] = ???
}