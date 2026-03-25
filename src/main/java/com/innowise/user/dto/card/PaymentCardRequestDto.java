package com.innowise.user.dto.card;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class PaymentCardRequestDto {

    @NotNull
    private Long userId;

    @NotBlank
    @Size(min = 16, max = 16)
    private String number;

    @NotBlank
    private String holder;

    @Future
    private LocalDate expirationDate;

    /**
     * Empty constructor for serialization/deserialization.
     * Jackson requires a no-args constructor.
     */
    public PaymentCardRequestDto() {
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getHolder() {
        return holder;
    }

    public void setHolder(String holder) {
        this.holder = holder;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

}
