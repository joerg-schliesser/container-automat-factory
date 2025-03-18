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
import de.containerautomat.automaton.runtime.RuntimeAlphabetSymbol;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * The declaration of a data type that represents a symbol of the input alphabet of the
 * DFA implemented by the generated application.
 */
@JsonDeserialize(as = RuntimeAlphabetSymbol.class)
public interface AlphabetSymbol {

    @NotEmpty
    @Pattern(regexp = DeterministicFiniteAutomaton.ALLOWED_SYMBOLS_REGEXP)
    String getSymbol();

    @Size(max = DeterministicFiniteAutomaton.MAX_LENGTH_DESCRIPTION)
    String getDescription();

}
