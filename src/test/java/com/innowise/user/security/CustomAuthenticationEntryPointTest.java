package com.innowise.user.security;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class CustomAuthenticationEntryPointTest {

    private CustomAuthenticationEntryPoint entryPoint;

    @Mock
    private HttpServletRequest request;

    @Mock
    private AuthenticationException exception;

    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        entryPoint = new CustomAuthenticationEntryPoint();
        response = new MockHttpServletResponse();
    }

    @Test
    void shouldReturn401_andWriteJsonResponse() throws Exception {

        entryPoint.commence(request, response, exception);

        assertEquals(401, response.getStatus());
        assertEquals("application/json", response.getContentType());

        String body = response.getContentAsString();
        assert(body.contains("Unauthorized"));
        assert(body.contains("Authentication required"));
    }
}
