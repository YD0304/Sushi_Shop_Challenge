package com.YD0304.sushi_shop.exception; 

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.YD0304.sushi_shop.exception.GlobalExceptionHandler;
import com.YD0304.sushi_shop.dto.CodeResponse;

public class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleIllegalArgument() {
        IllegalArgumentException ex =
                new IllegalArgumentException("Invalid argument");

        ResponseEntity<CodeResponse> response =
                handler.handleIllegalArgument(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().getCode());
        assertEquals("Invalid argument", response.getBody().getMsg());
    }

    @Test
    void handleIllegalState() {
        IllegalStateException ex =
                new IllegalStateException("Invalid state");

        ResponseEntity<CodeResponse> response =
                handler.handleIllegalState(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().getCode());
        assertEquals("Invalid state", response.getBody().getMsg());
    }
@Test
void handleGeneric() {
    Exception ex = new Exception("Unexpected");

    ResponseEntity<CodeResponse> response =
            handler.handleGeneric(ex);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,
            response.getStatusCode());
    assertEquals(500, response.getBody().getCode());
    assertEquals("Internal server error",
            response.getBody().getMsg());
}


@Test
void handleTypeMismatch() {
    MethodArgumentTypeMismatchException ex =
            mock(MethodArgumentTypeMismatchException.class);

    when(ex.getValue()).thenReturn("California Roll");
    when(ex.getName()).thenReturn("id");

    ResponseEntity<CodeResponse> response =
            handler.handleTypeMismatch(ex);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals(400, response.getBody().getCode());
    assertEquals(
        "Invalid value 'California Roll' for parameter 'id'",
        response.getBody().getMsg()
    );
}}

