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
package de.containerautomat.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.containerautomat.automaton.DeterministicFiniteAutomaton;
import de.containerautomat.automaton.runtime.RuntimeDeterministicFiniteAutomaton;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.InputStream;

/**
 * A Spring configuration that provides the Deterministic Finite Automaton (DFA) of the
 * generated application as a Spring bean {@link DeterministicFiniteAutomaton}.
 * <p/>
 * In addition, the {@link Jackson2ObjectMapperBuilder} is adapted to the requirements
 * of the application.
 */
@Configuration
@ComponentScan(basePackages = "de.containerautomat")
public class ContainerAutomatCoreConfig {

    public static final String PROPERTY_CONTAINERAUTOMAT_APP_IS_ENTRY = "containerautomat.app.is-entry";
    public static final String PROPERTY_CONTAINERAUTOMAT_APP_IS_STATE = "containerautomat.app.is-state";
    public static final String PROPERTY_CONTAINERAUTOMAT_DFA_RESOURCE_PATH = "containerautomat.dfa.resource-path";
    public static final String PROPERTY_CONTAINERAUTOMAT_STATE_NAME = "containerautomat.state.name";
    public static final String PROPERTY_CONTAINERAUTOMAT_PROCESSING_MIN_DURATION_MILLIS = "containerautomat.processing.min-duration-millis";
    public static final String PROPERTY_CONTAINERAUTOMAT_PROCESSING_MAX_DURATION_MILLIS = "containerautomat.processing.max-duration-millis";


    @Bean
    public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder() {

        return new Jackson2ObjectMapperBuilder()
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .indentOutput(true);
    }

    @Bean
    @SneakyThrows
    public DeterministicFiniteAutomaton deterministicFiniteAutomaton(@Value("${" + PROPERTY_CONTAINERAUTOMAT_DFA_RESOURCE_PATH + ":/dfa.json}") String dfaResourcePath, ObjectMapper objectMapper) {

        ClassPathResource dfaResource = new ClassPathResource(dfaResourcePath);
        try (InputStream inputStream = dfaResource.getInputStream()) {
            return objectMapper.readValue(inputStream, RuntimeDeterministicFiniteAutomaton.class);
        }
    }

}
