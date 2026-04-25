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

  private CreatePaymentCommand lastCommand;
  private String lastQueriedOrderId;
  private int createPaymentCallCount;
  private int getOrderStatusCallCount;

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

  public CreatePaymentCommand getLastCommand() {
    return lastCommand;
  }

  public String getLastQueriedOrderId() {
    return lastQueriedOrderId;
  }

  public int getCreatePaymentCallCount() {
    return createPaymentCallCount;
  }

  public int getGetOrderStatusCallCount() {
    return getOrderStatusCallCount;
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
}
