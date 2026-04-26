package com.orama.e_commerce.service.fake;

import com.orama.e_commerce.exceptions.payment.PaymentGatewayException;
import com.orama.e_commerce.service.gateway.CreatePaymentCommand;
import com.orama.e_commerce.service.gateway.GatewayOrderResult;
import com.orama.e_commerce.service.gateway.GatewayPaymentResult;
import com.orama.e_commerce.service.gateway.PaymentGateway;

public class FakePaymentGateway implements PaymentGateway {

  private GatewayPaymentResult nextCreateResult;
  private PaymentGatewayException nextCreateException;
  private GatewayOrderResult nextOrderStatusResult;
  private PaymentGatewayException nextOrderStatusException;
  private GatewayOrderResult nextCancelOrderResult;
  private PaymentGatewayException nextCancelOrderException;
  private GatewayOrderResult nextRefundOrderResult;
  private PaymentGatewayException nextRefundOrderException;

  private CreatePaymentCommand lastCommand;
  private String lastQueriedOrderId;
  private String lastCancelledOrderId;
  private String lastCancelIdempotencyKey;
  private String lastRefundedOrderId;
  private String lastRefundIdempotencyKey;
  private int createPaymentCallCount;
  private int getOrderStatusCallCount;
  private int cancelOrderCallCount;
  private int refundOrderCallCount;

  public void setNextCreateResult(GatewayPaymentResult result) {
    this.nextCreateResult = result;
    this.nextCreateException = null;
  }

  public void setNextCreateException(PaymentGatewayException exception) {
    this.nextCreateException = exception;
    this.nextCreateResult = null;
  }

  public void setNextOrderStatusResult(GatewayOrderResult result) {
    this.nextOrderStatusResult = result;
    this.nextOrderStatusException = null;
  }

  public void setNextOrderStatusException(PaymentGatewayException exception) {
    this.nextOrderStatusException = exception;
    this.nextOrderStatusResult = null;
  }

  public void setNextCancelOrderResult(GatewayOrderResult result) {
    this.nextCancelOrderResult = result;
    this.nextCancelOrderException = null;
  }

  public void setNextCancelOrderException(PaymentGatewayException exception) {
    this.nextCancelOrderException = exception;
    this.nextCancelOrderResult = null;
  }

  public void setNextRefundOrderResult(GatewayOrderResult result) {
    this.nextRefundOrderResult = result;
    this.nextRefundOrderException = null;
  }

  public void setNextRefundOrderException(PaymentGatewayException exception) {
    this.nextRefundOrderException = exception;
    this.nextRefundOrderResult = null;
  }

  public CreatePaymentCommand getLastCommand() {
    return lastCommand;
  }

  public String getLastQueriedOrderId() {
    return lastQueriedOrderId;
  }

  public String getLastCancelledOrderId() {
    return lastCancelledOrderId;
  }

  public String getLastCancelIdempotencyKey() {
    return lastCancelIdempotencyKey;
  }

  public String getLastRefundedOrderId() {
    return lastRefundedOrderId;
  }

  public String getLastRefundIdempotencyKey() {
    return lastRefundIdempotencyKey;
  }

  public int getCreatePaymentCallCount() {
    return createPaymentCallCount;
  }

  public int getGetOrderStatusCallCount() {
    return getOrderStatusCallCount;
  }

  public int getCancelOrderCallCount() {
    return cancelOrderCallCount;
  }

  public int getRefundOrderCallCount() {
    return refundOrderCallCount;
  }

  @Override
  public GatewayPaymentResult createPayment(CreatePaymentCommand command) {
    this.lastCommand = command;
    this.createPaymentCallCount++;
    if (nextCreateException != null) {
      PaymentGatewayException toThrow = nextCreateException;
      nextCreateException = null;
      throw toThrow;
    }
    if (nextCreateResult == null) {
      throw new IllegalStateException(
          "FakePaymentGateway: createPayment chamado sem programacao previa");
    }
    GatewayPaymentResult result = nextCreateResult;
    nextCreateResult = null;
    return result;
  }

  @Override
  public GatewayOrderResult getOrderStatus(String providerOrderId) {
    this.lastQueriedOrderId = providerOrderId;
    this.getOrderStatusCallCount++;
    if (nextOrderStatusException != null) {
      PaymentGatewayException toThrow = nextOrderStatusException;
      nextOrderStatusException = null;
      throw toThrow;
    }
    if (nextOrderStatusResult == null) {
      throw new IllegalStateException(
          "FakePaymentGateway: getOrderStatus chamado sem programacao previa");
    }
    GatewayOrderResult result = nextOrderStatusResult;
    nextOrderStatusResult = null;
    return result;
  }

  @Override
  public GatewayOrderResult cancelOrder(String providerOrderId, String idempotencyKey) {
    this.lastCancelledOrderId = providerOrderId;
    this.lastCancelIdempotencyKey = idempotencyKey;
    this.cancelOrderCallCount++;
    if (nextCancelOrderException != null) {
      PaymentGatewayException toThrow = nextCancelOrderException;
      nextCancelOrderException = null;
      throw toThrow;
    }
    if (nextCancelOrderResult == null) {
      throw new IllegalStateException(
          "FakePaymentGateway: cancelOrder chamado sem programacao previa");
    }
    GatewayOrderResult result = nextCancelOrderResult;
    nextCancelOrderResult = null;
    return result;
  }

  @Override
  public GatewayOrderResult refundOrder(String providerOrderId, String idempotencyKey) {
    this.lastRefundedOrderId = providerOrderId;
    this.lastRefundIdempotencyKey = idempotencyKey;
    this.refundOrderCallCount++;
    if (nextRefundOrderException != null) {
      PaymentGatewayException toThrow = nextRefundOrderException;
      nextRefundOrderException = null;
      throw toThrow;
    }
    if (nextRefundOrderResult == null) {
      throw new IllegalStateException(
          "FakePaymentGateway: refundOrder chamado sem programacao previa");
    }
    GatewayOrderResult result = nextRefundOrderResult;
    nextRefundOrderResult = null;
    return result;
  }
}
