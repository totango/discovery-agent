package com.totango.discoveryagent.gson;

import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Optional;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.totango.discoveryagent.model.Value;

public class ValueDeserializer implements JsonDeserializer<Value> {

  @Override
  public Value deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    
    JsonObject jsonObj = (JsonObject) json;
    
    int createIndex = jsonObj.get("CreateIndex").getAsInt();
    int modifyIndex = jsonObj.get("ModifyIndex").getAsInt();
    int lockIndex = jsonObj.get("LockIndex").getAsInt();
    String key = jsonObj.get("Key").getAsString();
    int flags = jsonObj.get("Flags").getAsInt();
    JsonElement sessionJsonElement = jsonObj.get("Session");
    
    JsonElement valueJsonElement = jsonObj.get("Value");
    Optional<byte[]> value;
    if (valueJsonElement.isJsonNull()) {
      value = Optional.empty(); 
    } else {
      value = Optional.of(jsonObj.get("Value").getAsString().getBytes(Charset.forName("UTF-8")));
    }
    
    Optional<String> session = Optional.ofNullable(sessionJsonElement).map(element -> element.getAsString());

    return new Value(key, value, createIndex, modifyIndex, lockIndex, flags, session);
  }
}