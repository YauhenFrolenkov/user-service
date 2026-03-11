package com.innowise.user.service.impl;

import com.innowise.user.dto.card.PaymentCardRequestDto;
import com.innowise.user.dto.card.PaymentCardResponseDto;
import com.innowise.user.entity.PaymentCard;
import com.innowise.user.entity.User;
import com.innowise.user.exception.MaxCardsExceededException;
import com.innowise.user.mapper.PaymentCardMapper;
import com.innowise.user.repository.PaymentCardRepository;
import com.innowise.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentCardServiceImplTest {

    @Mock
    private PaymentCardRepository paymentCardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentCardMapper paymentCardMapper;

    @InjectMocks
    private PaymentCardServiceImpl paymentCardService;

    private User user;
    private PaymentCard card;
    private PaymentCardRequestDto cardRequestDto;
    private PaymentCardResponseDto cardResponseDto;

    @BeforeEach
    void setUp() {

        user = new User();
        user.setId(1L);
        user.setName("Yauhen");
        user.setEmail("yauhen@example.com");
        user.setActive(true);

        card = new PaymentCard();
        card.setId(1L);
        card.setNumber("1234-5678-9012-3456");
        card.setHolder("Yauhen");
        card.setExpirationDate(LocalDate.of(2030, 12, 31));
        card.setActive(true);
        card.setUser(user);

        cardRequestDto = new PaymentCardRequestDto();
        cardRequestDto.setNumber("1234-5678-9012-3456");
        cardRequestDto.setHolder("Yauhen");
        cardRequestDto.setExpirationDate(LocalDate.of(2030, 12, 31));
        cardRequestDto.setUserId(1L);

        cardResponseDto = new PaymentCardResponseDto();
        cardResponseDto.setId(1L);
        cardResponseDto.setNumber("1234-5678-9012-3456");
        cardResponseDto.setHolder("Yauhen");
        cardResponseDto.setExpirationDate(LocalDate.of(2030, 12, 31));
        cardResponseDto.setActive(true);
    }

    @Test
    void testCreateCard_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(paymentCardRepository.countByUserId(1L)).thenReturn(0);
        when(paymentCardMapper.toEntity(cardRequestDto)).thenReturn(card);
        when(paymentCardRepository.save(card)).thenReturn(card);
        when(paymentCardMapper.toDto(card)).thenReturn(cardResponseDto);

        PaymentCardResponseDto result = paymentCardService.createCard(cardRequestDto);

        assertNotNull(result);
        assertEquals("1234-5678-9012-3456", result.getNumber());
        assertTrue(result.getActive());
        verify(paymentCardRepository, times(1)).save(card);
    }

    @Test
    void testCreateCard_MaxCardsExceeded() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(paymentCardRepository.countByUserId(1L)).thenReturn(5);

        assertThrows(MaxCardsExceededException.class,
                () -> paymentCardService.createCard(cardRequestDto));
    }

    @Test
    void testGetCardById_Success() {
        when(paymentCardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(paymentCardMapper.toDto(card)).thenReturn(cardResponseDto);

        PaymentCardResponseDto result = paymentCardService.getCardById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(paymentCardRepository, times(1)).findById(1L);
    }

    @Test
    void testGetCardsByUserId_Success() {
        when(paymentCardRepository.findByUserId(1L)).thenReturn(List.of(card));
        when(paymentCardMapper.toDto(card)).thenReturn(cardResponseDto);

        List<PaymentCardResponseDto> result = paymentCardService.getCardsByUserId(1L);

        assertEquals(1, result.size());
        verify(paymentCardRepository, times(1)).findByUserId(1L);
    }

    @Test
    void testUpdateCard_Success() {
        PaymentCardRequestDto updateDto = new PaymentCardRequestDto();
        updateDto.setNumber("9999-8888-7777-6666");
        updateDto.setHolder("Yauhen Updated");
        updateDto.setExpirationDate(LocalDate.of(2031, 1, 31));
        updateDto.setUserId(1L);

        when(paymentCardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(paymentCardRepository.save(card)).thenReturn(card);
        when(paymentCardMapper.toDto(card)).thenReturn(cardResponseDto);

        PaymentCardResponseDto result = paymentCardService.updateCard(1L, updateDto);

        assertEquals("9999-8888-7777-6666", card.getNumber());
        assertEquals("Yauhen Updated", card.getHolder());
        assertEquals(LocalDate.of(2031, 1, 31), card.getExpirationDate());

        assertNotNull(result);
        verify(paymentCardRepository, times(1)).save(card);
    }

    @Test
    void testActivateDeactivateCard() {

        card.setActive(false);
        when(paymentCardRepository.findById(1L)).thenReturn(Optional.of(card));
        paymentCardService.activateCard(1L);
        assertTrue(card.getActive());
        verify(paymentCardRepository, times(1)).save(card);

        paymentCardService.deactivateCard(1L);
        assertFalse(card.getActive());
        verify(paymentCardRepository, times(2)).save(card);
    }

    @Test
    void testGetAllCards() {
        Page<PaymentCard> page = new PageImpl<>(List.of(card));
        when(paymentCardRepository.findAll(PageRequest.of(0, 10))).thenReturn(page);
        when(paymentCardMapper.toDto(card)).thenReturn(cardResponseDto);

        Page<PaymentCardResponseDto> result = paymentCardService.getAllCards(PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testGetAllActiveCards() {
        when(paymentCardRepository.findAllActiveCards()).thenReturn(List.of(card));
        when(paymentCardMapper.toDto(card)).thenReturn(cardResponseDto);

        List<PaymentCardResponseDto> result = paymentCardService.getAllActiveCards();

        assertEquals(1, result.size());
        assertTrue(result.get(0).getActive());
    }

    @Test
    void testGetCardByNumber_Success() {
        when(paymentCardRepository.findByNumberNative("1234-5678-9012-3456")).thenReturn(Optional.of(card));
        when(paymentCardMapper.toDto(card)).thenReturn(cardResponseDto);

        PaymentCardResponseDto result = paymentCardService.getCardByNumber("1234-5678-9012-3456");

        assertNotNull(result);
        assertEquals("1234-5678-9012-3456", result.getNumber());
    }

}
