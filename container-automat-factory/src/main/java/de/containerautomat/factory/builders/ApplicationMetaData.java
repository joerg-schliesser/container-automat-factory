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

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.apache.commons.lang3.tuple.Pair;

import static de.containerautomat.factory.builders.ApplicationTemplatesConstants.*;

/**
 * A data type that represents the meta-data for the generation of a
 * Container-Automat application.
 * <p/>
 * The meta-data consists on the one hand of names that are used in
 * various places in the code generation:
 * <ul>
 *     <li>The name of the application.</li>
 *     <li>The Java package for the application source code.</li>
 *     <li>The name of a container registry.</li>
 * </ul>
 * In addition, the meta-data contains IDs for the message broker and the
 * database to be used, as well as a flag for the inclusion or exclusion
 * of optional services in the generated application.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationMetaData {

    @Getter
    public enum MessagingType {
        RABBITMQ("RabbitMq"), ARTEMIS("Artemis"), KAFKA("Kafka");

        private final String displayName;

        MessagingType(String displayName) {
            this.displayName = displayName;
        }

        public Pair<String, String> getLocalhostConnectionProperty() {

            return getLocalhostConnectionProperty(this);
        }

        public String getContainerName(String appName) {

            return appName.toLowerCase() + "-" + name().toLowerCase();
        }

        public static Pair<String, String> getLocalhostConnectionProperty(MessagingType messagingType) {

            return switch (messagingType) {
                case ARTEMIS -> Pair.of(PROPERTY_NAME_SPRING_ARTEMIS_BROKER_URL, PROPERTY_VALUE_SPRING_ARTEMIS_BROKER_URL);
                case KAFKA -> Pair.of(PROPERTY_NAME_SPRING_KAFKA_BOOTSTRAP_SERVERS, PROPERTY_VALUE_SPRING_KAFKA_BOOTSTRAP_SERVERS);
                case RABBITMQ -> Pair.of(PROPERTY_NAME_SPRING_RABBITMQ_HOST, PROPERTY_VALUE_LOCALHOST);
            };
        }
    }

    @Getter
    public enum StorageType {

        REDIS("Redis"), MONGODB("MongoDb"), POSTGRESQL("PostgreSql");

        private final String displayName;

        StorageType(String displayName) {
            this.displayName = displayName;
        }

        public Pair<String, String> getLocalhostConnectionProperty() {

            return getLocalhostConnectionProperty(this);
        }

        public String getContainerName(String appName) {

            return appName.toLowerCase() + "-" + name().toLowerCase();
        }

        public static Pair<String, String> getLocalhostConnectionProperty(StorageType storageType) {

            return switch (storageType) {
                case REDIS -> Pair.of(PROPERTY_NAME_SPRING_DATA_REDIS_HOST, PROPERTY_VALUE_LOCALHOST);
                case MONGODB -> Pair.of(PROPERTY_NAME_SPRING_DATA_MONGODB_HOST, PROPERTY_VALUE_LOCALHOST);
                case POSTGRESQL -> Pair.of(PROPERTY_NAME_SPRING_DATASOURCE_POSTGRESQL_URL, PROPERTY_VALUE_SPRING_DATASOURCE_POSTGRESQL_URL);
            };
        }
    }


    static final String REGEXP_CRLF = "\\r?\\n?";
    static final String REGEXP_APP_NAME = "[\\p{Alpha}_][\\p{Alnum}_]*";
    static final String REGEXP_APP_PACKAGE = "[\\p{Lower}_]((\\.[\\p{Lower}_])?[\\p{Lower}\\d_]*)*";
    static final String REGEXP_CONTAINER_REGISTRY = "[\\p{Alpha}_][\\p{Alnum}_\\-/]*";


    static final int MAX_APP_NAME = 30;
    static final int MAX_APP_PACKAGE = 100;
    static final int MAX_CONTAINER_REGISTRY = 100;

    static final String PROPERTY_NAME_SPRING_ARTEMIS_BROKER_URL = "spring.artemis.broker-url";
    static final String PROPERTY_NAME_SPRING_KAFKA_BOOTSTRAP_SERVERS = "spring.kafka.bootstrap-servers";
    static final String PROPERTY_NAME_SPRING_RABBITMQ_HOST = "spring.rabbitmq.host";
    static final String PROPERTY_NAME_SPRING_DATA_REDIS_HOST = "spring.data.redis.host";
    static final String PROPERTY_NAME_SPRING_DATA_MONGODB_HOST = "spring.data.mongodb.host";
    static final String PROPERTY_NAME_SPRING_DATASOURCE_POSTGRESQL_URL = "spring.datasource.url";
    static final String PROPERTY_VALUE_SPRING_ARTEMIS_BROKER_URL = "tcp://localhost:${ARTEMIS_PORT}";
    static final String PROPERTY_VALUE_SPRING_KAFKA_BOOTSTRAP_SERVERS = "localhost:${KAFKA_PORT}";
    static final String PROPERTY_VALUE_SPRING_DATASOURCE_POSTGRESQL_URL = "jdbc:postgresql://localhost:${POSTGRESQL_PORT}/ContainerAutomatDB";
    static final String PROPERTY_VALUE_LOCALHOST = "localhost";


    @NotEmpty
    @Pattern(regexp = REGEXP_APP_NAME)
    @Size(max = MAX_APP_NAME)
    private String appName;

    @NotEmpty
    @Pattern(regexp = REGEXP_APP_PACKAGE)
    @Size(max = MAX_APP_PACKAGE)
    private String appPackage;

    @NotEmpty
    @Size(max = MAX_CONTAINER_REGISTRY)
    private String containerRegistry;

    @NonNull
    private StorageType storageType;

    @NonNull
    private MessagingType messagingType;

    private boolean includeOptionalServices;


    public String removeUnneededMessagingTypeSections(String sourceText) {

        var result = sourceText;
        for (MessagingType messagingTypeCandidate : MessagingType.values()) {
            if (messagingTypeCandidate == messagingType) {
                result = removeTextSectionStartAndEndMarkers(result, messagingTypeCandidate.name().toUpperCase());
            } else {
                result = removeTextSections(result, messagingTypeCandidate.name().toUpperCase());
            }
        }
        return result;
    }

    public String removeUnneededStorageTypeSections(String sourceText) {

        var result = sourceText;
        for (ApplicationMetaData.StorageType storageTypeCandidate : ApplicationMetaData.StorageType.values()) {
            if (storageTypeCandidate == storageType) {
                result = removeTextSectionStartAndEndMarkers(result, storageTypeCandidate.name().toUpperCase());
            } else {
                result = removeTextSections(result, storageTypeCandidate.name().toUpperCase());
            }
        }
        return result;
    }

    public String resolveOptionalServicePlaceholders(String sourceText) {

        if(includeOptionalServices) {
            return removeTextSectionStartAndEndMarkers(sourceText, OPTIONAL_SERVICE);
        }
        return removeTextSections(sourceText, OPTIONAL_SERVICE);
    }

    static String removeTextSections(String sourceText, String sectionName) {

        var sectionStartMarker = createTextSectionStartOrEndMarker(sectionName, true);
        var sectionEndMarker = createTextSectionStartOrEndMarker(sectionName, false);

        String result = sourceText;
        for (; ; ) {
            var start = result.indexOf(sectionStartMarker);
            if (start == -1) {
                return result;
            }

            var end = result.indexOf(sectionEndMarker, start);
            if (end == -1) {
                return result;
            }
            end += sectionEndMarker.length();

            if (end < result.length() && result.charAt(end) == '\r') {
                end++;
            }
            if (end < result.length() && result.charAt(end) == '\n') {
                end++;
            }

            result = result.substring(0, start) + result.substring(end);
        }
    }

    static String removeTextSectionStartAndEndMarkers(String sourceText, String sectionName) {

        var sectionStartMarker = createTextSectionStartOrEndMarker(sectionName, true) + REGEXP_CRLF;
        var sectionEndMarker = createTextSectionStartOrEndMarker(sectionName, false) + REGEXP_CRLF;
        return sourceText.replaceAll(sectionStartMarker, "").replaceAll(sectionEndMarker, "");
    }

    static String createTextSectionStartOrEndMarker(final String sectionName, final boolean isStart) {

        return PLACEHOLDER_DELIMITER + sectionName + (isStart ? START_SUFFIX : END_SUFFIX) + PLACEHOLDER_DELIMITER;
    }

}
