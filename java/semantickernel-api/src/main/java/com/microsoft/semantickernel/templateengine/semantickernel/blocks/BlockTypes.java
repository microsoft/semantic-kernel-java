// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.templateengine.semantickernel.blocks;

public enum BlockTypes {
    /**
     * Undefined block type
     */
    Undefined(0),
    /**
     * Text block type
     */
    Text(1),
    /**
     * Code block type
     */
    Code(2),
    /**
     * Variable block type
     */
    Variable(3),
    /**
     * Value block type
     */
    Value(4),
    /**
     * Function block type
     */
    FunctionId(5),
    NamedArg(6);

    BlockTypes(int i) {
    }
}
