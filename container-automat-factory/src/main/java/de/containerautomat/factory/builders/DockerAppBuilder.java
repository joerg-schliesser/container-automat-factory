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

import de.containerautomat.automaton.AutomatonState;
import lombok.SneakyThrows;

import java.io.IOException;
import java.util.TreeSet;

import static de.containerautomat.factory.builders.ApplicationTemplatesConstants.*;

/**
 * A class that provides methods for generating docker-specific
 * files for a Container-Automat application.
 * <p/>
 * Through these methods, two areas are covered:
 * <ul>
 *     <li>
 *         Files for building Docker images that provide application-specific
 *         services implemented by Java applications.
 *     </li>
 *     <li>
 *         Files for running the generated application using Docker Compose,
 *         where containers for the message broker, the database, and possibly
 *         other optional services are also started together with the Java
 *         services.
 *     </li>
 * </ul>
 */
class DockerAppBuilder {

    private static final int COMPOSE_SERVICE_VOLUMES_INDENT_SPACES = 4;

    private final DfaApplicationBuilder dfaApplicationBuilder;

    private final ApplicationMetaData applicationMetaData;


    public DockerAppBuilder(DfaApplicationBuilder dfaApplicationBuilder) {
        this.dfaApplicationBuilder = dfaApplicationBuilder;
        this.applicationMetaData = dfaApplicationBuilder.getApplicationMetaData();
    }

    @SneakyThrows
    void createDockerBuildFiles() {

        String[] dockerBuildFileTemplates = {
                "dockerbuild/dockerignore.txt",
                "dockerbuild/dockerbuild-container-automat.cmd.txt",
                "dockerbuild/dockerbuild-container-automat.sh.txt",
                "dockerbuild/container-automat-entry.dockerfile.txt",
                "dockerbuild/container-automat-state.dockerfile.txt"
        };

        String[] dockerBuildFileTargets = {
                "dockerbuild/.dockerignore",
                "dockerbuild/dockerbuild-container-automat.cmd",
                "dockerbuild/dockerbuild-container-automat.sh",
                "dockerbuild/container-automat-entry.dockerfile",
                "dockerbuild/container-automat-state.dockerfile"
        };

        dfaApplicationBuilder.createTargetFiles(dockerBuildFileTemplates, dockerBuildFileTargets);
    }

    @SneakyThrows
    void createDockerComposeFiles() {

        String[] dockerComposeFileTemplates = {
                "dockercompose/compose-container-automat.cmd.txt",
                "dockercompose/compose-container-automat.sh.txt"
        };

        String[] dockerComposeFileTargets = {
                "dockercompose/compose-container-automat.cmd",
                "dockercompose/compose-container-automat.sh"
        };

        dfaApplicationBuilder.createTargetFiles(dockerComposeFileTemplates, dockerComposeFileTargets);

        createComposeYmlFile();
        createComposeEnvFile();
        if (applicationMetaData.isIncludeOptionalServices()) {
            dfaApplicationBuilder.createLogstashPipelineConfig("dockercompose");
        }
    }

