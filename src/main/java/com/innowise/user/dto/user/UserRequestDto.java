package com.innowise.user.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

public class UserRequestDto {

    @NotNull
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private String surname;

    @Past
    private LocalDate birthDate;

    @Email
    @NotBlank
    private String email;

    /**
     * Empty constructor for serialization/deserialization.
     * Jackson requires a no-args constructor.
     */
    public UserRequestDto() {
    }

    public Long getId() {return id;}

    public void setId(Long id) {this.id = id;}

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }
}
