package com.microsoft.semantickernel.tests.connectors.memory.jdbc;

import com.microsoft.semantickernel.connectors.data.jdbc.JDBCVectorStoreQueryProvider;
import com.microsoft.semantickernel.connectors.data.jdbc.JDBCVectorStoreRecordCollection;
import com.microsoft.semantickernel.connectors.data.jdbc.JDBCVectorStoreRecordCollectionOptions;
import com.microsoft.semantickernel.connectors.data.mysql.MySQLVectorStoreQueryProvider;
import com.microsoft.semantickernel.connectors.data.postgres.PostgreSQLVectorStoreQueryProvider;
import com.microsoft.semantickernel.data.recordoptions.GetRecordOptions;
import com.microsoft.semantickernel.tests.connectors.memory.Hotel;
import com.mysql.cj.jdbc.MysqlDataSource;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import javax.annotation.Nonnull;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@Testcontainers
public class JDBCVectorStoreRecordCollectionTest {

    @Container
    private static final MySQLContainer<?> MYSQL_CONTAINER = new MySQLContainer<>("mysql:5.7.34");

    private static final DockerImageName PGVECTOR = DockerImageName.parse("pgvector/pgvector:pg16").asCompatibleSubstituteFor("postgres");
    @Container
    private static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER = new PostgreSQLContainer<>(PGVECTOR);

    public enum QueryProvider {
        MySQL,
        PostgreSQL
    }

    private JDBCVectorStoreRecordCollection<Hotel> buildRecordCollection(QueryProvider provider, @Nonnull String collectionName) {
        JDBCVectorStoreQueryProvider queryProvider;
        DataSource dataSource;

        switch (provider) {
            case MySQL:
                MysqlDataSource mysqlDataSource = new MysqlDataSource();
                mysqlDataSource.setUrl(MYSQL_CONTAINER.getJdbcUrl());
                mysqlDataSource.setUser(MYSQL_CONTAINER.getUsername());
                mysqlDataSource.setPassword(MYSQL_CONTAINER.getPassword());
                dataSource = mysqlDataSource;
                queryProvider = MySQLVectorStoreQueryProvider.builder()
                        .withDataSource(dataSource)
                        .build();
                break;
            case PostgreSQL:
                PGSimpleDataSource pgSimpleDataSource = new PGSimpleDataSource();
                pgSimpleDataSource.setUrl(POSTGRESQL_CONTAINER.getJdbcUrl());
                pgSimpleDataSource.setUser(POSTGRESQL_CONTAINER.getUsername());
                pgSimpleDataSource.setPassword(POSTGRESQL_CONTAINER.getPassword());
                dataSource = pgSimpleDataSource;
                queryProvider = PostgreSQLVectorStoreQueryProvider.builder()
                        .withDataSource(dataSource)
                        .build();
                break;
            default:
                throw new IllegalArgumentException("Unknown query provider: " + provider);
        }


        JDBCVectorStoreRecordCollection<Hotel> recordCollection =  new JDBCVectorStoreRecordCollection<>(
                dataSource,
                collectionName,
                JDBCVectorStoreRecordCollectionOptions.<Hotel>builder()
                        .withRecordClass(Hotel.class)
                        .withQueryProvider(queryProvider)
                        .build());

        recordCollection.prepareAsync().block();
        recordCollection.createCollectionIfNotExistsAsync().block();
        return recordCollection;
    }

    @ParameterizedTest
    @EnumSource(QueryProvider.class)
    public void buildRecordCollection(QueryProvider provider) {
        assertNotNull(buildRecordCollection(provider, "buildTest"));
    }

    private List<Hotel> getHotels() {
        return List.of(
                new Hotel("id_1", "Hotel 1", 1, "Hotel 1 description", Arrays.asList(1.0f, 2.0f, 3.0f), 4.0),
                new Hotel("id_2", "Hotel 2", 2, "Hotel 2 description", Arrays.asList(1.0f, 2.0f, 3.0f), 3.0),
                new Hotel("id_3", "Hotel 3", 3, "Hotel 3 description", Arrays.asList(1.0f, 2.0f, 3.0f), 5.0),
                new Hotel("id_4", "Hotel 4", 4, "Hotel 4 description", Arrays.asList(1.0f, 2.0f, 3.0f), 4.0),
                new Hotel("id_5", "Hotel 5", 5, "Hotel 5 description", Arrays.asList(1.0f, 2.0f, 3.0f), 5.0)
        );
    }

