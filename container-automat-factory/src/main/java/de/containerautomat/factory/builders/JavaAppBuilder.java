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

import com.fasterxml.jackson.databind.ObjectMapper;
import de.containerautomat.automaton.AlphabetSymbol;
import de.containerautomat.automaton.AutomatonState;
import de.containerautomat.automaton.DeterministicFiniteAutomaton;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static de.containerautomat.factory.builders.ApplicationTemplatesConstants.*;

/**
 * A class that provides methods for generating java-specific
 * files for a Container-Automat application.
 * <p/>
 * Through these methods, the following areas are covered:
 * <ul>
 *     <li>
 *         POM files and directories for a multi-module Maven project for
 *         the Java applications that represent states of the DFA automaton
 *         implemented by the generated application as a whole.
 *     </li>
 *     <li>
 *         The generation of Java source and resource files in the
 *         respective directories of the aforementioned Maven project.
 *     </li>
 *     <li>
 *         Files for starting the aforementioned Java applications in a local
 *         environment, i.e. in the Windows command line or in the Linux shell,
 *         as well as for starting separately running Docker containers for
 *         the message broker, the database, and possibly other optional
 *         services.
 *     </li>
 * </ul>
 */
class JavaAppBuilder {

    private static final String CONTAINER_AUTOMAT_PROJECT_PREFIX = "container-automat-";
    private static final String CONTAINER_AUTOMAT_PACKAGE = "de.containerautomat";
    private static final String PROJECT_SUFFIX_CORE = "-core";
    private static final String PROJECT_SUFFIX_ENTRY = "-entry";
    private static final String PROJECT_SUFFIX_STATE = "-state";
    private static final String PARENT_FOLDER_JAVA = "java/";
    private static final String FOLDER_SRC_MAIN_JAVA = "src/main/java";
    private static final String FOLDER_SRC_TESTJAVA = "src/test/java";

    private final DfaApplicationBuilder dfaApplicationBuilder;

    private final ApplicationMetaData applicationMetaData;

    private final String appNameLowercase;


    public JavaAppBuilder(DfaApplicationBuilder dfaApplicationBuilder) {
        this.dfaApplicationBuilder = dfaApplicationBuilder;
        this.applicationMetaData = dfaApplicationBuilder.getApplicationMetaData();
        this.appNameLowercase = applicationMetaData.getAppName().toLowerCase();
    }

    @SneakyThrows
    void createPomFiles() {

        String[] pomTemplates = {
                "poms/parent.pom.xml.txt",
                "poms/core.pom.xml.txt",
                "poms/entry.pom.xml.txt",
                "poms/state.pom.xml.txt"
        };

        String[] pomTargets = {
                "pom.xml",
                "container-automat-core/pom.xml",
                "container-automat-entry/pom.xml",
                "container-automat-state/pom.xml"
        };

        String[] appInPomPlaceholders = {
                PLACEHOLDER_DELIMITER + "container-automat.target.appName" + PLACEHOLDER_DELIMITER,
                PLACEHOLDER_DELIMITER + "container-automat.target.appName.lowercase" + PLACEHOLDER_DELIMITER,
                PLACEHOLDER_DELIMITER + "container-automat.target.appPackage" + PLACEHOLDER_DELIMITER
        };

        String[] appInPomValues = {
                applicationMetaData.getAppName(),
                applicationMetaData.getAppName().toLowerCase(),
                applicationMetaData.getAppPackage()
        };

        for (int templateIndex = 0; templateIndex < pomTemplates.length; templateIndex++) {
            var pom = dfaApplicationBuilder.readTemplateResource(pomTemplates[templateIndex]);
            for (int placeholderIndex = 0; placeholderIndex < appInPomPlaceholders.length; placeholderIndex++) {
                pom = pom.replace(appInPomPlaceholders[placeholderIndex], appInPomValues[placeholderIndex]);
            }
            pom = applicationMetaData.removeUnneededMessagingTypeSections(pom);
            pom = applicationMetaData.removeUnneededStorageTypeSections(pom);
            dfaApplicationBuilder.writeTargetFile(pom, pomTargets[templateIndex].replace(CONTAINER_AUTOMAT_PROJECT_PREFIX, applicationMetaData.getAppName().toLowerCase() + "-"));
        }
    }

