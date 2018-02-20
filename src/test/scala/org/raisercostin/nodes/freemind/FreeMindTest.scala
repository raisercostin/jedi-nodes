package org.raisercostin.nodes.freemind

import org.junit.Test
import org.junit.Assert._
import org.raisercostin.jedi.Locations
import org.junit.Ignore
import org.raisercostin.syaml.Syaml
import scala.collection.Seq
import org.raisercostin.syaml.StringSyamlSource
import org.raisercostin.jedi.impl.SlfLogger

class FreeMind2SyamlTest extends SlfLogger{
  @Ignore @Test def readMindMap() {
    //val data: Syaml = FreeMindViaXslt.load(Locations.classpath("data.mm").asFile).get
    val data: Syaml = FreeMind.yaml.load(Locations.classpath("data.mm").asFile).get
    assertEquals("", data.query("data.menu").isSuccess)
  }
  @Test def readMindMapAsObject() {
    val data: Syaml = FreeMindAsObject.load(Locations.classpath("data.mm")).get
    assertEquals("org.raisercostin.syaml.SyamlMap", data.getClass().getName)
    //println(data.asMap.get.mkString("\n"))
    logger.debug(data.toString)
    assertTrue(data.query("data").isSuccess)
    assertFalse(data.query("data2").isSuccess)
    assertTrue(data.query("data","menu").nonEmpty)
    assertTrue(data.query("data.menu").nonEmpty)
  }

  @Test def convertAdvanced5bToSyaml() {
    val mindMap = node("convertNodes5bToSyaml", node("k1", node("v1")), node("k2", node("v2", node("v4"), node("v5"))), node("k3", node("v3")), node("k4"))
    val yaml = FreeMindAsObject.toYaml(mindMap)(StringSyamlSource("from a text"))
    println("convertNodes5bToSyaml="+yaml)
    assertEquals("v1", yaml.query("convertNodes5bToSyaml", "0", "k1").asString.get)
    assertEquals("v1", yaml.query("convertNodes5bToSyaml.0.k1").asString.get)
    assertEquals("v4,v5", yaml.query("convertNodes5bToSyaml", "1", "k2", "v2").asList[String].get.mkString(","))
    assertEquals("v4,v5", yaml.query("convertNodes5bToSyaml.1.k2.v2").asList[String].get.mkString(","))
  }

  @Ignore @Test def readYamlAsNodes() {
    val yamlString = FreeMindAsObject.load(Locations.classpath("data.mm")).get.dump
    println(yamlString)
  }

  //plain conversion
  def node(text: String, node: MindNode*): MindNode = MindNode(text, 0, 0, text, Seq(), node)

