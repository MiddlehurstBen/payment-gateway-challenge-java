package com.checkout.payment.gateway.validator;

import com.checkout.payment.gateway.model.PostPaymentRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestValidatorTest {

  @Test
  void whenCardNumberTooShortThenValidationFails() {
    PostPaymentRequest request = createValidRequest();

    request.setCardNumber("1234567890123"); // 13 digits

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> RequestValidator.validateRequest(request)
    );
    assertTrue(
        exception.getMessage().contains("Card number must be at least 14 digits long and at most 19 digits long"));
  }

  @Test
  void whenCardNumberTooLongThenValidationFails() {
    PostPaymentRequest request = createValidRequest();

    request.setCardNumber("12345678901234567890"); // 20 digits

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> RequestValidator.validateRequest(request)
    );
    assertTrue(
        exception.getMessage().contains("Card number must be at least 14 digits long and at most 19 digits long"));
  }

  @Test
  void whenExpiryMonthTooLowThenValidationFails() {
    PostPaymentRequest request = createValidRequest();

    request.setExpiryMonth("0");

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> RequestValidator.validateRequest(request)
    );
    assertTrue(exception.getMessage().contains("Expiry date must be in format MM/YYYY") ||
        exception.getMessage().contains("Expiry month must be between 1 and 12"));
  }

  @Test
  void whenExpiryMonthTooHighThenValidationFails() {
    PostPaymentRequest request = createValidRequest();

    request.setExpiryMonth("13");

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> RequestValidator.validateRequest(request)
    );
    assertTrue(exception.getMessage().contains("Expiry date must be in format MM/YYYY") ||
        exception.getMessage().contains("Expiry month must be between 1 and 12"));
  }

  @Test
  void whenExpiryDateInPastThenValidationFails() {
    PostPaymentRequest request = createValidRequest();

    request.setExpiryMonth("01");
    request.setExpiryYear("2020");

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> RequestValidator.validateRequest(request)
    );
    assertTrue(exception.getMessage().contains("Expiry date must be in the future"));
  }

  @Test
  void whenExpiryDateIsNowThenValidationPasses() {

    PostPaymentRequest request = createValidRequest();
    java.time.YearMonth now = java.time.YearMonth.now();

    request.setExpiryMonth(String.valueOf(now.getMonthValue()));
    request.setExpiryYear(String.valueOf(now.getYear()));

    assertDoesNotThrow(() -> RequestValidator.validateRequest(request));
  }

  @Test
  void whenCurrencyInvalidThenValidationFails() {
    PostPaymentRequest request = createValidRequest();
    request.setCurrency("AUD");

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> RequestValidator.validateRequest(request)
    );
    assertTrue(exception.getMessage().contains("Currency code is not supported"));
  }

  @Test
  void whenCvvTooShortThenValidationFails() {
    PostPaymentRequest request = createValidRequest();
    request.setCvv("12");

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> RequestValidator.validateRequest(request)
    );
    assertTrue(exception.getMessage().contains("CVV must be 3 or 4 digits long"));
  }

  @Test
  void whenCvvTooLongThenValidationFails() {
    PostPaymentRequest request = createValidRequest();
    request.setCvv("12345");

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> RequestValidator.validateRequest(request)
    );
    assertTrue(exception.getMessage().contains("CVV must be 3 or 4 digits long"));
  }

  @Test
  void whenAllFieldsValidThenValidationPasses() {
    PostPaymentRequest request = createValidRequest();

    assertDoesNotThrow(() -> RequestValidator.validateRequest(request));
  }

  private PostPaymentRequest createValidRequest() {
    PostPaymentRequest request = new PostPaymentRequest();
    request.setCardNumber("2222405343248113");
    request.setExpiryMonth("12");
    request.setExpiryYear("2026");
    request.setCurrency("GBP");
    request.setAmount(100);
    request.setCvv("123");
    return request;
  }
}
