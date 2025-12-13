package com.checkout.payment.gateway.model;

import com.checkout.payment.gateway.enums.PaymentStatus;
import lombok.Data;
import java.util.UUID;

@Data
public class PaymentResponse {
  private UUID id;
  private PaymentStatus status;
  private String cardNumberLastFour;
  private String expiryMonth;
  private String expiryYear;
  private String currency;
  private int amount;
}
