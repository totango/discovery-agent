package com.totango.discoveryagent;

import java.net.URL;

import com.google.common.base.Charsets;
import com.google.common.io.CharSource;
import com.google.common.io.Resources;

public class ResourceLoader {

  public static String load(String resourceName) {
    try {
      URL healthJsonURI = Resources.getResource(resourceName);
      CharSource healthJsonSource = Resources.asCharSource(healthJsonURI, Charsets.UTF_8);
      return healthJsonSource.read();
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }
}
