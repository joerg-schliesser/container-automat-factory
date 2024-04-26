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
import lombok.*;

/**
 * An implementation of the data type {@link AlphabetSymbol}, which also
 * implements the {@link Comparable} interface.
 * <p/>
 * In addition, {@link #equals(Object)} and {@link #hashCode()} are
 * overridden, so that symbol objects that differ only in their
 * description are considered to be the same.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RuntimeAlphabetSymbol implements Comparable<RuntimeAlphabetSymbol>, AlphabetSymbol {

    @NonNull
    private String symbol;

    private String description;


    @Override
    public int compareTo(RuntimeAlphabetSymbol o) {

        return symbol.compareTo(o.symbol);
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return symbol.equals(((RuntimeAlphabetSymbol) obj).symbol);
    }

    @Override
    public int hashCode() {

        return symbol.hashCode();
    }

}
