// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.vectorsearch.options;

public class EqualityFilterClause extends FilterClause {

    private final String fieldName;
    private final Object value;

    public EqualityFilterClause(String fieldName, Object value) {
        super(FilterClauseType.EQUALITY);
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
