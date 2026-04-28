package com.orama.e_commerce.config;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RateLimitFilterTest {

  @Test
  void wildcardEndpointLimitAppliesToPaymentOrderPath() throws ServletException, IOException {
    RateLimitProperties properties = new RateLimitProperties();
    properties.setDefaultRequestsPerMinute(60);
    RateLimitProperties.EndpointLimit limit = new RateLimitProperties.EndpointLimit();
    limit.setRequestsPerMinute(10);
    properties.getEndpoints().put("POST:/api/payments/orders/**", limit);

    RateLimitFilter filter = new RateLimitFilter(properties);

    for (int i = 0; i < 10; i++) {
      MockHttpServletResponse response = execute(filter, "/api/payments/orders/" + i);
      assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    MockHttpServletResponse blockedResponse = execute(filter, "/api/payments/orders/999");

    assertThat(blockedResponse.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
  }

  @Test
  void xForwardedForIsIgnoredByDefaultToAvoidClientSpoofing() throws ServletException, IOException {
    RateLimitProperties properties = new RateLimitProperties();
    properties.setDefaultRequestsPerMinute(1);
    RateLimitFilter filter = new RateLimitFilter(properties);

    MockHttpServletResponse firstResponse =
        execute(filter, "/api/payments/orders/1", "198.51.100.10");
    MockHttpServletResponse spoofedHeaderResponse =
        execute(filter, "/api/payments/orders/1", "203.0.113.99");

    assertThat(firstResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
    assertThat(spoofedHeaderResponse.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
  }

  @Test
  void xForwardedForCanBeTrustedWhenProxyNormalizesTheHeader()
      throws ServletException, IOException {
    RateLimitProperties properties = new RateLimitProperties();
    properties.setDefaultRequestsPerMinute(1);
    properties.setTrustXForwardedFor(true);
    RateLimitFilter filter = new RateLimitFilter(properties);

    MockHttpServletResponse firstResponse =
        execute(filter, "/api/payments/orders/1", "198.51.100.10");
    MockHttpServletResponse secondClientResponse =
        execute(filter, "/api/payments/orders/1", "203.0.113.99");

    assertThat(firstResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
    assertThat(secondClientResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  private MockHttpServletResponse execute(RateLimitFilter filter, String path)
      throws ServletException, IOException {
    return execute(filter, path, null);
  }

  private MockHttpServletResponse execute(RateLimitFilter filter, String path, String forwardedFor)
      throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest("POST", path);
    request.setRemoteAddr("192.0.2.10");
    if (forwardedFor != null) {
      request.addHeader("X-Forwarded-For", forwardedFor);
    }
    MockHttpServletResponse response = new MockHttpServletResponse();
    filter.doFilter(request, response, new MockFilterChain());
    return response;
  }
}
