package com.checkout.payment.gateway.bank;

import com.checkout.payment.gateway.model.PostPaymentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

  public BankResponse processPayment(PostPaymentRequest request) {
    String url = bankUrl + "/payments";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<PostPaymentRequest> entity = new HttpEntity<>(request, headers);

    LOG.info("Calling bank simulator at: {}", url);

    try {
      ResponseEntity<BankResponse> response = restTemplate.postForEntity(url, entity, BankResponse.class);
      BankResponse bankResponse = response.getBody();

      LOG.info("Bank response - Status: {}, Authorized: {}",
          response.getStatusCode(),
          bankResponse != null && bankResponse.isAuthorized());

      if (response.getStatusCode().is2xxSuccessful() && bankResponse != null) {
        bankResponse.setHttpStatusCode(response.getStatusCode().value());
      }

      return bankResponse;
    } catch (org.springframework.web.client.HttpServerErrorException.ServiceUnavailable e) {
      LOG.error("Bank service unavailable (503)", e);
      BankResponse errorResponse = new BankResponse();
      errorResponse.setHttpStatusCode(503);
      errorResponse.setAuthorized(false);
      return errorResponse;
    } catch (org.springframework.web.client.ResourceAccessException e) {
      // Connection refused, timeout, or network error - bank is not reachable
      LOG.error("Unable to reach bank service (connection error)", e);
      BankResponse errorResponse = new BankResponse();
      errorResponse.setHttpStatusCode(503);
      errorResponse.setAuthorized(false);
      return errorResponse;
    } catch (Exception e) {
      LOG.error("Unexpected error calling bank simulator", e);
      BankResponse errorResponse = new BankResponse();
      errorResponse.setHttpStatusCode(503);
      errorResponse.setAuthorized(false);
      return errorResponse;
    }
  }
}
