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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Subscription;

import com.totango.discoveryagent.model.Service;

public class RoundRobinLoadBalancer implements LoadBalancer {

  private static final Logger Logger =  LoggerFactory.getLogger(RoundRobinLoadBalancer.class);
  
  private List<Service> services = new ArrayList<>();
  
  @SuppressWarnings("unused")
  private Subscription discoverySubscription;
  
  private AtomicInteger nextServiceCounter = new AtomicInteger(-1);

  private String serviceName;

  private DiscoveryService discoveryService;

  public RoundRobinLoadBalancer(DiscoveryService discoveryService, String serviceName) {
    this.serviceName = serviceName;
    this.discoveryService = discoveryService;
  }
  
  public void init() throws IOException {
    this.services = discoveryService.getServices(serviceName);
    this.discoverySubscription = discoveryService.subscribe(serviceName, services -> {
      this.services = services;
    }, throwable -> {
      Logger.error(String.format("Failed to listen for \"%s\" service", serviceName), throwable);
    });
  }
  
  public <T> T withNextEndpoint(Function2<String, Integer, T> func) throws NoServiceAvailable  {
    Optional<Service> service = nextService();
    if (!service.isPresent()) {
      throw new NoServiceAvailable(String.format("There is no \"%s\" service available", serviceName));
    }
    return func.apply(service.get().getServiceAddress(), service.get().getServicePort());
  }
  
  private Optional<Service> nextService() {
    if (services.isEmpty()) {
      return Optional.empty();
    }
    int next = nextServiceCounter.incrementAndGet();
    int i = next % services.size();
    return Optional.of(services.get(i));
  }
  
  @FunctionalInterface
  interface Function2 <A, B, T> {
    public T apply (A a, B b);
  }
}
