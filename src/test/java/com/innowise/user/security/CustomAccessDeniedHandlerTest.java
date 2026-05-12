package com.innowise.user.security;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class CustomAccessDeniedHandlerTest {

    private CustomAccessDeniedHandler handler;

    @Mock
    private HttpServletRequest request;

    @Mock
    private AccessDeniedException exception;

    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        handler = new CustomAccessDeniedHandler();
        response = new MockHttpServletResponse();
    }

    @Test
    void shouldReturn403_andWriteJsonResponse() throws Exception {

        handler.handle(request, response, exception);

        assertEquals(403, response.getStatus());
        assertEquals("application/json", response.getContentType());

        String body = response.getContentAsString();
        assert(body.contains("Forbidden"));
        assert(body.contains("Access denied"));
    }

}
