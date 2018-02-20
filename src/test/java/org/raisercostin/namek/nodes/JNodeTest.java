package org.raisercostin.namek.nodes;

import java.util.Map;

import org.junit.Test;
import org.raisercostin.namek.nodes.JNode;

public class JNodeTest {

  @Test
  public void testYaml() {
    JNode a = JNode.parseYaml("key1 : value1\nkey2 : value2");
    testJNode(a);
  }

  @Test
  public void testJson() {
    JNode a = JNode.parseJson("{'key1' : 'value1', 'key2' : 'value2'}".replaceAll("'","\""));
    testJNode(a);
  }

  private void testJNode(JNode node) {
    System.out.println(node.child("key1").as(Integer.class));
    //System.out.println(node.as(Map<String,String>.class));
  }
}
