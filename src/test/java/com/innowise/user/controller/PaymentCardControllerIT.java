package com.innowise.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.user.dto.card.PaymentCardRequestDto;
import com.innowise.user.dto.card.PaymentCardResponseDto;
import com.innowise.user.entity.User;
import com.innowise.user.repository.PaymentCardRepository;
import com.innowise.user.repository.UserRepository;
import com.innowise.user.security.CardSecurity;
import com.innowise.user.security.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PaymentCardControllerIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("user")
            .withPassword("password");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentCardRepository paymentCardRepository;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private CardSecurity cardSecurity;

    private PaymentCardRequestDto cardRequestDto;

    @BeforeEach
    void setUp() {

        when(cardSecurity.isUserSelfOrAdmin(anyLong(), any())).thenReturn(true);
        when(cardSecurity.isCardOwnerOrAdmin(anyLong(), any())).thenReturn(true);

        paymentCardRepository.deleteAll();
        userRepository.deleteAll();

        cardRequestDto = new PaymentCardRequestDto();
        cardRequestDto.setNumber("1111222233334444");
        cardRequestDto.setHolder("Yauhen Fraliankou");
        cardRequestDto.setExpirationDate(LocalDate.of(2030, 12, 31));
    }

    private User createUser() {
        User user = new User();
        user.setName("Yauhen");
        user.setSurname("Fraliankou");
        user.setEmail("yauhen@example.com");
        user.setActive(true);

        return userRepository.save(user);
    }

    private PaymentCardResponseDto createCard(User user) throws Exception {

        cardRequestDto.setUserId(user.getId());

        String jsonRequest = objectMapper.writeValueAsString(cardRequestDto);

        String content = mockMvc.perform(post("/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(content, PaymentCardResponseDto.class);
    }

    @Test
    @WithMockUser(username = "1", roles = {"ADMIN"})
    void testCreateCard() throws Exception {
        User savedUser = createUser();
        cardRequestDto.setUserId(savedUser.getId());

        String jsonRequest = objectMapper.writeValueAsString(cardRequestDto);

        mockMvc.perform(post("/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.number").value("1111222233334444"))
                .andExpect(jsonPath("$.holder").value("Yauhen Fraliankou"))
                .andExpect(jsonPath("$.userId").value(savedUser.getId()));
    }

    @Test
    @WithMockUser(username = "1", roles = {"ADMIN"})
    void testCreateSixthCard_ShouldReturnError() throws Exception {
        User savedUser = createUser();

        for (int i = 0; i < 5; i++) {
            createCard(savedUser);
        }

        cardRequestDto.setUserId(savedUser.getId());
        cardRequestDto.setNumber("6666777788889999");
        String jsonRequest = objectMapper.writeValueAsString(cardRequestDto);

        mockMvc.perform(post("/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("User " + savedUser.getId() + " cannot have more than 5 cards"));
    }

    @Test
    @WithMockUser(username = "1", roles = {"ADMIN"})
    void testGetCardById() throws Exception {
        User savedUser = createUser();
        PaymentCardResponseDto createdCard = createCard(savedUser);

        mockMvc.perform(get("/cards/{id}", createdCard.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(createdCard.getId()))
                .andExpect(jsonPath("$.number").value("1111222233334444"))
                .andExpect(jsonPath("$.userId").value(savedUser.getId()));
    }

    @Test
    @WithMockUser(username = "1", roles = {"ADMIN"})
    void testGetCardsByUserId() throws Exception {
        User savedUser = createUser();
        createCard(savedUser);

        mockMvc.perform(get("/cards/user/{userId}", savedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].holder").value("Yauhen Fraliankou"))
                .andExpect(jsonPath("$[0].userId").value(savedUser.getId()));
    }

    @Test
    @WithMockUser(username = "1", roles = {"ADMIN"})
    void testUpdateCard() throws Exception {
        User savedUser = createUser();
        PaymentCardResponseDto createdCard = createCard(savedUser);

        PaymentCardRequestDto updatedDto = new PaymentCardRequestDto();
        updatedDto.setNumber("9999888877776666");
        updatedDto.setHolder("Updated Holder");
        updatedDto.setExpirationDate(LocalDate.of(2031, 12, 31));
        updatedDto.setUserId(savedUser.getId());

        String updateJson = objectMapper.writeValueAsString(updatedDto);

        mockMvc.perform(put("/cards/{id}", createdCard.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.holder").value("Updated Holder"));
    }

    @Test
    @WithMockUser(username = "1", roles = {"ADMIN"})
    void testActivateDeactivateCard() throws Exception {
        User savedUser = createUser();
        PaymentCardResponseDto createdCard = createCard(savedUser);

        mockMvc.perform(patch("/cards/{id}/deactivate", createdCard.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(patch("/cards/{id}/activate", createdCard.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "1", roles = {"ADMIN"})
    void testGetAllCards() throws Exception {
        User savedUser = createUser();
        createCard(savedUser);

        mockMvc.perform(get("/cards")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].number").value("1111222233334444"))
                .andExpect(jsonPath("$.content[0].userId").value(savedUser.getId()));
    }

    @Test
    @WithMockUser(username = "1", roles = {"ADMIN"})
    void testGetAllActiveCards() throws Exception {
        User savedUser = createUser();
        createCard(savedUser);

        mockMvc.perform(get("/cards/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].number").value("1111222233334444"))
                .andExpect(jsonPath("$[0].userId").value(savedUser.getId()));
    }

    @Test
    @WithMockUser(username = "1", roles = {"ADMIN"})
    void testGetCardByNumber() throws Exception {
        User savedUser = createUser();
        PaymentCardResponseDto createdCard = createCard(savedUser);

        mockMvc.perform(get("/cards/number")
                        .param("number", createdCard.getNumber()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(createdCard.getNumber()))
                .andExpect(jsonPath("$.userId").value(savedUser.getId()));
    }
}
