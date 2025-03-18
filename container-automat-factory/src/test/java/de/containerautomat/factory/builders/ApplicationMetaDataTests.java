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

import de.containerautomat.factory.testutils.FactoryTestDataProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;
import java.util.UUID;

import static de.containerautomat.factory.builders.ApplicationMetaData.PROPERTY_NAME_SPRING_ARTEMIS_BROKER_URL;
import static de.containerautomat.factory.builders.ApplicationMetaData.PROPERTY_NAME_SPRING_DATASOURCE_POSTGRESQL_URL;
import static de.containerautomat.factory.builders.ApplicationMetaData.PROPERTY_NAME_SPRING_DATA_MONGODB_HOST;
import static de.containerautomat.factory.builders.ApplicationMetaData.PROPERTY_NAME_SPRING_DATA_REDIS_HOST;
import static de.containerautomat.factory.builders.ApplicationMetaData.PROPERTY_NAME_SPRING_KAFKA_BOOTSTRAP_SERVERS;
import static de.containerautomat.factory.builders.ApplicationMetaData.PROPERTY_NAME_SPRING_RABBITMQ_HOST;
import static de.containerautomat.factory.builders.ApplicationMetaData.PROPERTY_VALUE_LOCALHOST;
import static de.containerautomat.factory.builders.ApplicationMetaData.PROPERTY_VALUE_SPRING_ARTEMIS_BROKER_URL;
import static de.containerautomat.factory.builders.ApplicationMetaData.PROPERTY_VALUE_SPRING_DATASOURCE_POSTGRESQL_URL;
import static de.containerautomat.factory.builders.ApplicationMetaData.PROPERTY_VALUE_SPRING_KAFKA_BOOTSTRAP_SERVERS;
import static de.containerautomat.factory.builders.ApplicationTemplatesConstants.END_SUFFIX;
import static de.containerautomat.factory.builders.ApplicationTemplatesConstants.PLACEHOLDER_DELIMITER;
import static de.containerautomat.factory.builders.ApplicationTemplatesConstants.START_SUFFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A test suite for testing methods defined in the {@link ApplicationMetaData}
 * class to adapt source texts according to the message broker and the
 * database to be used for the Container-Automat application to be generated.
 */
class ApplicationMetaDataTests {

    private static final String TEST_TEXT_SECTION_START_TEMPLATE = PLACEHOLDER_DELIMITER + "%s" + START_SUFFIX + PLACEHOLDER_DELIMITER;
    private static final String TEST_TEXT_SECTION_END_TEMPLATE = PLACEHOLDER_DELIMITER + "%s" + END_SUFFIX + PLACEHOLDER_DELIMITER;


    @ParameterizedTest
    @EnumSource(ApplicationMetaData.MessagingType.class)
    void remove_unneeded_messaging_type_sections_from_text(ApplicationMetaData.MessagingType messagingTypeToUse) {

        var testText = """
                Top section
                Top section
                §ARTEMIS_START§
                Artemis section 1
                Artemis section 1
                Artemis section 1
                §ARTEMIS_END§
                §KAFKA_START§
                Kafka section 1
                Kafka section 1
                Kafka section 1
                Kafka section 1
                §KAFKA_END§
                §RABBITMQ_START§
                RabbitMq section 1
                RabbitMq section 1
                §RABBITMQ_END§
                center section
                center section
                center section
                §RABBITMQ_START§
                RabbitMq section 2
                RabbitMq section 2
                RabbitMq section 2
                §RABBITMQ_END§
                §ARTEMIS_START§
                Artemis section 2
                Artemis section 2
                §ARTEMIS_END§
                §KAFKA_START§
                Kafka section 2
                §KAFKA_END§
                bottom section
                """;

        var expectedArtemisText = """
                Top section
                Top section
                Artemis section 1
                Artemis section 1
                Artemis section 1
                center section
                center section
                center section
                Artemis section 2
                Artemis section 2
                bottom section
                """;

        var expectedKafkaText = """
                Top section
                Top section
                Kafka section 1
                Kafka section 1
                Kafka section 1
                Kafka section 1
                center section
                center section
                center section
                Kafka section 2
                bottom section
                """;

        var expectedRabbitMqText = """
                Top section
                Top section
                RabbitMq section 1
                RabbitMq section 1
                center section
                center section
                center section
                RabbitMq section 2
                RabbitMq section 2
                RabbitMq section 2
                bottom section
                """;

        var expectedText = switch (messagingTypeToUse) {
            case ARTEMIS -> expectedArtemisText;
            case KAFKA -> expectedKafkaText;
            case RABBITMQ -> expectedRabbitMqText;
        };

        var aplicationMetaData = ApplicationMetaData.builder()
                .messagingType(messagingTypeToUse)
                .storageType(ApplicationMetaData.StorageType.MONGODB)
                .appName("SampleApp")
                .appPackage("org.testcompany.sampleapp")
                .containerRegistry("testcompany")
                .includeOptionalServices(true)
                .build();

        var resultText = aplicationMetaData.removeUnneededMessagingTypeSections(testText);

        assertEquals(expectedText, resultText);
    }

