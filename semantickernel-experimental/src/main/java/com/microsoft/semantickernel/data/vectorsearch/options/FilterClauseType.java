// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.vectorsearch.options;

public enum FilterClauseType {
    /**
     * An equality filter clause.
     */
    EQUALITY,
    /**
     * A filter clause that checks if the tag list contains a value.
     */
    TAG_LIST_CONTAINS
}
