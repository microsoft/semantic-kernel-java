// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.presidio.models;

import java.util.List;

public record AnonymizerResult(
    String text,
    List<AnonymizerItem> items) {

}
