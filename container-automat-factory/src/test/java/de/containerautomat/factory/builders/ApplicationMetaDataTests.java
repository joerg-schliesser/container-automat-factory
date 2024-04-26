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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.UUID;

import static de.containerautomat.factory.builders.ApplicationTemplatesConstants.*;
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

    @Test
    void remove_text_sections_from_text() {

        var testSectionName = "TestSection" + UUID.randomUUID();
        var testStartMarker = String.format(TEST_TEXT_SECTION_START_TEMPLATE, testSectionName);
        var testEndMarker = String.format(TEST_TEXT_SECTION_END_TEMPLATE, testSectionName);
        var testTemplate = """
                remaining top
                remaining top
                %1$s
                toBeRemoved 1
                toBeRemoved 1
                %2$s
                remaining center
                %1$s
                toBeRemoved 2
                toBeRemoved 2
                toBeRemoved 2
                %2$s
                remaining bottom
                remaining bottom
                remaining bottom
                """;
        var testText = testTemplate.formatted(testStartMarker, testEndMarker);

        var extedtedText = """
                remaining top
                remaining top
                remaining center
                remaining bottom
                remaining bottom
                remaining bottom
                """;

        var resultText = ApplicationMetaData.removeTextSections(testText, testSectionName);

        assertEquals(extedtedText, resultText);
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

}
