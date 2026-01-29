package com.orama.e_commerce.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

  private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
  private final RateLimitProperties rateLimitProperties;

  public RateLimitFilter(RateLimitProperties rateLimitProperties) {
    this.rateLimitProperties = rateLimitProperties;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String clientIp = getClientIp(request);
    String endpoint = getEndpointKey(request);
    String bucketKey = clientIp + ":" + endpoint;

    int limit = getLimitForEndpoint(endpoint);
    Bucket bucket = buckets.computeIfAbsent(bucketKey, k -> createBucket(limit));

    if (bucket.tryConsume(1)) {
      response.setHeader("X-Rate-Limit-Remaining", String.valueOf(bucket.getAvailableTokens()));
      filterChain.doFilter(request, response);
    } else {
      response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
      response.setContentType("application/json");
      response.setHeader("X-Rate-Limit-Remaining", "0");
      response.getWriter().write("{\"error\": \"Rate limit exceeded. Try again later.\"}");
    }
  }

  private int getLimitForEndpoint(String endpoint) {
    Map<String, RateLimitProperties.EndpointLimit> endpoints = rateLimitProperties.getEndpoints();

    for (Map.Entry<String, RateLimitProperties.EndpointLimit> entry : endpoints.entrySet()) {
      if (endpoint.startsWith(entry.getKey())) {
        return entry.getValue().getRequestsPerMinute();
      }
    }

    return rateLimitProperties.getDefaultRequestsPerMinute();
  }

  private String getEndpointKey(HttpServletRequest request) {
    String method = request.getMethod();
    String uri = request.getRequestURI();
    return method + ":" + uri;
  }

  private Bucket createBucket(int requestsPerMinute) {
    Bandwidth limit =
        Bandwidth.classic(
            requestsPerMinute, Refill.greedy(requestsPerMinute, Duration.ofMinutes(1)));
    return Bucket.builder().addLimit(limit).build();
  }

  private String getClientIp(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
      return xForwardedFor.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }
}
