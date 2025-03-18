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
import de.containerautomat.automaton.AlphabetSymbol;
import de.containerautomat.automaton.AutomatonState;
import de.containerautomat.automaton.DeterministicFiniteAutomaton;
import de.containerautomat.automaton.StateTransition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A test suite for testing the properties declared in the interface {@link DeterministicFiniteAutomaton}
 * and implemented by the class {@link RuntimeDeterministicFiniteAutomaton}. The following areas are tested:
 * <ul>
 *     <li>Serialization and deserialization in JSON format.</li>
 *     <li>
 *         The default implementation of {@link DeterministicFiniteAutomaton#getState(String)}
 *         and {@link DeterministicFiniteAutomaton#getTransition(String, String)}.
 *     </li>
 * </ul>
 */
class RuntimeDeterministicFiniteAutomatonTests {

    private static final String TEST_SYMBOL_0 = "0";
    private static final String TEST_SYMBOL_1 = "1";
    private static final String TEST_INVALID_SYMBOL_2 = "2";
    private static final String TEST_STATE_S1 = "S1";
    private static final String TEST_STATE_S2 = "S2";
    private static final String TEST_INVALID_STATE_3 = "S3";
    private static final String TEST_DESCRIPTION_STATE_S1 = "Even number of zeros read.";
    private static final String TEST_DESCRIPTION_STATE_S2 = "Odd number of zeros read.";
    private static final String TEST_DESCRIPTION_DFA = "Test version of DFA for checking input of an even number of zeros.";
    private static final String TEST_TEMPLATE_DESCRIPTION_SYMBOL = "The %s symbol.";
    private static final String TEST_TEMPLATE_DESCRIPTION_TRANSITION = "Input of symbol %s.";
    private static final String TEST_DFA_JSON = """
            {
              "alphabet": [
                {
                  "symbol": "%1$s",
                  "description": "The %1$s symbol."
                },
                {
                  "symbol": "%2$s",
                  "description": "The %2$s symbol."
                }
              ],
              "states": [
                {
                  "name": "%3$s",
                  "description": "%5$s"
                },
                {
                  "name": "%4$s",
                  "description": "%6$s"
                }
              ],
              "transitions": [
                {
                  "currentStateName": "%3$s",
                  "inputSymbol": "%1$s",
                  "subsequentStateName": "%4$s",
                  "description": "Input of symbol %1$s."
                },
                {
                  "currentStateName": "%3$s",
                  "inputSymbol": "%2$s",
                  "subsequentStateName": "%3$s",
                  "description": "Input of symbol %2$s."
                },
                {
                  "currentStateName": "%4$s",
                  "inputSymbol": "%1$s",
                  "subsequentStateName": "%3$s",
                  "description": "Input of symbol %1$s."
                },
                {
                  "currentStateName": "%4$s",
                  "inputSymbol": "%2$s",
                  "subsequentStateName": "%4$s",
                  "description": "Input of symbol %2$s."
                }
              ],
              "startState": "%3$s",
              "acceptStates": [
                "%3$s"
              ],
              "description": "%7$s"
            }
            """.formatted(TEST_SYMBOL_0, TEST_SYMBOL_1, TEST_STATE_S1, TEST_STATE_S2, TEST_DESCRIPTION_STATE_S1, TEST_DESCRIPTION_STATE_S2, TEST_DESCRIPTION_DFA);


    private ObjectMapper objectMapper;


    @BeforeEach
    void setUp() {

        objectMapper = new ObjectMapper();
    }

    @Test
    void json_created_from_automaton() {

        var symbol0 = new RuntimeAlphabetSymbol(TEST_SYMBOL_0, TEST_TEMPLATE_DESCRIPTION_SYMBOL.formatted(TEST_SYMBOL_0));
        var symbol1 = new RuntimeAlphabetSymbol(TEST_SYMBOL_1, TEST_TEMPLATE_DESCRIPTION_SYMBOL.formatted(TEST_SYMBOL_1));
        Set<AlphabetSymbol> alphabet = Set.of(symbol0, symbol1);

        var stateS1 = new RuntimeAutomatonState(TEST_STATE_S1, TEST_DESCRIPTION_STATE_S1);
        var stateS2 = new RuntimeAutomatonState(TEST_STATE_S2, TEST_DESCRIPTION_STATE_S2);
        Set<AutomatonState> states = Set.of(stateS1, stateS2);

        var transitionS1_0_S2 = new RuntimeStateTransition(TEST_STATE_S1, TEST_SYMBOL_0, TEST_STATE_S2, TEST_TEMPLATE_DESCRIPTION_TRANSITION.formatted(TEST_SYMBOL_0));
        var transitionS1_1_S1 = new RuntimeStateTransition(TEST_STATE_S1, TEST_SYMBOL_1, TEST_STATE_S1, TEST_TEMPLATE_DESCRIPTION_TRANSITION.formatted(TEST_SYMBOL_1));
        var transitionS2_0_S1 = new RuntimeStateTransition(TEST_STATE_S2, TEST_SYMBOL_0, TEST_STATE_S1, TEST_TEMPLATE_DESCRIPTION_TRANSITION.formatted(TEST_SYMBOL_0));
        var transitionS2_1_S2 = new RuntimeStateTransition(TEST_STATE_S2, TEST_SYMBOL_1, TEST_STATE_S2, TEST_TEMPLATE_DESCRIPTION_TRANSITION.formatted(TEST_SYMBOL_1));
        Set<StateTransition> transitions = Set.of(transitionS1_0_S2, transitionS1_1_S1, transitionS2_0_S1, transitionS2_1_S2);

        var acceptStates = Set.of(TEST_STATE_S1);

        var dfa = new RuntimeDeterministicFiniteAutomaton(alphabet, states, transitions, TEST_STATE_S1, acceptStates, TEST_DESCRIPTION_DFA);

        var dfaJson = assertDoesNotThrow(() -> objectMapper.writeValueAsString(dfa));
        assertDoesNotThrow(() -> JSONAssert.assertEquals(TEST_DFA_JSON, dfaJson, false));
    }

    @Test
    void automaton_created_from_json() {

        var dfa = assertDoesNotThrow(() -> objectMapper.readValue(TEST_DFA_JSON, DeterministicFiniteAutomaton.class));

        assertInstanceOf(RuntimeDeterministicFiniteAutomaton.class, dfa);
        assertEquals(TEST_DESCRIPTION_DFA, dfa.getDescription());
        assertEquals(TEST_STATE_S1, dfa.getStartState());
        assertEquals(Set.of(TEST_STATE_S1), dfa.getAcceptStates());

        assertEquals(2, dfa.getAlphabet().size());
        assertTrue(dfa.getAlphabet().stream().anyMatch(symbol -> symbol.getSymbol().equals(TEST_SYMBOL_0) && symbol.getDescription().equals(TEST_TEMPLATE_DESCRIPTION_SYMBOL.formatted(TEST_SYMBOL_0))));
        assertTrue(dfa.getAlphabet().stream().anyMatch(symbol -> symbol.getSymbol().equals(TEST_SYMBOL_1) && symbol.getDescription().equals(TEST_TEMPLATE_DESCRIPTION_SYMBOL.formatted(TEST_SYMBOL_1))));

        assertEquals(2, dfa.getStates().size());
        assertTrue(dfa.getStates().stream().anyMatch(state -> state.getName().equals(TEST_STATE_S1) && state.getDescription().equals(TEST_DESCRIPTION_STATE_S1)));
        assertTrue(dfa.getStates().stream().anyMatch(state -> state.getName().equals(TEST_STATE_S2) && state.getDescription().equals(TEST_DESCRIPTION_STATE_S2)));

        assertEquals(4, dfa.getTransitions().size());
        var transitionS1_0_S2 = new RuntimeStateTransition(TEST_STATE_S1, TEST_SYMBOL_0, TEST_STATE_S2, TEST_TEMPLATE_DESCRIPTION_TRANSITION.formatted(TEST_SYMBOL_0));
        assertTrue(dfa.getTransitions().stream().anyMatch(transition -> transition.equals(transitionS1_0_S2) && transition.getDescription().equals(TEST_TEMPLATE_DESCRIPTION_TRANSITION.formatted(TEST_SYMBOL_0))));
        var transitionS1_1_S1 = new RuntimeStateTransition(TEST_STATE_S1, TEST_SYMBOL_1, TEST_STATE_S1, TEST_TEMPLATE_DESCRIPTION_TRANSITION.formatted(TEST_SYMBOL_1));
        assertTrue(dfa.getTransitions().stream().anyMatch(transition -> transition.equals(transitionS1_1_S1) && transition.getDescription().equals(TEST_TEMPLATE_DESCRIPTION_TRANSITION.formatted(TEST_SYMBOL_1))));
        var transitionS2_0_S1 = new RuntimeStateTransition(TEST_STATE_S2, TEST_SYMBOL_0, TEST_STATE_S1, TEST_TEMPLATE_DESCRIPTION_TRANSITION.formatted(TEST_SYMBOL_0));
        assertTrue(dfa.getTransitions().stream().anyMatch(transition -> transition.equals(transitionS2_0_S1) && transition.getDescription().equals(TEST_TEMPLATE_DESCRIPTION_TRANSITION.formatted(TEST_SYMBOL_0))));
        var transitionS2_1_S2 = new RuntimeStateTransition(TEST_STATE_S2, TEST_SYMBOL_1, TEST_STATE_S2, TEST_TEMPLATE_DESCRIPTION_TRANSITION.formatted(TEST_SYMBOL_1));
        assertTrue(dfa.getTransitions().stream().anyMatch(transition -> transition.equals(transitionS2_1_S2) && transition.getDescription().equals(TEST_TEMPLATE_DESCRIPTION_TRANSITION.formatted(TEST_SYMBOL_1))));
    }

    @Test
    void error_for_not_existing_transition() {

        var dfa = assertDoesNotThrow(() -> objectMapper.readValue(TEST_DFA_JSON, DeterministicFiniteAutomaton.class));

        var exception = assertThrows(IllegalArgumentException.class, () -> dfa.getTransition(TEST_STATE_S1, TEST_INVALID_SYMBOL_2));
        assertEquals(RuntimeDeterministicFiniteAutomaton.ERROR_MESSAGE_NO_TRANSITION_TEMPLATE.formatted(TEST_STATE_S1, TEST_INVALID_SYMBOL_2), exception.getMessage());
    }

    @Test
    void error_for_not_existing_state() {

        var dfa = assertDoesNotThrow(() -> objectMapper.readValue(TEST_DFA_JSON, DeterministicFiniteAutomaton.class));

        var exception = assertThrows(IllegalArgumentException.class, () -> dfa.getState(TEST_INVALID_STATE_3));
        assertEquals(RuntimeDeterministicFiniteAutomaton.ERROR_MESSAGE_NO_STATE_TEMPLATE.formatted(TEST_INVALID_STATE_3), exception.getMessage());
    }

}
