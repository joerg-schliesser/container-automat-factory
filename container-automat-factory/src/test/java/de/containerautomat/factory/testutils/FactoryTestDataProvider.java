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
package de.containerautomat.factory.testutils;

import de.containerautomat.automaton.AlphabetSymbol;
import de.containerautomat.automaton.AutomatonState;
import de.containerautomat.automaton.DeterministicFiniteAutomaton;
import de.containerautomat.automaton.StateTransition;
import de.containerautomat.automaton.runtime.RuntimeAlphabetSymbol;
import de.containerautomat.automaton.runtime.RuntimeAutomatonState;
import de.containerautomat.automaton.runtime.RuntimeDeterministicFiniteAutomaton;
import de.containerautomat.automaton.runtime.RuntimeStateTransition;
import de.containerautomat.factory.api.DfaApplicationParameters;
import de.containerautomat.factory.builders.ApplicationMetaData;

import java.util.Set;

/**
 * A class that provides static methods for creating test data
 * for the test suites of the Container-Automat factory.
 */
public class FactoryTestDataProvider {

    public static final String TEST_APP_NAME = "TestApp";
    public static final String TEST_APP_PACKAGE = "tests.testapp";
    public static final String TEST_CONTAINER_REGISTRY = "tests";
    public static final ApplicationMetaData.StorageType TEST_STORAGE_TYPE = ApplicationMetaData.StorageType.MONGODB;
    public static final ApplicationMetaData.MessagingType TEST_MESSAGING_TYPE = ApplicationMetaData.MessagingType.RABBITMQ;


    private FactoryTestDataProvider() {
    }

    public static DfaApplicationParameters createTestDfaApplicationParameters() {
        return new DfaApplicationParameters(createTestDfa(), createTestApplicationMetaData(true));
    }

    public static ApplicationMetaData createTestApplicationMetaData(boolean includeOptionalServices) {

        return ApplicationMetaData.builder()
                .storageType(TEST_STORAGE_TYPE)
                .appName(TEST_APP_NAME)
                .appPackage(TEST_APP_PACKAGE)
                .containerRegistry(TEST_CONTAINER_REGISTRY)
                .messagingType(TEST_MESSAGING_TYPE)
                .includeOptionalServices(includeOptionalServices)
                .build();
    }

    public static DeterministicFiniteAutomaton createTestDfa() {

        var symbol0 = new RuntimeAlphabetSymbol("0", "The 0 symbol.");
        var symbol1 = new RuntimeAlphabetSymbol("1", "The 1 symbol.");
        Set<AlphabetSymbol> alphabet = Set.of(symbol0, symbol1);

        var stateS1 = new RuntimeAutomatonState("S1", "Even number of zeros read.");
        var stateS2 = new RuntimeAutomatonState("S2", "Odd number of zeros read.");
        Set<AutomatonState> states = Set.of(stateS1, stateS2);

        var transitionS10S2 = new RuntimeStateTransition("S1", "0", "S2", "Input of symbol 0.");
        var transitionS11S1 = new RuntimeStateTransition("S1", "1", "S1", "Input of symbol 1.");
        var transitionS20S1 = new RuntimeStateTransition("S2", "0", "S1", "Input of symbol 0.");
        var transitionS21S2 = new RuntimeStateTransition("S2", "1", "S2", "Input of symbol 1.");
        Set<StateTransition> transitions = Set.of(transitionS10S2, transitionS11S1, transitionS20S1, transitionS21S2);

        var acceptStates = Set.of("S1");

        return new RuntimeDeterministicFiniteAutomaton(alphabet, states, transitions, "S1", acceptStates, "Test version of DFA for checking input of an even number of zeros.");
    }

}
