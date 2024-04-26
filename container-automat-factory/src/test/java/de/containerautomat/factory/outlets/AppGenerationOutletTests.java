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

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * An abstract base class for test suites to test the methods defined in
 * the abstract base class {@link AppGenerationOutlet} for normalizing
 * file paths.
 */
abstract class AppGenerationOutletTests {

    protected static final char CHAR_SLASH = '/';
    protected static final char CHAR_BACKSLASH = '\\';
    protected static final String STRING_SLASH = "/";
    protected static final String STRING_BACKSLASH = "\\";
    protected static final String TEST_PATH_WITH_SLASH = "test/path/with/slash";
    protected static final String TEST_PATH_WITH_BACKSLASH = "test\\path\\with\\backslash";
    protected static final String TEST_PATH_WITH_MIXED_SEPARATEOR = "test/path/with\\mixed\\separator";


    protected abstract AppGenerationOutlet getOutletToTest();


    @Test
    void empty_filename_not_allowed() {

        var outlet = getOutletToTest();
        assertThrows(IllegalArgumentException.class, () -> outlet.createNormalizedTargetFilePath(null));
        assertThrows(IllegalArgumentException.class, () -> outlet.createNormalizedTargetFilePath(""));
    }

    @Test
    void empty_filepath_not_allowed() {

        var outlet = getOutletToTest();
        assertThrows(IllegalArgumentException.class, () -> outlet.createNormalizedTargetFilePath(STRING_SLASH));
        assertThrows(IllegalArgumentException.class, () -> outlet.createNormalizedTargetFilePath(STRING_BACKSLASH));
    }

    @Test
    void normalize_path_with_slash_separator() {

        var outlet = getOutletToTest();
        var resultNormalizedFilePath = outlet.normalizeFileSeparator(TEST_PATH_WITH_SLASH);
        if (outlet.getFileSeparatorChar() == CHAR_SLASH) {
            assertEquals(TEST_PATH_WITH_SLASH, resultNormalizedFilePath);
            assertEquals(-1, resultNormalizedFilePath.indexOf(CHAR_BACKSLASH));
        } else if (outlet.getFileSeparatorChar() == CHAR_BACKSLASH) {
            assertEquals(TEST_PATH_WITH_SLASH.replace(CHAR_SLASH, CHAR_BACKSLASH), resultNormalizedFilePath);
        } else {
            fail("Unknown file separator char: " + outlet.getFileSeparatorChar());
        }
    }

    @Test
    void normalize_path_with_backslash_separator() {

        var outlet = getOutletToTest();
        var resultNormalizedFilePath = outlet.normalizeFileSeparator(TEST_PATH_WITH_BACKSLASH);
        if (outlet.getFileSeparatorChar() == CHAR_SLASH) {
            assertEquals(TEST_PATH_WITH_BACKSLASH.replace(CHAR_BACKSLASH, CHAR_SLASH), resultNormalizedFilePath);
        } else if (outlet.getFileSeparatorChar() == CHAR_BACKSLASH) {
            assertEquals(TEST_PATH_WITH_BACKSLASH, resultNormalizedFilePath);
            assertEquals(-1, resultNormalizedFilePath.indexOf(CHAR_SLASH));
        } else {
            fail("Unknown file separator char: " + outlet.getFileSeparatorChar());
        }
    }

    @Test
    void normalize_path_with_mixed_separator() {

        var outlet = getOutletToTest();
        var expectedPath = TEST_PATH_WITH_MIXED_SEPARATEOR.replace(CHAR_BACKSLASH, CHAR_SLASH).replace(CHAR_SLASH, outlet.getFileSeparatorChar());
        var resultNormalizedFilePath = outlet.normalizeFileSeparator(TEST_PATH_WITH_MIXED_SEPARATEOR);

        assertEquals(expectedPath, resultNormalizedFilePath);
        if (outlet.getFileSeparatorChar() == CHAR_SLASH) {
            assertEquals(-1, resultNormalizedFilePath.indexOf(CHAR_BACKSLASH));
        } else if (outlet.getFileSeparatorChar() == CHAR_BACKSLASH) {
            assertEquals(-1, resultNormalizedFilePath.indexOf(CHAR_SLASH));
        } else {
            fail("Unknown file separator char: " + outlet.getFileSeparatorChar());
        }
    }

    @Test
    void normalize_empty_path() {

        var outlet = getOutletToTest();
        assertEquals("", outlet.normalizeFileSeparator(null));
        assertEquals("", outlet.normalizeFileSeparator(""));
    }

}