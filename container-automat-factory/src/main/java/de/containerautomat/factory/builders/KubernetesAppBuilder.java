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

import static de.containerautomat.factory.builders.ApplicationTemplatesConstants.CONTAINER_SYSTEM_KUBERNETES;
import static de.containerautomat.factory.builders.ApplicationTemplatesConstants.CONTAINER_SYSTEM_KUBERNETES_ENVIRONMENT_VALUES_INDENT_SPACES;
import static de.containerautomat.factory.builders.ApplicationTemplatesConstants.ENVIRONMENT_COMMAND_PLACEHOLDER;
import static de.containerautomat.factory.builders.ApplicationTemplatesConstants.ENVIRONMENT_PASSWORDS_PLACEHOLDER;
import static de.containerautomat.factory.builders.ApplicationTemplatesConstants.ENVIRONMENT_VALUES_PLACEHOLDER;
import static de.containerautomat.factory.builders.ApplicationTemplatesConstants.GENERATION_ID_PLACEHOLDER;
import static de.containerautomat.factory.builders.ApplicationTemplatesConstants.KAFKA_ENVIRONMENT_PLACEHOLDER;
import static de.containerautomat.factory.builders.ApplicationTemplatesConstants.KAFKA_KUBERNETES_LOG_DIRS;
import static de.containerautomat.factory.builders.ApplicationTemplatesConstants.KAFKA_LOG_DIRS_PLACEHOLDER;
import static de.containerautomat.factory.builders.ApplicationTemplatesConstants.MESSAGING_ENVIRONMENT_PLACEHOLDER;
import static de.containerautomat.factory.builders.ApplicationTemplatesConstants.STORAGE_ENVIRONMENT_PLACEHOLDER;

/**
 * A class that provides methods for generating kubernetes-specific
 * files for a Container-Automat application.
 * <p/>
 * Through these methods, two areas are covered:
 * <ul>
 *     <li>
 *         Kubernetes yaml files for the services that comprise the generated
 *         application, i.e. the Java programs that represent the states of the
 *         DFA, the message broker, the database, and possibly other optional
 *         services.
 *     </li>
 *     <li>
 *         Shell scripts to execute 'kubectl kustomize', 'kubectl apply', and
 *         'kubectl delete' for a Kubernetes cluster of the generated application.
 *     </li>
 * </ul>
 */
class KubernetesAppBuilder {

    private static final String PARENT_FOLDER_KUBERNETES = "kubernetes/";
    private static final String FILE_EXTENSION_YAML = ".yaml";


    private final DfaApplicationBuilder dfaApplicationBuilder;

    private final ApplicationMetaData applicationMetaData;


    public KubernetesAppBuilder(DfaApplicationBuilder dfaApplicationBuilder) {
        this.dfaApplicationBuilder = dfaApplicationBuilder;
        this.applicationMetaData = dfaApplicationBuilder.getApplicationMetaData();
    }

