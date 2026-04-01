package com.shopease.checkout.common.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleIllegalArgumentReturnsBadRequest() {
        var ex = new IllegalArgumentException("Email already registered");
        var problem = handler.handleIllegalArgument(ex);

        assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
        assertEquals("Email already registered", problem.getDetail());
        assertEquals("Bad Request", problem.getTitle());
        assertNotNull(problem.getType());
        assertTrue(problem.getType().toString().contains("bad-request"));
    }

    @Test
    void handleValidationReturnsUnprocessableWithFieldErrors() throws Exception {
        var bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "email", "must not be blank"));
        bindingResult.addError(new FieldError("request", "items", "Cart cannot be empty"));

        // Get a MethodParameter for the test
        var methodParam = new MethodParameter(
                this.getClass().getDeclaredMethod("handleValidationReturnsUnprocessableWithFieldErrors"), -1);
        var ex = new MethodArgumentNotValidException(methodParam, bindingResult);

        var problem = handler.handleValidation(ex);

        assertEquals(HttpStatus.UNPROCESSABLE_CONTENT.value(), problem.getStatus());
        assertEquals("Validation Failed", problem.getTitle());
        assertNotNull(problem.getDetail());
        assertTrue(problem.getDetail().contains("email: must not be blank"));
        assertTrue(problem.getDetail().contains("items: Cart cannot be empty"));
        assertNotNull(problem.getType());
        assertTrue(problem.getType().toString().contains("validation"));
    }

    @Test
    void handleGeneralReturnsInternalServerError() {
        var ex = new RuntimeException("Unexpected database failure");
        var problem = handler.handleGeneral(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), problem.getStatus());
        assertEquals("An unexpected error occurred", problem.getDetail());
        assertEquals("Internal Server Error", problem.getTitle());
        assertNotNull(problem.getType());
        assertTrue(problem.getType().toString().contains("internal"));
    }

    @Test
    void handleGeneralDoesNotLeakExceptionDetails() {
        var ex = new RuntimeException("SQL syntax error at line 42: DROP TABLE users");
        var problem = handler.handleGeneral(ex);

        assertNotNull(problem.getDetail());
        assertFalse(problem.getDetail().contains("SQL"));
        assertFalse(problem.getDetail().contains("DROP TABLE"));
        assertEquals("An unexpected error occurred", problem.getDetail());
    }
}
