// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.presidio.models;

public record Anonymizer(
    Integer charsToMask,
    boolean fromEnd,
    String maskingChar,
    String type
) {

}
