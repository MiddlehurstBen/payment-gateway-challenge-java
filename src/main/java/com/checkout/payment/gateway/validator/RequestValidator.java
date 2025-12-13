package com.checkout.payment.gateway.validator;

import com.checkout.payment.gateway.enums.CurrencyCodes;
import com.checkout.payment.gateway.model.PostPaymentRequest;

public class RequestValidator {


  public static final int MAX_CARD_LENGTH = 19;
  public static final int MIN_CARD_LENGTH = 14;

  public static void validateRequest(PostPaymentRequest request) {
    validateCardNumber(request.getCardNumber());
    validateExpiryDate(request.getExpiryMonth(), request.getExpiryYear());
    validateCurrencyCode(request.getCurrencyCode());
    validateAmount(request.getAmount());
    validateCvv(request.getCvv());
  }

  private static void validateCardNumber(String cardNumber) {
    if (cardNumber == null || cardNumber.isEmpty()) {
      throw new IllegalArgumentException("Card number is required");
    }

    if (!cardNumber.matches("\\d+")) {
      throw new IllegalArgumentException("Card number must contain only numeric characters");
    }

    if (cardNumber.length() < MIN_CARD_LENGTH || cardNumber.length() > MAX_CARD_LENGTH) {
      throw new IllegalArgumentException("Card number must be at least " + MIN_CARD_LENGTH + " digits long and at most " + MAX_CARD_LENGTH + " digits long");
    }
  }

  private static void validateExpiryDate(String expiryMonth, String expiryYear) {
    String expiryDate = expiryMonth + "/" + expiryYear;

    if (expiryDate.equals("/")) {
      throw new IllegalArgumentException("Expiry date is required");
    }

    if (!expiryDate.matches("^(0[1-9]|1[0-2])/\\d{2,4}$")) {
      throw new IllegalArgumentException("Expiry date must be in format MM/YYYY");
    }

    String[] parts = expiryDate.split("/");
    int month = Integer.parseInt(parts[0]);
    int year = Integer.parseInt(parts[1].length() == 2 ? "20" + parts[1] : parts[1]);

    if (month < 1 || month > 12) {
      throw new IllegalArgumentException("Expiry month must be between 1 and 12");
    }

    java.time.YearMonth now = java.time.YearMonth.now();
    java.time.YearMonth exp = java.time.YearMonth.of(year, month);

    if (exp.isBefore(now)) {
      throw new IllegalArgumentException("Expiry date must be in the future");
    }
  }

  private static void validateCurrencyCode(String currencyCode) {
    if (currencyCode == null || currencyCode.isEmpty()) {
      throw new IllegalArgumentException("Currency is required");
    }

    if (!currencyCode.matches("[A-Z]{3}")) {
      throw new IllegalArgumentException("Currency must be a 3-letter uppercase code");
    }

    try {
      CurrencyCodes.valueOf(currencyCode);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Currency code is not supported. Supported currencies: " + java.util.Arrays.toString(CurrencyCodes.values()));
    }
  }

  private static void validateAmount(int amount) {
    if (amount <= 0) {
      throw new IllegalArgumentException("Amount must be greater than 0");
    }
  }

  private static void validateCvv(String cvv) {
    if (cvv == null || cvv.isEmpty()) {
      throw new IllegalArgumentException("CVV is required");
    }

    if (!cvv.matches("\\d+")) {
      throw new IllegalArgumentException("CVV must contain only numeric characters");
    }

    if (cvv.length() != 3 && cvv.length() != 4) {
      throw new IllegalArgumentException("CVV must be 3 or 4 digits long");
    }
  }

}
