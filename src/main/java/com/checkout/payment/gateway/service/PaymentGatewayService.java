package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.client.BankClient;
import com.checkout.payment.gateway.exception.BadRequestException;
import com.checkout.payment.gateway.exception.NotFoundException;
import com.checkout.payment.gateway.exception.ServiceUnavailableException;
import com.checkout.payment.gateway.model.BankRequest;
import com.checkout.payment.gateway.model.BankResponse;
import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import com.checkout.payment.gateway.model.PaymentResponse;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import java.util.UUID;
import com.checkout.payment.gateway.validator.RequestValidator;
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
    return paymentsRepository.get(id).orElseThrow(() -> new NotFoundException("No payment found for ID " + id));
  }

  public PaymentResponse processPayment(PostPaymentRequest paymentRequest) {

    UUID paymentId = UUID.randomUUID();

    try {
      RequestValidator.validateRequest(paymentRequest);
    } catch (IllegalArgumentException e) {
      LOG.error("Payment request validation failed for ID {}: {}", paymentId, e.getMessage());
      throw new BadRequestException("Validation failed: " + e.getMessage());
    }

    BankResponse bankResponse = bankClient.processPayment(populateBankRequest(paymentRequest));

    if (bankResponse.getHttpStatusCode() == 503) {
      throw new ServiceUnavailableException("Bank service unavailable");
    }

    PaymentResponse response = populatePaymentResponse(paymentId, bankResponse, paymentRequest);
    paymentsRepository.add(response);

    return response;
  }


  private BankRequest populateBankRequest(PostPaymentRequest paymentRequest) {
    BankRequest bankRequest = new BankRequest();
    bankRequest.setCardNumber(paymentRequest.getCardNumber());
    bankRequest.setExpiryDate(paymentRequest.getExpiryMonth() + "/" + paymentRequest.getExpiryYear());
    bankRequest.setCvv(paymentRequest.getCvv());
    bankRequest.setAmount(paymentRequest.getAmount());
    bankRequest.setCurrency(paymentRequest.getCurrency());
    return bankRequest;
  }

  private PaymentResponse populatePaymentResponse(UUID paymentId, BankResponse bankResponse,
      PostPaymentRequest paymentRequest) {

    PaymentResponse response = new PaymentResponse();

    String cardNumber = paymentRequest.getCardNumber();
    String lastFourDigits = cardNumber.substring(cardNumber.length() - 4);

    response.setCardNumberLastFour(lastFourDigits);
    response.setId(paymentId);
    response.setStatus(bankResponse.isAuthorized() ? PaymentStatus.AUTHORIZED : PaymentStatus.DECLINED);
    response.setExpiryMonth(paymentRequest.getExpiryMonth());
    response.setExpiryYear(paymentRequest.getExpiryYear());
    response.setCurrency(paymentRequest.getCurrency());
    response.setAmount(paymentRequest.getAmount());
    
    return response;
  }
}