    @SneakyThrows
    void createGeneralKubernetesFiles() {

        final String[] generalKubernetesFileTemplates = {
                "kubernetes/k8s-create-container-automat.cmd.txt",
                "kubernetes/k8s-create-container-automat.sh.txt",
                "kubernetes/k8s-delete-container-automat.cmd.txt",
                "kubernetes/k8s-delete-container-automat.sh.txt"
        };
        final String[] generalKubernetesFileTargets = {
                "kubernetes/k8s-create-container-automat.cmd",
                "kubernetes/k8s-create-container-automat.sh",
                "kubernetes/k8s-delete-container-automat.cmd",
                "kubernetes/k8s-delete-container-automat.sh"
        };
        dfaApplicationBuilder.createTargetFiles(generalKubernetesFileTemplates, generalKubernetesFileTargets);

        var storageType = applicationMetaData.getStorageType();
        var messagingType = applicationMetaData.getMessagingType();
        String[] requiredServiceManifestTemplates = {
                PARENT_FOLDER_KUBERNETES + storageType.getDisplayName().toLowerCase() + ".yaml.txt",
                PARENT_FOLDER_KUBERNETES + messagingType.getDisplayName().toLowerCase() + ".yaml.txt"
        };
        String[] requiredServiceManifestTargets = {
                PARENT_FOLDER_KUBERNETES + storageType.getDisplayName().toLowerCase() + FILE_EXTENSION_YAML,
                PARENT_FOLDER_KUBERNETES + messagingType.getDisplayName().toLowerCase() + FILE_EXTENSION_YAML
        };
        dfaApplicationBuilder.createTargetFiles(requiredServiceManifestTemplates, requiredServiceManifestTargets);

        if (applicationMetaData.isIncludeOptionalServices()) {

            final String[] optionalServiceManifestTemplates = {
                    "kubernetes/elastic.yaml.txt",
                    "kubernetes/kibana.yaml.txt",
                    "kubernetes/logstash.yaml.txt"
            };
            final String[] optionalServiceManifestTargets = {
                    "kubernetes/elastic.yaml",
                    "kubernetes/kibana.yaml",
                    "kubernetes/logstash.yaml"
            };

            for (int templateIndex = 0; templateIndex < optionalServiceManifestTemplates.length; templateIndex++) {

                var logstashManifest = dfaApplicationBuilder.readTemplateResource(optionalServiceManifestTemplates[templateIndex]);
                var kuberntesMessagingEnvironment = dfaApplicationBuilder.getContainerServiceEnvironment(applicationMetaData.getMessagingType().name(), CONTAINER_SYSTEM_KUBERNETES, CONTAINER_SYSTEM_KUBERNETES_ENVIRONMENT_VALUES_INDENT_SPACES);
                logstashManifest = logstashManifest.replace(MESSAGING_ENVIRONMENT_PLACEHOLDER, kuberntesMessagingEnvironment);
                logstashManifest = dfaApplicationBuilder.resolveApplicationAndServicePlaceholders(logstashManifest);
                dfaApplicationBuilder.writeTargetFile(logstashManifest, optionalServiceManifestTargets[templateIndex]);
            }

            createLogstashConfigManifest();
        }
    }

    @SneakyThrows
    void createKustomizationFile() {

        var kustomizeTemplate = dfaApplicationBuilder.readTemplateResource("kubernetes/kustomization.yaml.txt");
        var kustomizeBuilder = new StringBuilder(kustomizeTemplate);
        for (int stateNumber = 1; stateNumber <= dfaApplicationBuilder.getDfaApplicationParameters().getDfa().getStates().size(); stateNumber++) {
            kustomizeBuilder.append("- state-");
            kustomizeBuilder.append(stateNumber);
            kustomizeBuilder.append(".yaml\r\n");
        }

        kustomizeTemplate = kustomizeBuilder.toString();
        var kustomizationEnvironmentValues = getKustomizationValues("environment/container-environment.txt");
        kustomizeTemplate = kustomizeTemplate.replace(ENVIRONMENT_VALUES_PLACEHOLDER, kustomizationEnvironmentValues);
        var kustomizationPasswordValues = getKustomizationValues("environment/container-passwords.txt");
        kustomizeTemplate = kustomizeTemplate.replace(ENVIRONMENT_PASSWORDS_PLACEHOLDER, kustomizationPasswordValues);
        if (applicationMetaData.getMessagingType().equals(ApplicationMetaData.MessagingType.KAFKA)) {
            var kafkaEnvironmentValues = getKustomizationValues("environment/kafka.env.txt");
            kustomizeTemplate = kustomizeTemplate.replace(KAFKA_ENVIRONMENT_PLACEHOLDER, kafkaEnvironmentValues);
            kustomizeTemplate = kustomizeTemplate.replace(KAFKA_LOG_DIRS_PLACEHOLDER, KAFKA_KUBERNETES_LOG_DIRS);
        } else {
            kustomizeTemplate = kustomizeTemplate.replace(KAFKA_ENVIRONMENT_PLACEHOLDER, "");
        }
        kustomizeTemplate = dfaApplicationBuilder.resolveApplicationAndServicePlaceholders(kustomizeTemplate);
        kustomizeTemplate = applicationMetaData.resolveOptionalServicePlaceholders(kustomizeTemplate);
        dfaApplicationBuilder.writeTargetFile(kustomizeTemplate, "kubernetes/kustomization.yaml");
    }

