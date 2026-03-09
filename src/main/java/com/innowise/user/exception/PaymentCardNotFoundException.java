package com.innowise.user.exception;

public class PaymentCardNotFoundException extends RuntimeException {

    public PaymentCardNotFoundException(Long id) {
        super("Payment card not found with id: " + id);
    }

    public PaymentCardNotFoundException(String number) {
        super("Payment card not found with number: " + number);
    }
}