    @ParameterizedTest
    @EnumSource(QueryProvider.class)
    public void upsertAndGetRecordAsync(QueryProvider provider) {
        String collectionName = "upsertAndGetRecordAsync";
        JDBCVectorStoreRecordCollection<Hotel>  recordCollection = buildRecordCollection(provider, collectionName);

        List<Hotel> hotels = getHotels();
        for (Hotel hotel : hotels) {
             recordCollection.upsertAsync(hotel, null).block();
        }

        // Upsert the first time
        for (Hotel hotel : hotels) {
            Hotel retrievedHotel =  recordCollection.getAsync(hotel.getId(), null).block();
            assertNotNull(retrievedHotel);
            assertEquals(hotel.getId(), retrievedHotel.getId());
            assertEquals(hotel.getRating(), retrievedHotel.getRating());

            // Update the rating
            hotel.setRating(1.0);
        }

        // Upsert the second time with updated rating
        for (Hotel hotel : hotels) {
            recordCollection.upsertAsync(hotel, null).block();
        }

        for (Hotel hotel : hotels) {
            Hotel retrievedHotel =  recordCollection.getAsync(hotel.getId(), null).block();
            assertNotNull(retrievedHotel);
            assertEquals(hotel.getId(), retrievedHotel.getId());
            assertEquals(1.0, retrievedHotel.getRating());
        }
    }

    @ParameterizedTest
    @EnumSource(QueryProvider.class)
    public void getBatchAsync(QueryProvider provider) {
        String collectionName = "getBatchAsync";
        JDBCVectorStoreRecordCollection<Hotel>  recordCollection = buildRecordCollection(provider, collectionName);

        List<Hotel> hotels = getHotels();
        for (Hotel hotel : hotels) {
             recordCollection.upsertAsync(hotel, null).block();
        }

        List<String> keys = new ArrayList<>();
        for (Hotel hotel : hotels) {
            keys.add(hotel.getId());
        }

        List<Hotel> retrievedHotels =  recordCollection.getBatchAsync(keys, null).block();
        assertNotNull(retrievedHotels);
        assertEquals(hotels.size(), retrievedHotels.size());
    }

    @ParameterizedTest
    @EnumSource(QueryProvider.class)
    public void upsertBatchAndGetBatchAsync(QueryProvider provider) {
        String collectionName = "upsertBatchAndGetBatchAsync";
        JDBCVectorStoreRecordCollection<Hotel>  recordCollection = buildRecordCollection(provider, collectionName);

        List<Hotel> hotels = getHotels();
         recordCollection.upsertBatchAsync(hotels, null).block();

        List<String> keys = new ArrayList<>();
        for (Hotel hotel : hotels) {
            keys.add(hotel.getId());
        }

        List<Hotel> retrievedHotels =  recordCollection.getBatchAsync(keys, null).block();
        assertNotNull(retrievedHotels);
        assertEquals(hotels.size(), retrievedHotels.size());
    }

    @ParameterizedTest
    @EnumSource(QueryProvider.class)
    public void insertAndReplaceAsync(QueryProvider provider) {
        String collectionName = "insertAndReplaceAsync";
        JDBCVectorStoreRecordCollection<Hotel>  recordCollection = buildRecordCollection(provider, collectionName);

        List<Hotel> hotels = getHotels();
         recordCollection.upsertBatchAsync(hotels, null).block();
         recordCollection.upsertBatchAsync(hotels, null).block();
         recordCollection.upsertBatchAsync(hotels, null).block();

        List<String> keys = new ArrayList<>();
        for (Hotel hotel : hotels) {
            keys.add(hotel.getId());
        }

        List<Hotel> retrievedHotels =  recordCollection.getBatchAsync(keys, null).block();
        assertNotNull(retrievedHotels);
        assertEquals(hotels.size(), retrievedHotels.size());
    }

