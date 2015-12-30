package com.totango.discoveryagent;

import java.util.Objects;
import java.util.function.Consumer;

@FunctionalInterface
public interface ThrowableConsumer<T> {

  /**
   * Performs this operation on the given argument.
   *
   * @param t the input argument
   */
  void accept(T t) throws Exception;
  
  default Consumer<T> andThen(Consumer<? super T> after) {
    Objects.requireNonNull(after);
    return (T t) -> { 
      try {
        accept(t);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      after.accept(t); };
  }
}
