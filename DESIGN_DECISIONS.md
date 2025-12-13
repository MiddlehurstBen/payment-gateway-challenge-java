# Payment Gateway - Design Decisions & Architecture

## Overview
This document outlines the key architectural and design decisions made during the development of the Payment Gateway API.

---

##  Validation Strategy

### Decision: Manual Validation Instead of Bean Validation (@Valid)

**Approach:**
- Custom validation performed in `RequestValidator` utility class
- No `@Valid` annotation used on `@RequestBody` parameters

**Reasoning:**

- **Custom Error Messages**: Able to provide more detailed, user-friendly error messages
- **Validation Logic**: Complex validation can be implemented (e.g., expiry date in the future)
-  **Flexibility**: Easier to modify validation rules and error messages without touching model classes

**Validation Rules Implemented:**
- All below fields are required
- Card number: 14-19 digits, numeric only 
- Expiry date: MM/YY or MM/YYYY format, must be in the future
- Currency: Must be one of the supported codes (GBP, USD, EUR)
- Amount: Must be greater than 0
- CVV: Must be 3 or 4 digits

---

## Payment Response

### Decision: Unified Payment Response Structure

**Approach:**
- Single `PaymentResponse` class to represent both `POST` and `GET` responses

**Reasoning:**
- **Simplicity**: Easier to manage and extend a single response structure
- **Removing Redundancy**: Avoids duplication of similar fields across multiple response classes

---

## Bank Request

### Decision: Direct Mapping of Payment Request to Bank Request

**Approach:**
- `BankRequest` class mirrors fields from `PaymentRequest` 
-  Expiry Month and Year are set to Expiry Date in `BankRequest`

**Reasoning:**
- **Open For Future Changes**: If bank requirements change, mapping logic can be adjusted without affecting `PaymentRequest`

---

## Bank Response

### Decision: Storing Authorization Code 

**Approach:**
- `BankResponse` class includes `authorizationCode` field

**Reasoning:**
- **Requirement Changes:** Authorization code is stored but is not used in the `PaymentResponse` this is added in case of future requirements

---

## Persistence

### Decision: Only saving in memory payments that are successfully processed

**Approach:**
- 
- Only payments that pass validation are saved in the `PaymentsRepository` 
- `Status` field indicates whether payment is `AUTHORIZED` or `DECLINED`

**Reasoning:**
- **Data Relevance**: Only valid payments are relevant for future retrieval
- **Filtering**: Only payments that are valid reach the bank simulator


--- 
##  Error Handling & HTTP Status Codes

### Decision: Centralized Exception Handling with Proper Status Codes

**Approach:**
- `@ControllerAdvice` with `CommonExceptionHandler` catches all exceptions globally
- Specific exception classes for different error scenarios
- Generic exception handler as safety net

**HTTP Status Code Mapping:**
- **400 (Bad Request)**: Validation failures, malformed requests
- **404 (Not Found)**: Payment ID does not exist in repository
- **503 (Service Unavailable)**: Bank simulator is down or unreachable
- **500 (Internal Server Error)**: Unexpected errors

---

