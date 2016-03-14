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

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.totango.discoveryagent.model.Service;
import com.totango.discoveryagent.model.ServiceGroup;
import com.totango.discoveryagent.model.Value;

public class ConsulClient {
  
  private static final String SERVICE_HEALTH_URL_ENDPOINT = "http://%s:%d/v1/health/service/%s?passing";
  
  private static final String SERVICE_HEALTH_WAIT_URL_ENDPOINT = "http://%s:%d/v1/health/service/%s?index=%s&wait=%ds&passing";
  
  private static final String SERVICE_HEALTH_WITH_TAG_URL_ENDPOINT = "http://%s:%d/v1/health/service/%s?index=%s&tag=%s&wait=%ds&passing";
  
  private static final String KEY_VALUE_URL_ENDPOINT = "http://%s:%d/v1/kv/%s?passing";
  
  private static final String KEY_VALUE_WAIT_URL_ENDPOINT = "http://%s:%d/v1/kv/%s?index=%s&wait=%ds&passing";
  
  private static final String INDEX_HEADER_NAME = "X-Consul-Index";
  
  public static final Type SERVICE_LIST_TYPE = new TypeToken<List<Service>>(){}.getType();
  
  public static final Type VALUE_LIST_TYPE = new TypeToken<List<Value>>(){}.getType();
  
  private final OkHttpClient okClient;
  
  private final Gson gson;
  
  private final String host;
  
  private final int port;

  private int waitTimeInSec;

  public ConsulClient(OkHttpClient okClient, Gson gson, String host, int port, int waitTimeInSec) {
    this.okClient = okClient;
    this.gson = gson;
    this.host = host;
    this.port = port;    
    this.waitTimeInSec = waitTimeInSec;
  }
  
  public Optional<ServiceGroup> discoverService(String serviceName) throws IOException {
    String url = String.format(SERVICE_HEALTH_URL_ENDPOINT, host, port, serviceName);
    return getServiceGroup(url);
  }
  
  public Optional<ServiceGroup> discoverService(ServiceRequest request) throws IOException {
    String url = buildDiscoverServiceUrl(request);
    return getServiceGroup(url);
  }
  
  private String buildDiscoverServiceUrl(ServiceRequest request) {
    if (request.tag() == null) {
      return String.format(SERVICE_HEALTH_WAIT_URL_ENDPOINT, host, port, request.serviceName(), request.index(), waitTimeInSec);
    }
    
    return String.format(SERVICE_HEALTH_WITH_TAG_URL_ENDPOINT, host, port,
        request.serviceName(), request.index(), request.tag(), waitTimeInSec);
  }

  private Optional<ServiceGroup> getServiceGroup(String url) throws IOException {
    Request request = new Request.Builder()
      .url(url)
      .build();
    
    Response response = okClient.newCall(request).execute();
    if (response.isSuccessful()) {
      return toServiceGroup(response);
    }
    return Optional.empty();
  }
  
  private Optional<ServiceGroup> toServiceGroup(Response response) throws IOException {
    Optional<String> responseIndex = Optional.ofNullable(response.header(INDEX_HEADER_NAME));
    String message = response.body().string();
    List<Service> serviceList = gson.fromJson(message, SERVICE_LIST_TYPE);
    
    return Optional.ofNullable(serviceList)
      .map(services -> {
        return new ServiceGroup(services, responseIndex);
      });
  }
  
  public Optional<Value> keyValue(String key) throws IOException {
    String url = String.format(KEY_VALUE_URL_ENDPOINT, host, port, key);
    return value(url);
  }
  
  public Optional<Value> keyValue(String key, String index) throws IOException {
    String url = String.format(KEY_VALUE_WAIT_URL_ENDPOINT, host, port, key, index, waitTimeInSec);
    return value(url);
  }
  
  private Optional<Value> value(String url) throws IOException {
    Request request = new Request.Builder()
      .url(url)
      .build();
  
    Response response = okClient.newCall(request).execute();
    if (response.isSuccessful()) {
      List<Value> value = toValue(response);
      if (!value.isEmpty()) {
        return Optional.ofNullable(value.get(0));
      }
    }
    return Optional.empty();
  }
  
  private List<Value> toValue(Response response) throws IOException {
    String message = response.body().string();
    return gson.fromJson(message, VALUE_LIST_TYPE);
  }
  
}
