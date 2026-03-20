package com.innowise.user.service;

import com.innowise.user.dto.card.PaymentCardRequestDto;
import com.innowise.user.dto.card.PaymentCardResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface PaymentCardService {

    PaymentCardResponseDto createCard(PaymentCardRequestDto dto);

    PaymentCardResponseDto getCardById(Long id);

    List<PaymentCardResponseDto> getCardsByUserId(Long userId);

    PaymentCardResponseDto updateCard(Long id, PaymentCardRequestDto card);

    void activateCard(Long id);

    void deactivateCard(Long id);

    Page<PaymentCardResponseDto> getAllCards(Pageable pageable);

    List<PaymentCardResponseDto> getAllActiveCards();

    PaymentCardResponseDto getCardByNumber(String number);
}