    private String getKustomizationValues(String valuesTemplateFile) throws IOException {

        var kustomzationValues = dfaApplicationBuilder.readTemplateResource(valuesTemplateFile);
        kustomzationValues = kustomzationValues.replace(GENERATION_ID_PLACEHOLDER, dfaApplicationBuilder.getGenerationId());
        kustomzationValues = kustomzationValues.replace(ENVIRONMENT_COMMAND_PLACEHOLDER, "  - ");
        return kustomzationValues;
    }

    @SneakyThrows
    void createEntryManifest() {

        var entryTemplate = dfaApplicationBuilder.readTemplateResource("kubernetes/entry.yaml.txt");
        var kubernetesStorageEnvironment = dfaApplicationBuilder.getContainerServiceEnvironment(applicationMetaData.getStorageType().name(), CONTAINER_SYSTEM_KUBERNETES, CONTAINER_SYSTEM_KUBERNETES_ENVIRONMENT_VALUES_INDENT_SPACES);
        entryTemplate = entryTemplate.replace(STORAGE_ENVIRONMENT_PLACEHOLDER, kubernetesStorageEnvironment);
        var kuberntesMessagingEnvironment = dfaApplicationBuilder.getContainerServiceEnvironment(applicationMetaData.getMessagingType().name(), CONTAINER_SYSTEM_KUBERNETES, CONTAINER_SYSTEM_KUBERNETES_ENVIRONMENT_VALUES_INDENT_SPACES);
        entryTemplate = entryTemplate.replace(MESSAGING_ENVIRONMENT_PLACEHOLDER, kuberntesMessagingEnvironment);
        entryTemplate = dfaApplicationBuilder.resolveApplicationAndServicePlaceholders(entryTemplate);
        dfaApplicationBuilder.writeTargetFile(entryTemplate, "kubernetes/entry.yaml");
    }

    @SneakyThrows
    void createStateManifests() {

        var kubernetesStateTemplate = dfaApplicationBuilder.readTemplateResource("kubernetes/state.yaml.txt");
        var kubernetesStorageEnvironment = dfaApplicationBuilder.getContainerServiceEnvironment(applicationMetaData.getStorageType().name(), CONTAINER_SYSTEM_KUBERNETES, CONTAINER_SYSTEM_KUBERNETES_ENVIRONMENT_VALUES_INDENT_SPACES);
        kubernetesStateTemplate = kubernetesStateTemplate.replace(STORAGE_ENVIRONMENT_PLACEHOLDER, kubernetesStorageEnvironment);
        var kuberntesMessagingEnvironment = dfaApplicationBuilder.getContainerServiceEnvironment(applicationMetaData.getMessagingType().name(), CONTAINER_SYSTEM_KUBERNETES, CONTAINER_SYSTEM_KUBERNETES_ENVIRONMENT_VALUES_INDENT_SPACES);
        kubernetesStateTemplate = kubernetesStateTemplate.replace(MESSAGING_ENVIRONMENT_PLACEHOLDER, kuberntesMessagingEnvironment);
        kubernetesStateTemplate = dfaApplicationBuilder.resolveApplicationAndServicePlaceholders(kubernetesStateTemplate);

        int stateNumber = 1;
        int managementPort = 9995;
        for (AutomatonState state : new TreeSet<>(dfaApplicationBuilder.getDfaApplicationParameters().getDfa().getStates())) {
            var stateManifest = dfaApplicationBuilder.resolveStateSpecificPlaceholders(kubernetesStateTemplate, state.getName(), stateNumber, managementPort--);
            dfaApplicationBuilder.writeTargetFile(stateManifest, "kubernetes/state-" + (stateNumber++) + FILE_EXTENSION_YAML);
        }
    }

    @SneakyThrows
    void createLogstashConfigManifest() {

        var logstashConfigManifest = dfaApplicationBuilder.readTemplateResource("kubernetes/logstash-config.yaml.txt");
        logstashConfigManifest = dfaApplicationBuilder.resolveApplicationAndServicePlaceholders(logstashConfigManifest);
        var logstashPipelineConfig = dfaApplicationBuilder.getLogstashPipelineConfig(4);
        logstashConfigManifest += logstashPipelineConfig;
        dfaApplicationBuilder.writeTargetFile(logstashConfigManifest, "kubernetes/logstash-config.yaml");
    }

}
