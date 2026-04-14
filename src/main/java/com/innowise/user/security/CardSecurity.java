package com.innowise.user.security;


import com.innowise.user.repository.PaymentCardRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("cardSecurity")
public class CardSecurity {

    private final PaymentCardRepository paymentCardRepository;

    public CardSecurity(PaymentCardRepository paymentCardRepository) {
        this.paymentCardRepository = paymentCardRepository;
    }

    public boolean isCardOwnerOrAdmin(Long cardId, Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return true;
        }

        return paymentCardRepository.findById(cardId)
                .map(card -> card.getUser().getId().equals(currentUserId))
                .orElse(false);
    }

    public boolean isUserSelfOrAdmin(Long targetUserId, Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        return isAdmin || currentUserId.equals(targetUserId);
    }
}

