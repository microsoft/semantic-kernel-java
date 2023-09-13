// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.memory.sqlite;

import com.microsoft.semantickernel.connectors.memory.jdbc.JDBCConnector;
import com.microsoft.semantickernel.connectors.memory.jdbc.JDBCMemoryStore;
import com.microsoft.semantickernel.connectors.memory.jdbc.SQLConnector;
import com.microsoft.semantickernel.connectors.memory.jdbc.SQLMemoryStore;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.annotation.CheckReturnValue;
import reactor.core.publisher.Mono;

public class SQLiteMemoryStore extends JDBCMemoryStore {
    private SQLiteMemoryStore(SQLConnector connector) {
        super(connector);
    }

    /** Builds a SQLiteMemoryStore. */
    public static class Builder implements SQLMemoryStore.Builder<SQLiteMemoryStore> {
        private Connection connection;

        /**
         * Builds and returns an SQLiteMemoryStore instance with the specified database connection.
         * The build process will connect to the database and create the required tables.
         *
         * @return An SQLiteMemoryStore instance configured with the provided database connection.
         * @deprecated Use {@link #buildAsync()} instead.
         */
        @Override
        @Deprecated
        public SQLiteMemoryStore build() {
            return this.buildAsync().block();
        }

        /**
         * Asynchronously builds and returns an SQLiteMemoryStore instance with the specified
         * database connection.
         *
         * @return A Mono with an SQLiteMemoryStore instance configured with the provided SQLite
         *     database connection.
         */
        @Override
        @CheckReturnValue
        public Mono<SQLiteMemoryStore> buildAsync() {
            JDBCConnector connector = new JDBCConnector(connection);
            SQLiteMemoryStore memoryStore = new SQLiteMemoryStore(connector);
            return connector.createTableAsync().thenReturn(memoryStore);
        }

        /**
         * Sets the SQLite database connection to be used by the SQLite memory store being built.
         *
         * @param connection The Connection object representing the SQLite database connection.
         * @return The updated Builder instance to continue the building process for an
         *     SQLiteMemoryStore.
         */
        @Override
        public Builder withConnection(Connection connection) {
            this.connection = connection;
            return this;
        }

        /**
         * Creates and sets an SQLite database connection using the specified filename. This will
         * overwrite any previously set connection.
         *
         * @param filename The filename of the SQLite database.
         * @return The updated Builder instance with the SQLite database connection set.
         * @throws SQLException If there is an issue with establishing the database connection.
         */
        public Builder withFilename(String filename) throws SQLException {
            return withConnection(DriverManager.getConnection("jdbc:sqlite:" + filename));
        }
    }
}
