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

  private MockHttpServletResponse execute(RateLimitFilter filter, String path)
      throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest("POST", path);
    request.setRemoteAddr("192.0.2.10");
    MockHttpServletResponse response = new MockHttpServletResponse();
    filter.doFilter(request, response, new MockFilterChain());
    return response;
  }
}
