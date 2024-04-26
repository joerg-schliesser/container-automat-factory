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

import de.containerautomat.automaton.StateTransition;
import lombok.*;

import java.util.Objects;

/**
 * An implementation of the data type {@link StateTransition}, which also
 * implements the {@link Comparable} interface.
 * <p/>
 * In addition, {@link #equals(Object)} and {@link #hashCode()} are
 * overridden, so that transition objects that differ only in their
 * description are considered to be the same.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RuntimeStateTransition implements Comparable<RuntimeStateTransition>, StateTransition {

    @NonNull
    private String currentStateName;

    @NonNull
    private String inputSymbol;

    @NonNull
    private String subsequentStateName;

    private String description;


    @Override
    public int compareTo(RuntimeStateTransition o) {

        var result = currentStateName.compareTo(o.currentStateName);
        if (result != 0) {
            return result;
        }
        result = inputSymbol.compareTo(o.inputSymbol);
        if (result != 0) {
            return result;
        }
        return subsequentStateName.compareTo(o.subsequentStateName);
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return this.compareTo((RuntimeStateTransition) obj) == 0;
    }

    @Override
    public int hashCode() {

        return Objects.hash(currentStateName, inputSymbol, subsequentStateName);
    }

}