    @SneakyThrows
    void createJavaFiles() {

        String[] javaTemplates = {
                "ContainerAutomatControllerBase.java",
                "ContainerAutomatEntryController.java",
                "AlphabetSymbol.java",
                "AutomatonState.java",
                "DeterministicFiniteAutomaton.java",
                "StateTransition.java",
                "RuntimeAlphabetSymbol.java",
                "RuntimeAutomatonState.java",
                "RuntimeDeterministicFiniteAutomaton.java",
                "RuntimeStateTransition.java",
                "ContainerAutomatCoreConfig.java",
                "ContainerAutomatCommand.java",
                "ContainerAutomatCommandProcessor.java",
                "ContainerAutomatEvent.java",
                "ContainerAutomatEventListener.java",
                "ContainerAutomatMessaging.java",
                "ContainerAutomatProcessingInstance.java",
                "ContainerAutomatProcessingStep.java",
                "ContainerAutomatStorage.java",
                "ContainerAutomatRuntimeCommand.java",
                "ContainerAutomatRuntimeEvent.java",
                "ContainerAutomatRuntimeProcessor.java",
                "ContainerAutomatRuntimeRequest.java",
                "ContainerAutomatWorkSimulator.java"
        };

        for (String javaTemplate : javaTemplates) {
            createJavaFile(PARENT_FOLDER_JAVA + javaTemplate, PROJECT_SUFFIX_CORE, FOLDER_SRC_MAIN_JAVA);
        }

        String[] javaTestTemplates = {
                "RuntimeAlphabetSymbolTests.java",
                "RuntimeAutomatonStateTests.java",
                "RuntimeDeterministicFiniteAutomatonTests.java",
                "RuntimeStateTransitionTests.java",
                "ContainerAutomatCoreConfigTests.java",
                "ContainerAutomatRuntimeCommandTests.java",
                "ContainerAutomatRuntimeEventTests.java",
                "ContainerAutomatRuntimeProcessorTests.java",
                "ContainerAutomatRuntimeRequestTests.java",
                "ContainerAutomatWorkSimulatorTests.java"
        };

        for (String javaTemplate : javaTestTemplates) {
            createJavaFile(PARENT_FOLDER_JAVA + javaTemplate, PROJECT_SUFFIX_CORE, FOLDER_SRC_TESTJAVA);
        }

        var storageType = applicationMetaData.getStorageType();
        String[] storageTemplates = {
                storageType.getDisplayName() + "ContainerAutomatConfig.java",
                storageType.getDisplayName() + "ContainerAutomatProcessingInstance.java",
                storageType.getDisplayName() + "ContainerAutomatProcessingInstanceRepository.java",
                storageType.getDisplayName() + "ContainerAutomatProcessingStep.java",
                storageType.getDisplayName() + "ContainerAutomatProcessingStepRepository.java",
                storageType.getDisplayName() + "ContainerAutomatStorage.java"
        };

        for (String storageTemplate : storageTemplates) {
            createJavaFile(PARENT_FOLDER_JAVA + storageTemplate, PROJECT_SUFFIX_CORE, FOLDER_SRC_MAIN_JAVA);
        }

        var messagingType = applicationMetaData.getMessagingType();
        String[] messagingTemplates = {
                messagingType.getDisplayName() + "ContainerAutomatCommandProcessor.java",
                messagingType.getDisplayName() + "ContainerAutomatConfig.java",
                messagingType.getDisplayName() + "ContainerAutomatEventListener.java",
                messagingType.getDisplayName() + "ContainerAutomatMessaging.java"
        };

        for (String messagingTemplate : messagingTemplates) {
            createJavaFile(PARENT_FOLDER_JAVA + messagingTemplate, PROJECT_SUFFIX_CORE, FOLDER_SRC_MAIN_JAVA);
        }

        String[] messagingTestTemplates = {
                messagingType.getDisplayName() + "ContainerAutomatCommandProcessorTests.java",
                messagingType.getDisplayName() + "ContainerAutomatConfigTests.java",
                messagingType.getDisplayName() + "ContainerAutomatEventListenerTests.java",
                messagingType.getDisplayName() + "ContainerAutomatMessagingTests.java"
        };

        for (String messagingTemplate : messagingTestTemplates) {
            createJavaFile(PARENT_FOLDER_JAVA + messagingTemplate, PROJECT_SUFFIX_CORE, FOLDER_SRC_TESTJAVA);
        }

        createJavaFile("java/apps/ContainerAutomatEntryApp.java", PROJECT_SUFFIX_ENTRY, FOLDER_SRC_MAIN_JAVA);
        createJavaFile("java/apps/ContainerAutomatStateApp.java", PROJECT_SUFFIX_STATE, FOLDER_SRC_MAIN_JAVA);
        createContainerAutomatRuntimeRequestJava();
    }

