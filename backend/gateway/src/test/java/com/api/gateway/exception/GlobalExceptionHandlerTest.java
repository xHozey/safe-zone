package com.api.gateway.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsMissingRoutesToNotFound() {
        var response = handler.handleNotFound(mock(NoResourceFoundException.class));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(Map.of("status", 404, "message", "Route not found"), response.getBody());
    }

    @Test
    void preservesResponseStatusAndReason() {
        var response = handler.handleResponseStatus(new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed"));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(Map.of("status", 403, "message", "Not allowed"), response.getBody());
    }

    @Test
    void usesFallbackMessageWhenResponseStatusHasNoReason() {
        var response = handler.handleResponseStatus(new ResponseStatusException(HttpStatus.BAD_REQUEST));

        assertEquals(Map.of("status", 400, "message", "Request failed"), response.getBody());
    }

    @Test
    void hidesUnexpectedExceptionDetails() {
        var response = handler.handleUnexpected(new IllegalStateException("private detail"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(Map.of("status", 500, "message", "An unexpected server error occurred"), response.getBody());
    }
}
