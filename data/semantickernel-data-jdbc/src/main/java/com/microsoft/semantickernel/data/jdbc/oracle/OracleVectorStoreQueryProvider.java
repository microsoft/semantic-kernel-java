package com.microsoft.semantickernel.data.jdbc.oracle;

import com.microsoft.semantickernel.data.jdbc.JDBCVectorStoreQueryProvider;
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

    private OracleVectorStoreQueryProvider(@Nonnull DataSource dataSource, @Nonnull String collectionsTable, @Nonnull String prefixForCollectionTables) {
        super(dataSource, collectionsTable, prefixForCollectionTables);
        this.collectionsTable = collectionsTable;
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
    public void upsertRecords(String collectionName, List<?> records, VectorStoreRecordDefinition recordDefinition, UpsertRecordOptions options) {

        // Using hsqldb impl


        super.upsertRecords(collectionName, records, recordDefinition, options);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder
        extends JDBCVectorStoreQueryProvider.Builder {

        private DataSource dataSource;
        private String collectionsTable = DEFAULT_COLLECTIONS_TABLE;
        private String prefixForCollectionTables = DEFAULT_PREFIX_FOR_COLLECTION_TABLES;

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

        @Override
        public OracleVectorStoreQueryProvider build() {
            return new OracleVectorStoreQueryProvider(dataSource, collectionsTable, prefixForCollectionTables);
        }
    }
}