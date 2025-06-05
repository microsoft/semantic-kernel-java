package com.microsoft.semantickernel.data.jdbc.oracle;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.semantickernel.data.jdbc.JDBCVectorStoreQueryProvider;
import com.microsoft.semantickernel.data.jdbc.postgres.PostgreSQLVectorStoreQueryProvider;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordDefinition;
import com.microsoft.semantickernel.data.vectorstorage.options.UpsertRecordOptions;
import com.microsoft.semantickernel.exceptions.SKException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import javax.annotation.Nonnull;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class OracleVectorStoreQueryProvider extends JDBCVectorStoreQueryProvider {

    // This could be removed if super.collectionTable made protected
    private final String collectionsTable;

    // This could be common to all query providers
    private final ObjectMapper objectMapper;

    private OracleVectorStoreQueryProvider(@Nonnull DataSource dataSource, @Nonnull String collectionsTable, @Nonnull String prefixForCollectionTables,
        ObjectMapper objectMapper) {
        super(dataSource, collectionsTable, prefixForCollectionTables);
        this.collectionsTable = collectionsTable;
        this.objectMapper = objectMapper;
    }

    @Override
    public void prepareVectorStore() {
        String createCollectionsTable = formatQuery(
            "CREATE TABLE IF NOT EXISTS %s (collectionId VARCHAR(255) PRIMARY KEY)",
            validateSQLidentifier(collectionsTable));

        try (Connection connection = dataSource.getConnection();
            PreparedStatement createTable = connection.prepareStatement(createCollectionsTable)) {
            createTable.execute();
        } catch (SQLException e) {
            throw new SKException("Failed to prepare vector store", e);
        }
    }

    @Override
    public void createCollection(String collectionName,
        VectorStoreRecordDefinition recordDefinition) {
        // TODO Override implementation. Eg: mapping TEXT to VARCHAR
        super.createCollection(collectionName, recordDefinition);
    }

    @Override
    public void upsertRecords(String collectionName, List<?> records, VectorStoreRecordDefinition recordDefinition, UpsertRecordOptions options) {

        // TODO look for public void createCollection(String collectionName, VectorStoreRecordDefinition recordDefinition) {

        // TODO Make this a MERGE query

//        String upsertStatemente = formatQuery("""
//            MERGE INTO %s EXIST_REC USING (SELECT ? AS ID) NEW_REC ON (EXIST_REC.%s = NEW_REC.ID)
//            WHEN MATACHED THEN UPDATE SET EXISTING REC
//            """,
//                getCollectionTableName(collectionName),
//                recordDefinition.getKeyField().getName(),
//                getQueryColumnsFromFields(fields),
//                getWildcardString(fields.size()),
//                onDuplicateKeyUpdate);super.upsertRecords(collectionName, records, recordDefinition, options);

        String query = formatQuery("INSERT INTO %s (%s, %s, %s) values (?, ?, ?)",
            getCollectionTableName(collectionName),
            recordDefinition.getAllFields().get(0).getStorageName(),
            recordDefinition.getAllFields().get(1).getStorageName(),
            recordDefinition.getAllFields().get(2).getStorageName());

        try (Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(query)) {
            for (Object record : records) {
                JsonNode jsonNode = objectMapper.valueToTree(record);
                for (int i = 0; i < 3; i++) {
                    statement.setObject(i + 1, jsonNode
                        .get(recordDefinition.getAllFields().get(i).getStorageName()).asText());
                }
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            throw new SKException("Failed to upsert records", e);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder
        extends JDBCVectorStoreQueryProvider.Builder {

        private DataSource dataSource;
        private String collectionsTable = DEFAULT_COLLECTIONS_TABLE;
        private String prefixForCollectionTables = DEFAULT_PREFIX_FOR_COLLECTION_TABLES;
        private ObjectMapper objectMapper = new ObjectMapper();

        @SuppressFBWarnings("EI_EXPOSE_REP2")
        public Builder withDataSource(DataSource dataSource) {
            this.dataSource = dataSource;
            return this;
        }

        /**
         * Sets the collections table name.
         * @param collectionsTable the collections table name
         * @return the builder
         */
        public Builder withCollectionsTable(String collectionsTable) {
            this.collectionsTable = validateSQLidentifier(collectionsTable);
            return this;
        }

        /**
         * Sets the prefix for collection tables.
         * @param prefixForCollectionTables the prefix for collection tables
         * @return the builder
         */
        public Builder withPrefixForCollectionTables(String prefixForCollectionTables) {
            this.prefixForCollectionTables = validateSQLidentifier(prefixForCollectionTables);
            return this;
        }

        public Builder withObjectMapper(
            ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
            return this;
        }

        @Override
        public OracleVectorStoreQueryProvider build() {
            return new OracleVectorStoreQueryProvider(dataSource, collectionsTable,
                prefixForCollectionTables, objectMapper);
        }
    }
}