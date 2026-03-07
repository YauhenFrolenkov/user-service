package com.innowise.user.service;

import com.innowise.user.entity.PaymentCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface PaymentCardService {

    PaymentCard createCard(PaymentCard card);

    PaymentCard getCardById(Long id);

    List<PaymentCard> getCardsByUserId(Long userId);

    PaymentCard updateCard(Long id, PaymentCard card);

    void activateCard(Long id);

    void deactivateCard(Long id);

    Page<PaymentCard> getAllCards(Pageable pageable);

    List<PaymentCard> getAllActiveCards();

    PaymentCard getCardByNumber(String number);
}
