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
package de.containerautomat.automaton;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.containerautomat.automaton.runtime.RuntimeAutomatonState;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * The declaration of a data type that represents a state of the deterministic
 * finite automaton implemented by the generated application.
 */
@JsonDeserialize(as = RuntimeAutomatonState.class)
public interface AutomatonState {

    @NotEmpty
    @Pattern(regexp = DeterministicFiniteAutomaton.ALLOWED_STATE_NAME_REGEXP)
    String getName();

    @Size(max = DeterministicFiniteAutomaton.MAX_LENGTH_DESCRIPTION)
    String getDescription();

}
