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
package de.containerautomat.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.containerautomat.automaton.DeterministicFiniteAutomaton;
import de.containerautomat.automaton.runtime.RuntimeDeterministicFiniteAutomaton;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A test suite for testing the methods in {@link ContainerAutomatCoreConfig}
 * that define Spring beans.
 */
@JsonTest
@ContextConfiguration(classes = {ContainerAutomatCoreConfig.class})
class ContainerAutomatCoreConfigTests {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    DeterministicFiniteAutomaton dfaBean;


    @Test
    void object_mapper_is_available() {

        assertNotNull(objectMapper);
    }

    @Test
    void dfa_bean_is_available() {

        assertNotNull(dfaBean);
    }

    @Test
    void object_mapper_with_include_non_null_and_disabled_dates_as_timestamps() {

        var inclusion = objectMapper.getSerializationConfig().getDefaultPropertyInclusion().getValueInclusion();
        assertEquals(NON_NULL, inclusion);

        var feature = objectMapper.getSerializationConfig().isEnabled(WRITE_DATES_AS_TIMESTAMPS);
        assertFalse(feature);
    }

    @Test
    void dfa_bean_equals_dfa_from_classpath() {

        ClassPathResource dfaResource = new ClassPathResource("dfa.json");
        var dfaFromResource = assertDoesNotThrow(() -> objectMapper.readValue(dfaResource.getInputStream(), DeterministicFiniteAutomaton.class));

        assertInstanceOf(RuntimeDeterministicFiniteAutomaton.class, dfaBean);
        assertInstanceOf(RuntimeDeterministicFiniteAutomaton.class, dfaFromResource);

        assertEquals(dfaFromResource.getDescription(), dfaBean.getDescription());
        assertEquals(dfaFromResource.getStartState(), dfaBean.getStartState());
        assertEquals(dfaFromResource.getAcceptStates(), dfaBean.getAcceptStates());

        assertEquals(dfaFromResource.getAlphabet().size(), dfaBean.getAlphabet().size());
        dfaFromResource.getAlphabet().forEach(symbolFromResource -> assertTrue(dfaBean.getAlphabet().stream().anyMatch(symbolFromBean -> symbolFromBean.getSymbol().equals(symbolFromResource.getSymbol()) && symbolFromBean.getDescription().equals(symbolFromResource.getDescription()))));
        dfaBean.getAlphabet().forEach(symbolFromBean -> assertTrue(dfaFromResource.getAlphabet().stream().anyMatch(symbolFromResource -> symbolFromResource.getSymbol().equals(symbolFromBean.getSymbol()) && symbolFromResource.getDescription().equals(symbolFromBean.getDescription()))));

        assertEquals(dfaFromResource.getStates().size(), dfaBean.getStates().size());
        dfaFromResource.getStates().forEach(stateFromResource -> assertTrue(dfaBean.getStates().stream().anyMatch(stateFromBean -> stateFromBean.equals(stateFromResource) && stateFromBean.getDescription().equals(stateFromResource.getDescription()))));
        dfaBean.getStates().forEach(stateFromBean -> assertTrue(dfaFromResource.getStates().stream().anyMatch(stateFromResource -> stateFromResource.equals(stateFromBean) && stateFromResource.getDescription().equals(stateFromBean.getDescription()))));

        assertEquals(dfaFromResource.getTransitions().size(), dfaBean.getTransitions().size());
        dfaFromResource.getTransitions().forEach(transitionFromResource -> assertTrue(dfaBean.getTransitions().stream().anyMatch(transitionFromBean -> transitionFromBean.equals(transitionFromResource) && transitionFromBean.getDescription().equals(transitionFromResource.getDescription()))));
        dfaBean.getTransitions().forEach(transitionFromBean -> assertTrue(dfaFromResource.getTransitions().stream().anyMatch(transitionFromResource -> transitionFromResource.equals(transitionFromBean) && transitionFromResource.getDescription().equals(transitionFromBean.getDescription()))));
    }

}
