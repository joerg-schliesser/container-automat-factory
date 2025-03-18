/*
 * Copyright 2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.containerautomat.api;

import jakarta.servlet.http.HttpServletRequest;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * A test suite for testing methods defined in the {@link ContainerAutomatControllerBase}
 * class for handling REST request errors in the generated Container-Automat application.
 */
class ContainerAutomatControllerBaseTests {


    static final String TEST_REQUEST_URI = "/testrequests";
    static final String TEST_REQUEST_URL = "http://localhost:8080" + TEST_REQUEST_URI;

    static class ContainerAutomatControlerBaseImpl extends ContainerAutomatControllerBase {

    }

    private ContainerAutomatControlerBaseImpl controllerBase;

    private HttpServletRequest requestMock;


    @BeforeEach
    void setUp() {

        requestMock = Mockito.mock(HttpServletRequest.class);
        Mockito.when(requestMock.getRequestURL()).thenReturn(new StringBuffer(TEST_REQUEST_URL));
        Mockito.when(requestMock.getRequestURI()).thenReturn(TEST_REQUEST_URI);

        controllerBase = new ContainerAutomatControlerBaseImpl();
    }

    @Test
    void handle_method_argument_not_valid() {

        var testObjectName = "testObject";
        var testFieldName1 = "testField1";
        var testFieldName2 = "testField2";
        var testFieldErrorMessage1 = "field error message 1";
        var testFieldErrorMessage2 = "field error message 2";
        var testExceptionMock = Mockito.mock(MethodArgumentNotValidException.class);
        Mockito.when(testExceptionMock.getFieldErrors()).thenReturn(List.of(
                new FieldError(testObjectName, testFieldName1, testFieldErrorMessage1),
                new FieldError(testObjectName, testFieldName2, testFieldErrorMessage2)
        ));

        var response = controllerBase.handleErrors(testExceptionMock, requestMock);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        var expectedType = TEST_REQUEST_URL + "/" + MethodArgumentNotValidException.class.getSimpleName().toLowerCase();
        var expectedTitle = MethodArgumentNotValidException.class.getSimpleName();
        var expectedDetail = "%s %s, %s %s".formatted(testFieldName1, testFieldErrorMessage1, testFieldName2, testFieldErrorMessage2);

        var errorJsonObject = assertDoesNotThrow(() -> new JSONObject(response.getBody()));
        var errorType = assertDoesNotThrow(() -> errorJsonObject.getString("type"));
        var errorTitle = assertDoesNotThrow(() -> errorJsonObject.getString("title"));
        var errorDetail = assertDoesNotThrow(() -> errorJsonObject.getString("detail"));
        var errorInstance = assertDoesNotThrow(() -> errorJsonObject.getString("instance"));

        assertEquals(expectedType, errorType);
        assertEquals(expectedTitle, errorTitle);
        assertEquals(expectedDetail, errorDetail);
        assertEquals(TEST_REQUEST_URI, errorInstance);
    }

    @Test
    void handle_http_message_conversion() {

        var testErrorMessage = "http message conversion error message";
        var testException = new HttpMessageConversionException(testErrorMessage);

        var response = controllerBase.handleErrors(testException, requestMock);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        var expectedType = TEST_REQUEST_URL + "/" + HttpMessageConversionException.class.getSimpleName().toLowerCase();
        var expectedTitle = HttpMessageConversionException.class.getSimpleName();

        var errorJsonObject = assertDoesNotThrow(() -> new JSONObject(response.getBody()));
        var errorType = assertDoesNotThrow(() -> errorJsonObject.getString("type"));
        var errorTitle = assertDoesNotThrow(() -> errorJsonObject.getString("title"));
        var errorDetail = assertDoesNotThrow(() -> errorJsonObject.getString("detail"));
        var errorInstance = assertDoesNotThrow(() -> errorJsonObject.getString("instance"));

        assertEquals(expectedType, errorType);
        assertEquals(expectedTitle, errorTitle);
        assertEquals(testErrorMessage, errorDetail);
        assertEquals(TEST_REQUEST_URI, errorInstance);
    }

    @Test
    void handle_illegal_argument() {

        var testErrorMessage = "illegal argument error message";
        var testException = new IllegalArgumentException(testErrorMessage);

        var response = controllerBase.handleErrors(testException, requestMock);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        var expectedType = TEST_REQUEST_URL + "/" + IllegalArgumentException.class.getSimpleName().toLowerCase();
        var expectedTitle = IllegalArgumentException.class.getSimpleName();

        var errorJsonObject = assertDoesNotThrow(() -> new JSONObject(response.getBody()));
        var errorType = assertDoesNotThrow(() -> errorJsonObject.getString("type"));
        var errorTitle = assertDoesNotThrow(() -> errorJsonObject.getString("title"));
        var errorDetail = assertDoesNotThrow(() -> errorJsonObject.getString("detail"));
        var errorInstance = assertDoesNotThrow(() -> errorJsonObject.getString("instance"));

        assertEquals(expectedType, errorType);
        assertEquals(expectedTitle, errorTitle);
        assertEquals(testErrorMessage, errorDetail);
        assertEquals(TEST_REQUEST_URI, errorInstance);
    }

    @Test
    void handle_general_exception() {

        var testErrorMessage = "exception error message";
        var testException = new Exception(testErrorMessage);

        var response = controllerBase.handleErrors(testException, requestMock);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        var expectedType = TEST_REQUEST_URL + "/" + Exception.class.getSimpleName().toLowerCase();
        var expectedTitle = Exception.class.getSimpleName();

        var errorJsonObject = assertDoesNotThrow(() -> new JSONObject(response.getBody()));
        var errorType = assertDoesNotThrow(() -> errorJsonObject.getString("type"));
        var errorTitle = assertDoesNotThrow(() -> errorJsonObject.getString("title"));
        var errorDetail = assertDoesNotThrow(() -> errorJsonObject.getString("detail"));
        var errorInstance = assertDoesNotThrow(() -> errorJsonObject.getString("instance"));

        assertEquals(expectedType, errorType);
        assertEquals(expectedTitle, errorTitle);
        assertEquals(testErrorMessage, errorDetail);
        assertEquals(TEST_REQUEST_URI, errorInstance);
    }

}