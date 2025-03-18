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
package de.containerautomat.factory.builders;

import de.containerautomat.factory.api.DfaApplicationParameters;
import de.containerautomat.factory.outlets.AppGenerationOutlet;
import de.containerautomat.factory.outlets.ZiparchiveOutlet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static de.containerautomat.factory.builders.ApplicationTemplatesConstants.CONTAINER_AUTOMAT;
import static de.containerautomat.factory.builders.ApplicationTemplatesConstants.CONTAINER_AUTOMAT_CAMELCASE;
import static de.containerautomat.factory.builders.ApplicationTemplatesConstants.CONTAINER_AUTOMAT_KEBABCASE;
import static de.containerautomat.factory.builders.ApplicationTemplatesConstants.CONTAINER_AUTOMAT_LOWERCASE;
import static de.containerautomat.factory.builders.ApplicationTemplatesConstants.CONTAINER_AUTOMAT_UPPERCASE;
import static de.containerautomat.factory.builders.ApplicationTemplatesConstants.CONTAINER_REGISTRY_PLACEHOLDER;
import static de.containerautomat.factory.builders.ApplicationTemplatesConstants.LOGSTASH_CONF_SOURCE_PATH_TEMPLATE;
import static de.containerautomat.factory.builders.ApplicationTemplatesConstants.LOGSTASH_CONF_TARGET_PATH_TEMPLATE;
import static de.containerautomat.factory.builders.ApplicationTemplatesConstants.STATE_MANAGEMENT_PORT_BASE;
import static de.containerautomat.factory.builders.ApplicationTemplatesConstants.STATE_MANAGEMENT_PORT_PLACEHOLDER;
import static de.containerautomat.factory.builders.ApplicationTemplatesConstants.STATE_NAME_PLACEHOLDER;
import static de.containerautomat.factory.builders.ApplicationTemplatesConstants.STATE_NUMBER_PLACEHOLDER;
import static de.containerautomat.factory.builders.ApplicationTemplatesConstants.TEMPLATES_PARENT_FOLDER;
import static de.containerautomat.factory.testutils.FactoryTestDataProvider.TEST_APP_NAME;
import static de.containerautomat.factory.testutils.FactoryTestDataProvider.TEST_MESSAGING_TYPE;
import static de.containerautomat.factory.testutils.FactoryTestDataProvider.createTestApplicationMetaData;
import static de.containerautomat.factory.testutils.FactoryTestDataProvider.createTestDfa;
import static de.containerautomat.factory.testutils.FactoryTestDataProvider.createTestDfaApplicationParameters;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

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
        var kuberbernetesAppBuilderMock = Mockito.mock(KubernetesAppBuilder.class);

        var dfaApplicationBuilderSpy = Mockito.spy(new DfaApplicationBuilder(createTestDfaApplicationParameters(), new ZiparchiveOutlet()));

        Mockito.when(dfaApplicationBuilderSpy.createJavaAppBuilder()).thenReturn(javaAppBuilderMock);
        Mockito.when(dfaApplicationBuilderSpy.createDockerAppBuilder()).thenReturn(dockerAppBuilderMock);
        Mockito.when(dfaApplicationBuilderSpy.createKubernetesAppBuilder()).thenReturn(kuberbernetesAppBuilderMock);

        dfaApplicationBuilderSpy.createApplicationFiles();

        Mockito.verify(javaAppBuilderMock, Mockito.times(1)).createPomFiles();
        Mockito.verify(javaAppBuilderMock, Mockito.times(1)).createJavaFiles();
        Mockito.verify(javaAppBuilderMock, Mockito.times(1)).createResourceFiles();
        Mockito.verify(javaAppBuilderMock, Mockito.times(1)).createLocalRunFiles();

        Mockito.verify(dockerAppBuilderMock, Mockito.times(1)).createDockerBuildFiles();
        Mockito.verify(dockerAppBuilderMock, Mockito.times(1)).createDockerComposeFiles();

        Mockito.verify(kuberbernetesAppBuilderMock, Mockito.times(1)).createGeneralKubernetesFiles();
        Mockito.verify(kuberbernetesAppBuilderMock, Mockito.times(1)).createStateManifests();
        Mockito.verify(kuberbernetesAppBuilderMock, Mockito.times(1)).createEntryManifest();
        Mockito.verify(kuberbernetesAppBuilderMock, Mockito.times(1)).createKustomizationFile();

        Mockito.verify(dfaApplicationBuilderSpy, Mockito.times(1)).createReadmeFile();
    }

    @Test
    void arrays_of_file_templates_are_processed_for_target_file_creation() {

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
            Mockito.when(dfaApplicationBuilderSpy.readTemplateResource(anyString())).thenReturn(testSourceData);
            Mockito.when(dfaApplicationBuilderSpy.resolveApplicationAndServicePlaceholders(anyString())).thenReturn(testTargetData);
            Mockito.doNothing().when(dfaApplicationBuilderSpy).writeTargetFile(eq(testTargetData), anyString());

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
    void create_application_specific_text_by_resolving_application_and_service_placeholders() {

        var testText = """
                App name: %s
                Lower case app name: %s
                Camel case app name also becomes lower case app name: %s
                Kebab case app name also becomes lower case app name: %s
                Upper case app name: %s
                Container registry: %s
                """.formatted(CONTAINER_AUTOMAT, CONTAINER_AUTOMAT_LOWERCASE, CONTAINER_AUTOMAT_CAMELCASE, CONTAINER_AUTOMAT_KEBABCASE, CONTAINER_AUTOMAT_UPPERCASE, CONTAINER_REGISTRY_PLACEHOLDER);

        var testDfa = createTestDfa();
        var testApplicationMetaDataSpy = Mockito.spy(createTestApplicationMetaData(true));
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

        Mockito.verify(testApplicationMetaDataSpy, Mockito.times(1)).removeUnneededMessagingTypeSections(anyString());
        Mockito.verify(testApplicationMetaDataSpy, Mockito.times(1)).removeUnneededStorageTypeSections(anyString());

        assertNotEquals(testText, resultText);
        assertEquals(extedtedText, resultText);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 4})
    void create_state_specific_text_by_resolving_state_specific_placeholders(int stateNumber) {

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
    void get_container_service_environment_from_resource() {

        var serviceName = "TestService";
        var containerSystem = "testcontainers";
        var testEnvironmentSourcePath = "environment/testcontainers-environment-testservice.txt";
        var testEnvironmentSource = """
                §indent§- SERVICE_PORT=1234
                §indent§- SERVICE_USER=TEST_USER
                §indent§- SERVICE_PASSWORD=TEST_CREDENTIALS
                """;
        var testEnvironmentExpectedResult = """
                    - SERVICE_PORT=1234
                    - SERVICE_USER=TEST_USER
                    - SERVICE_PASSWORD=TEST_CREDENTIALS
                """;

        var dfaApplicationBuilderSpy = Mockito.spy(new DfaApplicationBuilder(createTestDfaApplicationParameters(), new ZiparchiveOutlet()));

        try {
            Mockito.doReturn(testEnvironmentSource).when(dfaApplicationBuilderSpy).readTemplateResource(testEnvironmentSourcePath);

            var resultText = assertDoesNotThrow(() -> dfaApplicationBuilderSpy.getContainerServiceEnvironment(serviceName, containerSystem,4));

            assertEquals(testEnvironmentExpectedResult, resultText);

        } catch (IOException ioe) {
            fail("Unexpected IOException: %s".formatted(ioe.getMessage()));
        }
    }

    @Test
    void get_logstash_pipeline_config_from_resource() {

        var testConfigSourcePath = LOGSTASH_CONF_SOURCE_PATH_TEMPLATE.formatted(TEST_MESSAGING_TYPE.name().toLowerCase());
        var testConfigSource = """
                §indent§input {
                §indent§  java_stdin {
                §indent§    id => "container-automat_stdin_events"
                §indent§  }
                §indent§}
                §indent§output {
                §indent§    java_stdout {
                §indent§        id => "container-automat_stdout_events"
                §indent§    }
                §indent§}
                """;
        var testConfigExpectedResult = """
                    input {
                      java_stdin {
                        id => "%1$s_stdin_events"
                      }
                    }
                    output {
                        java_stdout {
                            id => "%1$s_stdout_events"
                        }
                    }
                """.formatted(TEST_APP_NAME.toLowerCase());

        var dfaApplicationBuilderSpy = Mockito.spy(new DfaApplicationBuilder(createTestDfaApplicationParameters(), new ZiparchiveOutlet()));

        try {
            Mockito.doReturn(testConfigSource).when(dfaApplicationBuilderSpy).readTemplateResource(testConfigSourcePath);

            var resultText = assertDoesNotThrow(() -> dfaApplicationBuilderSpy.getLogstashPipelineConfig(4));

            assertEquals(testConfigExpectedResult, resultText);

        } catch (IOException ioe) {
            fail("Unexpected IOException: %s".formatted(ioe.getMessage()));
        }
    }

    @Test
    void create_logstash_pipeline_config_from_resource() {

        var testTargetFolder = "testtargetfolder";
        var testConfigTargetPath = LOGSTASH_CONF_TARGET_PATH_TEMPLATE.formatted(testTargetFolder, TEST_MESSAGING_TYPE.name().toLowerCase());
        var testConfigResult = """
                    input {
                      java_stdin {
                        id => "%1$s_stdin_events"
                      }
                    }
                    output {
                        java_stdout {
                            id => "%1$s_stdout_events"
                        }
                    }
                """.formatted(TEST_APP_NAME.toLowerCase());

        var dfaApplicationBuilderSpy = Mockito.spy(new DfaApplicationBuilder(createTestDfaApplicationParameters(), new ZiparchiveOutlet()));

        try {
            Mockito.doReturn(testConfigResult).when(dfaApplicationBuilderSpy).getLogstashPipelineConfig(0);

            dfaApplicationBuilderSpy.createLogstashPipelineConfig(testTargetFolder);

            Mockito.verify(dfaApplicationBuilderSpy, Mockito.times(1)).writeTargetFile(testConfigResult, testConfigTargetPath);

        } catch (IOException ioe) {
            fail("Unexpected IOException: %s".formatted(ioe.getMessage()));
        }
    }

    @Test
    void write_target_file_to_outlet_for_md_filetype() {

        var testData = """
                # Markdown line 1
                **Markdown line 2**
                ## Markdown line 3
                """;
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
    void write_target_file_to_outlet_for_java_filetype() {

        var testFileComment = """
                /*
                 * A file comment.
                 */""";
        var testSourceData = """
            package de.testapp.tests;
            
            public class TestApp {
            
                public static void main(String... args) {
                    System.out.println("Hello TestApp");
                }
            }
            """;
        var testResultData = """
            %s
            package de.testapp.tests;
            
            public class TestApp {
            
                public static void main(String... args) {
                    System.out.println("Hello TestApp");
                }
            }
            """.formatted(testFileComment);
        var testFilePath = "de/testapp/tests/ContainerAutomat.java";
        var testTargetFilePath = testFilePath.replace(CONTAINER_AUTOMAT_KEBABCASE, TEST_APP_NAME.toLowerCase());

        var outletMock = Mockito.mock(AppGenerationOutlet.class);

        var dfaApplicationBuilder = new DfaApplicationBuilder(createTestDfaApplicationParameters(), outletMock);

        try (MockedStatic<TargetFileCommentBuilder> commentBuilder = Mockito.mockStatic(TargetFileCommentBuilder.class)) {
            commentBuilder.when(() -> TargetFileCommentBuilder.insertGeneratorComment(eq(testSourceData), eq(DfaApplicationBuilder.TargetFileType.JAVA), anyString()))
                    .thenReturn(testResultData);
            commentBuilder.when(() -> TargetFileCommentBuilder.isCommentedTargetFileType(eq(DfaApplicationBuilder.TargetFileType.JAVA)))
                    .thenReturn(true);

            assertDoesNotThrow(() -> dfaApplicationBuilder.writeTargetFile(testSourceData, testFilePath));

            try {
                Mockito.verify(outletMock, Mockito.times(1)).writeTargetFile(testResultData, testTargetFilePath);
            } catch (IOException ioe) {
                fail("Unexpected exception %s".formatted(ioe.getMessage()));
            }
        }
    }

    @Test
    void write_target_file_to_outlet_for_sh_filetype() {

        var testFileComment = """
                #
                # A file comment.
                #""";
        var testSourceData = """
            #!/bin/sh
            
            # Run TestApp.
            """;
        var testResultData = """
            %1$s
            #!/bin/sh
            
            # Run TestApp.
            """.formatted(testFileComment);
        var testFilePath = "testapp/ContainerAutomat.sh";
        var testTargetFilePath = testFilePath.replace(CONTAINER_AUTOMAT_KEBABCASE, TEST_APP_NAME.toLowerCase());

        var outletMock = Mockito.mock(AppGenerationOutlet.class);

        var dfaApplicationBuilder = new DfaApplicationBuilder(createTestDfaApplicationParameters(), outletMock);

        try (MockedStatic<TargetFileCommentBuilder> commentBuilder = Mockito.mockStatic(TargetFileCommentBuilder.class)) {
            commentBuilder.when(() -> TargetFileCommentBuilder.insertGeneratorComment(eq(testSourceData), eq(DfaApplicationBuilder.TargetFileType.SH), anyString()))
                    .thenReturn(testResultData);
            commentBuilder.when(() -> TargetFileCommentBuilder.isCommentedTargetFileType(eq(DfaApplicationBuilder.TargetFileType.SH)))
                    .thenReturn(true);

            assertDoesNotThrow(() -> dfaApplicationBuilder.writeTargetFile(testSourceData, testFilePath));

            try {
                Mockito.verify(outletMock, Mockito.times(1)).writeTargetFile(testResultData, testTargetFilePath);
            } catch (IOException ioe) {
                fail("Unexpected exception %s".formatted(ioe.getMessage()));
            }
        }
    }

    @Test
    void write_target_file_replaces_app_name_placeholder() {

        var testData = "# Test text data.";
        var testFilePathTemplate = "path/to/%s-targetfile.md";
        var testFilePathInput = testFilePathTemplate.formatted(ApplicationTemplatesConstants.CONTAINER_AUTOMAT_KEBABCASE);

        var outletMock = Mockito.mock(AppGenerationOutlet.class);

        var dfaApplicationBuilder = new DfaApplicationBuilder(createTestDfaApplicationParameters(), outletMock);

        assertDoesNotThrow(() -> dfaApplicationBuilder.writeTargetFile(testData, testFilePathInput));

        var expectedFilePath = testFilePathTemplate.formatted(dfaApplicationBuilder.getApplicationMetaData().getAppName().toLowerCase());
        assertNotEquals(testFilePathInput, expectedFilePath);

        try {
            Mockito.verify(outletMock, Mockito.times(1)).writeTargetFile(testData, expectedFilePath);
        } catch (IOException ioe) {
            fail("Unexpected IOException: %s".formatted(ioe.getMessage()));
        }
    }

}
