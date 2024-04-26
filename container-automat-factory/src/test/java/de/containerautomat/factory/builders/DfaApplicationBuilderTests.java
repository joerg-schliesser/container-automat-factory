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
package de.containerautomat.factory.builders;

import de.containerautomat.factory.api.DfaApplicationParameters;
import de.containerautomat.factory.outlets.AppGenerationOutlet;
import de.containerautomat.factory.outlets.ZiparchiveOutlet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static de.containerautomat.factory.builders.ApplicationTemplatesConstants.*;
import static de.containerautomat.factory.testutils.FactoryTestDataProvider.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * A test suite for testing methods defined in the {@link DfaApplicationBuilder}
 * class for creating source files for the generated Container-Automat
 * application.
 */
class DfaApplicationBuilderTests {

    @Test
    void builders_for_application_creation_are_called() {

        var javaAppBuilderMock = Mockito.mock(JavaAppBuilder.class);
        var dockerAppBuilderMock = Mockito.mock(DockerAppBuilder.class);

        var dfaApplicationBuilderSpy = Mockito.spy(new DfaApplicationBuilder(createTestDfaApplicationParameters(), new ZiparchiveOutlet()));

        Mockito.when(dfaApplicationBuilderSpy.createJavaAppBuilder()).thenReturn(javaAppBuilderMock);
        Mockito.when(dfaApplicationBuilderSpy.createDockerAppBuilder()).thenReturn(dockerAppBuilderMock);

        dfaApplicationBuilderSpy.createApplicationFiles();

        Mockito.verify(javaAppBuilderMock, Mockito.times(1)).createPomFiles();
        Mockito.verify(javaAppBuilderMock, Mockito.times(1)).createJavaFiles();
        Mockito.verify(javaAppBuilderMock, Mockito.times(1)).createResourceFiles();
        Mockito.verify(javaAppBuilderMock, Mockito.times(1)).createLocalRunFiles();

        Mockito.verify(dockerAppBuilderMock, Mockito.times(1)).createDockerBuildFiles();
        Mockito.verify(dockerAppBuilderMock, Mockito.times(1)).createDockerComposeFiles();
    }

    @Test
    void arrays_of_target_file_templates_are_processed() {

        String[] testFileTemplates = {
                "sourcefolder/sourcefile1.txt",
                "sourcefolder/sourcefile2.txt",
                "sourcefolder/sourcefile3.txt"
        };

        String[] testFileTargets = {
                "targetfolder/targetfile1.txt",
                "targetfolder/targetfile2.txt",
                "targetfolder/targetfile3.txt"
        };

        var testSourceData = "Test source data.";
        var testTargetData = "Test target data.";

        var dfaApplicationBuilderSpy = Mockito.spy(new DfaApplicationBuilder(createTestDfaApplicationParameters(), new ZiparchiveOutlet()));

        try {
            Mockito.when(dfaApplicationBuilderSpy.readTemplateResource(Mockito.anyString())).thenReturn(testSourceData);
            Mockito.when(dfaApplicationBuilderSpy.resolveApplicationAndServicePlaceholders(Mockito.anyString())).thenReturn(testTargetData);
            Mockito.doNothing().when(dfaApplicationBuilderSpy).writeTargetFile(Mockito.eq(testTargetData), Mockito.anyString());

            assertDoesNotThrow(() -> dfaApplicationBuilderSpy.createTargetFiles(testFileTemplates, testFileTargets));

            for (int templateIndex = 0; templateIndex < testFileTemplates.length; templateIndex++) {
                Mockito.verify(dfaApplicationBuilderSpy, Mockito.times(1)).readTemplateResource(testFileTemplates[templateIndex]);
                Mockito.verify(dfaApplicationBuilderSpy, Mockito.times(1)).writeTargetFile(testTargetData, testFileTargets[templateIndex]);
            }
            Mockito.verify(dfaApplicationBuilderSpy, Mockito.times(testFileTemplates.length)).resolveApplicationAndServicePlaceholders(testSourceData);

        } catch (IOException ioe) {
            fail("Unexpected IOException: %s".formatted(ioe.getMessage()));
        }
    }

