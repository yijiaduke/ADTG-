package edu.duke.adtg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import edu.duke.adtg.ScriptExecutor;

public class ScriptExecutorTest {
    @Test
    void testExecuteFile() {

    }

    @Test
    void testWriteFile() {
        // Create a temporary file for testing
        File tempFile;
        try {
            tempFile = File.createTempFile("test", ".txt");
        } catch (IOException e) {
            fail("Failed to create temporary file");
            return;
        }

        // Define the content to write
        String content = "This is a test content";

        // Call the writeFile method
        ScriptExecutor.writeFile(tempFile, content);

        // Read the content from the file
        String fileContent;
        try (BufferedReader reader = new BufferedReader(new FileReader(tempFile))) {
            fileContent = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException e) {
            fail("Failed to read file content");
            return;
        }

        // Assert that the content matches
        assertEquals(content, fileContent);
    }
}