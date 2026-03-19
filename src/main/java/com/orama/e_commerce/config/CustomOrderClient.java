package com.orama.e_commerce.config;

import com.google.gson.JsonObject;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.MercadoPagoClient;
import com.mercadopago.core.MPRequestOptions;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.net.HttpMethod;
import com.mercadopago.net.MPRequest;
import com.mercadopago.net.MPResponse;
import com.mercadopago.resources.order.Order;
import com.mercadopago.serialization.Serializer;

public class CustomOrderClient extends MercadoPagoClient {

  public CustomOrderClient() {
    super(MercadoPagoConfig.getHttpClient());
  }

  public Order createOrder(JsonObject payload, MPRequestOptions options)
      throws MPException, MPApiException {
    MPRequest request =
        MPRequest.builder().uri("/v1/orders").method(HttpMethod.POST).payload(payload).build();
    MPResponse response = send(request, options);
    Order result = Serializer.deserializeFromJson(Order.class, response.getContent());
    result.setResponse(response);
    return result;
  }
}
