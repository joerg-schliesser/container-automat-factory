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

import lombok.SneakyThrows;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * A concrete implementation of the abstract data type {@link
 * AppGenerationOutlet} for writing the files of the generated
 * application to a ZIP archive.
 */
public class ZiparchiveOutlet extends AppGenerationOutlet {

    private final ZipOutputStream zipOutputStream;

    private final ByteArrayOutputStream zipByteArrayOutputStream;


    public ZiparchiveOutlet() {

        super('/');
        zipByteArrayOutputStream = new ByteArrayOutputStream();
        zipOutputStream = new ZipOutputStream(zipByteArrayOutputStream);
    }

    @SneakyThrows
    public byte[] getZipArchive() {

        zipOutputStream.close();
        return zipByteArrayOutputStream.toByteArray();
    }

    public void writeTargetFile(String data, String filePath) throws IOException {

        var targetFilePath = createNormalizedTargetFilePath(filePath);
        zipOutputStream.putNextEntry(new ZipEntry(targetFilePath));
        zipOutputStream.write(data.getBytes(StandardCharsets.UTF_8));
        zipOutputStream.closeEntry();
    }

}