package com.innowise.user.controller.junit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.user.controller.UserController;
import com.innowise.user.dto.user.UserRequestDto;
import com.innowise.user.dto.user.UserResponseDto;
import com.innowise.user.exception.UserNotFoundException;
import com.innowise.user.security.JwtProvider;
import com.innowise.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(UserControllerTest.Config.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtProvider jwtProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    private UserRequestDto userRequestDto;
    private UserResponseDto userResponseDto;

    @TestConfiguration
    static class Config {
        @Bean
        public UserService userService() {
            return Mockito.mock(UserService.class);
        }
    }

    @BeforeEach
    void setUp() {
        userRequestDto = new UserRequestDto();
        userRequestDto.setName("Yauhen");
        userRequestDto.setSurname("Fraliankou");
        userRequestDto.setBirthDate(LocalDate.of(1990, 1, 1));
        userRequestDto.setEmail("yauhen@example.com");

        userResponseDto = new UserResponseDto();
        userResponseDto.setId(1L);
        userResponseDto.setName("Yauhen");
        userResponseDto.setSurname("Fraliankou");
        userResponseDto.setBirthDate(LocalDate.of(1990, 1, 1));
        userResponseDto.setEmail("yauhen@example.com");
        userResponseDto.setActive(true);
    }

    @Test
    void testGetUserById() throws Exception {
        when(userService.getUserById(1L)).thenReturn(userResponseDto);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("yauhen@example.com"));
    }

    @Test
    void testGetUserByEmail() throws Exception {
        when(userService.getUserByEmail("yauhen@example.com")).thenReturn(Optional.of(userResponseDto));

        mockMvc.perform(get("/users/email")
                        .param("email", "yauhen@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("yauhen@example.com"));
    }

    @Test
    void testGetUserByEmail_NotFound() throws Exception {
        when(userService.getUserByEmail("unknown@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(get("/users/email")
                        .param("email", "unknown@example.com"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetUsers() throws Exception {
        Page<UserResponseDto> page = new PageImpl<>(List.of(userResponseDto));
        when(userService.getUsers(eq(null), eq(null), any()))
                .thenReturn(page);

        mockMvc.perform(get("/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    void testGetUsers_Empty() throws Exception {
        Page<UserResponseDto> emptyPage = new PageImpl<>(Collections.emptyList());
        when(userService.getUsers(eq(null), eq(null), any()))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void testActivateUser() throws Exception {
        doNothing().when(userService).activateUser(1L);

        mockMvc.perform(patch("/users/1/activate"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeactivateUser() throws Exception {
        doNothing().when(userService).deactivateUser(1L);

        mockMvc.perform(patch("/users/1/deactivate"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testActivateUser_NotFound() throws Exception {
        doThrow(new UserNotFoundException(1L))
                .when(userService).activateUser(1L);

        mockMvc.perform(patch("/users/1/activate"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeactivateUser_NotFound() throws Exception {
        doThrow(new UserNotFoundException(1L))
                .when(userService).deactivateUser(1L);

        mockMvc.perform(patch("/users/1/deactivate"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateUser_BadRequest() throws Exception {
        UserRequestDto invalid = new UserRequestDto();
        invalid.setEmail("invalid-email"); // если есть @Email validation

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetUserById_InvalidId() throws Exception {
        mockMvc.perform(get("/users/abc"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testGetUserByEmail_MissingParam() throws Exception {
        mockMvc.perform(get("/users/email"))
                .andExpect(status().isInternalServerError());
    }
}
