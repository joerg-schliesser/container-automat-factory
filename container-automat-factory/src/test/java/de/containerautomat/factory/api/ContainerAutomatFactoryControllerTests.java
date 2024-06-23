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
package de.containerautomat.factory.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.containerautomat.factory.testutils.FactoryTestDataProvider;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * A test suite for the class {@link ContainerAutomatFactoryController}
 * for receiving requests for the generation of a container automat
 * application based on the description of a Deterministic Finite
 * Automaton (DFA).
 */
@WebMvcTest(ContainerAutomatFactoryController.class)
class ContainerAutomatFactoryControllerTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ContainerAutomatFactoryController containerAutomatFactoryController;


    @Test
    void object_mapper_is_available() {

        assertNotNull(objectMapper);
    }

    @Test
    void rest_controller_is_available() {

        assertNotNull(containerAutomatFactoryController);
    }

    @Test
    void post_valid_application_parameters_returns_app_archive() {

        var testApplicationParameters = FactoryTestDataProvider.createTestDfaApplicationParameters();

        String[] expectedFiles = {
                "README.md",
                "pom.xml",
                "dockerbuild/dockerbuild-testapp.cmd",
                "dockerbuild/testapp-entry.dockerfile",
                "dockerbuild/testapp-state.dockerfile",
                "dockerbuild/.dockerignore",
                "dockerbuild/dockerbuild-testapp.sh",
                "dockercompose/compose-testapp.cmd",
                "dockercompose/.env",
                "dockercompose/compose-testapp.sh",
                "dockercompose/testapp-compose.yml",
                "kubernetes/k8s-create-testapp.cmd",
                "kubernetes/k8s-create-testapp.sh",
                "kubernetes/k8s-delete-testapp.cmd",
                "kubernetes/k8s-delete-testapp.sh",
                "kubernetes/elastic.yaml",
                "kubernetes/entry.yaml",
                "kubernetes/kibana.yaml",
                "kubernetes/kustomization.yaml",
                "kubernetes/logstash.yaml",
                "kubernetes/logstash-config.yaml",
                "kubernetes/mongodb.yaml",
                "kubernetes/rabbitmq.yaml",
                "kubernetes/state-1.yaml",
                "kubernetes/state-2.yaml",
                "testapp-core/pom.xml",
                "testapp-core/src/main/java/tests/testapp/api/TestAppControllerBase.java",
                "testapp-core/src/main/java/tests/testapp/api/TestAppEntryController.java",
                "testapp-core/src/main/java/tests/testapp/automaton/AlphabetSymbol.java",
                "testapp-core/src/main/java/tests/testapp/automaton/AutomatonState.java",
                "testapp-core/src/main/java/tests/testapp/automaton/DeterministicFiniteAutomaton.java",
                "testapp-core/src/main/java/tests/testapp/automaton/StateTransition.java",
                "testapp-core/src/main/java/tests/testapp/automaton/runtime/RuntimeAlphabetSymbol.java",
                "testapp-core/src/main/java/tests/testapp/automaton/runtime/RuntimeAutomatonState.java",
                "testapp-core/src/main/java/tests/testapp/automaton/runtime/RuntimeDeterministicFiniteAutomaton.java",
                "testapp-core/src/main/java/tests/testapp/automaton/runtime/RuntimeStateTransition.java",
                "testapp-core/src/main/java/tests/testapp/config/TestAppCoreConfig.java",
                "testapp-core/src/main/java/tests/testapp/processing/TestAppCommand.java",
                "testapp-core/src/main/java/tests/testapp/processing/TestAppCommandProcessor.java",
                "testapp-core/src/main/java/tests/testapp/processing/TestAppEvent.java",
                "testapp-core/src/main/java/tests/testapp/processing/TestAppEventListener.java",
                "testapp-core/src/main/java/tests/testapp/processing/TestAppMessaging.java",
                "testapp-core/src/main/java/tests/testapp/processing/TestAppProcessingInstance.java",
                "testapp-core/src/main/java/tests/testapp/processing/TestAppProcessingStep.java",
                "testapp-core/src/main/java/tests/testapp/processing/TestAppRequest.java",
                "testapp-core/src/main/java/tests/testapp/processing/TestAppStorage.java",
                "testapp-core/src/main/java/tests/testapp/processing/mongodb/MongoDbTestAppConfig.java",
                "testapp-core/src/main/java/tests/testapp/processing/mongodb/MongoDbTestAppProcessingInstance.java",
                "testapp-core/src/main/java/tests/testapp/processing/mongodb/MongoDbTestAppProcessingInstanceRepository.java",
                "testapp-core/src/main/java/tests/testapp/processing/mongodb/MongoDbTestAppProcessingStep.java",
                "testapp-core/src/main/java/tests/testapp/processing/mongodb/MongoDbTestAppProcessingStepRepository.java",
                "testapp-core/src/main/java/tests/testapp/processing/mongodb/MongoDbTestAppStorage.java",
                "testapp-core/src/main/java/tests/testapp/processing/rabbitmq/RabbitMqTestAppCommandProcessor.java",
                "testapp-core/src/main/java/tests/testapp/processing/rabbitmq/RabbitMqTestAppConfig.java",
                "testapp-core/src/main/java/tests/testapp/processing/rabbitmq/RabbitMqTestAppEventListener.java",
                "testapp-core/src/main/java/tests/testapp/processing/rabbitmq/RabbitMqTestAppMessaging.java",
                "testapp-core/src/main/java/tests/testapp/processing/runtime/TestAppRuntimeCommand.java",
                "testapp-core/src/main/java/tests/testapp/processing/runtime/TestAppRuntimeEvent.java",
                "testapp-core/src/main/java/tests/testapp/processing/runtime/TestAppRuntimeProcessor.java",
                "testapp-core/src/main/java/tests/testapp/processing/runtime/TestAppRuntimeRequest.java",
                "testapp-core/src/main/java/tests/testapp/processing/runtime/TestAppWorkSimulator.java",
                "testapp-core/src/main/resources/dfa.json",
                "testapp-core/src/main/resources/mongodb.properties",
                "testapp-core/src/main/resources/rabbitmq.properties",
                "testapp-core/src/test/java/tests/testapp/automaton/runtime/RuntimeAlphabetSymbolTests.java",
                "testapp-core/src/test/java/tests/testapp/automaton/runtime/RuntimeAutomatonStateTests.java",
                "testapp-core/src/test/java/tests/testapp/automaton/runtime/RuntimeDeterministicFiniteAutomatonTests.java",
                "testapp-core/src/test/java/tests/testapp/automaton/runtime/RuntimeStateTransitionTests.java",
                "testapp-core/src/test/java/tests/testapp/config/TestAppCoreConfigTests.java",
                "testapp-core/src/test/java/tests/testapp/processing/runtime/TestAppRuntimeCommandTests.java",
                "testapp-core/src/test/java/tests/testapp/processing/runtime/TestAppRuntimeEventTests.java",
                "testapp-core/src/test/java/tests/testapp/processing/runtime/TestAppRuntimeProcessorTests.java",
                "testapp-core/src/test/java/tests/testapp/processing/runtime/TestAppRuntimeRequestTests.java",
                "testapp-core/src/test/java/tests/testapp/processing/runtime/TestAppWorkSimulatorTests.java",
                "testapp-core/src/test/resources/test-dfa.json",
                "testapp-entry/pom.xml",
                "testapp-entry/src/main/java/tests/testapp/entry/TestAppEntryApp.java",
                "testapp-entry/src/main/resources/application.yml",
                "testapp-state/pom.xml",
                "testapp-state/src/main/java/tests/testapp/state/TestAppStateApp.java",
                "testapp-state/src/main/resources/application.yml",
                "localrun/dockercreate-testapp.cmd",
                "localrun/dockerdelete-testapp.cmd",
                "localrun/runlocal-testapp.cmd",
                "localrun/dockercreate-testapp.sh",
                "localrun/dockerdelete-testapp.sh",
                "localrun/runlocal-testapp.sh"
        };

        try {
            var testJson = objectMapper.writeValueAsString(testApplicationParameters);

            var resultBytes = mockMvc.perform(post(ContainerAutomatFactoryController.PATH_APPS_CREATE).content(testJson)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_OCTET_STREAM))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                    .andReturn().getResponse().getContentAsByteArray();

            for (String fileEntryPath : expectedFiles) {
                var zipEntry = getTestZipEntry(fileEntryPath, resultBytes);
                assertNotNull(zipEntry);
                assertFalse(zipEntry.isDirectory());
                assertEquals(fileEntryPath, zipEntry.getName());
            }

        } catch (Exception e) {
            fail("Unexpected Exception: %s".formatted(e.getMessage()), e);
        }
    }

    @Test
    void post_invalid_json_returns_error_details() {

        var testApplicationParameters = FactoryTestDataProvider.createTestDfaApplicationParameters();

        try {
            var testJson = objectMapper.writeValueAsString(testApplicationParameters);
            testJson = testJson.substring(0, testJson.lastIndexOf('}') - 1);

            var errorJson = mockMvc.perform(post(ContainerAutomatFactoryController.PATH_APPS_CREATE).content(testJson)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_OCTET_STREAM))
                    .andExpect(status().is4xxClientError())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andReturn().getResponse().getContentAsString();

            var errorJsonObject = assertDoesNotThrow(() -> new JSONObject(errorJson));
            var errorType = assertDoesNotThrow(() -> errorJsonObject.getString("type"));
            var errorTitle = assertDoesNotThrow(() -> errorJsonObject.getString("title"));
            var errorDetail = assertDoesNotThrow(() -> errorJsonObject.getString("detail"));
            var errorInstance = assertDoesNotThrow(() -> errorJsonObject.getString("instance"));

            assertEquals(ContainerAutomatFactoryController.PATH_APPS_CREATE, errorInstance);

            var expectedJson = ContainerAutomatFactoryController.PROBLEM_DETAIL_JSON_TEMPLATE.formatted(errorType, errorTitle, errorDetail, errorInstance);
            var expectedJsonNode = assertDoesNotThrow(() -> objectMapper.readTree(expectedJson));
            var errorJsonNode = assertDoesNotThrow(() -> objectMapper.readTree(errorJson));
            assertEquals(expectedJsonNode, errorJsonNode);

        } catch (Exception e) {
            fail("Unexpected Exception: %s".formatted(e.getMessage()), e);
        }
    }

    private ZipEntry getTestZipEntry(String entryPath, byte[] zipData) throws IOException {

        try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(zipData), StandardCharsets.UTF_8)) {
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                if (zipEntry.getName().equals(entryPath)) {
                    return zipEntry;
                }
            }
        }
        throw new IOException("Missing entry in zip archive: " + entryPath);
    }

}