    @ParameterizedTest
    @EnumSource(ApplicationMetaData.StorageType.class)
    void remove_unneeded_storage_type_sections_from_text(ApplicationMetaData.StorageType storageTypeToUse) {

        var testText = """
                Top section
                Top section
                §MONGODB_START§
                MongoDB section 1
                MongoDB section 1
                MongoDB section 1
                §MONGODB_END§
                §POSTGRESQL_START§
                PostgreSQL section 1
                PostgreSQL section 1
                PostgreSQL section 1
                §POSTGRESQL_END§
                §REDIS_START§
                Redis section 1
                Redis section 1
                §REDIS_END§
                center section
                center section
                center section
                §REDIS_START§
                Redis section 2
                Redis section 2
                Redis section 2
                §REDIS_END§
                §MONGODB_START§
                MongoDB section 2
                MongoDB section 2
                §MONGODB_END§
                §POSTGRESQL_START§
                PostgreSQL section 2
                PostgreSQL section 2
                §POSTGRESQL_END§
                bottom section
                """;

        var expectedMongoDbText = """
                Top section
                Top section
                MongoDB section 1
                MongoDB section 1
                MongoDB section 1
                center section
                center section
                center section
                MongoDB section 2
                MongoDB section 2
                bottom section
                """;

        var expectedPostgreSqlText = """
                Top section
                Top section
                PostgreSQL section 1
                PostgreSQL section 1
                PostgreSQL section 1
                center section
                center section
                center section
                PostgreSQL section 2
                PostgreSQL section 2
                bottom section
                """;

        var expectedRedisText = """
                Top section
                Top section
                Redis section 1
                Redis section 1
                center section
                center section
                center section
                Redis section 2
                Redis section 2
                Redis section 2
                bottom section
                """;

        var expectedText = switch (storageTypeToUse) {
            case REDIS -> expectedRedisText;
            case MONGODB -> expectedMongoDbText;
            case POSTGRESQL -> expectedPostgreSqlText;
        };

        var aplicationMetaData = ApplicationMetaData.builder()
                .storageType(storageTypeToUse)
                .appName("TestApp")
                .appPackage("org.testcompany.testapp")
                .containerRegistry("testregistry")
                .messagingType(ApplicationMetaData.MessagingType.RABBITMQ)
                .includeOptionalServices(true)
                .build();

        var resultText = aplicationMetaData.removeUnneededStorageTypeSections(testText);

        assertEquals(expectedText, resultText);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "\r"})
    void remove_text_sections_from_text(String lfPrefix) {

        var testSectionName = "TestSection" + UUID.randomUUID();
        var testStartMarker = String.format(TEST_TEXT_SECTION_START_TEMPLATE, testSectionName);
        var testEndMarker = String.format(TEST_TEXT_SECTION_END_TEMPLATE, testSectionName);
        var testTemplate = """
                remaining top%3$s
                remaining top%3$s
                %1$s%3$s
                toBeRemoved 1%3$s
                toBeRemoved 1%3$s
                %2$s%3$s
                remaining center%3$s
                %1$s%3$s
                toBeRemoved 2%3$s
                toBeRemoved 2%3$s
                toBeRemoved 2%3$s
                %2$s%3$s
                remaining bottom%3$s
                remaining bottom%3$s
                remaining bottom%3$s
                """;
        var testText = testTemplate.formatted(testStartMarker, testEndMarker, lfPrefix);

        var extedtedTemplate = """
                remaining top%1$s
                remaining top%1$s
                remaining center%1$s
                remaining bottom%1$s
                remaining bottom%1$s
                remaining bottom%1$s
                """;
        var extedtedText = extedtedTemplate.formatted(lfPrefix);

        var resultText = ApplicationMetaData.removeTextSections(testText, testSectionName);

        assertEquals(extedtedText, resultText);
    }

    @Test
    void remove_text_section_from_text_without_start_marker() {

        var testSectionName = "TestSection" + UUID.randomUUID();
        var testEndMarker = String.format(TEST_TEXT_SECTION_END_TEMPLATE, testSectionName);
        var testTemplate = """
                text part 1
                text part 1
                %1$s
                text part 2
                text part 2
                """;
        var testText = testTemplate.formatted(testEndMarker);

        var resultText = ApplicationMetaData.removeTextSections(testText, testSectionName);

        assertEquals(testText, resultText);
    }

    @Test
    void remove_text_section_from_text_without_end_marker() {

        var testSectionName = "TestSection" + UUID.randomUUID();
        var testStartMarker = String.format(TEST_TEXT_SECTION_START_TEMPLATE, testSectionName);
        var testTemplate = """
                text part 1
                text part 1
                %1$s
                text part 2
                text part 2
                """;
        var testText = testTemplate.formatted(testStartMarker);

        var resultText = ApplicationMetaData.removeTextSections(testText, testSectionName);

        assertEquals(testText, resultText);
    }

    @Test
    void remove_text_section_start_and_end_markers_from_text() {

        var testSectionName = "TestSection" + UUID.randomUUID();
        var testStartMarker = String.format(TEST_TEXT_SECTION_START_TEMPLATE, testSectionName);
        var testEndMarker = String.format(TEST_TEXT_SECTION_END_TEMPLATE, testSectionName);
        var testTemplate = """
                remaining top
                %1$s
                remaining part 1
                remaining part 1
                %2$s
                remaining part 2
                %1$s
                remaining part 3
                remaining part 3
                %2$s
                remaining bottom
                remaining bottom
                remaining bottom
                """;
        var testText = testTemplate.formatted(testStartMarker, testEndMarker);

        var expectedText = """
                remaining top
                remaining part 1
                remaining part 1
                remaining part 2
                remaining part 3
                remaining part 3
                remaining bottom
                remaining bottom
                remaining bottom
                """;

        var resultText = ApplicationMetaData.removeTextSectionStartAndEndMarkers(testText, testSectionName);

        assertEquals(expectedText, resultText);
    }

    @Test
    void create_text_section_start_and_end_markers() {

        var testSectionName = "TestSection";

        var expectedStartMarker = String.format(TEST_TEXT_SECTION_START_TEMPLATE, testSectionName);
        var expectedEndMarker = String.format(TEST_TEXT_SECTION_END_TEMPLATE, testSectionName);

        assertEquals(expectedStartMarker, ApplicationMetaData.createTextSectionStartOrEndMarker(testSectionName, true));
        assertEquals(expectedEndMarker, ApplicationMetaData.createTextSectionStartOrEndMarker(testSectionName, false));
    }

    @Test
    void resolve_optional_service_placeholders_including_optional_servies() {

        var testText = """
                Top section
                §OPTIONAL_SERVICE_START§
                Optional service section
                §OPTIONAL_SERVICE_END§
                Bottom section
                """;

        var expectedText = """
                Top section
                Optional service section
                Bottom section
                """;

        var applicationMetaData = FactoryTestDataProvider.createTestApplicationMetaData(true);
        var resultText = applicationMetaData.resolveOptionalServicePlaceholders(testText);

        assertEquals(expectedText, resultText);
    }

    @Test
    void resolve_optional_service_placeholders_excluding_optional_services() {

        var testText = """
                Top section
                §OPTIONAL_SERVICE_START§
                Optional service section
                §OPTIONAL_SERVICE_END§
                Bottom section
                """;

        var expectedText = """
                Top section
                Bottom section
                """;

        var applicationMetaData = FactoryTestDataProvider.createTestApplicationMetaData(false);
        var resultText = applicationMetaData.resolveOptionalServicePlaceholders(testText);

        assertEquals(expectedText, resultText);
    }

    @ParameterizedTest
    @EnumSource(ApplicationMetaData.MessagingType.class)
    void get_local_host_connection_property_for_messaging_type(ApplicationMetaData.MessagingType messagingType) {

        Map<ApplicationMetaData.MessagingType, String> propertyNames = Map.of(
                ApplicationMetaData.MessagingType.ARTEMIS, PROPERTY_NAME_SPRING_ARTEMIS_BROKER_URL,
                ApplicationMetaData.MessagingType.KAFKA, PROPERTY_NAME_SPRING_KAFKA_BOOTSTRAP_SERVERS,
                ApplicationMetaData.MessagingType.RABBITMQ, PROPERTY_NAME_SPRING_RABBITMQ_HOST
        );

        Map<ApplicationMetaData.MessagingType, String> propertyValues = Map.of(
                ApplicationMetaData.MessagingType.ARTEMIS, PROPERTY_VALUE_SPRING_ARTEMIS_BROKER_URL,
                ApplicationMetaData.MessagingType.KAFKA, PROPERTY_VALUE_SPRING_KAFKA_BOOTSTRAP_SERVERS,
                ApplicationMetaData.MessagingType.RABBITMQ, PROPERTY_VALUE_LOCALHOST
        );

        var expectedPropertyName = propertyNames.get(messagingType);
        var expectedPropertyValue = propertyValues.get(messagingType);
        var testPropertyObjectResult = messagingType.getLocalhostConnectionProperty();
        var testPropertyStaticResult = ApplicationMetaData.MessagingType.getLocalhostConnectionProperty(messagingType);

        assertEquals(expectedPropertyName, testPropertyObjectResult.getLeft());
        assertEquals(expectedPropertyValue, testPropertyObjectResult.getRight());
        assertEquals(expectedPropertyName, testPropertyStaticResult.getLeft());
        assertEquals(expectedPropertyValue, testPropertyStaticResult.getRight());
    }

    @ParameterizedTest
    @EnumSource(ApplicationMetaData.StorageType.class)
    void get_local_host_connection_property_for_storage_type(ApplicationMetaData.StorageType storageType) {

        Map<ApplicationMetaData.StorageType, String> propertyNames = Map.of(
                ApplicationMetaData.StorageType.MONGODB, PROPERTY_NAME_SPRING_DATA_MONGODB_HOST,
                ApplicationMetaData.StorageType.REDIS, PROPERTY_NAME_SPRING_DATA_REDIS_HOST,
                ApplicationMetaData.StorageType.POSTGRESQL, PROPERTY_NAME_SPRING_DATASOURCE_POSTGRESQL_URL
        );

        Map<ApplicationMetaData.StorageType, String> propertyValues = Map.of(
                ApplicationMetaData.StorageType.MONGODB, PROPERTY_VALUE_LOCALHOST,
                ApplicationMetaData.StorageType.REDIS, PROPERTY_VALUE_LOCALHOST,
                ApplicationMetaData.StorageType.POSTGRESQL, PROPERTY_VALUE_SPRING_DATASOURCE_POSTGRESQL_URL
        );

        var expectedPropertyName = propertyNames.get(storageType);
        var expectedPropertyValue = propertyValues.get(storageType);
        var testPropertyObjectResult = storageType.getLocalhostConnectionProperty();
        var testPropertyStaticResult = ApplicationMetaData.StorageType.getLocalhostConnectionProperty(storageType);

        assertEquals(expectedPropertyName, testPropertyObjectResult.getLeft());
        assertEquals(expectedPropertyValue, testPropertyObjectResult.getRight());
        assertEquals(expectedPropertyName, testPropertyStaticResult.getLeft());
        assertEquals(expectedPropertyValue, testPropertyStaticResult.getRight());
    }

    @ParameterizedTest
    @EnumSource(ApplicationMetaData.MessagingType.class)
    void get_container_name_for_messaging_type(ApplicationMetaData.MessagingType messagingType) {

        var applicationName = "TestApplicationName";
        var containerNamePrefix = applicationName.toLowerCase() + "-";

        Map<ApplicationMetaData.MessagingType, String> propertyNames = Map.of(
                ApplicationMetaData.MessagingType.ARTEMIS,containerNamePrefix + ApplicationMetaData.MessagingType.ARTEMIS.name().toLowerCase(),
                ApplicationMetaData.MessagingType.KAFKA, containerNamePrefix + ApplicationMetaData.MessagingType.KAFKA.name().toLowerCase(),
                ApplicationMetaData.MessagingType.RABBITMQ, containerNamePrefix + ApplicationMetaData.MessagingType.RABBITMQ.name().toLowerCase()
        );

        var expectedContainerName = propertyNames.get(messagingType);
        var testContainerName = messagingType.getContainerName(applicationName);

        assertEquals(expectedContainerName, testContainerName);
    }

    @ParameterizedTest
    @EnumSource(ApplicationMetaData.StorageType.class)
    void get_container_name_for_storage_type(ApplicationMetaData.StorageType storageType) {

        var applicationName = "TestApplicationName";
        var containerNamePrefix = applicationName.toLowerCase() + "-";

        Map<ApplicationMetaData.StorageType, String> propertyNames = Map.of(
                ApplicationMetaData.StorageType.MONGODB,containerNamePrefix + ApplicationMetaData.StorageType.MONGODB.name().toLowerCase(),
                ApplicationMetaData.StorageType.REDIS, containerNamePrefix + ApplicationMetaData.StorageType.REDIS.name().toLowerCase(),
                ApplicationMetaData.StorageType.POSTGRESQL, containerNamePrefix + ApplicationMetaData.StorageType.POSTGRESQL.name().toLowerCase()
        );

        var expectedContainerName = propertyNames.get(storageType);
        var testContainerName = storageType.getContainerName(applicationName);

        assertEquals(expectedContainerName, testContainerName);
    }
}
