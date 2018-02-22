package org.raisercostin.namek.nodes;

public interface JNode{
  default String asString() {
    return as(String.class);
  }   

  JNode child(String key);
  <T> T as(Class<T> clazz);
}