    @Test
    void create_application_specific_text() {

        var testText = """
                App name: %s
                Lower case app name: %s
                Camel case app name also becomes lower case app name: %s
                Kebab case app name also becomes lower case app name: %s
                Upper case app name: %s
                Container registry: %s
                """.formatted(CONTAINER_AUTOMAT, CONTAINER_AUTOMAT_LOWERCASE, CONTAINER_AUTOMAT_CAMELCASE, CONTAINER_AUTOMAT_KEBABCASE, CONTAINER_AUTOMAT_UPPERCASE, CONTAINER_REGISTRY_PLACEHOLDER);

        var testDfa = createTestDfa();
        var testApplicationMetaDataSpy = Mockito.spy(createTestApplicationMetaData());
        var testDfaApplicationParameters = new DfaApplicationParameters(testDfa, testApplicationMetaDataSpy);

        var testAppName = testApplicationMetaDataSpy.getAppName();
        var testAppNameLowerCase = testAppName.toLowerCase();
        var testAppNameUpperCase = testAppName.toUpperCase();
        var testContainerRegistry = testApplicationMetaDataSpy.getContainerRegistry();
        var extedtedText = """
                App name: %1$s
                Lower case app name: %2$s
                Camel case app name also becomes lower case app name: %2$s
                Kebab case app name also becomes lower case app name: %2$s
                Upper case app name: %3$s
                Container registry: %4$s
                """.formatted(testAppName, testAppNameLowerCase, testAppNameUpperCase, testContainerRegistry);

        var dfaApplicationBuilder = new DfaApplicationBuilder(testDfaApplicationParameters, new ZiparchiveOutlet());
        var resultText = dfaApplicationBuilder.resolveApplicationAndServicePlaceholders(testText);

        Mockito.verify(testApplicationMetaDataSpy, Mockito.times(1)).removeUnneededMessagingTypeSections(Mockito.anyString());
        Mockito.verify(testApplicationMetaDataSpy, Mockito.times(1)).removeUnneededStorageTypeSections(Mockito.anyString());

        assertNotEquals(testText, resultText);
        assertEquals(extedtedText, resultText);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 4})
    void create_state_specific_text(int stateNumber) {

        var testText = """
                State number: %s
                State name: %s
                State management port: %s
                """.formatted(STATE_NUMBER_PLACEHOLDER, STATE_NAME_PLACEHOLDER, STATE_MANAGEMENT_PORT_PLACEHOLDER);
        var testStateName = "S%d".formatted(stateNumber);
        var testManagementPort = STATE_MANAGEMENT_PORT_BASE - stateNumber;

        var dfaApplicationBuilder = new DfaApplicationBuilder(createTestDfaApplicationParameters(), new ZiparchiveOutlet());

        var resultText = dfaApplicationBuilder.resolveStateSpecificPlaceholders(testText, testStateName, stateNumber, testManagementPort);

        assertNotEquals(testText, resultText);

        var expectedText = """
                State number: %d
                State name: %s
                State management port: %d
                """.formatted(stateNumber, testStateName, testManagementPort);
        assertEquals(expectedText, resultText);
    }

    @Test
    void read_resource_file_from_templates_folder() {

        var testResourcePath = "tests/test-resource.txt";

        var dfaApplicationBuilder = new DfaApplicationBuilder(createTestDfaApplicationParameters(), new ZiparchiveOutlet());

        var resultText = assertDoesNotThrow(() -> dfaApplicationBuilder.readTemplateResource(testResourcePath));

        try (InputStream inputStream = dfaApplicationBuilder.getClass().getClassLoader().getResourceAsStream(TEMPLATES_PARENT_FOLDER + testResourcePath)) {
            byte[] bytes = FileCopyUtils.copyToByteArray(inputStream);
            var expectedText = new String(bytes, StandardCharsets.UTF_8);

            assertEquals(expectedText, resultText);

        } catch (Exception e) {
            fail("Unexpected exception %s".formatted(e.getMessage()));
        }
    }

    @Test
    void write_target_file_delegates_to_outlet() {

        var testData = "# Test text data.";
        var testFilePath = "path/to/targetfile.md";

        var outletMock = Mockito.mock(AppGenerationOutlet.class);

        var dfaApplicationBuilder = new DfaApplicationBuilder(createTestDfaApplicationParameters(), outletMock);

        assertDoesNotThrow(() -> dfaApplicationBuilder.writeTargetFile(testData, testFilePath));

        try {
            Mockito.verify(outletMock, Mockito.times(1)).writeTargetFile(testData, testFilePath);
        } catch (IOException ioe) {
            fail("Unexpected exception %s".formatted(ioe.getMessage()));
        }
    }

    @Test
    void write_target_file_replaces_app_name_placeholder() {

        var testData = "# Test text data.";
        var testFilePathTemplate = "path/to/%s-targetfile.md";
        var testFilePath = testFilePathTemplate.formatted(ApplicationTemplatesConstants.CONTAINER_AUTOMAT_KEBABCASE);

        var outletMock = Mockito.mock(AppGenerationOutlet.class);

        var dfaApplicationBuilder = new DfaApplicationBuilder(createTestDfaApplicationParameters(), outletMock);

        assertDoesNotThrow(() -> dfaApplicationBuilder.writeTargetFile(testData, testFilePath));

        var expectedFilePath = testFilePathTemplate.formatted(dfaApplicationBuilder.getApplicationMetaData().getAppName().toLowerCase());
        assertNotEquals(testFilePath, expectedFilePath);

        try {
            Mockito.verify(outletMock, Mockito.times(1)).writeTargetFile(testData, expectedFilePath);
        } catch (IOException ioe) {
            fail("Unexpected IOException: %s".formatted(ioe.getMessage()));
        }
    }

}
