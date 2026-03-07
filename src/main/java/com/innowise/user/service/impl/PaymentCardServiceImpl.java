package com.innowise.user.service.impl;

import com.innowise.user.entity.PaymentCard;
import com.innowise.user.entity.User;
import com.innowise.user.repository.PaymentCardRepository;
import com.innowise.user.repository.UserRepository;
import com.innowise.user.service.PaymentCardService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PaymentCardServiceImpl implements PaymentCardService {

    private final PaymentCardRepository paymentCardRepository;
    private final UserRepository userRepository;

    public PaymentCardServiceImpl(PaymentCardRepository paymentCardRepository, UserRepository userRepository) {
        this.paymentCardRepository = paymentCardRepository;
        this.userRepository = userRepository;
    }

    @Override
    public PaymentCard createCard(PaymentCard card) {
        User user = card.getUser();
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("Card must be associated with an existing user");
        }

        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        int currentCards = paymentCardRepository.countByUserId(existingUser.getId());
        if (currentCards >= 5) {
            throw new IllegalStateException("User cannot have more than 5 cards");
        }

        card.setUser(existingUser);
        return paymentCardRepository.save(card);
    }

    @Override
    public PaymentCard getCardById(Long id) {
        return paymentCardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment card not found"));
    }

    @Override
    public List<PaymentCard> getCardsByUserId(Long userId) {
        return paymentCardRepository.findByUserId(userId);
    }

    @Override
    public PaymentCard updateCard(Long id, PaymentCard card) {
        PaymentCard existing = getCardById(id);
        existing.setNumber(card.getNumber());
        existing.setHolder(card.getHolder());
        existing.setExpirationDate(card.getExpirationDate());
        existing.setActive(card.getActive());
        return paymentCardRepository.save(existing);
    }

    @Override
    public void activateCard(Long id) {
        PaymentCard card = getCardById(id);
        card.setActive(true);
        paymentCardRepository.save(card);
    }

    @Override
    public void deactivateCard(Long id) {
        PaymentCard card = getCardById(id);
        card.setActive(false);
        paymentCardRepository.save(card);
    }

    @Override
    public Page<PaymentCard> getAllCards(Pageable pageable) {
        return paymentCardRepository.findAll(pageable);
    }

    @Override
    public List<PaymentCard> getAllActiveCards() {
        return paymentCardRepository.findAllActiveCards();
    }

    @Override
    public PaymentCard getCardByNumber(String number) {
        return paymentCardRepository.findByNumberNative(number)
                .orElseThrow(() -> new IllegalArgumentException("Card not found"));
    }
}
