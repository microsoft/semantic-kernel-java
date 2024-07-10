package com.microsoft.semantickernel.tests.connectors.memory.jdbc;

import com.microsoft.semantickernel.connectors.memory.jdbc.JDBCVectorRecordStore;
import com.microsoft.semantickernel.connectors.memory.jdbc.JDBCVectorStoreOptions;
import com.microsoft.semantickernel.tests.connectors.memory.Hotel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@Testcontainers
public class JDBCVectorRecordStoreTest {
    @Container
    private static final MySQLContainer<?> CONTAINER = new MySQLContainer<>("mysql:5.7.34");
    private static final String MYSQL_USER = "test";
    private static final String MYSQL_PASSWORD = "test";
    private static Connection connection;
    private static final String STORAGE_TABLE_NAME = "data";
    @BeforeAll
    static void setup() throws SQLException {
        connection = DriverManager.getConnection(CONTAINER.getJdbcUrl(), MYSQL_USER, MYSQL_PASSWORD);

        String createTable = String.format("CREATE TABLE %s ("
            + " id VARCHAR(255) PRIMARY KEY,"
            + " name VARCHAR(255) NOT NULL,"
            + " code INT NOT NULL,"
            + " description VARCHAR(255) NOT NULL,"
            + " descriptionEmbedding VARCHAR(255) NOT NULL,"
            + " rating DOUBLE NOT NULL)", STORAGE_TABLE_NAME);

        connection.createStatement().execute(createTable);
    }

    private String sanitizeKey(String key, String collection) {
        return collection + ":" + key;
    }

    private JDBCVectorRecordStore<Hotel> buildRecordStore(@Nonnull String collectionName) {
        return new JDBCVectorRecordStore<>(connection, JDBCVectorStoreOptions.<Hotel>builder()
                .withStorageTableName(STORAGE_TABLE_NAME)
                .withDefaultCollectionName(collectionName)
                .withRecordClass(Hotel.class)
                .withSanitizeKeyFunction(this::sanitizeKey)
                .build());
    }

    @Test
    public void buildRecordStore() {
        assertNotNull(buildRecordStore("buildTest"));
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

    @Test
    public void upsertAndGetRecordAsync() {
        String collectionName = "upsertAndGetRecordAsync";
        JDBCVectorRecordStore<Hotel> recordStore = buildRecordStore(collectionName);

        List<Hotel> hotels = getHotels();
        for (Hotel hotel : hotels) {
            recordStore.upsertAsync(hotel, null).block();
        }

        for (Hotel hotel : hotels) {
            Hotel retrievedHotel = recordStore.getAsync(hotel.getId(), null).block();
            assertNotNull(retrievedHotel);
            assertEquals(sanitizeKey(hotel.getId(), collectionName), retrievedHotel.getId());
        }
    }

    @Test
    public void getBatchAsync() {
        String collectionName = "getBatchAsync";
        JDBCVectorRecordStore<Hotel> recordStore = buildRecordStore(collectionName);

        List<Hotel> hotels = getHotels();
        for (Hotel hotel : hotels) {
            recordStore.upsertAsync(hotel, null).block();
        }

        List<String> keys = new ArrayList<>();
        for (Hotel hotel : hotels) {
            keys.add(hotel.getId());
        }

        List<Hotel> retrievedHotels = recordStore.getBatchAsync(keys, null).block();
        assertNotNull(retrievedHotels);
        assertEquals(hotels.size(), retrievedHotels.size());
    }

    @Test
    public void upsertBatchAndGetBatchAsync() {
        String collectionName = "upsertBatchAndGetBatchAsync";
        JDBCVectorRecordStore<Hotel> recordStore = buildRecordStore(collectionName);

        List<Hotel> hotels = getHotels();
        recordStore.upsertBatchAsync(hotels, null).block();

        List<String> keys = new ArrayList<>();
        for (Hotel hotel : hotels) {
            keys.add(hotel.getId());
        }

        List<Hotel> retrievedHotels = recordStore.getBatchAsync(keys, null).block();
        assertNotNull(retrievedHotels);
        assertEquals(hotels.size(), retrievedHotels.size());
    }

    @Test
    public void insertAndReplaceAsync() {
        String collectionName = "insertAndReplaceAsync";
        JDBCVectorRecordStore<Hotel> recordStore = buildRecordStore(collectionName);

        List<Hotel> hotels = getHotels();
        recordStore.upsertBatchAsync(hotels, null).block();
        recordStore.upsertBatchAsync(hotels, null).block();
        recordStore.upsertBatchAsync(hotels, null).block();

        List<String> keys = new ArrayList<>();
        for (Hotel hotel : hotels) {
            keys.add(hotel.getId());
        }

        List<Hotel> retrievedHotels = recordStore.getBatchAsync(keys, null).block();
        assertNotNull(retrievedHotels);
        assertEquals(hotels.size(), retrievedHotels.size());
    }

    @Test
    public void deleteRecordAsync() {
        String collectionName = "deleteRecordAsync";
        JDBCVectorRecordStore<Hotel> recordStore = buildRecordStore(collectionName);

        List<Hotel> hotels = getHotels();
        recordStore.upsertBatchAsync(hotels, null).block();

        for (Hotel hotel : hotels) {
            recordStore.deleteAsync(hotel.getId(), null).block();
            Hotel retrievedHotel = recordStore.getAsync(hotel.getId(), null).block();
            assertNull(retrievedHotel);
        }
    }

    @Test
    public void deleteBatchAsync() {
        String collectionName = "deleteBatchAsync";
        JDBCVectorRecordStore<Hotel> recordStore = buildRecordStore(collectionName);

        List<Hotel> hotels = getHotels();
        recordStore.upsertBatchAsync(hotels, null).block();

        List<String> keys = new ArrayList<>();
        for (Hotel hotel : hotels) {
            keys.add(hotel.getId());
        }

        recordStore.deleteBatchAsync(keys, null).block();

        for (String key : keys) {
            Hotel retrievedHotel = recordStore.getAsync(key, null).block();
            assertNull(retrievedHotel);
        }
    }
}
