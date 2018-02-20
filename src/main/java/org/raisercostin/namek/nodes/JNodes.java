package org.raisercostin.namek.nodes;
import org.raisercostin.jedi.InputLocation;
import org.raisercostin.jedi.Locations;
public class JNodes {
  // def loadYaml(location: InputLocation): JNode = SNodes.loadYaml(location).get
  // def loadJson(location: InputLocation): JNode = SNodes.loadJsonRapture(location).get
  // def loadXml(location: InputLocation): JNode = SNodes.loadXml(location).get
  // def loadXmlRapture(location: InputLocation): JNode = SNodes.loadXmlRapture(location).get
  // def loadFreemind(location: InputLocation): JNode = SNodes.loadFreemind(location).get
  public static JNode parseYaml(String content) {
    return SNodes.parseYaml(content).get();
  }

  public static JNode parseJson(String content) {
    return SNodes.parseJson(content).get();
  }

  public static JNode parseXml(String content) {
    return SNodes.parseXml(content).get();
  }

  public static JNode parseFreemind(String content) {
    return SNodes.parseFreemind(content).get();
  }

  public static JNode loadYaml(InputLocation location) {
    return SNodes.loadYaml(location).get();
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
}
