// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.memory.jdbc;

import com.microsoft.semantickernel.memory.recorddefinition.VectorStoreRecordDefinition;
import com.microsoft.semantickernel.memory.recorddefinition.VectorStoreRecordField;

import java.util.List;
import java.util.function.BiFunction;

public class MySQLVectorStoreDefaultQueryProvider<Record> extends
    JDBCVectorStoreDefaultQueryProvider<Record> implements JDBCVectorStoreQueryProvider<Record> {
    public MySQLVectorStoreDefaultQueryProvider(String storageTableName,
        VectorStoreRecordDefinition recordDefinition,
        Class<Record> recordClass,
        BiFunction<String, String, String> sanitizeKeyFunction) {
        super(storageTableName, recordDefinition, recordClass, sanitizeKeyFunction);
    }

    /**
     * Creates a new builder.
     * @param <Record> the record type
     * @return the builder
     */
    public static <Record> Builder<Record> builder() {
        return new Builder<>();
    }

    @Override
    public String formatUpsertQuery() {
        List<VectorStoreRecordField> fields = this.recordDefinition.getAllFields();

        StringBuilder onDuplicateKeyUpdate = new StringBuilder();
        for (int i = 0; i < fields.size(); ++i) {
            onDuplicateKeyUpdate.append(fields.get(i).getName())
                .append(" = VALUES(")
                .append(fields.get(i).getName())
                .append(")");

            if (i < fields.size() - 1) {
                onDuplicateKeyUpdate.append(", ");
            }
        }

        return "INSERT INTO " + storageTableName
            + " (" + getQueryColumnsFromFields(fields) + ")"
            + " VALUES (" + getWildcardString(fields.size()) + ")"
            + " ON DUPLICATE KEY UPDATE " + onDuplicateKeyUpdate;
    }

    public static class Builder<Record>
        extends JDBCVectorStoreDefaultQueryProvider.Builder<Record> {
        public MySQLVectorStoreDefaultQueryProvider<Record> build() {
            return new MySQLVectorStoreDefaultQueryProvider<>(storageTableName, recordDefinition,
                recordClass, sanitizeKeyFunction);
        }
    }
}
