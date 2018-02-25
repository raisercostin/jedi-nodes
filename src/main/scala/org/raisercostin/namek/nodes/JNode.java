package org.raisercostin.namek.nodes;

import java.util.Optional;

public interface JNode{
  default String asString() {
    return asClass(String.class);
  }   

  JNode child(String key);
  JNode addChildToJNode(String key, Object value);
  
  <T> T asClass(Class<T> clazz);
  SNode asSNode();
  Optional<String> asOptionalString();

  /**Checks that the node is valid. For json it might use http://json-schema.org, for xml xsd or xslt. The node could have a default validator.*/
  default void validate() {
    validate(getDefaultValidator());
  }
  default void validate(JNodeValidator validator) {
    validator.validate(this);
  }

  default JNodeValidator getDefaultValidator() {
    return new NoOpJNodeValidator();
  }
}
