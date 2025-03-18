/*
 * Copyright 2025 the original author or authors.
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

import de.containerautomat.factory.ContainerAutomatFactoryApp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A test suite for testing methods defined in the {@link TargetFileCommentBuilder}
 * class for creating file comments in the generated Container-Automat application
 * according to the respective file type.
 */
class TargetFileCommentBuilderTests {

    static final String TEST_GENERATION_ID = "TestGeneration";


    @ParameterizedTest
    @EnumSource(value = DfaApplicationBuilder.TargetFileType.class, names = {"CMD", "DOCKERFILE", "JAVA", "SH", "YAML", "YML", "XML"})
    void is_commented_target_file_type_identifies_supported_file_types(DfaApplicationBuilder.TargetFileType fileType) {

        assertTrue(TargetFileCommentBuilder.isCommentedTargetFileType(fileType));
    }

    @ParameterizedTest
    @EnumSource(value = DfaApplicationBuilder.TargetFileType.class, names = {"CONF", "DOCKERIGNORE", "ENV", "JSON", "MD", "PROPERTIES"})
    void is_commented_target_file_type_identifies_unsupported_file_types(DfaApplicationBuilder.TargetFileType fileType) {

        assertFalse(TargetFileCommentBuilder.isCommentedTargetFileType(fileType));
    }

    @Test
    void insert_generator_comment_on_top_of_source_file_using_java_as_representative_filetype() {

        var testSource = """
                // Java source line 1.
                // Java source line 2.
                // Java source line 3.
                """;
        var testTargetFileComment = TargetFileCommentBuilder.targetFileTypeComments.get(DfaApplicationBuilder.TargetFileType.JAVA);
        var testComment = getGeneratorComment(ContainerAutomatFactoryApp.class.getCanonicalName(), Instant.now(), testTargetFileComment, TEST_GENERATION_ID);

        var testResult = TargetFileCommentBuilder.insertGeneratorComment(testSource, DfaApplicationBuilder.TargetFileType.JAVA, TEST_GENERATION_ID);
        var testResultLines = testResult.lines().toList();

        var expectedResult = testComment + System.lineSeparator() + testSource;
        var expectedLines = expectedResult.lines().toList();

        assertEquals(expectedLines.size(), testResultLines.size());
        for(int i = 0; i < expectedLines.size(); i++) {
            var expectedLine = expectedLines.get(i);
            var testResultLine = testResultLines.get(i);

            if(expectedLine.startsWith(" * Time of generation:")) {
                assertTrue(testResultLine.startsWith(" * Time of generation:"));
            } else {
                assertEquals(expectedLine, testResultLine);
            }
        }
    }

    @Test
    void insert_generator_comment_within_source_file_using_cmd_as_representative_filetype() {

        var testSourceTop = """
                rem CMD line 1.
                """;
        var testSourceBottom = """
                rem CMD line 2.
                rem CMD line 3.
                """;
        var testSource = testSourceTop + testSourceBottom;
        var testTargetFileComment = TargetFileCommentBuilder.targetFileTypeComments.get(DfaApplicationBuilder.TargetFileType.CMD);
        var testComment = getGeneratorComment(ContainerAutomatFactoryApp.class.getCanonicalName(), Instant.now(), testTargetFileComment, TEST_GENERATION_ID);

        var testResult = TargetFileCommentBuilder.insertGeneratorComment(testSource, DfaApplicationBuilder.TargetFileType.CMD, TEST_GENERATION_ID);
        var testResultLines = testResult.lines().toList();

        var expectedResult = testSourceTop + testComment + System.lineSeparator() + System.lineSeparator() + testSourceBottom;
        var expectedLines = expectedResult.lines().toList();

        assertEquals(expectedLines.size(), testResultLines.size());
        for(int i = 0; i < expectedLines.size(); i++) {
            var expectedLine = expectedLines.get(i);
            var testResultLine = testResultLines.get(i);

            if(expectedLine.startsWith("rem Time of generation:")) {
                assertTrue(testResultLine.startsWith("rem Time of generation:"));
            } else {
                assertEquals(expectedLine, testResultLine);
            }
        }
    }

    @Test
    void insert_generator_comment_within_empty_source_file_using_cmd_as_representative_filetype() {

        var testSource = "";
        var testTargetFileComment = TargetFileCommentBuilder.targetFileTypeComments.get(DfaApplicationBuilder.TargetFileType.CMD);
        var testComment = getGeneratorComment(ContainerAutomatFactoryApp.class.getCanonicalName(), Instant.now(), testTargetFileComment, TEST_GENERATION_ID);

        var testResult = TargetFileCommentBuilder.insertGeneratorComment(testSource, DfaApplicationBuilder.TargetFileType.CMD, TEST_GENERATION_ID);
        var testResultLines = testResult.lines().toList();

        var expectedResult = System.lineSeparator() + testComment;
        var expectedLines = expectedResult.lines().toList();

        assertEquals(expectedLines.size(), testResultLines.size());
        for(int i = 0; i < expectedLines.size(); i++) {
            var expectedLine = expectedLines.get(i);
            var testResultLine = testResultLines.get(i);

            if(expectedLine.startsWith("rem Time of generation:")) {
                assertTrue(testResultLine.startsWith("rem Time of generation:"));
            } else {
                assertEquals(expectedLine, testResultLine);
            }
        }
    }

    @Test
    void insert_generator_comment_using_not_suported_filetype() {

        var testSource = """
                # Markdown line 1
                **Markdown line 2**
                ## Markdown line 3
                """;

        var testResult = TargetFileCommentBuilder.insertGeneratorComment(testSource, DfaApplicationBuilder.TargetFileType.MD, TEST_GENERATION_ID);

        assertEquals(testSource, testResult);
    }

    static String getGeneratorComment(String appName, Instant generationTime, TargetFileCommentBuilder.TargetFileCommentAttributes fileComment, String generationId) {
        return TargetFileCommentBuilder.GENERATOR_COMMENT_TEMPLATE.formatted(
                appName,
                generationTime,
                fileComment.commentStart(),
                fileComment.commentLinePrefix(),
                fileComment.commentEnd(),
                generationId).trim();
    }

}
