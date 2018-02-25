package org.raisercostin.namek.nodes;
import org.raisercostin.jedi.InputLocation;
public class JNodes {
  public static JNode parseYaml(String content) {
    return SNodes.parseYaml(content).get();
  }
  public static JNode parseYamlViaJson(String content) {
    return SNodes.parseYamlViaJson(content).get();
  }
  public static JNode parseYamlViaSyaml(String content) {
    return SNodes.parseYamlViaSyaml(content).get();
  }

  public static RaptureJsonANode parseJson(String content) {
    return SNodes.parseJson(content).get();
  }

  public static JNode parseXml(String content) {
    return SNodes.parseXml(content).get();
  }

  public static JNode parseXmlViaRapture(String content) {
    return SNodes.parseXmlViaRapture(content).get();
  }

  public static JNode parseFreemind(String content) {
    return SNodes.parseFreemind(content).get();
  }

  public static JNode loadYaml(InputLocation location) {
    return SNodes.loadYaml(location).get();
  }

  public static JNode loadYamlViaSyaml(InputLocation location) {
    return SNodes.loadYamlViaSyaml(location).get();
  }

  public static JNode loadYamlViaJson(InputLocation location) {
    return SNodes.loadYamlViaJson(location).get();
  }

  public static JNode loadFreemind(InputLocation location) {
    return SNodes.loadXml(location).get();
  }

  public static JNode loadJson(InputLocation location) {
    return SNodes.loadJson(location).get();
  }

  public static JNode loadXml(InputLocation location) {
    return SNodes.loadXml(location).get();
  }

  public static JNode loadXmlViaRapture(InputLocation location) {
    return SNodes.loadXmlViaRapture(location).get();
  }
  
  public static RaptureJsonANode yamlToJson(JNode node) {
     return SNodes.yamlToJson(node).get();
  }
}