    private void createJavaFile(String templatePath, String targetProjectSuffix, String srcFolder) throws IOException {

        var javaTemplate = dfaApplicationBuilder.readTemplateResource(templatePath + ".txt");
        var javaSource = prepareJavaSource(javaTemplate);
        wirteJavaFile(javaSource, templatePath, targetProjectSuffix, srcFolder);
    }

    private String prepareJavaSource(String javaSource) {

        javaSource = javaSource.replace(CONTAINER_AUTOMAT_PACKAGE, applicationMetaData.getAppPackage());
        javaSource = dfaApplicationBuilder.resolveApplicationAndServicePlaceholders(javaSource);
        return javaSource;
    }

    private void wirteJavaFile(String javaSource, String templatePath, String targetProjectSuffix, String srcFolder) throws IOException {

        Optional<String> firstLine = javaSource.lines().filter(line -> line.startsWith(JAVA_PACKAGE_LINE_PREFIX)).findFirst();
        var packageLine = firstLine.orElseThrow(() -> new IllegalArgumentException("Empty Java template " + templatePath + "."));
        var packagePath = packageLine.substring("package ".length(), packageLine.length() - 1).replace('.', File.separatorChar);
        var targetPath = applicationMetaData.getAppName().toLowerCase() + targetProjectSuffix + File.separator + srcFolder + File.separator + packagePath;

        var fileNameStartIndex = templatePath.lastIndexOf('/') + 1;
        var javaFilename = (fileNameStartIndex == 0) ? templatePath : templatePath.substring(fileNameStartIndex);
        javaFilename = javaFilename.replace(CONTAINER_AUTOMAT, applicationMetaData.getAppName());

        dfaApplicationBuilder.writeTargetFile(javaSource, targetPath + File.separator + javaFilename);
    }

    private void createContainerAutomatRuntimeRequestJava() throws IOException {

        var javaTemplate = dfaApplicationBuilder.readTemplateResource(PARENT_FOLDER_JAVA + "ContainerAutomatRequest.java.txt");
        var javaSource = prepareJavaSource(javaTemplate);
        var topEnd = javaSource.indexOf(BEFORE_INPUT_STRING_REGEXP_MARKER);
        var bottomStart = javaSource.indexOf(AFTER_INPUT_STRING_REGEXP_MARKER);
        if (topEnd == -1 || bottomStart == -1) {
            throw new IllegalArgumentException("Missing INPUT_STRING_REGEXP_MARKER in Java template ContainerAutomatRequest.java.txt.");
        }
        var topPart = javaSource.substring(0, topEnd);
        var bottomPart = javaSource.substring(bottomStart + AFTER_INPUT_STRING_REGEXP_MARKER.length());
        var inputStringRegExp = "String INPUT_STRING_REGEXP = \"[" + createDfaSymbolsString() + "]*\";";
        javaSource = topPart + inputStringRegExp + bottomPart;
        wirteJavaFile(javaSource, PARENT_FOLDER_JAVA + "ContainerAutomatRequest.java", PROJECT_SUFFIX_CORE, FOLDER_SRC_MAIN_JAVA);
    }

