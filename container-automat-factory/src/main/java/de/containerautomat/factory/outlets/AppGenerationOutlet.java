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
package de.containerautomat.factory.outlets;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * An abstract base class for representing a system or mechanism for
 * outputting the files of a generated container automat application.
 * <p/>
 * Examples of such mechanisms are the local file system or a ZIP archive.
 * <p/>
 * The abstract base class provides methods for normalizing file paths in
 * the context of application generation. A normalized file path is
 * characterized by the following properties:
 * <ul>
 *     <li>
 *         The file separators '/' and '\\' possible in source paths are
 *         unified according to the separator determined by the concrete
 *         implementation of the abstract class.
 *     </li>
 *     <li>A leading separator, if present, is removed.</li>
 *     <li>An empty target path is not allowed.</li>
 * </ul>
 */
@Getter
@RequiredArgsConstructor
public abstract class AppGenerationOutlet {

    private final char fileSeparatorChar;


    public String normalizeFileSeparator(String filePath) {

        if (!StringUtils.hasLength(filePath)) {
            return "";
        }
        return filePath.replace('/', fileSeparatorChar).replace('\\', fileSeparatorChar);

    }

    protected String createNormalizedTargetFilePath(String sourcePath) {

        var normalizedFilePath = normalizeFileSeparator(sourcePath);
        if (!normalizedFilePath.isEmpty() && normalizedFilePath.charAt(0) == fileSeparatorChar) {
            normalizedFilePath = normalizedFilePath.substring(1);
        }
        if (normalizedFilePath.isEmpty()) {
            throw new IllegalArgumentException("File path must not be empty");
        }

        return normalizedFilePath;
    }

    public abstract void writeTargetFile(String data, String filePath) throws IOException;

}

