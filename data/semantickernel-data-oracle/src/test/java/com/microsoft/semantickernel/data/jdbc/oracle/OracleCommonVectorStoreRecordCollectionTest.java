/*
 ** Semantic Kernel Oracle connector version 1.0.
 **
 ** Copyright (c) 2025 Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */
package com.microsoft.semantickernel.data.jdbc.oracle;

import oracle.jdbc.OracleConnection;
import oracle.jdbc.datasource.impl.OracleDataSource;
import org.testcontainers.oracle.OracleContainer;
import org.testcontainers.utility.MountableFile;
import java.sql.SQLException;
import java.time.Duration;

public class OracleCommonVectorStoreRecordCollectionTest {

    protected static final String ORACLE_IMAGE_NAME = "gvenzl/oracle-free:23.7-slim-faststart";
    protected static final OracleDataSource DATA_SOURCE;
    protected static final OracleDataSource SYSDBA_DATA_SOURCE;

    static {

        try {
            DATA_SOURCE = new oracle.jdbc.datasource.impl.OracleDataSource();
            SYSDBA_DATA_SOURCE = new oracle.jdbc.datasource.impl.OracleDataSource();
            String urlFromEnv = System.getenv("ORACLE_JDBC_URL");

            if (urlFromEnv == null) {
                // The Ryuk component is relied upon to stop this container.
                OracleContainer oracleContainer = new OracleContainer(ORACLE_IMAGE_NAME)
                    .withCopyFileToContainer(MountableFile.forClasspathResource("/initialize.sql"),
                        "/container-entrypoint-initdb.d/initialize.sql")
                    .withStartupTimeout(Duration.ofSeconds(600))
                    .withConnectTimeoutSeconds(600)
                    .withDatabaseName("pdb1")
                    .withUsername("testuser")
                    .withPassword("testpwd");
                oracleContainer.start();

                initDataSource(
                    DATA_SOURCE,
                    oracleContainer.getJdbcUrl(),
                    oracleContainer.getUsername(),
                    oracleContainer.getPassword());
                initDataSource(SYSDBA_DATA_SOURCE, oracleContainer.getJdbcUrl(), "sys", oracleContainer.getPassword());
            } else {
                initDataSource(
                    DATA_SOURCE,
                    urlFromEnv,
                    System.getenv("ORACLE_JDBC_USER"),
                    System.getenv("ORACLE_JDBC_PASSWORD"));
                initDataSource(
                    SYSDBA_DATA_SOURCE,
                    urlFromEnv,
                    System.getenv("ORACLE_JDBC_USER"),
                    System.getenv("ORACLE_JDBC_PASSWORD"));
            }
            SYSDBA_DATA_SOURCE.setConnectionProperty(OracleConnection.CONNECTION_PROPERTY_INTERNAL_LOGON, "SYSDBA");

        } catch (SQLException sqlException) {
            throw new AssertionError(sqlException);
        }
    }

    static void initDataSource(OracleDataSource dataSource, String url, String username, String password) {
        dataSource.setURL(url);
        dataSource.setUser(username);
        dataSource.setPassword(password);
    }

}
