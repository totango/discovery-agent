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

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.totango.discoveryagent.gson.ServiceDeserializer;
import com.totango.discoveryagent.gson.ValueDeserializer;
import com.totango.discoveryagent.model.Service;
import com.totango.discoveryagent.model.Value;

public class ConsulClientFactory {

  private static final int MAX_WAIT_TIME_IN_SEC =  600;
  
  private static final int DEFAULT_WAIT_TIME_IN_SEC =  300;
  
  private static final int READ_TIMEOUT_DELTA =  10;
  
  private static final String DEFAULT_HOST =  "localhost";
  
  private static final int DEFAULT_PORT =  8500;
  
  private String host = DEFAULT_HOST;
  
  private int port = DEFAULT_PORT;
  
  private int waitTimeInSec = DEFAULT_WAIT_TIME_IN_SEC;
  
  private Gson createGson() {
    GsonBuilder gsonBuilder = new GsonBuilder();
    gsonBuilder.registerTypeAdapter(Service.class, new ServiceDeserializer());
    gsonBuilder.registerTypeAdapter(Value.class, new ValueDeserializer());
    return gsonBuilder.create();
  }
  
  public ConsulClient client() {
    if (waitTimeInSec > MAX_WAIT_TIME_IN_SEC || waitTimeInSec < 1) {
      throw new IllegalArgumentException("Wait timeout should be between 1 to 600 (1s to 10m)");
    }
    
    Gson gson = createGson();
    OkHttpClient.Builder builder = new OkHttpClient.Builder();
    builder.readTimeout(waitTimeInSec + READ_TIMEOUT_DELTA, TimeUnit.SECONDS);
    builder.connectTimeout(waitTimeInSec + READ_TIMEOUT_DELTA, TimeUnit.SECONDS);
    OkHttpClient okHttpClient = builder.build();
    
    return new ConsulClient(okHttpClient, gson, host, port, waitTimeInSec);
  }
  
  public ConsulClientFactory host(String host) {
    this.host = host;
    return this;
  }
  
  public ConsulClientFactory port(int port) {
    this.port = port;
    return this;
  }
  
  public ConsulClientFactory waitTimeInSec(int waitTimeInSec) {
    this.waitTimeInSec = waitTimeInSec;
    return this;
  }
}
