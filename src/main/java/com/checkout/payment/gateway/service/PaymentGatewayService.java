package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.bank.BankClient;
import com.checkout.payment.gateway.bank.BankResponse;
import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.EventProcessingException;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import com.checkout.payment.gateway.model.PaymentResponse;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import java.util.UUID;
import com.checkout.payment.gateway.validation.RequestValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PaymentGatewayService {

  private static final Logger LOG = LoggerFactory.getLogger(PaymentGatewayService.class);

  private final PaymentsRepository paymentsRepository;
  private final BankClient bankClient;

  public PaymentGatewayService(PaymentsRepository paymentsRepository, BankClient bankClient) {
    this.paymentsRepository = paymentsRepository;
    this.bankClient = bankClient;
  }

  public PaymentResponse getPaymentById(UUID id) {
    LOG.debug("Requesting access to to payment with ID {}", id);
    return paymentsRepository.get(id).orElseThrow(() -> new EventProcessingException("Invalid ID"));
  }

  public PaymentResponse processPayment(PostPaymentRequest paymentRequest) {

    UUID paymentId = UUID.randomUUID();

    try {
      RequestValidator.validateRequest(paymentRequest);
    } catch (IllegalArgumentException e) {
      LOG.error("Payment request validation failed for ID {}: {}", paymentId, e.getMessage());
      throw new EventProcessingException("Validation failed: " + e.getMessage());
    }

    BankResponse bankResponse = bankClient.processPayment(paymentRequest);

    if (bankResponse.getHttpStatusCode() == 200) {
      PaymentResponse response = populatePaymentResponse(paymentId, bankResponse, paymentRequest);
      paymentsRepository.add(response);
      return response;
    } else if (bankResponse.getHttpStatusCode() == 503) {
      LOG.error("Bank service unavailable for payment ID {}", paymentId);
      throw new EventProcessingException("Bank service unavailable");
    } else {
      LOG.error("Bank processing failed for payment ID {}", paymentId);
      throw new EventProcessingException("Bank processing failed");
    }
  }

  private PaymentResponse populatePaymentResponse(UUID paymentId, BankResponse bankResponse, PostPaymentRequest request) {
    PaymentResponse response = new PaymentResponse();
    response.setId(paymentId);
    response.setStatus(bankResponse.isAuthorized() ? PaymentStatus.AUTHORIZED : PaymentStatus.DECLINED);
    

    String cardNumber = String.valueOf(request.getCardNumber());
    int lastFourDigits = Integer.parseInt(cardNumber.substring(cardNumber.length() - 4));
    response.setCardNumberLastFour(lastFourDigits);
    
    response.setExpiryMonth(request.getExpiryMonth());
    response.setExpiryYear(request.getExpiryYear());
    response.setCurrency(request.getCurrency());
    response.setAmount(request.getAmount());
    
    return response;
  }
}
