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
