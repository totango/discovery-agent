package com.totango.discoveryagent.model;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ServiceGroup {

  private final List<Service> services;
  private final Optional<String> index;
  
  public ServiceGroup(List<Service> services, Optional<String> index) {
    this.services = services;
    this.index = index;
  }

  public List<Service> getServices() {
    return services;
  }
  
  public Optional<String> getIndex() {
    return index;
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ServiceGroup that = (ServiceGroup) o;

    return Objects.equals(services, that.services)
        && Objects.equals(index, that.index);
  }

  @Override
  public int hashCode() {
    return Objects.hash(services, index);
  }
}
