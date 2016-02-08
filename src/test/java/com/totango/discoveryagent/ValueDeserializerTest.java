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
package com.totango.discoveryagent;

import static com.totango.discoveryagent.ResourceLoader.load;
import static org.junit.Assert.assertEquals;

import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.totango.discoveryagent.gson.ValueDeserializer;
import com.totango.discoveryagent.model.Value;

public class ValueDeserializerTest {

  public static final Type VALUE_LIST_TYPE = new TypeToken<List<Value>>(){}.getType();
  
  private static final String KEY_VALUE_JSON = load("v1_kv_zip_1.json");
  private static final String KEY_VALUE_NULL_JSON = load("v1_kv_zip_null_value.json");
  
  private static final Value ZIP_FULL_VALUE = new Value("zip", Optional.of("dGVzdA==".getBytes(Charset.forName("UTF-8"))),
      100, 200, 200, 0, Optional.of("adf4238a-882b-9ddc-4a9d-5b6758e4159e"));
  
  private static final Value ZIP_NULL_VALUE = new Value("zip", Optional.empty(),
      100, 200, 200, 0, Optional.of("adf4238a-882b-9ddc-4a9d-5b6758e4159e"));
  
  @Test
  public void emptyJsonArrayShouldReturnEmptyList() {
    GsonBuilder gsonBuilder = new GsonBuilder();
    gsonBuilder.registerTypeAdapter(Value.class, new ValueDeserializer());
    Gson gson = gsonBuilder.create();
    
    List<Value> values = gson.fromJson("[]", VALUE_LIST_TYPE);
    assertEquals(0, values.size());
  }
  
  @Test
  public void jsonNullValueShouldReturnValueWithOptionalValue() {
    GsonBuilder gsonBuilder = new GsonBuilder();
    gsonBuilder.registerTypeAdapter(Value.class, new ValueDeserializer());
    Gson gson = gsonBuilder.create();
    
    List<Value> values = gson.fromJson(KEY_VALUE_NULL_JSON, VALUE_LIST_TYPE);
    assertEquals(ZIP_NULL_VALUE, values.get(0));
  }
  
  @Test
  public void jsonWithValueShouldReturnFullValueObject() {
    GsonBuilder gsonBuilder = new GsonBuilder();
    gsonBuilder.registerTypeAdapter(Value.class, new ValueDeserializer());
    Gson gson = gsonBuilder.create();
    
    List<Value> values = gson.fromJson(KEY_VALUE_JSON, VALUE_LIST_TYPE);
    assertEquals(ZIP_FULL_VALUE, values.get(0));
  }
}
