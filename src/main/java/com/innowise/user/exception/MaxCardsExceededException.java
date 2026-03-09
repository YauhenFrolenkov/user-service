package com.innowise.user.exception;

public class MaxCardsExceededException extends RuntimeException {

    public MaxCardsExceededException(Long userId) {
        super("User " + userId + " cannot have more than 5 cards");
    }
}
