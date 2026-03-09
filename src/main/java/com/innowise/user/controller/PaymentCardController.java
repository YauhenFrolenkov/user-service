package com.innowise.user.controller;

import com.innowise.user.dto.card.PaymentCardRequestDto;
import com.innowise.user.dto.card.PaymentCardResponseDto;
import com.innowise.user.service.PaymentCardService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/cards")
public class PaymentCardController {

    private final PaymentCardService paymentCardService;

    public PaymentCardController(PaymentCardService paymentCardService) {
        this.paymentCardService = paymentCardService;
    }

    @PostMapping
    public ResponseEntity<PaymentCardResponseDto> createCard(@Valid @RequestBody PaymentCardRequestDto dto) {
        PaymentCardResponseDto response = paymentCardService.createCard(dto);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentCardResponseDto> getCardById(@PathVariable Long id) {
        PaymentCardResponseDto card = paymentCardService.getCardById(id);
        return ResponseEntity.ok(card);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentCardResponseDto>> getCardsByUserId(@PathVariable Long userId) {
        List<PaymentCardResponseDto> cards = paymentCardService.getCardsByUserId(userId);
        return ResponseEntity.ok(cards);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentCardResponseDto> updateCard(
            @PathVariable Long id,
            @Valid @RequestBody PaymentCardRequestDto dto
    ) {
        PaymentCardResponseDto updated = paymentCardService.updateCard(id, dto);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateCard(@PathVariable Long id) {
        paymentCardService.activateCard(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateCard(@PathVariable Long id) {
        paymentCardService.deactivateCard(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<PaymentCardResponseDto>> getAllCards(Pageable pageable) {
        Page<PaymentCardResponseDto> cards = paymentCardService.getAllCards(pageable);
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/active")
    public ResponseEntity<List<PaymentCardResponseDto>> getAllActiveCards() {
        List<PaymentCardResponseDto> cards = paymentCardService.getAllActiveCards();
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/number")
    public ResponseEntity<PaymentCardResponseDto> getCardByNumber(@RequestParam String number) {
        PaymentCardResponseDto card = paymentCardService.getCardByNumber(number);
        return ResponseEntity.ok(card);
    }

}
