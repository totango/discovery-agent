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

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import rx.Subscription;

import com.jayway.awaitility.Duration;
import com.totango.discoveryagent.model.Service;
import com.totango.discoveryagent.model.ServiceGroup;

public class DiscoveryServiceTest {

  private static final Service SERVICE1 = new Service("pong-service-1", "192.168.25.111", "pong", "pong", Arrays.asList("jvm"), 9877);
  
  private static final Service SERVICE2 = new Service("pong-service-2", "192.168.25.112", "pong", "pong", Arrays.asList("jvm"), 9877);
  
  @Test
  public void getServiceShouldReturnEmptyServiceListForUnknownService() throws Exception {
    
    ConsulClient consulClient = mock(ConsulClient.class);
    when(consulClient.discoverService(any())).thenReturn(Optional.empty());
    
    DiscoveryService discoveryService = new DiscoveryService(consulClient, 0, i -> i, TimeUnit.MILLISECONDS);
    List<Service> services = discoveryService.getServices("unknown-service");
    assertEquals(0, services.size()); 
  }
  
  @Test
  public void getServiceShouldReturnServiceListForKnownService() throws Exception {
    
    List<Service> expected = Arrays.asList(SERVICE1, SERVICE2);
    ServiceGroup serviceGroup = new ServiceGroup(expected, Optional.empty());
    
    ConsulClient consulClient = mock(ConsulClient.class);
    when(consulClient.discoverService(any())).thenReturn(Optional.of(serviceGroup));
    
    DiscoveryService discoveryService = new DiscoveryService(consulClient, 0, i -> i, TimeUnit.MILLISECONDS);
    List<Service> services = discoveryService.getServices("pong");
    assertEquals(expected, services);
  }
  
  @Test(timeout = 1000)
  public void subscribeForServiceShouldListenForChangeEvents() throws Exception {
    
    List<Service> twoServices = Arrays.asList(SERVICE1, SERVICE2);
    List<Service> singleService = Arrays.asList(SERVICE1);
    
    ServiceGroup twoServiceGroup = new ServiceGroup(twoServices, Optional.empty());
    ServiceGroup singleServiceGroup = new ServiceGroup(singleService, Optional.empty());
    
    ConsulClient consulClient = mock(ConsulClient.class);
    when(consulClient.discoverService(any()))
      .thenReturn(Optional.of(twoServiceGroup));
    
    final List<Service> services = new ArrayList<>();
    
    DiscoveryService discoveryService = new DiscoveryService(consulClient, 1, i -> i, TimeUnit.MILLISECONDS);
    
    Subscription subscribe = discoveryService.subscribe("pong", update -> {
      synchronized (services) {
        services.clear();
        services.addAll(update);
      }
    });

    await().pollInterval(new Duration(1, MILLISECONDS))
      .atMost(100, MILLISECONDS)
      .until(() -> services.size(), equalTo(2));
    
    synchronized (services) {
      assertEquals(twoServices, services);
    }

    when(consulClient.discoverService(any()))
      .thenReturn(Optional.of(singleServiceGroup));
    
    await().pollInterval(new Duration(1, MILLISECONDS))
      .atMost(100, MILLISECONDS)
      .until(() -> services.size(), equalTo(1));
    
    subscribe.unsubscribe();
    synchronized (services) {
      assertEquals(singleService, services);
    }
  }
  
  @SuppressWarnings("unchecked")
  @Test(timeout = 1000)
  public void subscribeWithErrorShouldTriggerRetry() throws Exception {
    
    List<Service> singleService = Arrays.asList(SERVICE1);
    ServiceGroup singleServiceGroup = new ServiceGroup(singleService, Optional.empty());
    
    ConsulClient consulClient = mock(ConsulClient.class);
    when(consulClient.discoverService(any()))
      .thenThrow(UnknownHostException.class)
      .thenThrow(UnknownHostException.class)
      .thenThrow(UnknownHostException.class)
      .thenReturn(Optional.of(singleServiceGroup))
      .thenThrow(IOException.class)
      .thenThrow(IOException.class)
      .thenReturn(Optional.of(singleServiceGroup));
      
    int[] expectedRetry = {1, 2, 3, 1 ,2};
    
    AtomicInteger callsCounter = new AtomicInteger(0);
    DiscoveryService discoveryService = new DiscoveryService(consulClient, 3, i -> {
      callsCounter.incrementAndGet();
      assertTrue("expected " + expectedRetry[i-1] + " got " + i, i == expectedRetry[i-1]);
      return i;
    }, TimeUnit.MILLISECONDS);
    
    Subscription subscribe = discoveryService.subscribe("pong", update -> {});

    await().pollInterval(new Duration(2, MILLISECONDS))
      .atMost(300, MILLISECONDS)
      .untilAtomic(callsCounter, equalTo(5));
    
    subscribe.unsubscribe();
  }
}
