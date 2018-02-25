package org.raisercostin.nodes

import scala.util.Try
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.raisercostin.jedi.Locations
import org.raisercostin.namek.nodes.SNode
import org.raisercostin.namek.nodes.SNodes
import org.raisercostin.nodes.freemind.FreeMind
import org.raisercostin.nodes.freemind.MindMap
import org.raisercostin.syaml.Syaml
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigList
import com.typesafe.config.ConfigRenderOptions
import org.junit.Ignore
import org.junit.Test
import org.junit.Assert._
import org.raisercostin.namek.nodes.SNodes
import org.raisercostin.namek.nodes.SNode
import scala.util.Try
import rapture.json.Json
import org.raisercostin.syaml.StringSyamlSource
import org.raisercostin.syaml.SyamlValue
import org.raisercostin.namek.nodes.RaptureJsonANode

class SNodeTest extends org.raisercostin.jedi.impl.SlfLogger {
  @Test def readMenu() {
    val data: Syaml = FreeMind.yaml.load(Locations.classpath("dcs-data.mm").asFile).get
    println(data.dump)
    val menu = data.query("dcs.menu.Solutions & Services")
    println("** menu  ***" + menu.dump)
    val config: Config = Locations.classpath("data.conf").usingReader(ConfigFactory.parseReader)
    println(config.toString())
  }
  @Test def readMenuViaHocon() {
    case class RichConfig(val config: Config) {
      def getConfig2(path: String): Config = {
        path.split("\\.").foldLeft[Any](config) {
          case (x: Config, key) =>
            println("search" + key)
            Try { x.getConfig(key) }.
              recover {
                case e: Throwable =>
                  x.getAnyRefList(key)
              }.get
          case (x: ConfigList, key) =>
            x.get(key.toInt)
        }
      }.asInstanceOf[Config]
    }
    implicit def toRichConfig(config: Config): RichConfig = RichConfig(config)

    val config: Config = FreeMind.hocon.load(Locations.classpath("dcs-data.mm"))
    println(config.root().render(ConfigRenderOptions.defaults().setOriginComments(false)))
    //    val menu0 = config.getConfig("dcs").
    //        getAnyRefList("menu").
    //        get(0).
    //        getConfig("Home/About Us")
    val menu1 = config.getConfig2("dcs.menu.0").getConfig("Home/About Us")
    println(menu1)
    val menu = config.getList("dcs.menu.Solutions \\& Services")
  }
  @Test def readMenuViaTree() {
    val tree: MindMap = FreeMind.tree.load(Locations.classpath("dcs-data.mm"))
  }
  @Test def readMenuViaXml() {
    val tree: SNode = SNodes.loadXmlViaScala(Locations.classpath("dcs-data.mm")).get
    //println(tree)
    //println(tree.query("map.node[@TEXT=dcs].node[@TEXT=menu].node[0]"))
    assertTrue(tree.isSuccess)
  }
  @Test def readMenuViaJavaXml() {
    val tree: SNode = SNodes.loadXmlViaJava(Locations.classpath("dcs-data.mm")).get
    //println(tree)
    assertTrue(tree.isSuccess)
    val menu: SNode = tree.query("//node[@TEXT='menu']", "//node[@TEXT='names']")
    println(menu)
    assertTrue(menu.isSuccess)
    assertEquals(3, menu.children.all.size)
  }
  @Test def readMenuViaBusinessNodeOverJavaXml() {
    val tree: SNode = SNodes.loadFreemind(Locations.classpath("dcs-data.mm")).get
    //println(tree)
    assertTrue(tree.isSuccess)
    val menu: SNode = tree.query("menu.names")
    println(menu)
    assertTrue(menu.isSuccess)
    assertEquals(3, menu.children.all.size)
  }
  @Test def parseNodeFromYaml() {
    val all: SNode = SNodes.parseYaml("""
      something:
        child1:
          at1: value1
          at2: value2
          at3: value3
        child2: value2-child2""").get
    println(all)
    val something = all.child("something") //.toSeq
    //    println(something)
    //    assertEquals(2, something.size)
    //    assertEquals(3, something(0).children.size)
    //    assertEquals("value1", something(0).asSyamlPair.valueAsYaml.get("at1").toStringValue)
    //data(0).value.asInstanceOf[Throwable].printStackTrace()
  }
  @Test def parseNodeFromJsonViaRapture() {
    val all: SNode = SNodes.parseJson("""
      {  
   "something":{  
      "child1":[  
         {  
            "at1":"value1"
         },
         {  
            "at2":"value2"
         },
         {  
            "at3":"value3"
         }
      ],
      "child2":"value2-child2"
   }
}""").get
    println("all=" + all)
    println("all2[something]=" + all.something)
    //import rapture.json._
    //import rapture.core._
    //import rapture.json.formatters.humanReadable._
    //import rapture.json.jsonBackends.spray._
    import rapture.core.modes.returnTry
    //    println("all3[something]="+all.something.as[Json])
    //    println("all4[something]="+all.something.child1.as[Json])
    val something = all.child("something") //.toSeq
    //    println(something)
    //    assertEquals(2, something.size)
    //    assertEquals(3, something(0).children.size)
    //    assertEquals("value1", something(0).asSyamlPair.valueAsYaml.get("at1").toStringValue)
    //data(0).value.asInstanceOf[Throwable].printStackTrace()
  }
  @Test def childrenOfSyamlNode() {
    val all: Syaml = Syaml.parse("""
      something:
        child1:
          at1: value1
          at2: value2
          at3: value3
        child2: value2-child2""")(StringSyamlSource("from a text"))
    println(all)
    val something = all.get("something").children.toSeq
    println(something)
    assertEquals(2, something.size)
    assertEquals(3, something(0).children.size)
    assertEquals("value1", something(0).asSyamlPair.valueAsYaml.get("at1").toStringValue)
    //data(0).value.asInstanceOf[Throwable].printStackTrace()
  }

