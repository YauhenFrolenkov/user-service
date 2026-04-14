package com.innowise.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.user.dto.user.UserRequestDto;
import com.innowise.user.dto.user.UserResponseDto;
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


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserControllerIT {

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

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private CardSecurity cardSecurity;

    private UserRequestDto userRequestDto;
    private static long userIdSequence = 1;

    @BeforeEach
    void setUp() {

        userRepository.deleteAll();

        userRequestDto = new UserRequestDto();
        userRequestDto.setId(userIdSequence++);
        userRequestDto.setName("Yauhen");
        userRequestDto.setSurname("Fraliankou");
        userRequestDto.setEmail("yauhen@example.com");

        when(cardSecurity.isUserSelfOrAdmin(anyLong(), any())).thenReturn(true);
        when(cardSecurity.isCardOwnerOrAdmin(anyLong(), any())).thenReturn(true);

    }

    private UserResponseDto createUser() throws Exception {
        String jsonRequest = objectMapper.writeValueAsString(userRequestDto);

        String content = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(content, UserResponseDto.class);
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateUser() throws Exception {
        UserResponseDto createdUser = createUser();

        mockMvc.perform(get("/users/{id}", createdUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Yauhen"))
                .andExpect(jsonPath("$.surname").value("Fraliankou"))
                .andExpect(jsonPath("$.email").value("yauhen@example.com"))
                .andExpect(jsonPath("$.active").value(true));

    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetUserById() throws Exception {
        UserResponseDto createdUser = createUser();

        mockMvc.perform(get("/users/{id}", createdUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdUser.getId()))
                .andExpect(jsonPath("$.name").value("Yauhen"))
                .andExpect(jsonPath("$.surname").value("Fraliankou"))
                .andExpect(jsonPath("$.email").value("yauhen@example.com"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.cards").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetUsers() throws Exception {
        createUser();

        mockMvc.perform(get("/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("Yauhen"))
                .andExpect(jsonPath("$.content[0].cards").isArray());
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateUser() throws Exception {
        UserResponseDto createdUser = createUser();

        UserRequestDto updatedDto = new UserRequestDto();
        updatedDto.setId(createdUser.getId());
        updatedDto.setName("UpdatedName");
        updatedDto.setSurname("UpdatedSurname");
        updatedDto.setEmail("updated@example.com");

        String updateJson = objectMapper.writeValueAsString(updatedDto);

        mockMvc.perform(put("/users/{id}", createdUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("UpdatedName"))
                .andExpect(jsonPath("$.surname").value("UpdatedSurname"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testActivateDeactivateUser() throws Exception {
        UserResponseDto createdUser = createUser();

        mockMvc.perform(patch("/users/{id}/deactivate", createdUser.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/users/{id}", createdUser.getId()))
                .andExpect(status().isNotFound());

        mockMvc.perform(patch("/users/{id}/activate", createdUser.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/users/{id}", createdUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetUserByEmail() throws Exception {
        createUser();

        mockMvc.perform(get("/users/email")
                        .param("email", "yauhen@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("yauhen@example.com"));
    }

}
