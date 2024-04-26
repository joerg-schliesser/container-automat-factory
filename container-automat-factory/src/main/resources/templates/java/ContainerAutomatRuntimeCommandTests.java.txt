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
import de.containerautomat.processing.ContainerAutomatCommand;
import de.containerautomat.processing.ContainerAutomatProcessingInstance;
import lombok.*;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A test suite for testing the properties declared in the interface {@link ContainerAutomatCommand}
 * and implemented by the class {@link ContainerAutomatRuntimeCommand}. The following areas are tested:
 * <ul>
 *     <li>Serialization and deserialization in JSON format.</li>
 *     <li>The factory methods of the implementation.</li>
 *     <li>
 *         The default implementation of {@link ContainerAutomatCommand#hasInputSymbol()},
 *         {@link ContainerAutomatCommand#currentInputSymbol()} and
 *         {@link ContainerAutomatCommand#isProcessingEndCommand()}.
 *     </li>
 * </ul>
 */
class ContainerAutomatRuntimeCommandTests {

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


    private static final String TEST_INSTANCE_ID = UUID.randomUUID().toString();
    private static final String TEST_PROCESSING_INPUT = "0101";
    private static final int TEST_PROCESSING_POSITION = 1;
    private static final int FIRST_VALID_TEST_PROCESSING_POSITION = 0;
    private static final int VALID_END_OF_PROCESSING_TEST_PROCESSING_POSITION = TEST_PROCESSING_INPUT.length();
    private static final int FIRST_INVALID_TEST_PROCESSING_POSITION = TEST_PROCESSING_INPUT.length() + 1;
    private static final int NEGATIVE_TEST_PROCESSING_POSITION = -1;


    private ObjectMapper objectMapper;


    @BeforeEach
    void setUp() {

        objectMapper = new ObjectMapper();
    }

    @Test
    void command_created_from_json() {

        var testJson = """
                {
                  "processingInstanceId" : "%s",
                  "processingInput" : "%s",
                  "processingPosition" : "%d"
                }
                """.formatted(TEST_INSTANCE_ID, TEST_PROCESSING_INPUT, TEST_PROCESSING_POSITION);

        var command = assertDoesNotThrow(() -> objectMapper.readValue(testJson, ContainerAutomatCommand.class));

        assertInstanceOf(ContainerAutomatRuntimeCommand.class, command);
        assertEquals(TEST_INSTANCE_ID, command.getProcessingInstanceId());
        assertEquals(TEST_PROCESSING_INPUT, command.getProcessingInput());
        assertEquals(TEST_PROCESSING_POSITION, command.getProcessingPosition());
    }

    @Test
    void json_created_from_command() {

        var testCommand = new ContainerAutomatRuntimeCommand(TEST_INSTANCE_ID, TEST_PROCESSING_INPUT, TEST_PROCESSING_POSITION);

        var json = assertDoesNotThrow(() -> objectMapper.writeValueAsString(testCommand));
        var jsonObject = assertDoesNotThrow(() -> new JSONObject(json));

        assertEquals(testCommand.getProcessingInstanceId(), assertDoesNotThrow(() -> jsonObject.getString("processingInstanceId")));
        assertEquals(testCommand.getProcessingInput(), assertDoesNotThrow(() -> jsonObject.getString("processingInput")));
        assertEquals(testCommand.getProcessingPosition(), assertDoesNotThrow(() -> jsonObject.getInt("processingPosition")));
    }

    @Test
    void next_command_from_command() {

        var testCommand = ContainerAutomatRuntimeCommand.builder()
                .processingInstanceId(TEST_INSTANCE_ID)
                .processingInput(TEST_PROCESSING_INPUT)
                .processingPosition(TEST_PROCESSING_POSITION)
                .build();

        var nextCommand = assertDoesNotThrow(testCommand::nextCommand);

        assertEquals(TEST_INSTANCE_ID, nextCommand.getProcessingInstanceId());
        assertEquals(TEST_PROCESSING_INPUT, nextCommand.getProcessingInput());
        assertEquals(TEST_PROCESSING_POSITION + 1, nextCommand.getProcessingPosition());
    }

    @Test
    void next_command_from_command_failing() {

        var testCommand = ContainerAutomatRuntimeCommand.builder()
                .processingInstanceId(TEST_INSTANCE_ID)
                .processingInput(TEST_PROCESSING_INPUT)
                .processingPosition(VALID_END_OF_PROCESSING_TEST_PROCESSING_POSITION)
                .build();

        assertThrows(IndexOutOfBoundsException.class, testCommand::nextCommand);
    }

    @Test
    void command_from_processing_instance() {

        var testCreationTime = Instant.now();
        var testProcessingInstance = ProcessingInstanceImpl.builder()
                .processingInstanceId(TEST_INSTANCE_ID)
                .creationTime(testCreationTime)
                .input(TEST_PROCESSING_INPUT)
                .description("Test ProcessingInstanceImpl.")
                .build();

        var command = ContainerAutomatRuntimeCommand.fromProcessingInstance(testProcessingInstance);

        assertEquals(TEST_INSTANCE_ID, command.getProcessingInstanceId());
        assertEquals(TEST_PROCESSING_INPUT, command.getProcessingInput());
        assertEquals(0, command.getProcessingPosition());
    }

    @Test
    void command_has_input_symbol_at_test_processing_position() {

        var testCommand = ContainerAutomatRuntimeCommand.builder()
                .processingInstanceId(TEST_INSTANCE_ID)
                .processingInput(TEST_PROCESSING_INPUT)
                .processingPosition(TEST_PROCESSING_POSITION)
                .build();

        assertTrue(testCommand.hasInputSymbol());
    }

    @Test
    void command_has_input_symbol_at_first_valid_processing_position() {

        var testCommand = ContainerAutomatRuntimeCommand.builder()
                .processingInstanceId(TEST_INSTANCE_ID)
                .processingInput(TEST_PROCESSING_INPUT)
                .processingPosition(FIRST_VALID_TEST_PROCESSING_POSITION)
                .build();

        assertTrue(testCommand.hasInputSymbol());
    }

    @Test
    void command_does_not_have_input_symbol_at_valid_end_of_processing_processing_position() {

        var testCommand = ContainerAutomatRuntimeCommand.builder()
                .processingInstanceId(TEST_INSTANCE_ID)
                .processingInput(TEST_PROCESSING_INPUT)
                .processingPosition(VALID_END_OF_PROCESSING_TEST_PROCESSING_POSITION)
                .build();

        assertFalse(testCommand.hasInputSymbol());
    }

    @Test
    void command_does_not_have_input_symbol_at_first_invalid_processing_position() {

        var testCommand = ContainerAutomatRuntimeCommand.builder()
                .processingInstanceId(TEST_INSTANCE_ID)
                .processingInput(TEST_PROCESSING_INPUT)
                .processingPosition(FIRST_INVALID_TEST_PROCESSING_POSITION)
                .build();

        assertFalse(testCommand.hasInputSymbol());
    }

    @Test
    void command_does_not_have_input_symbol_at_negative_processing_position() {

        var testCommand = ContainerAutomatRuntimeCommand.builder()
                .processingInstanceId(TEST_INSTANCE_ID)
                .processingInput(TEST_PROCESSING_INPUT)
                .processingPosition(NEGATIVE_TEST_PROCESSING_POSITION)
                .build();

        assertFalse(testCommand.hasInputSymbol());
    }

    @Test
    void command_input_symbol_at_test_processing_position() {

        var testCommand = ContainerAutomatRuntimeCommand.builder()
                .processingInstanceId(TEST_INSTANCE_ID)
                .processingInput(TEST_PROCESSING_INPUT)
                .processingPosition(TEST_PROCESSING_POSITION)
                .build();

        var inputSymbol = testCommand.currentInputSymbol();

        assertTrue(inputSymbol.isPresent());
        assertEquals(Character.toString(TEST_PROCESSING_INPUT.charAt(TEST_PROCESSING_POSITION)), inputSymbol.get());
    }

    @Test
    void command_input_symbol_at_first_valid_processing_position() {

        var testCommand = ContainerAutomatRuntimeCommand.builder()
                .processingInstanceId(TEST_INSTANCE_ID)
                .processingInput(TEST_PROCESSING_INPUT)
                .processingPosition(FIRST_VALID_TEST_PROCESSING_POSITION)
                .build();

        var inputSymbol = testCommand.currentInputSymbol();

        assertTrue(inputSymbol.isPresent());
        assertEquals(Character.toString(TEST_PROCESSING_INPUT.charAt(FIRST_VALID_TEST_PROCESSING_POSITION)), inputSymbol.get());
    }

    @Test
    void no_command_input_symbol_at_valid_end_of_processing_processing_position() {

        var testCommand = ContainerAutomatRuntimeCommand.builder()
                .processingInstanceId(TEST_INSTANCE_ID)
                .processingInput(TEST_PROCESSING_INPUT)
                .processingPosition(VALID_END_OF_PROCESSING_TEST_PROCESSING_POSITION)
                .build();

        assertTrue(testCommand.currentInputSymbol().isEmpty());
    }

    @Test
    void no_command_input_symbol_at_first_invalid_processing_position() {

        var testCommand = ContainerAutomatRuntimeCommand.builder()
                .processingInstanceId(TEST_INSTANCE_ID)
                .processingInput(TEST_PROCESSING_INPUT)
                .processingPosition(FIRST_INVALID_TEST_PROCESSING_POSITION)
                .build();

        assertTrue(testCommand.currentInputSymbol().isEmpty());
    }

    @Test
    void no_command_input_symbol_at_negative_processing_position() {

        var testCommand = ContainerAutomatRuntimeCommand.builder()
                .processingInstanceId(TEST_INSTANCE_ID)
                .processingInput(TEST_PROCESSING_INPUT)
                .processingPosition(NEGATIVE_TEST_PROCESSING_POSITION)
                .build();

        assertTrue(testCommand.currentInputSymbol().isEmpty());
    }

    @Test
    void command_is_not_processing_end_command_at_test_processing_position() {

        var testCommand = ContainerAutomatRuntimeCommand.builder()
                .processingInstanceId(TEST_INSTANCE_ID)
                .processingInput(TEST_PROCESSING_INPUT)
                .processingPosition(TEST_PROCESSING_POSITION)
                .build();

        assertFalse(testCommand.isProcessingEndCommand());
    }

    @Test
    void command_is_not_processing_end_command_at_first_valid_processing_position() {

        var testCommand = ContainerAutomatRuntimeCommand.builder()
                .processingInstanceId(TEST_INSTANCE_ID)
                .processingInput(TEST_PROCESSING_INPUT)
                .processingPosition(FIRST_VALID_TEST_PROCESSING_POSITION)
                .build();

        assertFalse(testCommand.isProcessingEndCommand());
    }

    @Test
    void command_is_processing_end_command_at_valid_end_of_processing_processing_position() {

        var testCommand = ContainerAutomatRuntimeCommand.builder()
                .processingInstanceId(TEST_INSTANCE_ID)
                .processingInput(TEST_PROCESSING_INPUT)
                .processingPosition(VALID_END_OF_PROCESSING_TEST_PROCESSING_POSITION)
                .build();

        assertTrue(testCommand.isProcessingEndCommand());
    }

    @Test
    void command_is_processing_end_command_at_first_invalid_processing_position() {

        var testCommand = ContainerAutomatRuntimeCommand.builder()
                .processingInstanceId(TEST_INSTANCE_ID)
                .processingInput(TEST_PROCESSING_INPUT)
                .processingPosition(FIRST_INVALID_TEST_PROCESSING_POSITION)
                .build();

        assertTrue(testCommand.isProcessingEndCommand());
    }

    @Test
    void command_is_processing_end_command_at_negative_processing_position() {

        var testCommand = ContainerAutomatRuntimeCommand.builder()
                .processingInstanceId(TEST_INSTANCE_ID)
                .processingInput(TEST_PROCESSING_INPUT)
                .processingPosition(NEGATIVE_TEST_PROCESSING_POSITION)
                .build();

        assertTrue(testCommand.isProcessingEndCommand());
    }

}
