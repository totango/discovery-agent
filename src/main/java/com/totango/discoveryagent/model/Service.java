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
package com.totango.discoveryagent.model;

import java.util.List;
import java.util.Objects;

public class Service {

  private String node;
  private String nodeAddress;
  private String serviceAddress;
  private String serviceId;
  private String serviceName;
  private List<String> serviceTags;
  private int servicePort;

  public Service(String node, String nodeAddress, String serviceId, String serviceName,
      List<String> serviceTags, String serviceAddress, int servicePort) {
    this.node = node;
    this.nodeAddress = nodeAddress;
    this.serviceId = serviceId;
    this.serviceName = serviceName;
    this.serviceTags = serviceTags;
    this.serviceAddress = serviceAddress;
    this.servicePort = servicePort;
  }

  public String getNode() {
    return node;
  }

  public String getNodeAddress() {
    return nodeAddress;
  }

  public String getServiceId() {
    return serviceId;
  }

  public String getServiceName() {
    return serviceName;
  }

  public List<String> getServiceTags() {
    return serviceTags;
  }

  public String getServiceAddress() {
    return serviceAddress;
  }
  
  public int getServicePort() {
    return servicePort;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Service that = (Service) o;

    return Objects.equals(node, that.node)
        && Objects.equals(nodeAddress, that.nodeAddress)
        && Objects.equals(serviceId, that.serviceId)
        && Objects.equals(serviceName, that.serviceName)
        && Objects.equals(serviceTags, that.serviceTags)
        && Objects.equals(serviceAddress, that.serviceAddress)
        && Objects.equals(servicePort, that.servicePort);
  }

  @Override
  public int hashCode() {
    return Objects.hash(node, nodeAddress, serviceId, serviceName, serviceTags, serviceAddress, servicePort);
  }

}
