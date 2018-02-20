package org.raisercostin.namek.nodes;

import org.raisercostin.jedi.InputLocation;
import org.raisercostin.jedi.Locations;

public interface JNode {
  static JNode parseYaml(String content) {
    return loadYaml((InputLocation)Locations.memory("a").writeContent(content));
  }
  static JNode parseJson(String content) {
    return loadJson((InputLocation)Locations.memory("a").writeContent(content));
  }

  static JNode loadYaml(InputLocation location) {
    return JavaNodes.loadYaml(location);
  }

  static JNode loadJson(InputLocation location) {
    return JavaNodes.loadJson(location);
  }
  JNode child(String key);
  <T> T as(Class<T> clazz);
//   = {
//      return JavaNodes.as(this,clazz);
//  }
}
