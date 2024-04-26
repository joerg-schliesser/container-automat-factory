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

import com.fasterxml.jackson.databind.ObjectMapper;
import de.containerautomat.automaton.AlphabetSymbol;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A test suite for testing the properties declared in the interface {@link AlphabetSymbol}
 * and implemented by the class {@link RuntimeAlphabetSymbol}. The following areas are tested:
 * <ul>
 *     <li>Serialization and deserialization in JSON format.</li>
 *     <li>
 *         The implementation of {@link RuntimeAlphabetSymbol#equals(Object)}
 *         and {@link RuntimeAlphabetSymbol#hashCode()}, where symbol objects
 *         that differ only in their description should be considered equal.
 *     </li>
 *     <li>The implementation of the {@link Comparable} interface.</li>
 * </ul>
 */
class RuntimeAlphabetSymbolTests {

    private static final String TEST_SYMBOL_0 = "0";
    private static final String TEST_SYMBOL_1 = "1";
    private static final String TEST_DESCRIPTION_ZERO = "The zero symbol.";
    private static final String TEST_DESCRIPTION_ONE = "The one symbol.";
    private static final String TEST_DESCRIPTION_DIFFERS_ZERO = "Another description for the zero symbol.";


    private ObjectMapper objectMapper;


    @BeforeEach
    void setUp() {

        objectMapper = new ObjectMapper();
    }

    @Test
    void alphabet_symbol_created_from_json() {

        var testJson = """
                {
                  "symbol" : "%s",
                  "description" : "%s"
                }
                """.formatted(TEST_SYMBOL_0, TEST_DESCRIPTION_ZERO);

        var symbol0 = assertDoesNotThrow(() -> objectMapper.readValue(testJson, AlphabetSymbol.class));
        assertInstanceOf(RuntimeAlphabetSymbol.class, symbol0);
        assertEquals(TEST_SYMBOL_0, symbol0.getSymbol());
        assertEquals(TEST_DESCRIPTION_ZERO, symbol0.getDescription());
    }

    @Test
    void json_created_from_alphabet_symbol() {

        var testSymbol0 = new RuntimeAlphabetSymbol(TEST_SYMBOL_0, TEST_DESCRIPTION_ZERO);

        var json = assertDoesNotThrow(() -> objectMapper.writeValueAsString(testSymbol0));
        var jsonObject = assertDoesNotThrow(() -> new JSONObject(json));
        assertEquals(testSymbol0.getSymbol(), assertDoesNotThrow(() -> jsonObject.getString("symbol")));
        assertEquals(testSymbol0.getDescription(), assertDoesNotThrow(() -> jsonObject.getString("description")));
    }

    @Test
    void comparing_alphabet_symbols() {

        var symbol0 = new RuntimeAlphabetSymbol(TEST_SYMBOL_0, TEST_DESCRIPTION_ZERO);
        var symbol0_with_another_description = new RuntimeAlphabetSymbol(TEST_SYMBOL_0, TEST_DESCRIPTION_DIFFERS_ZERO);
        var symbol1 = new RuntimeAlphabetSymbol(TEST_SYMBOL_1, TEST_DESCRIPTION_ONE);

        assertEquals(0, symbol0.compareTo(symbol0_with_another_description));
        assertEquals(0, symbol0_with_another_description.compareTo(symbol0));
        assertTrue(symbol0.compareTo(symbol1) < 0);
        assertTrue(symbol1.compareTo(symbol0) > 0);
    }

    @Test
    void hashcodes_of_alphabet_symbols() {

        var symbol0 = new RuntimeAlphabetSymbol(TEST_SYMBOL_0, TEST_DESCRIPTION_ZERO);
        var symbol0_with_another_description = new RuntimeAlphabetSymbol(TEST_SYMBOL_0, TEST_DESCRIPTION_DIFFERS_ZERO);
        var symbol1 = new RuntimeAlphabetSymbol(TEST_SYMBOL_1, TEST_DESCRIPTION_ONE);

        assertEquals(symbol0.hashCode(), TEST_SYMBOL_0.hashCode());
        assertEquals(symbol1.hashCode(), TEST_SYMBOL_1.hashCode());
        assertEquals(symbol0.hashCode(), symbol0_with_another_description.hashCode());
        assertNotEquals(symbol0.hashCode(), symbol1.hashCode());
    }

    @Test
    void equality_of_alphabet_symbols() {

        var symbol0 = new RuntimeAlphabetSymbol(TEST_SYMBOL_0, TEST_DESCRIPTION_ZERO);
        var symbol0_with_another_description = new RuntimeAlphabetSymbol(TEST_SYMBOL_0, TEST_DESCRIPTION_DIFFERS_ZERO);
        var symbol1 = new RuntimeAlphabetSymbol(TEST_SYMBOL_1, TEST_DESCRIPTION_ONE);

        assertEquals(symbol0, symbol0_with_another_description);
        assertNotEquals(symbol0, symbol1);
    }

}
