package com.innowise.user.security;

import com.innowise.user.entity.PaymentCard;
import com.innowise.user.entity.User;
import com.innowise.user.repository.PaymentCardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CardSecurityTest {

    private PaymentCardRepository paymentCardRepository;
    private CardSecurity cardSecurity;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        paymentCardRepository = mock(PaymentCardRepository.class);
        cardSecurity = new CardSecurity(paymentCardRepository);
        authentication = mock(Authentication.class);
    }


    private void mockAdmin() {
        when(authentication.getAuthorities())
                .thenAnswer(invocation ->
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
                );
    }

    private void mockUser(Long userId) {
        when(authentication.getAuthorities())
                .thenAnswer(invocation ->
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                );

        when(authentication.getPrincipal()).thenReturn(userId);
    }

    @Test
    void shouldReturnTrue_whenAdmin() {
        mockAdmin();

        boolean result = cardSecurity.isUserSelfOrAdmin(1L, authentication);

        assertTrue(result);
    }

    @Test
    void shouldReturnTrue_whenSameUser() {
        mockUser(1L);

        boolean result = cardSecurity.isUserSelfOrAdmin(1L, authentication);

        assertTrue(result);
    }

    @Test
    void shouldReturnFalse_whenDifferentUser() {
        mockUser(2L);

        boolean result = cardSecurity.isUserSelfOrAdmin(1L, authentication);

        assertFalse(result);
    }

    @Test
    void shouldReturnTrue_whenAdmin_forCard() {
        mockAdmin();

        boolean result = cardSecurity.isCardOwnerOrAdmin(1L, authentication);

        assertTrue(result);
    }

    @Test
    void shouldReturnTrue_whenOwner() {
        mockUser(1L);

        User user = new User();
        user.setId(1L);

        PaymentCard card = new PaymentCard();
        card.setUser(user);

        when(paymentCardRepository.findById(10L))
                .thenReturn(Optional.of(card));

        boolean result = cardSecurity.isCardOwnerOrAdmin(10L, authentication);

        assertTrue(result);
    }

    @Test
    void shouldReturnFalse_whenNotOwner() {
        mockUser(1L);

        User user = new User();
        user.setId(2L);

        PaymentCard card = new PaymentCard();
        card.setUser(user);

        when(paymentCardRepository.findById(10L))
                .thenReturn(Optional.of(card));

        boolean result = cardSecurity.isCardOwnerOrAdmin(10L, authentication);

        assertFalse(result);
    }

    @Test
    void shouldReturnFalse_whenCardNotFound() {
        mockUser(1L);

        when(paymentCardRepository.findById(10L))
                .thenReturn(Optional.empty());

        boolean result = cardSecurity.isCardOwnerOrAdmin(10L, authentication);

        assertFalse(result);
    }
}