    private String createDfaSymbolsString() {

        return dfaApplicationBuilder.getDfaApplicationParameters()
                .getDfa()
                .getAlphabet()
                .stream()
                .map(AlphabetSymbol::getSymbol)
                .collect(Collectors.joining());
    }

    @SneakyThrows
    void createResourceFiles() {

        String[] appPropertiesTemplates = {
                "resources/entry.application.yml.txt",
                "resources/state.application.yml.txt",
                "resources/test-dfa.json.txt"
        };

        String[] appPropertiesTargets = {
                "container-automat-entry/src/main/resources/application.yml",
                "container-automat-state/src/main/resources/application.yml",
                "container-automat-core/src/test/resources/test-dfa.json"
        };

        for (int templateIndex = 0; templateIndex < appPropertiesTemplates.length; templateIndex++) {

            var templatePath = appPropertiesTemplates[templateIndex];
            var targetPath = appPropertiesTargets[templateIndex];
            createResourceFile(templatePath, targetPath);
        }

        var messagingTypeFile = applicationMetaData.getMessagingType().name().toLowerCase();
        var messagingTemplatePath = "resources/" + messagingTypeFile + ".properties.txt";
        var messagingTargetPath = "container-automat-core/src/main/resources/" + messagingTypeFile + ".properties";
        createResourceFile(messagingTemplatePath, messagingTargetPath);

        var storageTypeFile = applicationMetaData.getStorageType().name().toLowerCase();
        var storageTemplatePath = "resources/" + storageTypeFile + ".properties.txt";
        var storageTargetPath = "container-automat-core/src/main/resources/" + storageTypeFile + ".properties";
        createResourceFile(storageTemplatePath, storageTargetPath);

        DeterministicFiniteAutomaton dfa = dfaApplicationBuilder.getDfaApplicationParameters().getDfa();
        var dfaJson = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(dfa);
        dfaApplicationBuilder.writeTargetFile(dfaJson, "container-automat-core/src/main/resources/dfa.json".replace(CONTAINER_AUTOMAT_PROJECT_PREFIX, applicationMetaData.getAppName().toLowerCase() + "-"));
    }

    private void createResourceFile(String templatePath, String targetPath) throws IOException {

        var resource = dfaApplicationBuilder.readTemplateResource(templatePath);
        resource = dfaApplicationBuilder.resolveApplicationAndServicePlaceholders(resource);
        resource = resource.replace(STORAGE_TYPE_LOWERCASE_PLACEHOLDER, applicationMetaData.getStorageType().name().toLowerCase());
        resource = resource.replace(MESSAGING_TYPE_LOWERCASE_PLACEHOLDER, applicationMetaData.getMessagingType().name().toLowerCase());
        dfaApplicationBuilder.writeTargetFile(resource, targetPath.replace(CONTAINER_AUTOMAT_PROJECT_PREFIX, applicationMetaData.getAppName().toLowerCase() + "-"));
    }

    @SneakyThrows
    void createLocalRunFiles() {

        String[] dockerRunFileTemplates = {
                "localrun/dockerdelete-container-automat.cmd.txt",
                "localrun/dockerdelete-container-automat.sh.txt"
        };

        String[] dockerRunFileTargets = {
                "localrun/dockerdelete-container-automat.cmd",
                "localrun/dockerdelete-container-automat.sh"
        };

        dfaApplicationBuilder.createTargetFiles(dockerRunFileTemplates, dockerRunFileTargets);

        createDockerCreateCmdFile();
        createDockerCreateShFile();
        createLocalRunCmdFile();
        createLocalRunShFile();
        if (applicationMetaData.getMessagingType() == ApplicationMetaData.MessagingType.KAFKA) {
            createKafkaEnvFile();
        }
        if (applicationMetaData.isIncludeOptionalServices()) {
            dfaApplicationBuilder.createLogstashPipelineConfig("localrun");
        }

    }

