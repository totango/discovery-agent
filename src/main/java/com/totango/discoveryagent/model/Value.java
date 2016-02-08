/**
 *
 * Copyright (C) 2015 Totango , Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.totango.discoveryagent.model;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import java.util.Base64.Decoder;
import java.util.Optional;

public class Value {

  private static final byte[] EMPTY_VALUE=  new byte[0];
  
  private final String key;
  
  private final Optional<byte[]> value;

  private final int createIndex;
  
  private final int modifyIndex;
  
  private final int lockIndex;
  
  private final Optional<String> session;
  
  private final int flags;

  public Value(String key, Optional<byte[]> value, int createIndex, int modifyIndex, int lockIndex,
      int flags, Optional<String> session) {
    this.value = value;
    this.createIndex = createIndex;
    this.modifyIndex = modifyIndex;
    this.lockIndex = lockIndex;
    this.key = key;
    this.flags = flags;
    this.session = session;
  }
  
  public Optional<byte[]> getValue() {
    return value;
  }
  
  public Optional<String> getValueAsString() {
    return value.map(val-> {
      Decoder decoder = Base64.getDecoder();
      return new String(decoder.decode(val), Charset.forName("UTF-8"));
    });
  }

  public int getCreateIndex() {
    return createIndex;
  }

  public int getModifyIndex() {
    return modifyIndex;
  }

  public int getLockIndex() {
    return lockIndex;
  }

  public String getKey() {
    return key;
  }

  public Optional<String> getSession() {
    return session;
  }

  public int getFlags() {
    return flags;
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Value that = (Value) o;

    return Objects.equals(key, that.key)
        && Arrays.equals(value.orElse(EMPTY_VALUE), that.value.orElse(EMPTY_VALUE))
        && createIndex == that.createIndex
        && modifyIndex == that.modifyIndex
        && lockIndex == that.lockIndex
        && flags == that.flags
        && Objects.equals(session, that.session);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value, createIndex, modifyIndex, lockIndex, key, session, flags);
  }
}