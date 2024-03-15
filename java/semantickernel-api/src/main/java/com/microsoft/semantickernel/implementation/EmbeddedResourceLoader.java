// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.implementation;

import com.microsoft.semantickernel.exceptions.SKException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for loading resources from the classpath or filesystem.
 */
public class EmbeddedResourceLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddedResourceLoader.class);

    /**
     * Loads a file to a string from the classpath using getResourceAsStream
     *
     * @param fileName Filename to read
     * @param clazz    Class to use for classpath loading
     * @return File content
     * @throws FileNotFoundException Error in case the file doesn't exist
     */
    public static String readFile(String fileName, Class<?> clazz) throws FileNotFoundException {
        return readFile(fileName, clazz, ResourceLocation.CLASSPATH);
    }

    /**
     * Loads a file to a string from the classpath, classpath root or filesystem
     *
     * @param fileName  Filename to read
     * @param clazz     Class to use for classpath loading
     * @param locations Locations to search for the file
     * @return File content
     * @throws FileNotFoundException Error in case the file doesn't exist
     */
    public static String readFile(String fileName, @Nullable Class<?> clazz,
        ResourceLocation... locations)
        throws FileNotFoundException {

        List<ResourceLocation> locationsList = Arrays.stream(locations)
            .collect(Collectors.toList());

        Optional<String> fileContents = locationsList.stream()
            .map(
                type -> {
                    switch (type) {
                        case CLASSPATH:
                            if (clazz == null) {
                                throw new SKException(
                                    "Resource location CLASSPATH requires a class");
                            }
                            return getResourceAsStream(fileName, clazz);
                        case CLASSPATH_ROOT:
                            try (InputStream inputStream = Thread.currentThread()
                                .getContextClassLoader()
                                .getResourceAsStream(fileName)) {
                                return readInputStream(fileName, inputStream);
                            } catch (IOException e) {
                                // IGNORE
                            }
                            break;
                        case FILESYSTEM:
                            return readFileFromFileSystem(fileName);
                        default:
                    }
                    return null;
                })
            .filter(Objects::nonNull)
            .findFirst();

        if (fileContents.isPresent()) {
            return fileContents.get();
        }

        throw new FileNotFoundException("Could not find file " + fileName);
    }

    @Nullable
    // visible for testing
    static String getResourceAsStream(String fileName, Class<?> clazz) {
        try (InputStream inputStream = clazz.getResourceAsStream(fileName)) {
            return readInputStream(fileName, inputStream);
        } catch (Exception e) {
            // IGNORE
        }
        return null;
    }

    @Nullable
    // visible for testing
    static String readFileFromFileSystem(String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            try (InputStream inputStream = Files.newInputStream(file.toPath())) {
                return readInputStream(fileName, inputStream);
            } catch (IOException e) {
                // IGNORE
            }
        }
        return null;
    }

    @Nullable
    // visible for testing
    private static String readInputStream(String fileName, InputStream inputStream)
        throws FileNotFoundException {
        if (inputStream == null) {
            throw new FileNotFoundException("File not found: " + fileName);
        }

        try (BufferedReader bf = new BufferedReader(
            new InputStreamReader(inputStream, java.nio.charset.StandardCharsets.UTF_8))) {
            return bf.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            // IGNORE
            LOGGER.trace("Failed to load file: " + fileName, e);
        }
        return null;
    }

    /**
     * Enum for specifying the location of the resource
     */
    public enum ResourceLocation {
        /**
         * Load from the classpath
         */
        CLASSPATH,
        /**
         * Load from the filesystem
         */
        FILESYSTEM,
        /**
         * Load from the classpath root
         */
        CLASSPATH_ROOT
    }
}