    private void createDockerCreateCmdFile() throws IOException {

        var cmdDockerCreate = dfaApplicationBuilder.readTemplateResource("localrun/dockercreate-container-automat.cmd.txt");
        cmdDockerCreate = dfaApplicationBuilder.resolveApplicationAndServicePlaceholders(cmdDockerCreate);
        cmdDockerCreate = cmdDockerCreate.replace(ENVIRONMENT_COMMANDS_PLACEHOLDER, getEnvironmentCommands("set "));
        if (applicationMetaData.isIncludeOptionalServices()) {
            cmdDockerCreate = cmdDockerCreate.replace(LOGSTASH_PREPARE_COMMANDS_PLACEHOLDER, getLogstashPrepareCommands("cmd"));
        }
        cmdDockerCreate = applicationMetaData.resolveOptionalServicePlaceholders(cmdDockerCreate);
        dfaApplicationBuilder.writeTargetFile(cmdDockerCreate, "localrun/dockercreate-container-automat.cmd");
    }

    private String getEnvironmentCommands(String environmentCommandsPlaceholder) throws IOException {

        var environmentCommands = dfaApplicationBuilder.readTemplateResource("environment/container-environment.txt");
        environmentCommands += dfaApplicationBuilder.readTemplateResource("environment/container-passwords.txt");
        environmentCommands = environmentCommands.replace(ENVIRONMENT_COMMAND_PLACEHOLDER, environmentCommandsPlaceholder);
        environmentCommands = applicationMetaData.removeUnneededMessagingTypeSections(environmentCommands);
        environmentCommands = applicationMetaData.removeUnneededStorageTypeSections(environmentCommands);
        return environmentCommands;
    }

    private String getLogstashPrepareCommands(String shellVariant) throws IOException {

        var messageConfName = applicationMetaData.getMessagingType().name().toLowerCase();
        var logstashPrepareCommands = dfaApplicationBuilder.readTemplateResource(LOGSTASH_PREPARE_COMMANDS_TEMPLATE.formatted(messageConfName, shellVariant));
        logstashPrepareCommands = logstashPrepareCommands.replace(CONTAINER_AUTOMAT_KEBABCASE, appNameLowercase);
        return logstashPrepareCommands;
    }

    private void createLocalRunCmdFile() throws IOException {

        var cmdRunLocalBuilder = new StringBuilder(dfaApplicationBuilder.readTemplateResource("localrun/runlocal-container-automat.cmd.txt"));
        cmdRunLocalBuilder.append("start \"");
        cmdRunLocalBuilder.append(applicationMetaData.getAppName());
        cmdRunLocalBuilder.append(" Entry\" /D ..\\");
        cmdRunLocalBuilder.append(appNameLowercase);
        cmdRunLocalBuilder.append(PROJECT_SUFFIX_ENTRY);
        cmdRunLocalBuilder.append("\\target java ");
        appendJavaProperty(cmdRunLocalBuilder, applicationMetaData.getStorageType().getLocalhostConnectionProperty());
        cmdRunLocalBuilder.append(" ");
        appendJavaProperty(cmdRunLocalBuilder, applicationMetaData.getMessagingType().getLocalhostConnectionProperty());
        cmdRunLocalBuilder.append(" -jar .\\");
        cmdRunLocalBuilder.append(appNameLowercase);
        cmdRunLocalBuilder.append(PROJECT_SUFFIX_ENTRY);
        cmdRunLocalBuilder.append(".jar\r\n");

        getStartStateCmdCommands(cmdRunLocalBuilder);
        var cmdRunLocal = cmdRunLocalBuilder.toString();
        cmdRunLocal = applicationMetaData.removeUnneededMessagingTypeSections(cmdRunLocal);
        cmdRunLocal = applicationMetaData.removeUnneededStorageTypeSections(cmdRunLocal);
        cmdRunLocal = cmdRunLocal.replace(CONTAINER_AUTOMAT, applicationMetaData.getAppName());
        cmdRunLocal = cmdRunLocal.replace(CONTAINER_AUTOMAT_LOWERCASE, appNameLowercase);
        cmdRunLocal = cmdRunLocal.replace(CONTAINER_AUTOMAT_KEBABCASE, appNameLowercase);

        dfaApplicationBuilder.writeTargetFile(cmdRunLocal, "localrun/runlocal-" + appNameLowercase + ".cmd");
    }

