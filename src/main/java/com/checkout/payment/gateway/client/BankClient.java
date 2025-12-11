package com.checkout.payment.gateway.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class BankClient {

  private static final Logger LOG = LoggerFactory.getLogger(BankClient.class);

  private final RestTemplate restTemplate;
  private final String bankUrl;

  public BankClient(RestTemplate restTemplate, 
                    @Value("${bank.simulator.url:http://localhost:8080}") String bankUrl) {
    this.restTemplate = restTemplate;
    this.bankUrl = bankUrl;
  }

  public BankResponse processPayment(BankPaymentRequest request) {
    String url = bankUrl + "/payments";
    
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<BankPaymentRequest> entity = new HttpEntity<>(request, headers);
    
    LOG.info("Calling bank simulator at: {}", url);
    
    try {
      return restTemplate.postForObject(url, entity, BankResponse.class);
    } catch (Exception e) {
      LOG.error("Error calling bank simulator", e);
      throw new RuntimeException("Failed to process payment with bank", e);
    }
  }

  // Request format expected by bank simulator
  public static class BankPaymentRequest {
    @JsonProperty("card_number")
    private String cardNumber;
    
    @JsonProperty("expiry_date")
    private String expiryDate;
    
    private String currency;
    private int amount;
    private String cvv;

    public String getCardNumber() {
      return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
      this.cardNumber = cardNumber;
    }

    public String getExpiryDate() {
      return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
      this.expiryDate = expiryDate;
    }

    public String getCurrency() {
      return currency;
    }

    public void setCurrency(String currency) {
      this.currency = currency;
    }

    public int getAmount() {
      return amount;
    }

    public void setAmount(int amount) {
      this.amount = amount;
    }

    public String getCvv() {
      return cvv;
    }

    public void setCvv(String cvv) {
      this.cvv = cvv;
    }
  }

  // Response format from bank simulator
  public static class BankResponse {
    private boolean authorized;
    
    @JsonProperty("authorization_code")
    private String authorizationCode;

    public boolean isAuthorized() {
      return authorized;
    }

    public void setAuthorized(boolean authorized) {
      this.authorized = authorized;
    }

    public String getAuthorizationCode() {
      return authorizationCode;
    }

    public void setAuthorizationCode(String authorizationCode) {
      this.authorizationCode = authorizationCode;
    }
  }
}
