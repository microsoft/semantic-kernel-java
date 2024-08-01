package com.microsoft.semantickernel.tests.connectors.memory.jdbc;

import com.microsoft.semantickernel.connectors.data.jdbc.JDBCVectorStoreRecordCollection;
import com.microsoft.semantickernel.connectors.data.jdbc.JDBCVectorStoreRecordCollectionOptions;
import com.microsoft.semantickernel.connectors.data.jdbc.MySQLVectorStoreQueryProvider;
import com.microsoft.semantickernel.data.recordoptions.GetRecordOptions;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@Testcontainers
public class JDBCVectorStoreRecordCollectionTest {
    @Container
    private static final MySQLContainer<?> CONTAINER = new MySQLContainer<>("mysql:5.7.34");
    private static final String MYSQL_USER = "test";
    private static final String MYSQL_PASSWORD = "test";
    private static Connection connection;
    @BeforeAll
    static void setup() throws SQLException {
        connection = DriverManager.getConnection(CONTAINER.getJdbcUrl(), MYSQL_USER, MYSQL_PASSWORD);
    }

    private JDBCVectorStoreRecordCollection<Hotel> buildRecordCollection(@Nonnull String collectionName) {
        JDBCVectorStoreRecordCollection<Hotel> recordCollection =  new JDBCVectorStoreRecordCollection<>(
                connection,
                collectionName,
                JDBCVectorStoreRecordCollectionOptions.<Hotel>builder()
                        .withRecordClass(Hotel.class)
                        .withQueryProvider(MySQLVectorStoreQueryProvider.builder()
                                .withConnection(connection)
                                .build())
                        .build());

        recordCollection.prepareAsync().block();
        recordCollection.createCollectionIfNotExistsAsync().block();
        return recordCollection;
    }

    @Test
    public void buildRecordCollection() {
        assertNotNull(buildRecordCollection("buildTest"));
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
        JDBCVectorStoreRecordCollection<Hotel> recordStore = buildRecordCollection(collectionName);

        List<Hotel> hotels = getHotels();
        for (Hotel hotel : hotels) {
            recordStore.upsertAsync(hotel, null).block();
        }

        for (Hotel hotel : hotels) {
            Hotel retrievedHotel = recordStore.getAsync(hotel.getId(), null).block();
            assertNotNull(retrievedHotel);
            assertEquals(hotel.getId(), retrievedHotel.getId());
        }
    }

    @Test
    public void getBatchAsync() {
        String collectionName = "getBatchAsync";
        JDBCVectorStoreRecordCollection<Hotel> recordStore = buildRecordCollection(collectionName);

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
        JDBCVectorStoreRecordCollection<Hotel> recordStore = buildRecordCollection(collectionName);

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
        JDBCVectorStoreRecordCollection<Hotel> recordStore = buildRecordCollection(collectionName);

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
        JDBCVectorStoreRecordCollection<Hotel> recordStore = buildRecordCollection(collectionName);

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
        JDBCVectorStoreRecordCollection<Hotel> recordStore = buildRecordCollection(collectionName);

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

    @Test
    public void getWithNoVectors() {
        String collectionName = "getWithNoVectors";
        JDBCVectorStoreRecordCollection<Hotel> recordStore = buildRecordCollection(collectionName);

        List<Hotel> hotels = getHotels();
        recordStore.upsertBatchAsync(hotels, null).block();

        GetRecordOptions options = GetRecordOptions.builder()
            .includeVectors(false)
            .build();

        for (Hotel hotel : hotels) {
            Hotel retrievedHotel = recordStore.getAsync(hotel.getId(), options).block();
            assertNotNull(retrievedHotel);
            assertEquals(hotel.getId(), retrievedHotel.getId());
            assertNull(retrievedHotel.getDescriptionEmbedding());
        }

        options = GetRecordOptions.builder()
            .includeVectors(true)
            .build();

        for (Hotel hotel : hotels) {
            Hotel retrievedHotel = recordStore.getAsync(hotel.getId(), options).block();
            assertNotNull(retrievedHotel);
            assertEquals(hotel.getId(), retrievedHotel.getId());
            assertNotNull(retrievedHotel.getDescriptionEmbedding());
        }
    }

    @Test
    public void getBatchWithNoVectors() {
        String collectionName = "getBatchWithNoVectors";
        JDBCVectorStoreRecordCollection<Hotel> recordStore = buildRecordCollection(collectionName);

        List<Hotel> hotels = getHotels();
        recordStore.upsertBatchAsync(hotels, null).block();

        GetRecordOptions options = GetRecordOptions.builder()
            .includeVectors(false)
            .build();

        List<String> keys = new ArrayList<>();
        for (Hotel hotel : hotels) {
            keys.add(hotel.getId());
        }

        List<Hotel> retrievedHotels = recordStore.getBatchAsync(keys, options).block();
        assertNotNull(retrievedHotels);
        assertEquals(hotels.size(), retrievedHotels.size());

        for (Hotel hotel : retrievedHotels) {
            assertNull(hotel.getDescriptionEmbedding());
        }

        options = GetRecordOptions.builder()
            .includeVectors(true)
            .build();

        retrievedHotels = recordStore.getBatchAsync(keys, options).block();
        assertNotNull(retrievedHotels);
        assertEquals(hotels.size(), retrievedHotels.size());

        for (Hotel hotel : retrievedHotels) {
            assertNotNull(hotel.getDescriptionEmbedding());
        }
    }
}
