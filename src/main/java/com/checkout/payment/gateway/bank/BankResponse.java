package com.checkout.payment.gateway.bank;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BankResponse {
  private boolean authorized;
  private int httpStatusCode;
  
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

  public void setHttpStatusCode(int value) {
    this.httpStatusCode = value;
  }

  public int getHttpStatusCode() {
    return httpStatusCode;
  }
}