    private void createDockerCreateShFile() throws IOException {

        var shDockerCreate = dfaApplicationBuilder.readTemplateResource("localrun/dockercreate-container-automat.sh.txt");
        shDockerCreate = dfaApplicationBuilder.resolveApplicationAndServicePlaceholders(shDockerCreate);
        shDockerCreate = shDockerCreate.replace(ENVIRONMENT_COMMANDS_PLACEHOLDER, getEnvironmentCommands(""));
        if (applicationMetaData.isIncludeOptionalServices()) {
            shDockerCreate = shDockerCreate.replace(LOGSTASH_PREPARE_COMMANDS_PLACEHOLDER, getLogstashPrepareCommands("sh"));
        }
        shDockerCreate = applicationMetaData.resolveOptionalServicePlaceholders(shDockerCreate);
        dfaApplicationBuilder.writeTargetFile(shDockerCreate, "localrun/dockercreate-container-automat.sh");
    }

    private void getStartStateCmdCommands(StringBuilder cmdStartStatesBuilder) {

        int managementPort = ApplicationTemplatesConstants.STATE_MANAGEMENT_PORT_BASE;
        for (AutomatonState state : new TreeSet<>(dfaApplicationBuilder.getDfaApplicationParameters().getDfa().getStates())) {
            cmdStartStatesBuilder.append("timeout -T 3 /nobreak\r\n");
            cmdStartStatesBuilder.append("start \"");
            cmdStartStatesBuilder.append(applicationMetaData.getAppName());
            cmdStartStatesBuilder.append(" ");
            cmdStartStatesBuilder.append(state.getName());
            cmdStartStatesBuilder.append(" State\" /D ..\\");
            cmdStartStatesBuilder.append(appNameLowercase);
            cmdStartStatesBuilder.append(PROJECT_SUFFIX_STATE);
            cmdStartStatesBuilder.append("\\target java ");
            appendJavaProperty(cmdStartStatesBuilder, applicationMetaData.getStorageType().getLocalhostConnectionProperty());
            cmdStartStatesBuilder.append(" ");
            appendJavaProperty(cmdStartStatesBuilder, applicationMetaData.getMessagingType().getLocalhostConnectionProperty());
            cmdStartStatesBuilder.append(" ");
            appendJavaProperty(cmdStartStatesBuilder, "server.port", Integer.toString(managementPort--));
            cmdStartStatesBuilder.append(" ");
            appendJavaProperty(cmdStartStatesBuilder, appNameLowercase + ".state.name", state.getName());
            cmdStartStatesBuilder.append(" -jar .\\");
            cmdStartStatesBuilder.append(appNameLowercase);
            cmdStartStatesBuilder.append(PROJECT_SUFFIX_STATE);
            cmdStartStatesBuilder.append(".jar\r\n");
        }
    }

