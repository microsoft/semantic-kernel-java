// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.presidio.models;

public record AnonymizerItem(
    String operator,
    String entity_type,
    String text,
    Integer start,
    Integer end
) {

}