/*
 * Copyright 2024-2025 the original author or authors.
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

import com.fasterxml.jackson.databind.ObjectMapper;
import de.containerautomat.config.ContainerAutomatCoreConfig;
import de.containerautomat.processing.ContainerAutomatMessaging;
import de.containerautomat.processing.ContainerAutomatProcessingInstance;
import de.containerautomat.processing.ContainerAutomatRequest;
import de.containerautomat.processing.ContainerAutomatStorage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * A test suite for the class {@link ContainerAutomatEntryController}
 * for receiving requests for processing by the deterministic
 * finite automaton implemented by the generated application.
 */
@ContextConfiguration(classes = ContainerAutomatCoreConfig.class)
@TestPropertySource(properties = ContainerAutomatCoreConfig.PROPERTY_CONTAINERAUTOMAT_APP_IS_ENTRY + "=true")
@WebMvcTest(ContainerAutomatEntryController.class)
class ContainerAutomatEntryControllerTests {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @EqualsAndHashCode
    static class ProcessingInstanceImpl implements ContainerAutomatProcessingInstance {

        private String processingInstanceId;
        private Instant creationTime;
        private String input;
        private String description;
    }


    @MockBean
    ContainerAutomatStorage storage;

    @MockBean
    ContainerAutomatMessaging messaging;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ContainerAutomatEntryController containerAutomatEntryController;


    @Test
    void object_mapper_is_available() {

        assertNotNull(objectMapper);
    }

    @Test
    void rest_controller_is_available() {

        assertNotNull(containerAutomatEntryController);
    }

    @Test
    void post_valid_request_returns_processing_instance() {

        var testInput = "0101";
        var testDescription = "post_valid_request_returns_processing_instance";
        var testReqestJson = """
                {
                  "input": "%s",
                  "description": "%s"
                }
                """.formatted(testInput, testDescription);
        var testUuid = UUID.randomUUID().toString();
        var testTimestamp = Instant.now();
        var testProcessingInstance = ProcessingInstanceImpl.builder()
                .processingInstanceId(testUuid)
                .creationTime(testTimestamp)
                .input(testInput)
                .description(testDescription)
                .build();

        Mockito.when(storage.createProcessingInstance(Mockito.any(ContainerAutomatRequest.class))).thenReturn(testProcessingInstance);

        try {
            var resultJson = mockMvc.perform(post(ContainerAutomatEntryController.PATH_REQUESTS).content(testReqestJson)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andReturn().getResponse().getContentAsString();

            var resultObject = assertDoesNotThrow(() -> objectMapper.readValue(resultJson, ProcessingInstanceImpl.class));
            assertEquals(testProcessingInstance, resultObject);

        } catch (Exception e) {
            fail("Unexpected Exception: %s".formatted(e.getMessage()), e);
        }
    }

    @Test
    void post_invalid_request_returns_error_details() {

        var testReqestJson = """
                {
                  "input": "0101",
                  "description": "post_invalid_request_returns_error_details
                }
                """;

        try {
            var errorJson = mockMvc.perform(post(ContainerAutomatEntryController.PATH_REQUESTS).content(testReqestJson)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is4xxClientError())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andReturn().getResponse().getContentAsString();

            var errorJsonObject = assertDoesNotThrow(() -> new JSONObject(errorJson));
            var errorType = assertDoesNotThrow(() -> errorJsonObject.getString("type"));
            var errorTitle = assertDoesNotThrow(() -> errorJsonObject.getString("title"));
            var errorDetail = assertDoesNotThrow(() -> errorJsonObject.getString("detail"));
            var errorInstance = assertDoesNotThrow(() -> errorJsonObject.getString("instance"));

            assertEquals(ContainerAutomatEntryController.PATH_REQUESTS, errorInstance);

            var expectedJson = ContainerAutomatEntryController.PROBLEM_DETAIL_JSON_TEMPLATE.formatted(errorType, errorTitle, errorDetail, errorInstance);
            var expectedJsonNode = assertDoesNotThrow(() -> objectMapper.readTree(expectedJson));
            var errorJsonNode = assertDoesNotThrow(() -> objectMapper.readTree(errorJson));
            assertEquals(expectedJsonNode, errorJsonNode);

        } catch (Exception e) {
            fail("Unexpected Exception: %s".formatted(e.getMessage()), e);
        }
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void log_message_of_processing_instance_creation(CapturedOutput output) {

        var testInput = "0101";
        var testDescription = "_request_returns_processing_instance";
        var testUuid = UUID.randomUUID().toString();
        var testTimestamp = Instant.now();
        var testProcessingInstance = ProcessingInstanceImpl.builder()
                .processingInstanceId(testUuid)
                .creationTime(testTimestamp)
                .input(testInput)
                .description(testDescription)
                .build();
        var testMessage = ContainerAutomatEntryController.LOG_MESSAGE_NEW_REQUEST_PROCESSING_INSTANCE.formatted(testProcessingInstance.toString());

        containerAutomatEntryController.logProcessingInstanceCreated(testProcessingInstance);

        assertTrue(output.getOut().contains(testMessage));
    }

}
