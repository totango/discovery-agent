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
package com.totango.discoveryagent.gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.totango.discoveryagent.model.Service;

public class ServiceDeserializer implements JsonDeserializer<Service> {

  @Override
  public Service deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    JsonObject jsonObj = (JsonObject) json;
    
    JsonObject nodeObject = (JsonObject)jsonObj.get("Node");
    String node = nodeObject.get("Node").getAsString();
    String address = nodeObject.get("Address").getAsString();
    
    JsonObject serviceObject = (JsonObject)jsonObj.get("Service");
    String id = serviceObject.get("ID").getAsString();
    String name = serviceObject.get("Service").getAsString();
    int port = serviceObject.get("Port").getAsInt();
    
    JsonArray tagsJsonArray = serviceObject.get("Tags").getAsJsonArray();
    List<String> tags = new ArrayList<String>(tagsJsonArray.size());
    tagsJsonArray.forEach(element -> {
      tags.add(element.getAsString());
    });
    
    return new Service(node, address, id, name, tags, port);
  }
}
