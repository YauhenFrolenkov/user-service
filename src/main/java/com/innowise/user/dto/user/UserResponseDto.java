package com.innowise.user.dto.user;

import com.innowise.user.dto.card.PaymentCardResponseDto;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

public class UserResponseDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String surname;

    private LocalDate birthDate;

    private String email;

    private Boolean active;

    private List<PaymentCardResponseDto> cards;

    public UserResponseDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<PaymentCardResponseDto> getCards() {
        return cards;
    }

    public void setCards(List<PaymentCardResponseDto> cards) {
        this.cards = cards;
    }
}
