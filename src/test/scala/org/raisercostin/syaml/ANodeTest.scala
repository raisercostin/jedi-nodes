package org.raisercostin.syaml

import org.junit.Ignore
import org.junit.Test
import org.junit.Assert._
import org.raisercostin.namek.nodes.Nodes
import org.raisercostin.namek.nodes.ANode
import scala.util.Try
import rapture.json.Json

class ANodeTest extends org.raisercostin.jedi.impl.SlfLogger {
  @Test def parseNodeFromYaml() {
    val all:ANode = Nodes.parseYaml("""
      something:
        child1:
          at1: value1
          at2: value2
          at3: value3
        child2: value2-child2""").get
    println(all)
    val something = all.child("something").children//.toSeq
//    println(something)
//    assertEquals(2, something.size)
//    assertEquals(3, something(0).children.size)
//    assertEquals("value1", something(0).asSyamlPair.valueAsYaml.get("at1").toStringValue)
    //data(0).value.asInstanceOf[Throwable].printStackTrace()
  }
  @Test def parseNodeFromJsonViaRapture() {
    val all:ANode = Nodes.parseJsonRapture("""
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
    println("all="+all)
    println("all2[something]="+all.something)
  //import rapture.json._
  //import rapture.core._
  //import rapture.json.formatters.humanReadable._
  //import rapture.json.jsonBackends.spray._
  import rapture.core.modes.returnTry
    println("all3[something]="+all.something.as[Json])
    println("all4[something]="+all.something.child1.as[Json])
    val something = all.child("something").children//.toSeq
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
    println("call dynamic:["+all.$something+"]")
    println(for(a<-all.$something.child1.zipWithIndex) yield a)
    println(for(a<-all.$something.zipWithIndex) yield a)
    assertEquals("value2",all.$something.child1.zipWithIndex.toSeq.apply(1)._1.toStringValue)
    assertEquals("value2",all.something.child1.zipWithIndex.toSeq.apply(1)._1.toStringValue)
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