    private void createComposeYmlFile() throws IOException {

        var composeStateTemplate = dfaApplicationBuilder.readTemplateResource("dockercompose/compose-state.yml.txt");
        composeStateTemplate = dfaApplicationBuilder.resolveApplicationAndServicePlaceholders(composeStateTemplate);

        var composeYmlTemplate = dfaApplicationBuilder.readTemplateResource("dockercompose/container-automat-compose.yml.txt");
        var composeYmlBuilder = new StringBuilder(dfaApplicationBuilder.resolveApplicationAndServicePlaceholders(composeYmlTemplate));
        int stateNumber = 1;
        var managementPort = STATE_MANAGEMENT_PORT_BASE;
        for (AutomatonState state : new TreeSet<>(dfaApplicationBuilder.getDfaApplicationParameters().getDfa().getStates())) {
            composeYmlBuilder.append(dfaApplicationBuilder.resolveStateSpecificPlaceholders(composeStateTemplate, state.getName(), stateNumber++, managementPort--));
        }

        var composeYml = composeYmlBuilder.toString();
        composeYml = composeYml.replace(STORAGE_TYPE_CONTAINERNAME_PLACEHOLDER, applicationMetaData.getStorageType().getContainerName(applicationMetaData.getAppName()));
        var composeStorageEnvironment = dfaApplicationBuilder.getContainerServiceEnvironment(applicationMetaData.getStorageType().name(), CONTAINER_SYSTEM_COMPOSE, CONTAINER_SYSTEM_COMPOSE_ENVIRONMENT_VALUES_INDENT_SPACES);
        composeYml = composeYml.replace(STORAGE_ENVIRONMENT_PLACEHOLDER, composeStorageEnvironment);
        composeYml = composeYml.replace(MESSAGING_TYPE_CONTAINERNAME_PLACEHOLDER, applicationMetaData.getMessagingType().getContainerName(applicationMetaData.getAppName()));
        var composeMessagingEnvironment = dfaApplicationBuilder.getContainerServiceEnvironment(applicationMetaData.getMessagingType().name(), CONTAINER_SYSTEM_COMPOSE, CONTAINER_SYSTEM_COMPOSE_ENVIRONMENT_VALUES_INDENT_SPACES);
        composeYml = composeYml.replace(MESSAGING_ENVIRONMENT_PLACEHOLDER, composeMessagingEnvironment);
        composeYml = composeYml.replace(LOGSTASH_CONFFILENAME_PLACEHOLDER, "logstash-%s.conf".formatted(applicationMetaData.getMessagingType().name().toLowerCase()));
        if (applicationMetaData.getMessagingType() == ApplicationMetaData.MessagingType.ARTEMIS) {
            composeYml = composeYml.replace(LOGSTASH_JMS_JARS_VOLUME_PLACEHOLDER, getLogstashJmsJarsVolume());
        } else if (applicationMetaData.getMessagingType() == ApplicationMetaData.MessagingType.KAFKA) {
            composeYml = composeYml.replace(KAFKA_ENVIRONMENT_PLACEHOLDER, getKafkaEnvironment());
            composeYml = composeYml.replace(LOGSTASH_JMS_JARS_VOLUME_PLACEHOLDER, "");
        } else {
            composeYml = composeYml.replace(LOGSTASH_JMS_JARS_VOLUME_PLACEHOLDER, "");
        }
        composeYml = composeYml.replace("\r\n\r\n", "\r\n");
        composeYml = applicationMetaData.resolveOptionalServicePlaceholders(composeYml);
        dfaApplicationBuilder.writeTargetFile(composeYml, "dockercompose/container-automat-compose.yml");
    }

    private void createComposeEnvFile() throws IOException {

        var composeEnv = dfaApplicationBuilder.readTemplateResource("environment/container-environment.txt");
        composeEnv += dfaApplicationBuilder.readTemplateResource("environment/container-passwords.txt");
        composeEnv = composeEnv.replace(ENVIRONMENT_COMMAND_PLACEHOLDER, "");
        composeEnv = applicationMetaData.removeUnneededMessagingTypeSections(composeEnv);
        composeEnv = applicationMetaData.removeUnneededStorageTypeSections(composeEnv);
        composeEnv = applicationMetaData.resolveOptionalServicePlaceholders(composeEnv);
        dfaApplicationBuilder.writeTargetFile(composeEnv, "dockercompose/.env");
    }

    private String getKafkaEnvironment() throws IOException {

        var kafkaEnvironment = dfaApplicationBuilder.readTemplateResource("environment/kafka.env.txt");
        kafkaEnvironment = kafkaEnvironment.replace(KAFKA_LOG_DIRS_PLACEHOLDER, KAFKA_DOCKER_LOG_DIRS);
        kafkaEnvironment = dfaApplicationBuilder.resolveApplicationAndServicePlaceholders(kafkaEnvironment);
        kafkaEnvironment = kafkaEnvironment.replace(GENERATION_ID_PLACEHOLDER, dfaApplicationBuilder.getGenerationId());
        return kafkaEnvironment.replace(ApplicationTemplatesConstants.ENVIRONMENT_COMMAND_PLACEHOLDER, (" ".repeat(CONTAINER_SYSTEM_COMPOSE_ENVIRONMENT_VALUES_INDENT_SPACES)) + "- ");
    }

    private String getLogstashJmsJarsVolume() throws IOException {

        var logstashJmsVolume = dfaApplicationBuilder.readTemplateResource("dockercompose/compose-logstash-jms-jars-volume.txt");
        return logstashJmsVolume.replace(INDENT_PLACEHOLDER, " ".repeat(COMPOSE_SERVICE_VOLUMES_INDENT_SPACES));
    }

}
