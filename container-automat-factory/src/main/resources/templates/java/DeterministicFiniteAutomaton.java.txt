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
package de.containerautomat.automaton;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.containerautomat.automaton.runtime.RuntimeDeterministicFiniteAutomaton;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Set;

/**
 * The declaration of a data type that represents the Deterministic Finite
 * Automaton (DFA) implemented by the generated application.
 */
@JsonDeserialize(as = RuntimeDeterministicFiniteAutomaton.class)
public interface DeterministicFiniteAutomaton {

    String ERROR_MESSAGE_NO_TRANSITION_TEMPLATE = "No transition for state %s and input symbol %s.";
    String ERROR_MESSAGE_NO_STATE_TEMPLATE = "No state with name %s.";

    String ALLOWED_SYMBOLS_REGEXP = "[\\p{Alnum}\\p{Punct}°§]";
    String ALLOWED_STATE_NAME_REGEXP = "\\p{Alpha}\\p{Alnum}{0,7}";
    int MAX_LENGTH_DESCRIPTION = 100;
    int MAX_LENGTH_INPUT_SYMBOL = 1;
    int MAX_LENGTH_STATE_NAME = 8;

    @NotEmpty
    Set<@Valid AlphabetSymbol> getAlphabet();

    @NotEmpty
    Set<@Valid AutomatonState> getStates();

    @NotEmpty
    Set<@Valid StateTransition> getTransitions();

    @NotEmpty
    @Pattern(regexp = ALLOWED_STATE_NAME_REGEXP)
    String getStartState();

    @NotEmpty
    Set<@Pattern(regexp = ALLOWED_STATE_NAME_REGEXP) String> getAcceptStates();

    @Size(max = MAX_LENGTH_DESCRIPTION)
    String getDescription();


    default StateTransition getTransition(String stateName, String inputSymbol) {

        return getTransitions().stream()
                .filter(transition -> transition.getInputSymbol().equals(inputSymbol) && transition.getCurrentStateName().equals(stateName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(ERROR_MESSAGE_NO_TRANSITION_TEMPLATE.formatted(stateName, inputSymbol)));
    }

    default AutomatonState getState(String stateName) {

        return getStates().stream()
                .filter(state -> state.getName().equals(stateName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(ERROR_MESSAGE_NO_STATE_TEMPLATE.formatted(stateName)));
    }

}
