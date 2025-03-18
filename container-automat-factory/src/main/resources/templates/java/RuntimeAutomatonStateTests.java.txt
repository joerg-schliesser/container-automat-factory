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
package de.containerautomat.automaton.runtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.containerautomat.automaton.AutomatonState;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A test suite for testing the properties declared in the interface {@link AutomatonState}
 * and implemented by the class {@link RuntimeAutomatonState}. The following areas are tested:
 * <ul>
 *     <li>Serialization and deserialization in JSON format.</li>
 *     <li>
 *         The implementation of {@link RuntimeAutomatonState#equals(Object)}
 *         and {@link RuntimeAutomatonState#hashCode()}, where state objects
 *         that differ only in their description should be considered equal.
 *     </li>
 *     <li>The implementation of the {@link Comparable} interface.</li>
 * </ul>
 */
class RuntimeAutomatonStateTests {

    private static final String TEST_STATE_S1 = "S1";
    private static final String TEST_STATE_S2 = "S2";
    private static final String TEST_DESCRIPTION_STATE_S1 = "The S1 state.";
    private static final String TEST_DESCRIPTION_STATE_S2 = "The S2 state.";
    private static final String TEST_DESCRIPTION_DIFFERS_STATE_S1 = "Another description for the S1 state.";


    private ObjectMapper objectMapper;


    @BeforeEach
    void setUp() {

        objectMapper = new ObjectMapper();
    }

    @Test
    void automaton_state_created_from_json() {

        var testJson = """
                {
                  "name" : "%s",
                  "description" : "%s"
                }
                """.formatted(TEST_STATE_S1, TEST_DESCRIPTION_STATE_S1);

        var stateS1 = assertDoesNotThrow(() -> objectMapper.readValue(testJson, AutomatonState.class));

        assertInstanceOf(RuntimeAutomatonState.class, stateS1);
        assertEquals(TEST_STATE_S1, stateS1.getName());
        assertEquals(TEST_DESCRIPTION_STATE_S1, stateS1.getDescription());
    }

    @Test
    void json_created_from_automaton_state() {

        var stateS1 = new RuntimeAutomatonState(TEST_STATE_S1, TEST_DESCRIPTION_STATE_S1);
        var json = assertDoesNotThrow(() -> objectMapper.writeValueAsString(stateS1));
        var jsonObject = assertDoesNotThrow(() -> new JSONObject(json));

        assertEquals(stateS1.getName(), assertDoesNotThrow(() -> jsonObject.getString("name")));
        assertEquals(stateS1.getDescription(), assertDoesNotThrow(() -> jsonObject.getString("description")));
    }

    @Test
    void comparing_automaton_states() {

        var stateS1 = new RuntimeAutomatonState(TEST_STATE_S1, TEST_DESCRIPTION_STATE_S1);
        var stateS1_with_another_description = new RuntimeAutomatonState(TEST_STATE_S1, TEST_DESCRIPTION_DIFFERS_STATE_S1);
        var stateS2 = new RuntimeAutomatonState(TEST_STATE_S2, TEST_DESCRIPTION_STATE_S2);

        assertEquals(0, stateS1.compareTo(stateS1_with_another_description));
        assertEquals(0, stateS1_with_another_description.compareTo(stateS1));
        assertTrue(stateS1.compareTo(stateS2) < 0);
        assertTrue(stateS2.compareTo(stateS1) > 0);
    }

    @Test
    void hashcodes_of_automaton_states() {

        var stateS1 = new RuntimeAutomatonState(TEST_STATE_S1, TEST_DESCRIPTION_STATE_S1);
        var stateS1_with_another_description = new RuntimeAutomatonState(TEST_STATE_S1, TEST_DESCRIPTION_DIFFERS_STATE_S1);
        var stateS2 = new RuntimeAutomatonState(TEST_STATE_S2, TEST_DESCRIPTION_STATE_S2);

        assertEquals(stateS1.hashCode(), TEST_STATE_S1.hashCode());
        assertEquals(stateS2.hashCode(), TEST_STATE_S2.hashCode());
        assertEquals(stateS1.hashCode(), stateS1_with_another_description.hashCode());
        assertNotEquals(stateS1.hashCode(), stateS2.hashCode());
    }

    @Test
    void equality_of_automaton_states() {

        var stateS1 = new RuntimeAutomatonState(TEST_STATE_S1, TEST_DESCRIPTION_STATE_S1);
        var stateS1_with_another_description = new RuntimeAutomatonState(TEST_STATE_S1, TEST_DESCRIPTION_DIFFERS_STATE_S1);
        var stateS2 = new RuntimeAutomatonState(TEST_STATE_S2, TEST_DESCRIPTION_STATE_S2);

        assertEquals(stateS1, stateS1_with_another_description);
        assertNotEquals(stateS1, stateS2);
    }

    @Test
    void not_equal_to_null() {

        var stateS1 = new RuntimeAutomatonState(TEST_STATE_S1, TEST_DESCRIPTION_STATE_S1);

        assertNotEquals(null, stateS1);
    }

    @Test
    void not_equal_to_other_class() {

        var stateS1 = new RuntimeAutomatonState(TEST_STATE_S1, TEST_DESCRIPTION_STATE_S1);

        assertNotEquals(new Object(), stateS1);
    }

}