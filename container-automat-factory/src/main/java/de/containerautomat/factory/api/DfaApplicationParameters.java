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
package de.containerautomat.factory.api;

import de.containerautomat.automaton.DeterministicFiniteAutomaton;
import de.containerautomat.factory.builders.ApplicationMetaData;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A data type that represents the parameters for the generation of a
 * Container-Automat application based on a Deterministic Finite Automaton (DFA).
 * <p/>
 * The parameters include both the description of the DFA in the form of the
 * interface {@link DeterministicFiniteAutomaton} as well as meta-data for
 * the application to be generated in the form of the data type
 * {@link ApplicationMetaData}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DfaApplicationParameters {

    @Valid
    private DeterministicFiniteAutomaton dfa;

    @Valid
    private ApplicationMetaData applicationMetaData;

}
