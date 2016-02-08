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
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

import org.junit.Test;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.totango.discoveryagent.model.Service;
import com.totango.discoveryagent.model.ServiceGroup;
import com.totango.discoveryagent.model.Value;

public class ConsulClientTest {

  private static final Service SERVICE1 = new Service("pong-service-1", "192.168.25.111", "pong",
      "pong", Arrays.asList("jvm"), 9877);

  private static final Service SERVICE2 = new Service("pong-service-2", "192.168.25.112", "pong",
      "pong", Arrays.asList("jvm"), 9877);

  private static final String SERVICE_HEALTH_2_NODES_JSON = load("v1_health_service_pong-2-services.json");

  private static final String KEY_VALUE_JSON = load("v1_kv_zip_1.json");

  private static final Value ZIP_VALUE = new Value("zip", 
      Optional.of("dGVzdA==".getBytes(Charset.forName("UTF-8"))), 100, 200, 200, 0,
      Optional.of("adf4238a-882b-9ddc-4a9d-5b6758e4159e"));

  private final ServiceRequest serviceRequest = request().forService("whatever").build();

  @Test
  public void discoverServiceWithoutIndexShouldUseIndexZero() throws Exception {
    withMockedResponse(new MockResponse().setResponseCode(500),
        (ConsulClient consulClient, MockWebServer server) -> {

      consulClient.discoverService(serviceRequest);

      String path = server.takeRequest().getPath();
      String serviceName = path.substring(path.lastIndexOf('/') + 1, path.indexOf('?'));
      assertEquals("Service name doesn't match", serviceName, serviceRequest.serviceName());

      String index = path.substring(path.lastIndexOf("=") + 1, path.indexOf('&'));
      assertEquals("Index doesn't match", "0", index);
    });
  }

  @Test
  public void discoverServiceWithIndexShouldUseTheProvidedIndex() throws Exception {
    withMockedResponse(new MockResponse().setResponseCode(500),
        (ConsulClient consulClient, MockWebServer server) -> {

      ServiceRequest requestWithIndex = request().forService("whatever").lastUpdateIndex("1")
          .build();

      Optional<ServiceGroup> discoverService = consulClient.discoverService(requestWithIndex);

      String path = server.takeRequest().getPath();
      String serviceName = path.substring(path.lastIndexOf('/') + 1, path.indexOf('?'));
      assertEquals("Service name doesn't match", serviceName, serviceRequest.serviceName());

      String index = path.substring(path.lastIndexOf("=") + 1, path.indexOf('&'));
      assertEquals("Index doesn't match", "1", index);
    });
  }

  @Test
  public void discoverServiceWithTagShouldUseTheProvidedIndex() throws Exception {
    withMockedResponse(new MockResponse().setResponseCode(500),
        (ConsulClient consulClient, MockWebServer server) -> {

      ServiceRequest requestWithIndex = request().forService("whatever").withTag("master").build();

      consulClient.discoverService(requestWithIndex);

      String path = server.takeRequest().getPath();
      String serviceName = path.substring(path.lastIndexOf('/') + 1, path.indexOf('?'));
      assertEquals("Service name doesn't match", serviceName, serviceRequest.serviceName());

      String tag = path.substring(path.lastIndexOf("=") + 1, path.lastIndexOf('&'));
      assertEquals("Tag doesn't match", "master", tag);
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

  private void withMockedResponse(MockResponse res,
      ThrowableBiConsumer<ConsulClient, MockWebServer> func) throws Exception {
    MockWebServer server = new MockWebServer();
    server.enqueue(res);
    server.start();

    func.accept(consulClient(server), server);
    server.shutdown();
  }

  private ConsulClient consulClient(MockWebServer server) {
    ConsulClientFactory consulClientFactory = new ConsulClientFactory();
    return consulClientFactory.client(server.getHostName(), server.getPort());
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

}
