package com.checkout.payment.gateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.io.Serializable;

@Data
public class PostPaymentRequest implements Serializable {

  @JsonProperty(value = "card_number", required = true)
  private String cardNumber;
  @JsonProperty(value = "expiry_month", required = true)
  private String expiryMonth;
  @JsonProperty(value = "expiry_year", required = true)
  private String expiryYear;
  @JsonProperty(value = "currency", required = true)
  private String currency;
  @JsonProperty(value = "amount", required = true)
  private Integer amount;
  @JsonProperty(value = "cvv", required = true)
  private String cvv;

}
