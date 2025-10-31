package com.orama.e_commerce.dtos.stripe_entities;

public record StripeResponse(String status, String message, String sessionId, String sessionUrl) {
  public static class Builder {
    private String status;
    private String message;
    private String sessionId;
    private String sessionUrl;

    public Builder status(String status) {
      this.status = status;
      return this;
    }

    public Builder message(String message) {
      this.message = message;
      return this;
    }

    public Builder sessionId(String sessionId) {
      this.sessionId = sessionId;
      return this;
    }

    public Builder sessionUrl(String sessionUrl) {
      this.sessionUrl = sessionUrl;
      return this;
    }

    public StripeResponse build() {
      return new StripeResponse(status, message, sessionId, sessionUrl);
    }
  }

  public static Builder builder() {
    return new Builder();
  }
}
