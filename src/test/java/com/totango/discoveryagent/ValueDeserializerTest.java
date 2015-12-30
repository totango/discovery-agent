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
