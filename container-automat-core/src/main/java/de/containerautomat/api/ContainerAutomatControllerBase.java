/*
 * Copyright 2024 the original author or authors.
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Objects;
import java.util.stream.Collectors;

/**
 * An abstract base class for Spring REST controllers providing means for
 * error handling.
 */
public abstract class ContainerAutomatControllerBase {

    private static final Log log = LogFactory.getLog(ContainerAutomatControllerBase.class);

    public static final String PROBLEM_DETAIL_JSON_TEMPLATE = """
            {
                "type": "%s",
                "title": "%s",
                "detail": "%s",
                "instance": "%s"
            }
            """;

    @ExceptionHandler
    ResponseEntity<String> handleErrors(Exception e, HttpServletRequest request) {

        var detail = switch (e) {
            case MethodArgumentNotValidException manve -> manve.getFieldErrors()
                    .stream()
                    .map(fieldError -> fieldError.getField() + " " + fieldError.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            default -> e.getMessage();
        };

        var status = switch (e) {
            case MethodArgumentNotValidException ignored -> HttpStatus.BAD_REQUEST;
            case HttpMessageConversionException ignored -> HttpStatus.BAD_REQUEST;
            case IllegalArgumentException ignored -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };

        var responseJson = PROBLEM_DETAIL_JSON_TEMPLATE.formatted(
                request.getRequestURL().append("/").append(e.getClass().getSimpleName().toLowerCase()).toString(),
                e.getClass().getSimpleName(),
                Objects.toString(detail, "No detail message available.").replace("\"", "'"),
                request.getRequestURI());

        log.error("Unable to process request. Returning error message:%n%s".formatted(responseJson));

        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(responseJson);
    }

}
