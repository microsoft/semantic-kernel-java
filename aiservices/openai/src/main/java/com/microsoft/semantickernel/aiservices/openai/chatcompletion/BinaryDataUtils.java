// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.aiservices.openai.chatcompletion;

import com.azure.core.util.BinaryData;
import javax.annotation.Nullable;

public class BinaryDataUtils {

    @Nullable
    public static String toString(@Nullable BinaryData b) {
        if (b == null) {
            return null;
        }
        return b.toString();
    }
}
