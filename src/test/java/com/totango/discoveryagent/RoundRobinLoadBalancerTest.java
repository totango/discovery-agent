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

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import com.totango.discoveryagent.DiscoveryService;
import com.totango.discoveryagent.NoServiceAvailable;
import com.totango.discoveryagent.RoundRobinLoadBalancer;
import com.totango.discoveryagent.model.Service;

public class RoundRobinLoadBalancerTest {

  private static final String SERVICE_NAME = "pong-service-1";
  
  private static final Service SERVICE1 = new Service("pong-service-1", "192.168.25.111",
      "pong", "pong", Arrays.asList("jvm"), "192.168.25.111", 9877);
  
  private static final Service SERVICE2 = new Service("pong-service-2", "192.168.25.112",
      "pong", "pong", Arrays.asList("jvm"), "192.168.25.112", 9877);
  
  
  @Test(expected=NoServiceAvailable.class)
  public void emptyServiceListShouldThrowNoServiceAvailable() throws Exception {
    
    DiscoveryService discoveryService = mock(DiscoveryService.class);
    
    RoundRobinLoadBalancer balancer = new RoundRobinLoadBalancer(discoveryService, SERVICE_NAME);
    balancer.init();
    
    balancer.withNextEndpoint((host, port) -> {
      return "";
    });
  }
  
  @Test(expected=NoServiceAvailable.class)
  public void missingCalltoInitShouldThrowNoServiceAvailable() throws Exception {
    
    DiscoveryService discoveryService = mock(DiscoveryService.class);
    when(discoveryService.getServices(anyString())).thenReturn(Arrays.asList(SERVICE1, SERVICE2));
    
    RoundRobinLoadBalancer balancer = new RoundRobinLoadBalancer(discoveryService, SERVICE_NAME);
    
    balancer.withNextEndpoint((host, port) -> {
      return "";
    });
  }
  
  @Test
  public void whenServiceAvailaleCallFuncWithValuesInRoundRobin() throws Exception {
    
    DiscoveryService discoveryService = mock(DiscoveryService.class);
    when(discoveryService.getServices(anyString())).thenReturn(Arrays.asList(SERVICE1, SERVICE2));
    
    RoundRobinLoadBalancer balancer = new RoundRobinLoadBalancer(discoveryService, SERVICE_NAME);
    balancer.init();
    
    balancer.withNextEndpoint((host, port) -> {
      assertEquals(host, SERVICE1.getServiceAddress());
      assertEquals(port, new Integer(SERVICE1.getServicePort()));
      return null;
    });
    
    balancer.withNextEndpoint((host, port) -> {
      assertEquals(host, SERVICE2.getServiceAddress());
      assertEquals(port, new Integer(SERVICE2.getServicePort()));
      return null;
    });
    
    balancer.withNextEndpoint((host, port) -> {
      assertEquals(host, SERVICE1.getServiceAddress());
      assertEquals(port, new Integer(SERVICE1.getServicePort()));
      return null;
    });
  }
}
