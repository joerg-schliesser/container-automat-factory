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
package de.containerautomat.factory.outlets;

import lombok.Getter;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A concrete implementation of the abstract data type {@link
 * AppGenerationOutlet} for writing the files of the generated
 * application to the local file system.
 */
@Getter
public class FilesystemOutlet extends AppGenerationOutlet {

    private final String targetDirectory;


    public FilesystemOutlet(String targetDirectory) {

        super(File.separatorChar);
        targetDirectory = normalizeFileSeparator(targetDirectory);
        if (!targetDirectory.isEmpty() && targetDirectory.charAt(targetDirectory.length() - 1) != File.separatorChar) {
            this.targetDirectory = targetDirectory + File.separatorChar;
        } else {
            this.targetDirectory = targetDirectory;
        }
    }

    public void writeTargetFile(String data, String filePath) throws IOException {

        var normalizedFilePath = createNormalizedTargetFilePath(filePath);
        var targetPath = targetDirectory + normalizedFilePath;
        var targetFolder = Path.of(targetPath).getParent();
        Files.createDirectories(targetFolder);
        try (FileWriter fileWriter = new FileWriter(targetPath, StandardCharsets.UTF_8)) {
            FileCopyUtils.copy(data, fileWriter);
        }
    }

}
