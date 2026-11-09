package com.media.mediaService.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsStatusAndMissingRouteExceptions() {
        var statusResponse = handler.handleResponseStatus(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found"));
        var notFoundResponse = handler.handleNotFound(mock(NoResourceFoundException.class));

        assertEquals(Map.of("status", 404, "message", "Image not found"), statusResponse.getBody());
        assertEquals(Map.of("status", 404, "message", "Route not found"), notFoundResponse.getBody());
    }

    @Test
    void mapsUploadAndMalformedBodyExceptions() {
        assertEquals("Invalid upload request",
                handler.handleBadRequest(mock(MethodArgumentNotValidException.class)).getBody().get("message"));
        assertEquals("Invalid upload request",
                handler.handleBadRequest(mock(MultipartException.class)).getBody().get("message"));
        assertEquals("Malformed request body",
                handler.handleBadRequest(mock(HttpMessageNotReadableException.class)).getBody().get("message"));
        assertEquals("Image file must be at most 2 MB",
                handler.handleBadRequest(mock(MaxUploadSizeExceededException.class)).getBody().get("message"));
    }

    @Test
    void usesFallbackForBlankStatusReasonAndHidesUnexpectedDetails() {
        assertEquals("Request failed", handler
                .handleResponseStatus(new ResponseStatusException(HttpStatus.BAD_REQUEST, " "))
                .getBody().get("message"));
        var response = handler.handleUnexpected(new IllegalStateException("private detail"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected server error occurred", response.getBody().get("message"));
    }
}
