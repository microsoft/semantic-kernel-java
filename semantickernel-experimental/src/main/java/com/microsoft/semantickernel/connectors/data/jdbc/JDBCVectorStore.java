package com.microsoft.semantickernel.connectors.data.jdbc;

import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordDefinition;
import com.microsoft.semantickernel.exceptions.SKException;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A JDBC vector store.
 */
public class JDBCVectorStore implements SQLVectorStore<JDBCVectorStoreRecordCollection<?>> {
    private final Connection connection;
    private final JDBCVectorStoreOptions options;
    private final JDBCVectorStoreQueryProvider queryProvider;

    /**
     * Creates a new instance of the {@link JDBCVectorStore}.
     * If using this constructor, call {@link #prepareAsync()} before using the vector store.
     *
     * @param connection the connection
     * @param options    the options
     */
    public JDBCVectorStore(@Nonnull Connection connection, @Nullable JDBCVectorStoreOptions options) {
        this.connection = connection;
        this.options = options;

        if (this.options != null && this.options.getQueryProvider() != null) {
            this.queryProvider = this.options.getQueryProvider();
        } else {
            this.queryProvider = new JDBCVectorStoreDefaultQueryProvider(connection);
        }
    }

    /**
     * Creates a new builder for the vector store.
     *
     * @return the builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Gets a collection from the vector store.
     *
     * @param collectionName   The name of the collection.
     * @param recordClass      The class type of the record.
     * @param recordDefinition The record definition.
     * @return The collection.
     */
    @Override
    public <Key, Record> JDBCVectorStoreRecordCollection<?> getCollection(
            @Nonnull String collectionName,
            @Nonnull Class<Record> recordClass,
            @Nullable VectorStoreRecordDefinition recordDefinition) {

        if (this.options != null && this.options.getVectorStoreRecordCollectionFactory() != null) {
            return this.options.getVectorStoreRecordCollectionFactory()
                .createVectorStoreRecordCollection(
                    connection,
                    collectionName,
                    JDBCVectorStoreRecordCollectionOptions.<Record>builder()
                            .withRecordClass(recordClass)
                            .withRecordDefinition(recordDefinition)
                            .withQueryProvider(this.queryProvider)
                            .build());
        }

        return new JDBCVectorStoreRecordCollection<>(
                connection,
                collectionName,
                JDBCVectorStoreRecordCollectionOptions.<Record>builder()
                        .withRecordClass(recordClass)
                        .withRecordDefinition(recordDefinition)
                        .withQueryProvider(this.queryProvider)
                        .build());
    }

    /**
     * Gets the names of all collections in the vector store.
     *
     * @return A list of collection names.
     */
    @Override
    public Mono<List<String>> getCollectionNamesAsync() {
        return Mono.fromCallable(() -> {
            List<String> collectionNames = new ArrayList<>();
            try {
                ResultSet resultSet = queryProvider.getCollectionNames();
                while (resultSet.next()) {
                    collectionNames.add(resultSet.getString(1));
                }

                return collectionNames;
            } catch (SQLException e) {
                throw new SKException("Failed to get collection names.", e);
            }
        });
    }

    @Override
    public Mono<Void> prepareAsync() {
        return Mono.fromRunnable(() -> {
            try {
                queryProvider.prepareVectorStore();
            } catch (SQLException e) {
                throw new SKException("Failed to prepare vector store.", e);
            }
        });
    }

    /**
     * Builder for creating a {@link JDBCVectorStore}.
     */
    public static class Builder {
        private Connection connection;
        private JDBCVectorStoreOptions options;

        /**
         * Sets the connection.
         *
         * @param connection the connection
         * @return the builder
         */
        public Builder withConnection(Connection connection) {
            this.connection = connection;
            return this;
        }

        /**
         * Sets the options.
         *
         * @param options the options
         * @return the builder
         */
        public Builder withOptions(JDBCVectorStoreOptions options) {
            this.options = options;
            return this;
        }

        /**
         * Builds the {@link JDBCVectorStore}.
         *
         * @return the {@link JDBCVectorStore}
         */
        public JDBCVectorStore build() {
            return buildAsync().block();
        }

        /**
         * Builds the {@link JDBCVectorStore} asynchronously.
         *
         * @return the {@link Mono} with the {@link JDBCVectorStore}
         */
        public Mono<JDBCVectorStore> buildAsync() {
            if (connection == null) {
                throw new IllegalArgumentException("connection is required");
            }

            JDBCVectorStore vectorStore = new JDBCVectorStore(connection, options);
            return vectorStore.prepareAsync().thenReturn(vectorStore);
        }
    }
}
