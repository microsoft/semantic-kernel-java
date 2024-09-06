// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.vectorsearch.filtering;

public abstract class EqualityFilterClause implements FilterClause {

    private final String fieldName;
    private final Object value;

    public EqualityFilterClause(String fieldName, Object value) {
        this.fieldName = fieldName;
        this.value = value;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getValue() {
        return value;
    }
}
