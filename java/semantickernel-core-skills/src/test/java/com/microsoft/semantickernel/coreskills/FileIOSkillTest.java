// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.coreskills;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.microsoft.semantickernel.orchestration.ContextVariables;
import com.microsoft.semantickernel.orchestration.SKContext;

import reactor.test.StepVerifier;

public class FileIOSkillTest {

    @Test
    public void testReadFileAsync() throws IOException {
        // Create a temporary file with some content
        String content = "Hello, World!";
        Path tempFile = Files.createTempFile("test", ".txt");
        Files.write(tempFile, content.getBytes());

        // Create an instance of FileIOSkill
        FileIOSkill fileIOSkill = new FileIOSkill();

        // Call the readFileAsync method with the temporary file path
        String filePath = tempFile.toAbsolutePath().toString();
        String outcome = fileIOSkill.readFileAsync(filePath, "UTF-8");

        // Assert content (expected) is the same as the outcome (actual)
        Assertions.assertEquals(content, outcome);

        // Delete the temporary file
        Files.delete(tempFile);
    }

    @Test
    public void testWriteFileAsync() throws IOException {
        // Create a temporary file
        Path tempFile = Files.createTempFile("test", ".txt");

        // Create an instance of FileIOSkill
        FileIOSkill fileIOSkill = new FileIOSkill();

        // Define the content to write
        String content = "Hello, World!";

        // Call the writeFileAsync method with the temporary file path and content
        fileIOSkill.writeFileAsync(tempFile.toAbsolutePath().toString(), content, "UTF-8");

        // Read the written file to verify the content
        String fileContents = new String(Files.readAllBytes(tempFile));
        Assertions.assertEquals(content, fileContents);

        // Delete the temporary file
        Files.delete(tempFile);
    }
}