  @Test def dynamicProps() {
    val all: Syaml = Syaml.parse("""
      something:
        child1:
          at1: value1
          at2: value2
          at3: value3
        child2: value2-child2""")(StringSyamlSource("from a text"))
    println("call dynamic:[" + all.$something + "]")
    println(for (a <- all.$something.child1.zipWithIndex) yield a)
    println(for (a <- all.$something.zipWithIndex) yield a)
    assertEquals("value2", all.$something.child1.zipWithIndex.toSeq.apply(1)._1.toStringValue)
    assertEquals("value2", all.something.child1.zipWithIndex.toSeq.apply(1)._1.toStringValue)
  }
  @Test def readAll() {
    val node:SNode = SNodes.loadYaml(Locations.classpath("test2-iterate.yaml")).get
    assertEquals("",node.benefits.children2)
  }
  @Test def testYamlAsOptionString() {
    val node:SNode = SNodes.loadYaml(Locations.memory("").writeContent("title: title1")).get
    assertEquals("",node.title.asClass(classOf[Option[String]]))
  }
  @Test def testJsonAsOptionString() {
    val node:RaptureJsonANode = SNodes.loadJson(Locations.memory("").writeContent("""{"title": "title1"}""")).get
    import rapture.json.jsonBackends.spray._
    assertEquals(Some("title1"),node.title.asRapture[Option[String]])
    assertEquals(Some("title1"),node.title.as[Option[String]])
    assertEquals(Some("title1"),node.title.asOptionString)
    assertEquals(Some("title1"),node.title.asClass(classOf[Option[String]]))
  }
}

//Test dynamic access to Syaml
object b {
  def main(args: Array[String]): Unit = {
    val s = Syaml.parse("user: costin\nemail: unknown")(StringSyamlSource("from a text"))
    println("1=" + s.email.valueOrElse(""))
    val s2 = s.email
    println(s"2=$s2")
    val s3 = s2.valueOrElse("")
    println(s"2=$s2")
    println(SyamlValue("unknown")(StringSyamlSource("from a text")).valueOrElse(""))
    //import org.raisercostin.sekyll.PageItem
    //import org.raisercostin.sekyll.Site
    //    val item = PageItem("aaa", Site())
    //    val v2: Syaml = item.rawSys.system; println(v2)
    //    val v3: Syaml = v2.build; println(v3)
    //    val v4: Syaml = v3.email; println(v4)
    //    val v5 = v4.valueOrElse(""); println(v5)
    //    println(item.rawSys.system.build.email.valueOrElse(""))
  }
}