  @Test def convertNodes1_OneValue() {
    testPlain("v1", node("v1"))
  }
  @Test def convertNodes2_OneElementList() {
    testPlain("v1", node("v1"))
    //testPlain("List(v1)", node("", node("v1")))
    testPlain("v1", node("", node("v1")))
  }
  @Test def convertNodes3_OneElementTwoListsTest() {
    //testPlain("List(List(v1))", node("", node("", node("v1"))))
    testPlain("List(v1)", node("", node("", node("v1"))))
  }
  @Test def convertNodes4_2ElementsAsList() {
    testPlain("Map(v1 -> v2)", node("v1", node("v2")))
  }
  @Test def convertNodes5_2ElementsAsMap() {
    testPlain("List(v1, v2)", node("", node("v1"), node("v2")))
    testPlain("List(List(v1, v2))", node("",node("", node("v1"), node("v2"))))
    testPlain("List(List(List(v1, v2)))", node("", node("", node("", node("v1"), node("v2")))))

    testPlain("Map(k -> List(v1, v2))", node("k", node("v1"), node("v2")))
    testPlain("Map(k -> List(v1, v2))", node("k",node("", node("v1"), node("v2"))))
    testPlain("Map(k -> List(List(v1, v2)))", node("k", node("", node("", node("v1"), node("v2")))))
  }
  @Test def convertNodes6_3ElementsInMap() {
    testPlain("Map(x -> List(v1, v2))", node("x", node("v1"), node("v2")))
  }
  @Test def convertNodes7_3ElementsInListAndMap() {
    testPlain("List(k1, Map(k2 -> v2))", node("", node("k1"), node("k2", node("v2"))))
  }
  @Test def convertNodes8_3ElementsInList() {
    testPlain("List(v1, v2, v3)", node("", node("v1"), node("v2"), node("v3")))
  }
  @Test def convertNodes9_3ElementsInList() {
    testPlain("List(v1, List(v2, v3))", node("", node("v1"), node("", node("v2"), node("v3"))))
  }
  @Test def convertNodes10_4ElementsListOfMapOfList() {
    testPlain("List(v1, Map(k2 -> List(v2, v3)))", node("", node("v1"), node("k2", node("v2"), node("v3"))))
    testPlain("List(v1, Map(k2 -> List(v2, v3)))", node("", node("v1"), node("k2", node("", node("v2"), node("v3")))))
    testPlain("List(v1, Map(k2 -> List(List(v2, v3))))", node("", node("v1"), node("k2", node("", node("", node("v2"), node("v3"))))))
    testPlain("Map(k2 -> Map(v2 -> List(v4, v5)))",node("k2", node("v2", node("v4"), node("v5"))))
  }
  @Test def convertNodes11_4ElementsMap() {
    testPlain("Map(a -> b, c -> d)", node("", node("a", node("b")), node("c", node("d"))))
  }
  @Test def convertNodes12_4ElementsMapOfMap() {
    testPlain("Map(x -> Map(a -> b, c -> d))", node("x", node("a", node("b")), node("c", node("d"))))
  }
  @Test def convertNodes13_5ElementsMapOfLists() {
    testPlain("Map(a -> List(b, c), d -> e)", node("", node("a", node("b"), node("c")), node("d", node("e"))))
  }

  @Test def convertAdvanced4() {
    testPlain("Map(list - fake map -> List(Map(k1 -> v1), Map(k2 -> Map(v2 -> List(v4, v5))), Map(k3 -> v3), k4))", node("list - fake map", node("k1", node("v1")), node("k2", node("v2", node("v4"), node("v5"))), node("k3", node("v3")), node("k4")))
  }
  @Test def convertAdvanced4b() {
    testPlain("List(Map(k1 -> v1), Map(k2 -> Map(v2 -> List(v4, v5))), Map(k3 -> v3), k4)", node("", node("k1", node("v1")), node("k2", node("v2", node("v4"), node("v5"))), node("k3", node("v3")), node("k4")))
  }
  @Test def convertAdvanced5() {
    testPlain("Map(list - true map -> Map(k1 -> v1, k2 -> Map(v2 -> List(v4, v5)), k3 -> v3, k4 -> v4))", 
        node("list - true map", node("k1", node("v1")), node("k2", node("v2", node("v4"), node("v5"))), node("k3", node("v3")), node("k4", node("v4"))))
  }
  @Test def convertAdvanced5b() {
    testPlain("Map(k1 -> v1, k2 -> Map(v2 -> List(v4, v5)), k3 -> v3, k4 -> v4)", node("", node("k1", node("v1")), node("k2", node("v2", node("v4"), node("v5"))), node("k3", node("v3")), node("k4", node("v4"))))
  }
  @Test def convertAdvanced6b() {
    println("test6")
    testPlain("Map(k1 -> List(v1, v12), k2 -> Map(v2 -> List(v4, v5)), k3 -> v3, k4 -> v4)",
      node("", node("k1", node("v1"), node("v12")), node("k2", node("v2", node("v4"), node("v5"))), node("k3", node("v3")), node("k4", node("v4"))))
  }
  def testPlain(expected: Any, node: MindNode) = {
    logger.info(node.toString)
    val yaml = FreeMindAsObject.toPlainYaml(node)
    logger.info(yaml.toString)
    assertEquals(expected.toString, yaml.toString)
  }
}