package com.totango.discoveryagent;

import com.totango.discoveryagent.RoundRobinLoadBalancer.Function2;

public interface LoadBalancer {

  public <T> T withNextEndpoint(Function2<String, Integer, T> func) throws NoServiceAvailable;
}
