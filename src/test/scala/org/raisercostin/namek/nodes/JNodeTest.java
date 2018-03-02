package org.raisercostin.namek.nodes;

import static org.junit.Assert.assertEquals;

import java.util.Optional;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.raisercostin.jedi.Locations;
import org.yaml.snakeyaml.scanner.ScannerException;

public class JNodeTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();
  @Test
  public void testValidYamlViaSyaml() {
    JNode a = JNodes.loadYamlViaSyaml(Locations.classpath("test1-valid.yaml"));
    //assertEquals("SyamlANode(SyamlMap(SyamlPair(key1,Animals are: dogs, cats)))",a.toString());
    assertEquals("SyamlANode(SyamlMap(Map(key1 -> Animals are: dogs, cats)))",a.toString());
  }
  @Test
  public void testInvalidYamlViaSyaml() {
    exception.expect(RuntimeException.class);
    exception.expectMessage("mapping values are not allowed here");
    exception.expectMessage("key1");
    JNode b = JNodes.loadYamlViaSyaml(Locations.classpath("test1-invalid.yaml"));
    assertEquals("",b.asString());
  }
  @Test
  public void testValidYamlViaJson() {
    JNode a = JNodes.loadYamlViaJson(Locations.classpath("test1-valid.yaml"));
    //assertEquals("SyamlANode(SyamlMap(SyamlPair(key1,Animals are: dogs, cats)))",a.toString());
    assertEquals("RaptureJsonANode(json\"\"\"{\"key1\":\"Animals are: dogs, cats\"}\"\"\")",a.toString());
  }
  @Test
  public void testInvalidYamlViaJson() {
    exception.expect(RuntimeException.class);
    exception.expectMessage("mapping values are not allowed here");
    exception.expectMessage("key1");
    JNode b = JNodes.loadYamlViaJson(Locations.classpath("test1-invalid.yaml"));
    assertEquals("",b.asString());
  }
  
  @Test
  public void testJson() {
    RaptureJsonANode a = JNodes.parseJson("{'key1' : 'value1', 'key2' : 'value2'}".replaceAll("'","\""));
    a.validate(JsonNodeValidator.fromLocation(Locations.classpath("schema1.json")));
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

  @Ignore
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

  @Test
  public void testYaml() {
    JNode a = JNodes.parseYamlViaSyaml("key1 : value1\nkey2 : value2");
    JNode b = JNodes.yamlToJson(a);
    JNodeValidator schema = JsonNodeValidator.fromLocation(Locations.classpath("schema1.json"));
    b.validate(schema);
    JNodeValidator yamlSchemaValidator = YamlNodeValidator.from(JsonNodeValidator.fromLocation(Locations.classpath("schema1.json")));
    a.validate(yamlSchemaValidator);
    testJNode(a);
    assertEquals(Optional.empty(),a.child("key3").asOptionalString());
    JNode c = a.addChildToJNode("key3",5);
    assertEquals(Optional.of(5),c.child("key3").asOptionalString());
  }

  private void testJNode(JNode node) {
    assertEquals("value1",node.child("key1").asClass(String.class));
    assertEquals("value1",node.child("key1").asString());
    assertEquals("value1",node.child("key1").asSNode().asString());
    assertEquals(Optional.<String>of("value1"),node.child("key1").asOptionalString());
    assertEquals(Optional.empty(),node.child("key3").asOptionalString());
    node.validate();
    
    //String exported = node.save();
    //JNode node2 = node.parse(exported);
    //assertEquals(node,node2);
    //System.out.println(node.as(Map<String,String>.class));
  }
}
