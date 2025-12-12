package com.checkout.payment.gateway.controller;


import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.matchesPattern;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.model.PaymentResponse;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentGatewayControllerTest {

  @Autowired
  private MockMvc mvc;
  @Autowired
  PaymentsRepository paymentsRepository;

  @Test
  void whenPaymentWithIdExistThenCorrectPaymentIsReturned() throws Exception {
    PaymentResponse payment = new PaymentResponse();
    payment.setId(UUID.randomUUID());
    payment.setAmount(10);
    payment.setCurrency("USD");
    payment.setStatus(PaymentStatus.AUTHORIZED);
    payment.setExpiryMonth(12);
    payment.setExpiryYear(2024);
    payment.setCardNumberLastFour(4321);

    paymentsRepository.add(payment);

    mvc.perform(MockMvcRequestBuilders.get("/payment/" + payment.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(payment.getStatus().getName()))
        .andExpect(jsonPath("$.cardNumberLastFour").value(payment.getCardNumberLastFour()))
        .andExpect(jsonPath("$.expiryMonth").value(payment.getExpiryMonth()))
        .andExpect(jsonPath("$.expiryYear").value(payment.getExpiryYear()))
        .andExpect(jsonPath("$.currency").value(payment.getCurrency()))
        .andExpect(jsonPath("$.amount").value(payment.getAmount()));
  }

  @Test
  void whenPaymentWithIdDoesNotExistThen404IsReturned() throws Exception {
    mvc.perform(MockMvcRequestBuilders.get("/payment/" + UUID.randomUUID()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Page not found"));
  }

  @Test
  void whenValidPaymentRequestWithOddCardNumberThenAuthorized() throws Exception {
    String validPaymentRequest = """
        {
          "card_number": 2222405343248113,
          "expiry_month": 12,
          "expiry_year": 2026,
          "currency": "GBP",
          "amount": 100,
          "cvv": 123
        }
        """;

    mvc.perform(MockMvcRequestBuilders.post("/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validPaymentRequest))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.status").value("Authorized"))
        .andExpect(jsonPath("$.cardNumberLastFour").value(8113))
        .andExpect(jsonPath("$.expiryMonth").value(12))
        .andExpect(jsonPath("$.expiryYear").value(2026))
        .andExpect(jsonPath("$.currency").value("GBP"))
        .andExpect(jsonPath("$.amount").value(100));
  }

  @Test
  void whenValidPaymentRequestWithEvenCardNumberThenDeclined() throws Exception {
    String validPaymentRequest = """
        {
          "card_number": 2222405343248114,
          "expiry_month": 12,
          "expiry_year": 2026,
          "currency": "USD",
          "amount": 500,
          "cvv": 456
        }
        """;

    mvc.perform(MockMvcRequestBuilders.post("/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validPaymentRequest))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.status").value("Declined"))
        .andExpect(jsonPath("$.cardNumberLastFour").value(8114))
        .andExpect(jsonPath("$.expiryMonth").value(12))
        .andExpect(jsonPath("$.expiryYear").value(2026))
        .andExpect(jsonPath("$.currency").value("USD"))
        .andExpect(jsonPath("$.amount").value(500));
  }

  @Test
  void whenInvalidCardNumberThenRejected() throws Exception {
    String invalidPaymentRequest = """
        {
          "card_number": 123,
          "expiry_month": 12,
          "expiry_year": 2026,
          "currency": "GBP",
          "amount": 100,
          "cvv": 123
        }
        """;

    mvc.perform(MockMvcRequestBuilders.post("/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidPaymentRequest))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(matchesPattern(".*Card number must be between 14 and 19 digits long.*")));
  }

  @Test
  void whenInvalidExpiryDateThenRejected() throws Exception {
    String invalidPaymentRequest = """
        {
          "card_number": 2222405343248113,
          "expiry_month": 1,
          "expiry_year": 2020,
          "currency": "GBP",
          "amount": 100,
          "cvv": 123
        }
        """;

    mvc.perform(MockMvcRequestBuilders.post("/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidPaymentRequest))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(matchesPattern(".*Expiry date must be in the future.*")));
  }

  @Test
  void whenInvalidCurrencyThenRejected() throws Exception {
    String invalidPaymentRequest = """
        {
          "card_number": 2222405343248113,
          "expiry_month": 12,
          "expiry_year": 2026,
          "currency": "XYZ",
          "amount": 100,
          "cvv": 123
        }
        """;

    mvc.perform(MockMvcRequestBuilders.post("/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidPaymentRequest))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(matchesPattern(".*Currency code is not supported.*")));
  }

  @Test
  void whenInvalidCvvThenRejected() throws Exception {
    String invalidPaymentRequest = """
        {
          "card_number": 2222405343248113,
          "expiry_month": 12,
          "expiry_year": 2026,
          "currency": "GBP",
          "amount": 100,
          "cvv": 12
        }
        """;

    mvc.perform(MockMvcRequestBuilders.post("/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidPaymentRequest))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(matchesPattern(".*CVV must be 3 or 4 digits long.*")));
  }

  @Test
  void whenBankReturns503ThenErrorThrown() throws Exception {
    String paymentRequestWithCardEndingInZero = """
        {
          "card_number": 2222405343248110,
          "expiry_month": 12,
          "expiry_year": 2026,
          "currency": "EUR",
          "amount": 250,
          "cvv": 789
        }
        """;

    mvc.perform(MockMvcRequestBuilders.post("/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(paymentRequestWithCardEndingInZero))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(matchesPattern(".*Bank service unavailable.*")));
  }
}
