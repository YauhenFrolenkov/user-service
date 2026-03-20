package com.innowise.user.controller.junit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.innowise.user.controller.PaymentCardController;
import com.innowise.user.dto.card.PaymentCardRequestDto;
import com.innowise.user.dto.card.PaymentCardResponseDto;
import com.innowise.user.service.PaymentCardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentCardController.class)
@Import(PaymentCardControllerTest.Config.class)
@ExtendWith(SpringExtension.class)
class PaymentCardControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaymentCardService paymentCardService;

    private PaymentCardRequestDto cardRequestDto;
    private PaymentCardResponseDto cardResponseDto;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @TestConfiguration
    static class Config {
        @Bean
        public PaymentCardService paymentCardService() {
            return Mockito.mock(PaymentCardService.class);
        }
    }

    @BeforeEach
    void setUp() {
        cardRequestDto = new PaymentCardRequestDto();
        cardRequestDto.setNumber("1234567890123456"); // ровно 16 цифр
        cardRequestDto.setHolder("Yauhen");
        cardRequestDto.setExpirationDate(LocalDate.of(2030, 12, 31));
        cardRequestDto.setUserId(1L);

        cardResponseDto = new PaymentCardResponseDto();
        cardResponseDto.setId(1L);
        cardResponseDto.setNumber("1234567890123456");
        cardResponseDto.setHolder("Yauhen");
        cardResponseDto.setExpirationDate(LocalDate.of(2030, 12, 31));
        cardResponseDto.setActive(true);
    }

    @Test
    void testGetCardById() throws Exception {
        when(paymentCardService.getCardById(1L)).thenReturn(cardResponseDto);

        mockMvc.perform(get("/cards/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.number").value("1234567890123456"));
    }

    @Test
    void testGetCardsByUserId() throws Exception {
        when(paymentCardService.getCardsByUserId(1L)).thenReturn(List.of(cardResponseDto));

        mockMvc.perform(get("/cards/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].number").value("1234567890123456"));
    }

    @Test
    void testUpdateCard() throws Exception {
        when(paymentCardService.updateCard(eq(1L), any())).thenReturn(cardResponseDto);

        mockMvc.perform(put("/cards/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.number").value("1234567890123456"));
    }

    @Test
    void testActivateCard() throws Exception {
        doNothing().when(paymentCardService).activateCard(1L);

        mockMvc.perform(patch("/cards/1/activate"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeactivateCard() throws Exception {
        doNothing().when(paymentCardService).deactivateCard(1L);

        mockMvc.perform(patch("/cards/1/deactivate"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetAllActiveCards() throws Exception {
        when(paymentCardService.getAllActiveCards()).thenReturn(List.of(cardResponseDto));

        mockMvc.perform(get("/cards/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].number").value("1234567890123456"));
    }
}
