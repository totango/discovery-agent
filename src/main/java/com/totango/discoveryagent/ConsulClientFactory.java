package com.totango.discoveryagent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.totango.discoveryagent.gson.ServiceDeserializer;
import com.totango.discoveryagent.gson.ValueDeserializer;
import com.totango.discoveryagent.model.Service;
import com.totango.discoveryagent.model.Value;

public class ConsulClientFactory {

  private Gson gson;
  private OkHttpClient okHttpClient;

  public ConsulClientFactory() {
    initGson();
    this.okHttpClient = new OkHttpClient();
  }
  
  private void initGson() {
    GsonBuilder gsonBuilder = new GsonBuilder();
    gsonBuilder.registerTypeAdapter(Service.class, new ServiceDeserializer());
    gsonBuilder.registerTypeAdapter(Value.class, new ValueDeserializer());
    this.gson = gsonBuilder.create();
  }
  
  public ConsulClient client() {
    return new ConsulClient(okHttpClient, gson, "localhost", 8500);
  }
  
  public ConsulClient client(String host, int port) {
    return new ConsulClient(okHttpClient, gson, host, port);
  }
}
