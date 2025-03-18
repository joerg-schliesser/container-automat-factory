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

import org.junit.jupiter.api.Test;
import org.springframework.util.FileCopyUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A test suite for testing methods defined in the {@link ZiparchiveOutlet}
 * class to write files of a Container-Automat application to a ZIP archive.
 */
class ZiparchiveOutletTests extends AppGenerationOutletTests {

    private static final String TEST_FILENAME = "TestFile.txt";
    private static final String TEST_FOLDERS = "TestFolder/TestSubfolder";


    @Test
    void write_file_to_zip_archive_root() {

        write_file_to_given_path(TEST_FILENAME);
    }

    @Test
    void write_file_to_folder_in_zip_archive() {


        write_file_to_given_path(TEST_FOLDERS + "/" + TEST_FILENAME);
    }

    @Override
    protected AppGenerationOutlet getOutletToTest() {

        return new ZiparchiveOutlet();
    }

    private void write_file_to_given_path(String testFilePathInZipArchive) {

        var testData = """
                Line 1: Test start
                Line 2: %s
                Line 3: Test end
                """.formatted(UUID.randomUUID().toString());

        var outlet = new ZiparchiveOutlet();
        assertDoesNotThrow(() -> outlet.writeTargetFile(testData, testFilePathInZipArchive));

        var resultData = assertDoesNotThrow(() -> readTestFile(testFilePathInZipArchive, outlet));
        assertEquals(testData, resultData);
    }

    private String readTestFile(String filePath, ZiparchiveOutlet ziparchiveOutlet) throws IOException {

        var normalizedFilePath = ziparchiveOutlet.createNormalizedTargetFilePath(filePath);
        var zipData = ziparchiveOutlet.getZipArchive();
        try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(zipData), StandardCharsets.UTF_8)) {
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                if (zipEntry.getName().equals(normalizedFilePath)) {
                    byte[] bytes = FileCopyUtils.copyToByteArray(zipInputStream);
                    return new String(bytes, StandardCharsets.UTF_8);
                }
            }
        }
        throw new IOException("Missing entry in zip archive: " + filePath);
    }

}