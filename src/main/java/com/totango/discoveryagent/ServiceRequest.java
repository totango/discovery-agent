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

import java.util.Objects;

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
      if (serviceName == null) {
        throw new NullPointerException(String.valueOf("Service Name is mandatory"));
      }
      return new ServiceRequest(serviceName, index, tag);
    }
  }
}
