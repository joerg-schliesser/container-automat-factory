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
import de.containerautomat.processing.ContainerAutomatRequest;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A test suite for testing the properties declared in the interface {@link ContainerAutomatRequest}
 * and implemented by the class {@link ContainerAutomatRuntimeRequest}, where serialization and
 * deserialization in JSON format are tested.
 */
class ContainerAutomatRuntimeRequestTests {

    private static final String TEST_INPUT = "1010";
    private static final String TEST_DESCRIPTION = "Test ContainerAutomatRequest.";


    private ObjectMapper objectMapper;


    @BeforeEach
    void setUp() {

        objectMapper = new ObjectMapper();
    }

    @Test
    void request_created_from_json() {

        var testJson = """
                {
                  "input" : "%s",
                  "description" : "%s"
                }
                """.formatted(TEST_INPUT, TEST_DESCRIPTION);

        var request = assertDoesNotThrow(() -> objectMapper.readValue(testJson, ContainerAutomatRequest.class));

        assertInstanceOf(ContainerAutomatRuntimeRequest.class, request);
        assertEquals(TEST_INPUT, request.getInput());
        assertEquals(TEST_DESCRIPTION, request.getDescription());
    }

    @Test
    void json_created_from_request() {

        var request = new ContainerAutomatRuntimeRequest(TEST_INPUT, TEST_DESCRIPTION);
        var json = assertDoesNotThrow(() -> objectMapper.writeValueAsString(request));
        var jsonObject = assertDoesNotThrow(() -> new JSONObject(json));

        assertEquals(request.getInput(), assertDoesNotThrow(() -> jsonObject.getString("input")));
        assertEquals(request.getDescription(), assertDoesNotThrow(() -> jsonObject.getString("description")));
    }

}
