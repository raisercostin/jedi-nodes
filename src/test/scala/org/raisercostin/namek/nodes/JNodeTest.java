package org.raisercostin.namek.nodes;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class JNodeTest {

  @Test
  public void testYaml() {
    JNode a = JNodes.parseYaml("key1 : value1\nkey2 : value2");
    testJNode(a);
  }

  @Test
  public void testJson() {
    JNode a = JNodes.parseJson("{'key1' : 'value1', 'key2' : 'value2'}".replaceAll("'","\""));
    testJNode(a);
  }

  @Test
  public void testXml() {
    JNode a = JNodes.parseXml("<root1><key1>value1</key1></root1>");
    testJNode(a);
  }

  @Test
  public void testXmlViaRapture() {
    JNode a = JNodes.parseXmlViaRapture("<root1><key1>value1</key1></root1>");
    testJNode(a);
  }

  @Test
  public void testFreeMind() {
    JNode a = JNodes.parseFreemind("<map version=\"1.0.1\">\r\n" + 
        "<node CREATED=\"1519123949150\" ID=\"ID_794286514\" MODIFIED=\"1519123965329\" TEXT=\"root mind map\">\r\n" + 
        "<node CREATED=\"1519123951902\" ID=\"ID_61626386\" MODIFIED=\"1519123954464\" POSITION=\"right\" TEXT=\"key1\">\r\n" + 
        "<node CREATED=\"1519123955024\" ID=\"ID_1556257377\" MODIFIED=\"1519123956101\" TEXT=\"value1\"/>\r\n" + 
        "</node>\r\n" + 
        "<node CREATED=\"1519123956743\" ID=\"ID_1837259425\" MODIFIED=\"1519123957800\" POSITION=\"right\" TEXT=\"key2\">\r\n" + 
        "<node CREATED=\"1519123958204\" ID=\"ID_841316981\" MODIFIED=\"1519123959327\" TEXT=\"value2\"/>\r\n" + 
        "</node>\r\n" + 
        "</node>\r\n" + 
        "</map>".replaceAll("'","\""));
    testJNode(a);
  }

  private void testJNode(JNode node) {
    assertEquals("value1",node.child("key1").as(String.class));
    assertEquals("value1",node.child("key1").asString());
    node.validate();
    
    //String exported = node.save();
    //JNode node2 = node.parse(exported);
    //assertEquals(node,node2);
    //System.out.println(node.as(Map<String,String>.class));
  }
}
