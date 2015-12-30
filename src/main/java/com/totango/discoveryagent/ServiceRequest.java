package com.totango.discoveryagent;

import java.util.Objects;

import com.google.common.base.Preconditions;

public class ServiceRequest {

  private String serviceName; 
  private String index = "0"; 
  private String tag;
  
  private ServiceRequest(String serviceName, String index, String tag) {
    this.serviceName = serviceName;
    this.index = index;
    this.tag = tag;
  }
  
  public String serviceName() {
    return serviceName;
  }

  public String index() {
    return index;
  }

  public String tag() {
    return tag;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ServiceRequest that = (ServiceRequest) o;

    return Objects.equals(serviceName, that.serviceName)
        && Objects.equals(index, that.index)
        && Objects.equals(tag, that.tag);
  }

  @Override
  public int hashCode() {
    return Objects.hash(serviceName, index, tag);
  }
  
  public static final Builder request() {
    return new Builder();
  }
  
  public static class Builder {
  
    private String serviceName; 
    private String index = "0"; 
    private String tag;
    
    public Builder forService(String name) {
      this.serviceName = name;
      return this;
    }
    
    public Builder withTag(String tag) {
      this.tag = tag;
      return this;
    }
    
    public Builder lastUpdateIndex(String index) {
      this.index = index;
      return this;
    }
    
    public ServiceRequest build() {
      Preconditions.checkNotNull(serviceName, "Service Name is mandatory");
      return new ServiceRequest(serviceName, index, tag);
    }
  }
}
