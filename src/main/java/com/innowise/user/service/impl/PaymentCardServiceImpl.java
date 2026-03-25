package com.innowise.user.service.impl;

import com.innowise.user.dto.card.PaymentCardRequestDto;
import com.innowise.user.dto.card.PaymentCardResponseDto;
import com.innowise.user.entity.PaymentCard;
import com.innowise.user.entity.User;
import com.innowise.user.exception.MaxCardsExceededException;
import com.innowise.user.exception.PaymentCardNotFoundException;
import com.innowise.user.exception.UserNotFoundException;
import com.innowise.user.mapper.PaymentCardMapper;
import com.innowise.user.repository.PaymentCardRepository;
import com.innowise.user.repository.UserRepository;
import com.innowise.user.service.PaymentCardService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PaymentCardServiceImpl implements PaymentCardService {

    private final PaymentCardRepository paymentCardRepository;
    private final UserRepository userRepository;
    private final PaymentCardMapper paymentCardMapper;

    public PaymentCardServiceImpl(PaymentCardRepository paymentCardRepository, UserRepository userRepository, PaymentCardMapper paymentCardMapper) {
        this.paymentCardRepository = paymentCardRepository;
        this.userRepository = userRepository;
        this.paymentCardMapper = paymentCardMapper;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "users", key = "#dto.userId"),
            @CacheEvict(value = "cardsByUser", key = "#dto.userId")
    })
    public PaymentCardResponseDto createCard(PaymentCardRequestDto dto) {
        User user = userRepository.findByIdForUpdate(dto.getUserId())
                .orElseThrow(() -> new UserNotFoundException(dto.getUserId()));

        int currentCards = paymentCardRepository.countByUserIdAndActiveTrue(user.getId());
        if (currentCards >= 5) {
            throw new MaxCardsExceededException(user.getId());
        }

        PaymentCard card = paymentCardMapper.toEntity(dto);
        card.setUser(user);
        PaymentCard saved = paymentCardRepository.save(card);

        return paymentCardMapper.toDto(saved);
    }

    @Override
    @Cacheable(value = "cards", key = "#id")
    public PaymentCardResponseDto getCardById(Long id) {
        PaymentCard card = getCardEntityById(id);
        return paymentCardMapper.toDto(card);
    }

    @Override
    @Cacheable(value = "cardsByUser", key = "#userId")
    public List<PaymentCardResponseDto> getCardsByUserId(Long userId) {
        return paymentCardRepository.findByUserIdAndActiveTrue(userId).stream().map(paymentCardMapper::toDto).toList();
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "users", key = "#result.userId"),
            @CacheEvict(value = "cardsByUser", key = "#result.userId"),
            @CacheEvict(value = "cards", key = "#id")
    })
    public PaymentCardResponseDto updateCard(Long id, PaymentCardRequestDto dto) {
        PaymentCard existing = getCardEntityById(id);
        existing.setNumber(dto.getNumber());
        existing.setHolder(dto.getHolder());
        existing.setExpirationDate(dto.getExpirationDate());

        PaymentCard saved = paymentCardRepository.save(existing);
        return paymentCardMapper.toDto(saved);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "users", allEntries = true),
            @CacheEvict(value = "cardsByUser", allEntries = true),
            @CacheEvict(value = "cards", key = "#id")
    })
    public void activateCard(Long id) {
        PaymentCard card = getCardEntityById(id);
        card.setActive(true);
        paymentCardRepository.save(card);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "users", allEntries = true),
            @CacheEvict(value = "cardsByUser", allEntries = true),
            @CacheEvict(value = "cards", key = "#id")
    })
    public void deactivateCard(Long id) {
        PaymentCard card = getCardEntityById(id);
        card.setActive(false);
        paymentCardRepository.save(card);
    }

    @Override
    public Page<PaymentCardResponseDto> getAllCards(Pageable pageable) {
        return paymentCardRepository.findAll(pageable).map(paymentCardMapper::toDto);
    }

    @Override
    public List<PaymentCardResponseDto> getAllActiveCards() {
        return paymentCardRepository.findAllActiveCards().stream().map(paymentCardMapper::toDto).toList();
    }

    @Override
    public PaymentCardResponseDto getCardByNumber(String number) {
        PaymentCard card = paymentCardRepository.findByNumberNative(number)
                .orElseThrow(() -> new PaymentCardNotFoundException(number));
        return paymentCardMapper.toDto(card);
    }

    private PaymentCard getCardEntityById(Long id) {
        return paymentCardRepository.findByIdIncludingInactive(id)
                .orElseThrow(() -> new PaymentCardNotFoundException(id));
    }
}
