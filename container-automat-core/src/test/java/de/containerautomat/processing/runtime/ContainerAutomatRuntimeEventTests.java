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
package de.containerautomat.processing.runtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.containerautomat.config.ContainerAutomatCoreConfig;
import de.containerautomat.processing.ContainerAutomatEvent;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.test.context.ContextConfiguration;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A test suite for testing the properties declared in the interface {@link ContainerAutomatEvent}
 * and implemented by the class {@link ContainerAutomatRuntimeEvent}. The following areas are tested:
 * <ul>
 *     <li>Serialization and deserialization in JSON format.</li>
 *     <li>The default implementation of {@link ContainerAutomatEvent#currentInputSymbol()}.</li>
 * </ul>
 */
@JsonTest
@ContextConfiguration(classes = {ContainerAutomatCoreConfig.class})
class ContainerAutomatRuntimeEventTests {

    private static final ContainerAutomatEvent.EventType TEST_EVENT_TYPE = ContainerAutomatEvent.EventType.STATE_PROCESSING_FINISHED_CONTINUE_PROCESS;
    private static final Instant TEST_EVENT_TIME = Instant.now();
    private static final String TEST_PROCESSING_INSTANCE_ID = UUID.randomUUID().toString();
    private static final String TEST_PROCESSING_INPUT = "1010";
    private static final int TEST_PROCESSING_POSITION = 1;
    private static final int FIRST_VALID_TEST_PROCESSING_POSITION = 0;
    private static final int VALID_END_OF_PROCESSING_TEST_PROCESSING_POSITION = TEST_PROCESSING_INPUT.length();
    private static final int FIRST_INVALID_TEST_PROCESSING_POSITION = TEST_PROCESSING_INPUT.length() + 1;
    private static final int NEGATIVE_TEST_PROCESSING_POSITION = -1;
    private static final String TEST_STATE_NAME = "S1";
    private static final String TEST_DESCRIPTION = "Test ContainerAutomatEvent.";


    @Autowired
    ObjectMapper objectMapper;


    @Test
    void event_created_from_json() {

        var testEventTime = objectMapper.convertValue(TEST_EVENT_TIME, String.class);
        var testJson = """
                {
                  "eventType" : "%s",
                  "eventTime" : "%s",
                  "processingInstanceId" : "%s",
                  "processingInput" : "%s",
                  "processingPosition" : %d,
                  "stateName" : "%s",
                  "description" : "%s"
                }
                """.formatted(TEST_EVENT_TYPE.name(), testEventTime, TEST_PROCESSING_INSTANCE_ID, TEST_PROCESSING_INPUT, TEST_PROCESSING_POSITION, TEST_STATE_NAME, TEST_DESCRIPTION);

        var event = assertDoesNotThrow(() -> objectMapper.readValue(testJson, ContainerAutomatEvent.class));

        assertInstanceOf(ContainerAutomatRuntimeEvent.class, event);
        assertEquals(TEST_EVENT_TYPE, event.getEventType());
        assertEquals(TEST_EVENT_TIME, event.getEventTime());
        assertEquals(TEST_PROCESSING_INSTANCE_ID, event.getProcessingInstanceId());
        assertEquals(TEST_PROCESSING_INPUT, event.getProcessingInput());
        assertEquals(TEST_PROCESSING_POSITION, event.getProcessingPosition());
        assertEquals(TEST_STATE_NAME, event.getStateName());
        assertEquals(TEST_DESCRIPTION, event.getDescription());
    }

    @Test
    void json_created_from_event() {

        var event = ContainerAutomatRuntimeEvent.builder()
                .eventType(TEST_EVENT_TYPE)
                .eventTime(TEST_EVENT_TIME)
                .processingInstanceId(TEST_PROCESSING_INSTANCE_ID)
                .processingInput(TEST_PROCESSING_INPUT)
                .processingPosition(TEST_PROCESSING_POSITION)
                .stateName(TEST_STATE_NAME)
                .description(TEST_DESCRIPTION)
                .build();
        var testEventTime = objectMapper.convertValue(event.getEventTime(), String.class);

        var json = assertDoesNotThrow(() -> objectMapper.writeValueAsString(event));
        var jsonObject = assertDoesNotThrow(() -> new JSONObject(json));

        assertEquals(event.getEventType().name(), assertDoesNotThrow(() -> jsonObject.getString("eventType")));
        assertEquals(testEventTime, assertDoesNotThrow(() -> jsonObject.getString("eventTime")));
        assertEquals(event.getProcessingInstanceId(), assertDoesNotThrow(() -> jsonObject.getString("processingInstanceId")));
        assertEquals(event.getProcessingInput(), assertDoesNotThrow(() -> jsonObject.getString("processingInput")));
        assertEquals(event.getProcessingPosition(), assertDoesNotThrow(() -> jsonObject.getInt("processingPosition")));
        assertEquals(event.getStateName(), assertDoesNotThrow(() -> jsonObject.getString("stateName")));
        assertEquals(event.getDescription(), assertDoesNotThrow(() -> jsonObject.getString("description")));
    }

    @Test
    void event_input_symbol_at_first_valid_processing_position() {

        var testEvent = ContainerAutomatRuntimeEvent.builder()
                .eventType(TEST_EVENT_TYPE)
                .eventTime(TEST_EVENT_TIME)
                .processingInstanceId(TEST_PROCESSING_INSTANCE_ID)
                .processingInput(TEST_PROCESSING_INPUT)
                .processingPosition(FIRST_VALID_TEST_PROCESSING_POSITION)
                .stateName(TEST_STATE_NAME)
                .description(TEST_DESCRIPTION)
                .build();

        var inputSymbol = testEvent.currentInputSymbol();

        assertTrue(inputSymbol.isPresent());
        assertEquals(Character.toString(TEST_PROCESSING_INPUT.charAt(FIRST_VALID_TEST_PROCESSING_POSITION)), inputSymbol.get());
    }

    @Test
    void no_command_input_symbol_at_valid_end_of_processing_processing_position() {

        var testEvent = ContainerAutomatRuntimeEvent.builder()
                .eventType(TEST_EVENT_TYPE)
                .eventTime(TEST_EVENT_TIME)
                .processingInstanceId(TEST_PROCESSING_INSTANCE_ID)
                .processingInput(TEST_PROCESSING_INPUT)
                .processingPosition(VALID_END_OF_PROCESSING_TEST_PROCESSING_POSITION)
                .stateName(TEST_STATE_NAME)
                .description(TEST_DESCRIPTION)
                .build();

        assertTrue(testEvent.currentInputSymbol().isEmpty());
    }

    @Test
    void no_event_input_symbol_at_first_invalid_processing_position() {

        var testEvent = ContainerAutomatRuntimeEvent.builder()
                .eventType(TEST_EVENT_TYPE)
                .eventTime(TEST_EVENT_TIME)
                .processingInstanceId(TEST_PROCESSING_INSTANCE_ID)
                .processingInput(TEST_PROCESSING_INPUT)
                .processingPosition(FIRST_INVALID_TEST_PROCESSING_POSITION)
                .stateName(TEST_STATE_NAME)
                .description(TEST_DESCRIPTION)
                .build();

        assertTrue(testEvent.currentInputSymbol().isEmpty());
    }

    @Test
    void no_event_input_symbol_at_negative_processing_position() {

        var testEvent = ContainerAutomatRuntimeEvent.builder()
                .eventType(TEST_EVENT_TYPE)
                .eventTime(TEST_EVENT_TIME)
                .processingInstanceId(TEST_PROCESSING_INSTANCE_ID)
                .processingInput(TEST_PROCESSING_INPUT)
                .processingPosition(NEGATIVE_TEST_PROCESSING_POSITION)
                .stateName(TEST_STATE_NAME)
                .description(TEST_DESCRIPTION)
                .build();

        assertTrue(testEvent.currentInputSymbol().isEmpty());
    }

}