    @ParameterizedTest
    @EnumSource(QueryProvider.class)
    public void deleteRecordAsync(QueryProvider provider) {
        String collectionName = "deleteRecordAsync";
        JDBCVectorStoreRecordCollection<Hotel>  recordCollection = buildRecordCollection(provider, collectionName);

        List<Hotel> hotels = getHotels();
         recordCollection.upsertBatchAsync(hotels, null).block();

        for (Hotel hotel : hotels) {
             recordCollection.deleteAsync(hotel.getId(), null).block();
            Hotel retrievedHotel =  recordCollection.getAsync(hotel.getId(), null).block();
            assertNull(retrievedHotel);
        }
    }

    @ParameterizedTest
    @EnumSource(QueryProvider.class)
    public void deleteBatchAsync(QueryProvider provider) {
        String collectionName = "deleteBatchAsync";
        JDBCVectorStoreRecordCollection<Hotel>  recordCollection = buildRecordCollection(provider, collectionName);

        List<Hotel> hotels = getHotels();
         recordCollection.upsertBatchAsync(hotels, null).block();

        List<String> keys = new ArrayList<>();
        for (Hotel hotel : hotels) {
            keys.add(hotel.getId());
        }

         recordCollection.deleteBatchAsync(keys, null).block();

        for (String key : keys) {
            Hotel retrievedHotel =  recordCollection.getAsync(key, null).block();
            assertNull(retrievedHotel);
        }
    }

    @ParameterizedTest
    @EnumSource(QueryProvider.class)
    public void getWithNoVectors(QueryProvider provider) {
        String collectionName = "getWithNoVectors";
        JDBCVectorStoreRecordCollection<Hotel>  recordCollection = buildRecordCollection(provider, collectionName);

        List<Hotel> hotels = getHotels();
         recordCollection.upsertBatchAsync(hotels, null).block();

        GetRecordOptions options = GetRecordOptions.builder()
            .includeVectors(false)
            .build();

        for (Hotel hotel : hotels) {
            Hotel retrievedHotel =  recordCollection.getAsync(hotel.getId(), options).block();
            assertNotNull(retrievedHotel);
            assertEquals(hotel.getId(), retrievedHotel.getId());
            assertNull(retrievedHotel.getDescriptionEmbedding());
        }

        options = GetRecordOptions.builder()
            .includeVectors(true)
            .build();

        for (Hotel hotel : hotels) {
            Hotel retrievedHotel =  recordCollection.getAsync(hotel.getId(), options).block();
            assertNotNull(retrievedHotel);
            assertEquals(hotel.getId(), retrievedHotel.getId());
            assertNotNull(retrievedHotel.getDescriptionEmbedding());
        }
    }

    @ParameterizedTest
    @EnumSource(QueryProvider.class)
    public void getBatchWithNoVectors(QueryProvider provider) {
        String collectionName = "getBatchWithNoVectors";
        JDBCVectorStoreRecordCollection<Hotel>  recordCollection = buildRecordCollection(provider, collectionName);

        List<Hotel> hotels = getHotels();
         recordCollection.upsertBatchAsync(hotels, null).block();

        GetRecordOptions options = GetRecordOptions.builder()
            .includeVectors(false)
            .build();

        List<String> keys = new ArrayList<>();
        for (Hotel hotel : hotels) {
            keys.add(hotel.getId());
        }

        List<Hotel> retrievedHotels =  recordCollection.getBatchAsync(keys, options).block();
        assertNotNull(retrievedHotels);
        assertEquals(hotels.size(), retrievedHotels.size());

        for (Hotel hotel : retrievedHotels) {
            assertNull(hotel.getDescriptionEmbedding());
        }

        options = GetRecordOptions.builder()
            .includeVectors(true)
            .build();

        retrievedHotels =  recordCollection.getBatchAsync(keys, options).block();
        assertNotNull(retrievedHotels);
        assertEquals(hotels.size(), retrievedHotels.size());

        for (Hotel hotel : retrievedHotels) {
            assertNotNull(hotel.getDescriptionEmbedding());
        }
    }
}
