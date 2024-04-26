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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A test suite for testing methods defined in the {@link FilesystemOutlet}
 * class to write files of a Container-Automat application to the local
 * file system.
 */
class FilesystemOutletTests extends AppGenerationOutletTests {

    private static final String TEST_TARGET_DIRECTORY_NOT_ENDING_WITH_SEPARATOR = "test/target/directory";
    private static final String TEST_TARGET_DIRECTORY_ENDING_WITH_SEPARATOR = TEST_TARGET_DIRECTORY_NOT_ENDING_WITH_SEPARATOR + "/";
    private static final String TEST_FILENAME = "TestFile.txt";
    private static final String TEST_FOLDERS = "TestFolder" + File.separator + "TestSubfolder";


    @Test
    void normalize_filesystem_outlet_targetdirectory() {

        var outlet1 = new FilesystemOutlet(TEST_TARGET_DIRECTORY_ENDING_WITH_SEPARATOR);
        var outlet2 = new FilesystemOutlet(TEST_TARGET_DIRECTORY_NOT_ENDING_WITH_SEPARATOR);

        var expectedTargetDirectory1a = outlet1.normalizeFileSeparator(TEST_TARGET_DIRECTORY_ENDING_WITH_SEPARATOR);
        var expectedTargetDirectory1b = outlet1.normalizeFileSeparator(TEST_TARGET_DIRECTORY_NOT_ENDING_WITH_SEPARATOR) + outlet2.getFileSeparatorChar();
        var expectedTargetDirectory2a = outlet2.normalizeFileSeparator(TEST_TARGET_DIRECTORY_NOT_ENDING_WITH_SEPARATOR) + outlet2.getFileSeparatorChar();
        var expectedTargetDirectory2b = outlet2.normalizeFileSeparator(TEST_TARGET_DIRECTORY_ENDING_WITH_SEPARATOR);

        assertEquals(expectedTargetDirectory1a, outlet1.getTargetDirectory());
        assertEquals(expectedTargetDirectory2a, outlet2.getTargetDirectory());
        assertEquals(expectedTargetDirectory1a, expectedTargetDirectory2a);
        assertEquals(expectedTargetDirectory1b, expectedTargetDirectory1a);
        assertEquals(expectedTargetDirectory2b, expectedTargetDirectory2a);
    }

    @Test
    void write_file_to_target_directory_root(@TempDir Path targetPath) {

        write_file_to_given_path(targetPath, TEST_FILENAME);
    }

    @Test
    void write_file_to_folder_in_target_directory(@TempDir Path targetPath) {

        write_file_to_given_path(targetPath, TEST_FOLDERS + File.separator + TEST_FILENAME);
    }

    @Override
    protected AppGenerationOutlet getOutletToTest() {

        return new FilesystemOutlet("");
    }

    private void write_file_to_given_path(Path outletTargetPath, String testFilePathInOutlet) {

        var testData = """
                Line 1: Test start
                Line 2: %s
                Line 3: Test end
                """.formatted(UUID.randomUUID().toString());

        String testTargetDirectory;
        try {
            testTargetDirectory = outletTargetPath.toFile().getCanonicalPath();
        } catch (IOException e) {
            fail("Unexpected exception: " + e.getMessage(), e);
            return;
        }

        var outlet = new FilesystemOutlet(testTargetDirectory);
        assertDoesNotThrow(() -> outlet.writeTargetFile(testData, testFilePathInOutlet));

        var testTargetFilePath = testTargetDirectory + File.separator + testFilePathInOutlet;
        var resultData = assertDoesNotThrow(() -> readTestFile(testTargetFilePath));
        assertEquals(testData, resultData);
    }

    private String readTestFile(String filePath) throws IOException {

        try (InputStream inputStream = new FileInputStream(filePath)) {
            byte[] bytes = FileCopyUtils.copyToByteArray(inputStream);
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }

}
