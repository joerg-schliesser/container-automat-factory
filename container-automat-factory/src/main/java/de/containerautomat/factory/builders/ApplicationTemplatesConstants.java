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

/**
 * A class that contains constants used in the source templates for
 * generating a Container-Automat application.
 */
public class ApplicationTemplatesConstants {

    static final String CONTAINER_AUTOMAT = "ContainerAutomat";
    static final String CONTAINER_AUTOMAT_LOWERCASE = CONTAINER_AUTOMAT.toLowerCase();
    static final String CONTAINER_AUTOMAT_UPPERCASE = CONTAINER_AUTOMAT.toUpperCase();
    static final String CONTAINER_AUTOMAT_CAMELCASE = "containerAutomat";
    static final String CONTAINER_AUTOMAT_KEBABCASE = "container-automat";

    static final String TEMPLATES_PARENT_FOLDER = "templates/";

    static final String BEFORE_INPUT_STRING_REGEXP_MARKER = "// BEFORE_INPUT_STRING_REGEXP_MARKER";
    static final String AFTER_INPUT_STRING_REGEXP_MARKER = "// AFTER_INPUT_STRING_REGEXP_MARKER";

    static final String PLACEHOLDER_DELIMITER = "§";
    static final String START_SUFFIX = "_START";
    static final String END_SUFFIX = "_END";
    static final String JAVA_PACKAGE_LINE_PREFIX = "package ";

    static final String CONTAINER_REGISTRY_PLACEHOLDER = PLACEHOLDER_DELIMITER + "containerregistry" + PLACEHOLDER_DELIMITER;
    static final String CONTAINER_SYSTEM_COMPOSE = "compose";
    static final String CONTAINER_SYSTEM_KUBERNETES = "kubernetes";
    static final int CONTAINER_SYSTEM_COMPOSE_ENVIRONMENT_VALUES_INDENT_SPACES = 4;
    static final int CONTAINER_SYSTEM_KUBERNETES_ENVIRONMENT_VALUES_INDENT_SPACES = 8;

    static final String GENERATION_ID_PLACEHOLDER = PLACEHOLDER_DELIMITER + "generation_id" + PLACEHOLDER_DELIMITER;
    static final String INDENT_PLACEHOLDER = PLACEHOLDER_DELIMITER + "indent" + PLACEHOLDER_DELIMITER;

    static final String STATE_NUMBER_PLACEHOLDER = PLACEHOLDER_DELIMITER + "state_number" + PLACEHOLDER_DELIMITER;
    static final String STATE_NAME_PLACEHOLDER = PLACEHOLDER_DELIMITER + "state_name" + PLACEHOLDER_DELIMITER;
    static final String STATE_MANAGEMENT_PORT_PLACEHOLDER = PLACEHOLDER_DELIMITER + "state_management_port" + PLACEHOLDER_DELIMITER;
    static final int STATE_MANAGEMENT_PORT_BASE = 9995;

    static final String ENVIRONMENT_COMMAND_PLACEHOLDER = PLACEHOLDER_DELIMITER + "environment_command" + PLACEHOLDER_DELIMITER;
    static final String ENVIRONMENT_COMMANDS_PLACEHOLDER = PLACEHOLDER_DELIMITER + "environment_commands" + PLACEHOLDER_DELIMITER;
    static final String ENVIRONMENT_VALUES_PLACEHOLDER = PLACEHOLDER_DELIMITER + "environment_values" + PLACEHOLDER_DELIMITER;
    static final String ENVIRONMENT_PASSWORDS_PLACEHOLDER = PLACEHOLDER_DELIMITER + "environment_passwords" + PLACEHOLDER_DELIMITER;

    static final String MESSAGING_ENVIRONMENT_PLACEHOLDER = PLACEHOLDER_DELIMITER + "messaging_environment" + PLACEHOLDER_DELIMITER;
    static final String MESSAGING_TYPE_CONTAINERNAME_PLACEHOLDER = PLACEHOLDER_DELIMITER + "messaging_type_containername" + PLACEHOLDER_DELIMITER;
    static final String MESSAGING_TYPE_LOWERCASE_PLACEHOLDER = PLACEHOLDER_DELIMITER + "messaging_type_lowercase" + PLACEHOLDER_DELIMITER;

    static final String STORAGE_ENVIRONMENT_PLACEHOLDER = PLACEHOLDER_DELIMITER + "storage_environment" + PLACEHOLDER_DELIMITER;
    static final String STORAGE_TYPE_CONTAINERNAME_PLACEHOLDER = PLACEHOLDER_DELIMITER + "storage_type_containername" + PLACEHOLDER_DELIMITER;
    static final String STORAGE_TYPE_LOWERCASE_PLACEHOLDER = PLACEHOLDER_DELIMITER + "storage_type_lowercase" + PLACEHOLDER_DELIMITER;

    static final String KAFKA_ENVIRONMENT_PLACEHOLDER = PLACEHOLDER_DELIMITER + "kafka_environment" + PLACEHOLDER_DELIMITER;
    static final String KAFKA_LOG_DIRS_PLACEHOLDER = PLACEHOLDER_DELIMITER + "kafka_log_dirs" + PLACEHOLDER_DELIMITER;
    static final String KAFKA_DOCKER_LOG_DIRS = "/tmp/kraft-combined-logs";
    static final String KAFKA_KUBERNETES_LOG_DIRS = "/var/kafka/kraft-combined-logs";

    static final String OPTIONAL_SERVICE = "OPTIONAL_SERVICE";

    static final String LOGSTASH_PREPARE_COMMANDS_PLACEHOLDER = PLACEHOLDER_DELIMITER + "logstash_prepare_commands" + PLACEHOLDER_DELIMITER;
    static final String LOGSTASH_CONFFILENAME_PLACEHOLDER = PLACEHOLDER_DELIMITER + "logstash_conf_filename" + PLACEHOLDER_DELIMITER;
    static final String LOGSTASH_JMS_JARS_VOLUME_PLACEHOLDER = PLACEHOLDER_DELIMITER + "logstash_jms_jars_volume" + PLACEHOLDER_DELIMITER;
    static final String LOGSTASH_CONF_SOURCE_PATH_TEMPLATE = "configs/logstash-%s.conf.txt";
    static final String LOGSTASH_CONF_TARGET_PATH_TEMPLATE = "%s/logstash-%s.conf";
    static final String LOGSTASH_PREPARE_COMMANDS_TEMPLATE = "localrun/logstash-prepare-%s-%s.txt";

    private ApplicationTemplatesConstants() {
    }

}
