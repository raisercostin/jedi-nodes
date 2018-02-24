package org.raisercostin.namek.nodes;

public interface JNode{
  default String asString() {
    return as(String.class);
  }   

  JNode child(String key);
  <T> T as(Class<T> clazz);

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
