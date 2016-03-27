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
import static com.totango.discoveryagent.ServiceRequest.request;
import static org.junit.Assert.assertEquals;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import org.junit.Test;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.totango.discoveryagent.model.Service;
import com.totango.discoveryagent.model.ServiceGroup;
import com.totango.discoveryagent.model.Value;

public class ConsulClientTest {

  private static final Service SERVICE1 = new Service("pong-service-1", "192.168.25.111", "pong",
      "pong", Arrays.asList("jvm"), "192.168.25.111", 9877);

  private static final Service SERVICE2 = new Service("pong-service-2", "192.168.25.112", "pong",
      "pong", Arrays.asList("jvm"), "192.168.25.112", 9877);

  private static final String SERVICE_HEALTH_2_NODES_JSON = load("v1_health_service_pong-2-services.json");

  private static final String KEY_VALUE_JSON = load("v1_kv_zip_1.json");

  private static final Value ZIP_VALUE = new Value("zip", 
      Optional.of("dGVzdA==".getBytes(Charset.forName("UTF-8"))), 100, 200, 200, 0,
      Optional.of("adf4238a-882b-9ddc-4a9d-5b6758e4159e"));

  private final ServiceRequest serviceRequest = request().forService("whatever").build();

  @Test
  public void discoverServiceWithServiceNameShouldRetrunTheService() throws Exception {
    withMockedResponse(new MockResponse().setBody(SERVICE_HEALTH_2_NODES_JSON), (
        ConsulClient consulClient, MockWebServer server) -> {

      Optional<ServiceGroup> response = consulClient.discoverService(SERVICE1.getServiceName());

      String path = server.takeRequest().getPath();
      String serviceName = path.substring(path.lastIndexOf('/') + 1, path.indexOf('?'));
      assertEquals("Service name doesn't match", serviceName, SERVICE1.getServiceName());
      
      List<Service> expected = Arrays.asList(SERVICE1, SERVICE2);
      assertEquals(expected, response.get().getServices());
    });
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void waitTimeoutShouldBeGreaterThan0() {
    ConsulClientFactory consulClientFactory = new ConsulClientFactory();
    consulClientFactory.waitTimeInSec(0).client();
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void waitTimeoutShouldBeLessThan601() {
    ConsulClientFactory consulClientFactory = new ConsulClientFactory();
    consulClientFactory.waitTimeInSec(601).client();
  }
  
  @Test
  public void discoverServiceWithoutIndexShouldUseIndexZero() throws Exception {
    withMockedResponse(new MockResponse().setResponseCode(200),
        (ConsulClient consulClient, MockWebServer server) -> {

      consulClient.discoverService(serviceRequest);

      String path = server.takeRequest().getPath();
      String serviceName = path.substring(path.lastIndexOf('/') + 1, path.indexOf('?'));
      assertEquals("Service name doesn't match", serviceName, serviceRequest.serviceName());

      final Map<String, String> keyValueMap = pathKeyValue(path);
      assertEquals("Index doesn't match", "0", keyValueMap.get("index"));
    });
  }

  @Test
  public void discoverServiceWithIndexShouldUseTheProvidedIndex() throws Exception {
    withMockedResponse(new MockResponse().setResponseCode(200),
        (ConsulClient consulClient, MockWebServer server) -> {

      ServiceRequest requestWithIndex = request().forService("whatever").lastUpdateIndex("10")
          .build();

      @SuppressWarnings("unused")
      Optional<ServiceGroup> discoverService = consulClient.discoverService(requestWithIndex);

      String path = server.takeRequest().getPath();
      String serviceName = path.substring(path.lastIndexOf('/') + 1, path.indexOf('?'));
      assertEquals("Service name doesn't match", serviceName, serviceRequest.serviceName());

      final Map<String, String> keyValueMap = pathKeyValue(path);
      assertEquals("Index doesn't match", "10", keyValueMap.get("index"));
      assertEquals("Index doesn't match", "1s", keyValueMap.get("wait"));
    });
  }
  
  @Test
  public void discoverServiceWithIndexAndWaitShouldUseTheProvidedIndexAndTheWait() throws Exception {
    withMockedResponse(new MockResponse().setResponseCode(200), 300,
        (ConsulClient consulClient, MockWebServer server) -> {

      ServiceRequest requestWithIndex = request().forService("whatever").lastUpdateIndex("1")
          .build();

      @SuppressWarnings("unused")
      Optional<ServiceGroup> discoverService = consulClient.discoverService(requestWithIndex);

      String path = server.takeRequest().getPath();
      String serviceName = path.substring(path.lastIndexOf('/') + 1, path.indexOf('?'));
      assertEquals("Service name doesn't match", serviceName, serviceRequest.serviceName());

      final Map<String, String> keyValueMap = pathKeyValue(path);
      
      assertEquals("Index doesn't match", "1", keyValueMap.get("index"));
      assertEquals("Index doesn't match", "300s", keyValueMap.get("wait"));
    });
  }

  @Test
  public void discoverServiceWithTagShouldUseTheProvidedTag() throws Exception {
    withMockedResponse(new MockResponse().setResponseCode(200),
        (ConsulClient consulClient, MockWebServer server) -> {

      ServiceRequest requestWithIndex = request().forService("whatever").withTag("master").build();

      consulClient.discoverService(requestWithIndex);

      String path = server.takeRequest().getPath();
      String serviceName = path.substring(path.lastIndexOf('/') + 1, path.indexOf('?'));
      assertEquals("Service name doesn't match", serviceName, serviceRequest.serviceName());

      final Map<String, String> keyValueMap = pathKeyValue(path);
      assertEquals("Tag doesn't match", "master", keyValueMap.get("tag"));
      assertEquals("Index doesn't match", "1s", keyValueMap.get("wait"));
    });
  }
  
  @Test
  public void discoverServiceWithTagAndWaitShouldUseTheProvidedTagAndWait() throws Exception {
    withMockedResponse(new MockResponse().setResponseCode(200), 300,
        (ConsulClient consulClient, MockWebServer server) -> {

      ServiceRequest requestWithIndex = request().forService("whatever").withTag("master").build();

      consulClient.discoverService(requestWithIndex);

      String path = server.takeRequest().getPath();
      String serviceName = path.substring(path.lastIndexOf('/') + 1, path.indexOf('?'));
      assertEquals("Service name doesn't match", serviceName, serviceRequest.serviceName());

      final Map<String, String> keyValueMap = pathKeyValue(path);
      assertEquals("Tag doesn't match", "master", keyValueMap.get("tag"));
      assertEquals("Index doesn't match", "300s", keyValueMap.get("wait"));
    });
  }

  @Test
  public void discoverServiceShouldReturnNoneForBadResponseCode() throws Exception {
    withMockedResponse(new MockResponse().setResponseCode(500),
        (ConsulClient consulClient, MockWebServer server) -> {
          
      Optional<ServiceGroup> response = consulClient.discoverService(serviceRequest);
      assertEquals("Bad response code should return None", response, Optional.empty());
    });
  }

  @Test
  public void discoverServiceShouldReturnOptionalForBadResponseBody() throws Exception {
    withMockedResponse(new MockResponse().setBody(""),
        (ConsulClient consulClient, MockWebServer server) -> {

      Optional<ServiceGroup> response = consulClient.discoverService(serviceRequest);
      assertEquals("Bad response body should return None", response.isPresent(), false);
    });
  }

  @Test
  public void discoverServiceShouldReturnResutForGoodResponse() throws Exception {
    withMockedResponse(new MockResponse().setBody(SERVICE_HEALTH_2_NODES_JSON), (
        ConsulClient consulClient, MockWebServer server) -> {

      Optional<ServiceGroup> response = consulClient.discoverService(serviceRequest);
      List<Service> expected = Arrays.asList(SERVICE1, SERVICE2);
      assertEquals(expected, response.get().getServices());
    });
  }

  @Test
  public void keyValueShouldReturnEmptyIfKeyIsNotAvailable() throws Exception {
    withMockedResponse(new MockResponse().setBody("[]"), (ConsulClient consulClient,
        MockWebServer server) -> {

      Optional<Value> value = consulClient.keyValue("key1");
      assertEquals(Optional.empty(), value);
    });
  }

  @Test
  public void keyValueShouldReturnListOfResults() throws Exception {
    withMockedResponse(new MockResponse().setBody(KEY_VALUE_JSON), (ConsulClient consulClient,
        MockWebServer server) -> {

      Optional<Value> value = consulClient.keyValue("key1");
      assertEquals(ZIP_VALUE, value.get());
    });
  }
  
  @Test
  public void keyValueWithIndexShouldUseTheIndex() throws Exception {
    withMockedResponse(new MockResponse().setBody("[]"), (ConsulClient consulClient,
        MockWebServer server) -> {

      consulClient.keyValue("key1", "10");
      String path = server.takeRequest().getPath();
      final Map<String, String> keyValueMap = pathKeyValue(path);
      assertEquals("Tag doesn't match", "10", keyValueMap.get("index"));
      assertEquals("Index doesn't match", "1s", keyValueMap.get("wait"));
    });
  }
  
  @Test
  public void keyValueWithIndexAndWaitShouldUseTheIndexAndWait() throws Exception {
    withMockedResponse(new MockResponse().setBody("[]"), 300, (ConsulClient consulClient,
        MockWebServer server) -> {

      consulClient.keyValue("key1", "10");
      String path = server.takeRequest().getPath();
      final Map<String, String> keyValueMap = pathKeyValue(path);
      assertEquals("Tag doesn't match", "10", keyValueMap.get("index"));
      assertEquals("Index doesn't match", "300s", keyValueMap.get("wait"));
    });
  }
  
  @Test
  public void datacentersShouldReturnList() throws Exception {
    withMockedResponse(new MockResponse().setBody("[\"dc1\",\"dc2\"]"), 200, (ConsulClient consulClient,
        MockWebServer server) -> {

      List<String> datacenters = consulClient.datacenters();
      assertEquals("Datacenters don't match", Lists.newArrayList("dc1", "dc2"), datacenters);
    });
  }

  private void withMockedResponse(MockResponse res, int waitTimeInSec,
      ThrowableBiConsumer<ConsulClient, MockWebServer> func) throws Exception {
    
    MockWebServer server = new MockWebServer();
    server.enqueue(res);
    server.start();

    func.accept(consulClient(server, waitTimeInSec), server);
    server.shutdown();
  }
  
  private void withMockedResponse(MockResponse res,
      ThrowableBiConsumer<ConsulClient, MockWebServer> func) throws Exception {
    
    withMockedResponse(res, 1, func);
  }

  private ConsulClient consulClient(MockWebServer server, int waitTimeInSec) {
    ConsulClientFactory consulClientFactory = new ConsulClientFactory();
    return consulClientFactory
        .host(server.getHostName())
        .port(server.getPort())
        .waitTimeInSec(waitTimeInSec)
        .client();
  }

  @FunctionalInterface
  public interface ThrowableBiConsumer<T, U> {

    void accept(T t, U u) throws Exception;

    default BiConsumer<T, U> andThen(BiConsumer<? super T, ? super U> after) {
      Objects.requireNonNull(after);

      return (l, r) -> {
        try {
          accept(l, r);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        after.accept(l, r);
      };
    }
  }
  
  private Map<String, String> pathKeyValue(String path) {
    String query = path.substring(0, path.indexOf("&passing")).split("\\?")[1];
    final Map<String, String> keyValueMap = Splitter.on('&').withKeyValueSeparator("=").split(query);
    return keyValueMap;
  }

}
