package com.totango.discoveryagent.model;

import java.util.List;
import java.util.Objects;

public class Service {

  private String node;
  private String address;
  private String serviceId;
  private String serviceName;
  private List<String> serviceTags;
  private int servicePort;

  public Service(String node, String address, String serviceId, String serviceName,
      List<String> serviceTags, int servicePort) {
    this.node = node;
    this.address = address;
    this.serviceId = serviceId;
    this.serviceName = serviceName;
    this.serviceTags = serviceTags;
    this.servicePort = servicePort;
  }

  public String getNode() {
    return node;
  }

  public String getAddress() {
    return address;
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
        && Objects.equals(address, that.address)
        && Objects.equals(serviceId, that.serviceId)
        && Objects.equals(serviceName, that.serviceName)
        && Objects.equals(serviceTags, that.serviceTags)
        && Objects.equals(servicePort, that.servicePort);
  }

  @Override
  public int hashCode() {
    return Objects.hash(node, address, serviceId, serviceName, serviceTags, servicePort);
  }

}
