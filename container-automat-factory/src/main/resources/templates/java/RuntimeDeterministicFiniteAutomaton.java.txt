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
package de.containerautomat.automaton.runtime;

import de.containerautomat.automaton.AlphabetSymbol;
import de.containerautomat.automaton.AutomatonState;
import de.containerautomat.automaton.DeterministicFiniteAutomaton;
import de.containerautomat.automaton.StateTransition;
import lombok.*;

import java.util.Set;

/**
 * An implementation of the data type {@link DeterministicFiniteAutomaton}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuntimeDeterministicFiniteAutomaton implements DeterministicFiniteAutomaton {

    @NonNull
    private Set<AlphabetSymbol> alphabet;

    @NonNull
    private Set<AutomatonState> states;

    @NonNull
    private Set<StateTransition> transitions;

    @NonNull
    private String startState;

    @NonNull
    private Set<String> acceptStates;

    private String description;

}
