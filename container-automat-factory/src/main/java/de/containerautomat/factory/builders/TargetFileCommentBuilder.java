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

import de.containerautomat.factory.ContainerAutomatFactoryApp;

import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;

import static de.containerautomat.factory.builders.DfaApplicationBuilder.TargetFileType.*;

/**
 * A class that provides static methods to insert a comment into
 * the generated source files in the case of the following file
 * types, a comment suitable for the file type to indicate the
 * origin of the file:
 * <ul>
 *     <li>Batch files (CMD)</li>
 *     <li>Docker files (Dockerfile)</li>
 *     <li>Java files (JAVA)</li>
 *     <li>Shell files (SH)</li>
 *     <li>YAML files (YML)</li>
 *     <li>XML files (XML)</li>
 * </ul>
 */
class TargetFileCommentBuilder {

    record TargetFileComment(DfaApplicationBuilder.TargetFileType fileType, String commentStart, String commentLinePrefix, String commentEnd, int topLinesToSkip) {
    }


    static final String GENERATOR_COMMENT = """
            %3$s
            %4$s Generated by Container-Automat Factory
            %4$s %1$s
            %4$s Time of generation: %2$s
            %4$s
            %4$s Notes:
            %4$s
            %4$s - Container-Automat is a generator of service-oriented applications for
            %4$s   demonstration purposes that are based on Deterministic Finite Automata
            %4$s   (abbreviated DFA).
            %4$s - Further explanations can be found in the README.md file in the root folder
            %4$s   of the project this file belongs to and at www.container-automat.de.
            %4$s - The Container-Automat Factory as such is open source software licensed
            %4$s   under the Apache License, Version 2.0. You may obtain a copy of the license at:
            %4$s   https://www.apache.org/licenses/LICENSE-2.0
            %4$s - Please note that generated applications use external services like a
            %4$s   message broker, a database, and optional possibly others. Depending on the
            %4$s   environment in which the application is running and the organization using
            %4$s   it, different licensing requirements may apply to these services.
            %5$s
            """;

    static final Map<DfaApplicationBuilder.TargetFileType, TargetFileComment> targetFileTypeComments = new EnumMap<>(DfaApplicationBuilder.TargetFileType.class);

    static {
        for (DfaApplicationBuilder.TargetFileType fileType : DfaApplicationBuilder.TargetFileType.values()) {
            switch (fileType) {
                case CMD:
                    targetFileTypeComments.put(CMD, new TargetFileComment(fileType, "", "rem", "", 1));
                    break;
                case DOCKERFILE:
                    targetFileTypeComments.put(DOCKERFILE, new TargetFileComment(fileType, "", "#", "", 0));
                    break;
                case JAVA:
                    targetFileTypeComments.put(JAVA, new TargetFileComment(fileType, "/*", " *", " */", 0));
                    break;
                case SH:
                    targetFileTypeComments.put(SH, new TargetFileComment(fileType, "", "#", "", 1));
                    break;
                case YML:
                    targetFileTypeComments.put(YML, new TargetFileComment(fileType, "", "#", "", 0));
                    break;
                case XML:
                    targetFileTypeComments.put(XML, new TargetFileComment(fileType, "<!--", "   ", "-->", 3));
                    break;
                default:
                    break;
            }
        }
    }


    static boolean isCommentedTargetFileType(DfaApplicationBuilder.TargetFileType fileType) {
        return targetFileTypeComments.containsKey(fileType);
    }

    static String insertGeneratorComment(String sourceText, DfaApplicationBuilder.TargetFileType targetFileType) {

        if (!isCommentedTargetFileType(targetFileType)) {
            return sourceText;
        }
        TargetFileComment fileComment;
        fileComment = targetFileTypeComments.get(targetFileType);
        var comment = GENERATOR_COMMENT.formatted(
                ContainerAutomatFactoryApp.class.getCanonicalName(),
                Instant.now(),
                fileComment.commentStart(),
                fileComment.commentLinePrefix,
                fileComment.commentEnd).trim();

        if (fileComment.topLinesToSkip() == 0) {
            return comment + System.lineSeparator() + sourceText;
        }

        var result = new StringBuilder();
        var lineIndex = 0;
        for (var charIndex = 0; charIndex < sourceText.length(); charIndex++) {
            var currentChar = sourceText.charAt(charIndex);
            if (currentChar == '\n') {
                lineIndex++;
            }
            if (lineIndex == fileComment.topLinesToSkip()) {
                result.append(currentChar);
                result.append(comment);
                result.append(System.lineSeparator());
                result.append(sourceText.substring(charIndex));
                return result.toString();
            }
            result.append(currentChar);
        }
        return result.toString();
    }

}