    private void createLocalRunShFile() throws IOException {

        var shRunLocalBuilder = new StringBuilder(dfaApplicationBuilder.readTemplateResource("localrun/runlocal-container-automat.sh.txt"));
        shRunLocalBuilder.append("java ");
        appendJavaProperty(shRunLocalBuilder, applicationMetaData.getStorageType().getLocalhostConnectionProperty());
        shRunLocalBuilder.append(" ");
        appendJavaProperty(shRunLocalBuilder, applicationMetaData.getMessagingType().getLocalhostConnectionProperty());
        shRunLocalBuilder.append(" -jar");
        shRunLocalBuilder.append(" ../");
        shRunLocalBuilder.append(appNameLowercase);
        shRunLocalBuilder.append(PROJECT_SUFFIX_ENTRY);
        shRunLocalBuilder.append("/target/");
        shRunLocalBuilder.append(appNameLowercase);
        shRunLocalBuilder.append(PROJECT_SUFFIX_ENTRY);
        shRunLocalBuilder.append(".jar &\n");

        getStartStateShCommands(shRunLocalBuilder);
        var shRunLocal = shRunLocalBuilder.toString();
        shRunLocal = applicationMetaData.removeUnneededMessagingTypeSections(shRunLocal);
        shRunLocal = applicationMetaData.removeUnneededStorageTypeSections(shRunLocal);
        shRunLocal = shRunLocal.replace(CONTAINER_AUTOMAT, applicationMetaData.getAppName());
        shRunLocal = shRunLocal.replace(CONTAINER_AUTOMAT_LOWERCASE, appNameLowercase);
        shRunLocal = shRunLocal.replace(CONTAINER_AUTOMAT_KEBABCASE, appNameLowercase);

        dfaApplicationBuilder.writeTargetFile(shRunLocal, "localrun/runlocal-" + appNameLowercase + ".sh");
    }

    private void getStartStateShCommands(StringBuilder shStartStatesBuilder) {

        int managementPort = ApplicationTemplatesConstants.STATE_MANAGEMENT_PORT_BASE;
        for (AutomatonState state : new TreeSet<>(dfaApplicationBuilder.getDfaApplicationParameters().getDfa().getStates())) {
            shStartStatesBuilder.append("sleep 3\n");
            shStartStatesBuilder.append("java ");
            appendJavaProperty(shStartStatesBuilder, applicationMetaData.getStorageType().getLocalhostConnectionProperty());
            shStartStatesBuilder.append(" ");
            appendJavaProperty(shStartStatesBuilder, applicationMetaData.getMessagingType().getLocalhostConnectionProperty());
            shStartStatesBuilder.append(" ");
            appendJavaProperty(shStartStatesBuilder, "server.port", Integer.toString(managementPort--));
            shStartStatesBuilder.append(" ");
            appendJavaProperty(shStartStatesBuilder, appNameLowercase + ".state.name", state.getName());
            shStartStatesBuilder.append(" -jar");
            shStartStatesBuilder.append(" ../");
            shStartStatesBuilder.append(appNameLowercase);
            shStartStatesBuilder.append(PROJECT_SUFFIX_STATE);
            shStartStatesBuilder.append("/target/");
            shStartStatesBuilder.append(appNameLowercase);
            shStartStatesBuilder.append(PROJECT_SUFFIX_STATE);
            shStartStatesBuilder.append(".jar &\n");
        }
    }

    private void appendJavaProperty(StringBuilder stringBuilder, Pair<String, String> property) {

        appendJavaProperty(stringBuilder, property.getLeft(), property.getRight());
    }

    private void appendJavaProperty(StringBuilder stringBuilder, String name, String value) {

        stringBuilder.append("-D%s=\"%s\"".formatted(name, value));
    }

    private void createKafkaEnvFile() throws IOException {

        var kafkaEnvironment = dfaApplicationBuilder.readTemplateResource("environment/kafka.env.txt");
        kafkaEnvironment = kafkaEnvironment.replace(KAFKA_LOG_DIRS_PLACEHOLDER, KAFKA_DOCKER_LOG_DIRS);
        kafkaEnvironment = dfaApplicationBuilder.resolveApplicationAndServicePlaceholders(kafkaEnvironment);
        kafkaEnvironment = kafkaEnvironment.replace(GENERATION_ID_PLACEHOLDER, dfaApplicationBuilder.getGenerationId());
        kafkaEnvironment = kafkaEnvironment.replace(ApplicationTemplatesConstants.ENVIRONMENT_COMMAND_PLACEHOLDER, "");
        dfaApplicationBuilder.writeTargetFile(kafkaEnvironment, "localrun/kafka.env");
    }

}
