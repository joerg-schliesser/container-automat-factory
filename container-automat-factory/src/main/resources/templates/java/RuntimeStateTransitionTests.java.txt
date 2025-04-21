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
import de.containerautomat.automaton.StateTransition;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A test suite for testing the properties declared in the interface {@link StateTransition}
 * and implemented by the class {@link RuntimeStateTransition}. The following areas are tested:
 * <ul>
 *     <li>Serialization and deserialization in JSON format.</li>
 *     <li>
 *         The implementation of {@link RuntimeStateTransition#equals(Object)}
 *         and {@link RuntimeStateTransition#hashCode()}, where transition objects
 *         that differ only in their description should be considered equal.
 *     </li>
 *     <li>The implementation of the {@link Comparable} interface.</li>
 * </ul>
 */
class RuntimeStateTransitionTests {

    private static final String TEST_STATE_S1 = "S1";
    private static final String TEST_STATE_S2 = "S2";
    private static final String TEST_SYMBOL_0 = "0";
    private static final String TEST_SYMBOL_1 = "1";
    private static final String TEST_DESCRIPTION_TRANSITION_S1_0_S2 = "Transition from S1 to S2 on receipt of symbol 0.";
    private static final String TEST_DESCRIPTION_TRANSITION_S1_0_S1 = "Transition from S1 to S1 on receipt of symbol 0.";
    private static final String TEST_DESCRIPTION_TRANSITION_S1_1_S2 = "Transition from S1 to S2 on receipt of symbol 1.";
    private static final String TEST_DESCRIPTION_DIFFERS_TRANSITION_S1_0_S1 = "Another description for the transition from S1 to S1 on receipt of symbol 0.";


    private ObjectMapper objectMapper;


    @BeforeEach
    void setUp() {

        objectMapper = new ObjectMapper();
    }

    @Test
    void state_transition_created_from_json() {

        var testJson = """
                {
                  "currentStateName" : "%s",
                  "inputSymbol" : "%s",
                  "subsequentStateName" : "%s",
                  "description" : "%s"
                }
                """.formatted(TEST_STATE_S1, TEST_SYMBOL_0, TEST_STATE_S2, TEST_DESCRIPTION_TRANSITION_S1_0_S2);

        var transitionS1_0_S2 = assertDoesNotThrow(() -> objectMapper.readValue(testJson, StateTransition.class));

        assertInstanceOf(RuntimeStateTransition.class, transitionS1_0_S2);
        assertEquals(TEST_STATE_S1, transitionS1_0_S2.getCurrentStateName());
        assertEquals(TEST_SYMBOL_0, transitionS1_0_S2.getInputSymbol());
        assertEquals(TEST_STATE_S2, transitionS1_0_S2.getSubsequentStateName());
        assertEquals(TEST_DESCRIPTION_TRANSITION_S1_0_S2, transitionS1_0_S2.getDescription());
    }

    @Test
    void json_created_from_state_transition() {

        var transitionS1_0_S2 = new RuntimeStateTransition(TEST_STATE_S1, TEST_SYMBOL_0, TEST_STATE_S2, TEST_DESCRIPTION_TRANSITION_S1_0_S2);
        var json = assertDoesNotThrow(() -> objectMapper.writeValueAsString(transitionS1_0_S2));
        var jsonObject = assertDoesNotThrow(() -> new JSONObject(json));

        assertEquals(transitionS1_0_S2.getCurrentStateName(), assertDoesNotThrow(() -> jsonObject.getString("currentStateName")));
        assertEquals(transitionS1_0_S2.getInputSymbol(), assertDoesNotThrow(() -> jsonObject.getString("inputSymbol")));
        assertEquals(transitionS1_0_S2.getSubsequentStateName(), assertDoesNotThrow(() -> jsonObject.getString("subsequentStateName")));
        assertEquals(transitionS1_0_S2.getDescription(), assertDoesNotThrow(() -> jsonObject.getString("description")));
    }

    @Test
    void comparing_state_transitions() {

        var transitionS1_0_S2 = new RuntimeStateTransition(TEST_STATE_S1, TEST_SYMBOL_0, TEST_STATE_S2, TEST_DESCRIPTION_TRANSITION_S1_0_S2);
        var transitionS1_0_S2_with_another_description = new RuntimeStateTransition(TEST_STATE_S1, TEST_SYMBOL_0, TEST_STATE_S2, TEST_DESCRIPTION_DIFFERS_TRANSITION_S1_0_S1);
        var transitionS1_0_S1 = new RuntimeStateTransition(TEST_STATE_S1, TEST_SYMBOL_0, TEST_STATE_S1, TEST_DESCRIPTION_TRANSITION_S1_0_S1);
        var transitionS1_1_S2 = new RuntimeStateTransition(TEST_STATE_S1, TEST_SYMBOL_1, TEST_STATE_S2, TEST_DESCRIPTION_TRANSITION_S1_1_S2);

        assertEquals(0, transitionS1_0_S2.compareTo(transitionS1_0_S2_with_another_description));
        assertEquals(0, transitionS1_0_S2_with_another_description.compareTo(transitionS1_0_S2));
        assertTrue(transitionS1_0_S2.compareTo(transitionS1_1_S2) < 0);
        assertTrue(transitionS1_1_S2.compareTo(transitionS1_0_S2) > 0);
        assertTrue(transitionS1_0_S1.compareTo(transitionS1_0_S2) < 0);
        assertTrue(transitionS1_0_S2.compareTo(transitionS1_0_S1) > 0);
    }

    @Test
    void hashcodes_of_state_transitions() {

        var transitionS1_0_S2 = new RuntimeStateTransition(TEST_STATE_S1, TEST_SYMBOL_0, TEST_STATE_S2, TEST_DESCRIPTION_TRANSITION_S1_0_S2);
        var transitionS1_0_S2_with_another_description = new RuntimeStateTransition(TEST_STATE_S1, TEST_SYMBOL_0, TEST_STATE_S2, TEST_DESCRIPTION_DIFFERS_TRANSITION_S1_0_S1);
        var transitionS1_0_S1 = new RuntimeStateTransition(TEST_STATE_S1, TEST_SYMBOL_0, TEST_STATE_S1, TEST_DESCRIPTION_TRANSITION_S1_0_S1);
        var transitionS1_1_S2 = new RuntimeStateTransition(TEST_STATE_S1, TEST_SYMBOL_1, TEST_STATE_S2, TEST_DESCRIPTION_TRANSITION_S1_1_S2);

        assertEquals(transitionS1_0_S2.hashCode(), Objects.hash(TEST_STATE_S1, TEST_SYMBOL_0, TEST_STATE_S2));
        assertEquals(transitionS1_0_S2.hashCode(), transitionS1_0_S2_with_another_description.hashCode());
        assertEquals(transitionS1_0_S1.hashCode(), Objects.hash(TEST_STATE_S1, TEST_SYMBOL_0, TEST_STATE_S1));
        assertEquals(transitionS1_1_S2.hashCode(), Objects.hash(TEST_STATE_S1, TEST_SYMBOL_1, TEST_STATE_S2));
        assertNotEquals(transitionS1_0_S2.hashCode(), transitionS1_1_S2.hashCode());
        assertNotEquals(transitionS1_0_S2.hashCode(), transitionS1_0_S1.hashCode());
        assertNotEquals(transitionS1_0_S1.hashCode(), transitionS1_1_S2.hashCode());
    }

    @Test
    void equality_of_state_transitions() {

        var transitionS1_0_S2 = new RuntimeStateTransition(TEST_STATE_S1, TEST_SYMBOL_0, TEST_STATE_S2, TEST_DESCRIPTION_TRANSITION_S1_0_S2);
        var transitionS1_0_S2_with_another_description = new RuntimeStateTransition(TEST_STATE_S1, TEST_SYMBOL_0, TEST_STATE_S2, TEST_DESCRIPTION_DIFFERS_TRANSITION_S1_0_S1);
        var transitionS1_0_S1 = new RuntimeStateTransition(TEST_STATE_S1, TEST_SYMBOL_0, TEST_STATE_S1, TEST_DESCRIPTION_TRANSITION_S1_0_S1);
        var transitionS1_1_S2 = new RuntimeStateTransition(TEST_STATE_S1, TEST_SYMBOL_1, TEST_STATE_S2, TEST_DESCRIPTION_TRANSITION_S1_1_S2);

        assertEquals(transitionS1_0_S2.hashCode(), transitionS1_0_S2_with_another_description.hashCode());
        assertNotEquals(transitionS1_0_S2, transitionS1_1_S2);
        assertNotEquals(transitionS1_0_S2, transitionS1_0_S1);
        assertNotEquals(transitionS1_0_S1, transitionS1_1_S2);
    }

    @Test
    void not_equal_to_null() {

        var transitionS1_0_S2 = new RuntimeStateTransition(TEST_STATE_S1, TEST_SYMBOL_0, TEST_STATE_S2, TEST_DESCRIPTION_TRANSITION_S1_0_S2);

        assertFalse(transitionS1_0_S2.equals(null));
    }

    @Test
    void not_equal_to_other_class() {

        var transitionS1_0_S2 = new RuntimeStateTransition(TEST_STATE_S1, TEST_SYMBOL_0, TEST_STATE_S2, TEST_DESCRIPTION_TRANSITION_S1_0_S2);

        assertFalse(transitionS1_0_S2.equals(new Object()));
    }

}
