package com.orama.e_commerce.config;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitProperties {

  private int defaultRequestsPerMinute = 60;
  private Map<String, EndpointLimit> endpoints = new HashMap<>();

  public int getDefaultRequestsPerMinute() {
    return defaultRequestsPerMinute;
  }

  public void setDefaultRequestsPerMinute(int defaultRequestsPerMinute) {
    this.defaultRequestsPerMinute = defaultRequestsPerMinute;
  }

  public Map<String, EndpointLimit> getEndpoints() {
    return endpoints;
  }

  public void setEndpoints(Map<String, EndpointLimit> endpoints) {
    this.endpoints = endpoints;
  }

  public static class EndpointLimit {
    private int requestsPerMinute;

    public int getRequestsPerMinute() {
      return requestsPerMinute;
    }

    public void setRequestsPerMinute(int requestsPerMinute) {
      this.requestsPerMinute = requestsPerMinute;
    }
  }
}
