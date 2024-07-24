// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.redis;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.semantickernel.data.VectorStoreRecordCollection;
import com.microsoft.semantickernel.data.VectorStoreRecordMapper;
import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordDataField;
import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordDefinition;
import com.microsoft.semantickernel.data.recordoptions.DeleteRecordOptions;
import com.microsoft.semantickernel.data.recordoptions.GetRecordOptions;
import com.microsoft.semantickernel.data.recordoptions.UpsertRecordOptions;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.json.JSONArray;
import org.json.JSONObject;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.json.Path2;
import redis.clients.jedis.search.IndexDefinition;
import redis.clients.jedis.search.IndexOptions;
import redis.clients.jedis.search.Schema;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.AbstractMap.SimpleEntry;
import java.util.stream.Collectors;

public class RedisVectorStoreRecordCollection<Record>
    implements VectorStoreRecordCollection<String, Record> {

    private static final HashSet<Class<?>> supportedKeyTypes = new HashSet<>(
        Collections.singletonList(
            String.class));

    private static final HashSet<Class<?>> supportedVectorTypes = new HashSet<>(
        Arrays.asList(
            List.class,
            Collection.class));

    private final JedisPooled client;
    private final String collectionName;
    private final RedisVectorStoreRecordCollectionOptions<Record> options;
    private final VectorStoreRecordMapper<Record, Entry<String, Object>> vectorStoreRecordMapper;
    private final VectorStoreRecordDefinition recordDefinition;
    private final Path2[] dataFields;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Creates a new instance of the RedisVectorRecordStore.
     *
     * @param client  The Redis client.
     * @param options The options for the store.
     */
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public RedisVectorStoreRecordCollection(
        @Nonnull JedisPooled client,
        @Nonnull String collectionName,
        @Nonnull RedisVectorStoreRecordCollectionOptions<Record> options) {
        this.client = client;
        this.collectionName = collectionName;
        this.options = options;

        // If record definition is not provided, create one from the record class
        if (options.getRecordDefinition() == null) {
            this.recordDefinition = VectorStoreRecordDefinition.fromRecordClass(
                options.getRecordClass());
        } else {
            this.recordDefinition = options.getRecordDefinition();
        }

        // Validate supported types
        VectorStoreRecordDefinition.validateSupportedKeyTypes(options.getRecordClass(),
            recordDefinition, supportedKeyTypes);
        VectorStoreRecordDefinition.validateSupportedVectorTypes(options.getRecordClass(),
            recordDefinition, supportedVectorTypes);

        // If mapper is not provided, set a default one
        if (options.getVectorStoreRecordMapper() == null) {
            vectorStoreRecordMapper = new RedisVectorStoreRecordMapper.Builder<Record>()
                .withKeyFieldName(recordDefinition.getKeyField().getName())
                .withRecordClass(options.getRecordClass())
                .build();
        } else {
            vectorStoreRecordMapper = options.getVectorStoreRecordMapper();
        }

        // Creates a list of paths to retrieve from Redis when no vectors are requested
        // Paths are in the format of $.field
        this.dataFields = recordDefinition.getDataFields().stream()
            .map(VectorStoreRecordDataField::getName)
            .map(Path2::new)
            .toArray(Path2[]::new);
    }

    /**
     * Gets the name of the collection.
     *
     * @return The name of the collection.
     */
    @Override
    public String getCollectionName() {
        return collectionName;
    }

    /**
     * Checks if the collection exists in the store.
     *
     * @return A Mono emitting a boolean indicating if the collection exists.
     */
    @Override
    public Mono<Boolean> collectionExistsAsync() {
        return Mono.fromCallable(() -> {
            try {
                Map<String, Object> info = this.client.ftInfo(collectionName);
                return info != null && !info.isEmpty();
            } catch (Exception e) {
                if (!(e instanceof JedisDataException)) {
                    throw e;
                }
                return false;
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Creates the collection in the store.
     *
     * @return A Mono representing the completion of the creation operation.
     */
    @Override
    public Mono<Void> createCollectionAsync() {
        return Mono.fromRunnable(() -> {
            Schema schema = RedisVectorStoreCollectionCreateMapping
                .mapToSchema(recordDefinition.getAllFields());

            IndexDefinition indexDefinition = new IndexDefinition(IndexDefinition.Type.JSON)
                .setPrefixes(collectionName + ":");

            client.ftCreate(
                collectionName,
                IndexOptions.defaultOptions().setDefinition(indexDefinition),
                schema);
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public Mono<Void> createCollectionIfNotExistsAsync() {
        return collectionExistsAsync().flatMap(exists -> {
            if (!exists) {
                return createCollectionAsync();
            }

            return Mono.empty();
        });
    }

    /**
     * Deletes the collection from the store.
     *
     * @return A Mono representing the completion of the deletion operation.
     */
    @Override
    public Mono<Void> deleteCollectionAsync() {
        return Mono.fromRunnable(() -> client.ftDropIndex(collectionName))
            .subscribeOn(Schedulers.boundedElastic())
            .then();
    }

    private String getRedisKey(String key, String collectionName) {
        return options.isPrefixCollectionName() ? collectionName + ":" + key : key;
    }

    private JsonNode removeRedisPathPrefix(JSONObject object) {
        ObjectNode noPathPrefix = objectMapper.createObjectNode();
        object.keySet().forEach(key -> {
            String newKey = key;
            if (key.startsWith("$.")) {
                newKey = key.substring(2);
            }

            Object value = ((JSONArray) object.get(key)).get(0);
            noPathPrefix.set(newKey, objectMapper.valueToTree(value));
        });

        return noPathPrefix;
    }

    /**
     * Gets a record from the store.
     *
     * @param key       The key of the record to get.
     * @param options The options for getting the record.
     * @return A Mono emitting the record.
     */
    @Override
    public Mono<Record> getAsync(String key, GetRecordOptions options) {
        String redisKey = getRedisKey(key, collectionName);

        return Mono.defer(() -> {
            try {
                Object value;
                if (options == null || options.includeVectors()) {
                    value = client.jsonGet(redisKey);
                } else {
                    value = client.jsonGet(redisKey, dataFields);
                }

                if (value == null) {
                    return Mono.empty();
                }

                JsonNode jsonNode;
                if (options == null || options.includeVectors()) {
                    jsonNode = objectMapper.valueToTree(value);
                } else {
                    // Remove the $. prefix from every key in the JSON object
                    jsonNode = removeRedisPathPrefix((JSONObject) value);
                }

                return Mono.just(this.vectorStoreRecordMapper
                    .mapStorageModeltoRecord(new SimpleEntry<>(key, jsonNode)));
            } catch (Exception e) {
                return Mono.error(e);
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Gets a batch of records from the store.
     *
     * @param keys The keys of the records to get.
     * @param options The options for getting the records.
     * @return A Mono emitting a list of records.
     */
    @Override
    public Mono<List<Record>> getBatchAsync(List<String> keys,
        GetRecordOptions options) {
        Pipeline pipeline = client.pipelined();
        List<Entry<String, Response<Object>>> responses = new ArrayList<>(keys.size());
        keys.forEach(key -> {
            String redisKey = getRedisKey(key, collectionName);

            if (options == null || options.includeVectors()) {
                responses.add(new SimpleEntry<>(key, pipeline.jsonGet(redisKey)));
            } else {
                responses.add(new SimpleEntry<>(key, pipeline.jsonGet(redisKey, dataFields)));
            }
        });

        return Mono.defer(() -> {
            pipeline.sync();

            try {
                return Mono.just(responses.stream()
                    .map(entry -> {
                        Object value = entry.getValue().get();
                        if (value == null) {
                            return null;
                        }

                        JsonNode jsonNode;
                        if (options == null || options.includeVectors()) {
                            jsonNode = objectMapper.valueToTree(value);
                        } else {
                            jsonNode = removeRedisPathPrefix((JSONObject) value);
                        }
                        return this.vectorStoreRecordMapper
                            .mapStorageModeltoRecord(new SimpleEntry<>(entry.getKey(), jsonNode));
                    })
                    .collect(Collectors.toList()));
            } catch (Exception e) {
                return Mono.error(e);
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Inserts or updates a record in the store.
     *
     * @param data    The record to upsert.
     * @param options The options for upserting the record.
     * @return A Mono emitting the key of the upserted record.
     */
    @Override
    public Mono<String> upsertAsync(Record data, UpsertRecordOptions options) {
        Entry<String, Object> redisObject = this.vectorStoreRecordMapper
            .mapRecordToStorageModel(data);
        String redisKey = getRedisKey(redisObject.getKey(), collectionName);

        return Mono.fromRunnable(() -> client.jsonSet(redisKey, redisObject.getValue()))
            .subscribeOn(Schedulers.boundedElastic())
            .thenReturn(redisObject.getKey());
    }

    /**
     * Inserts or updates a batch of records in the store.
     *
     * @param data    The records to upsert.
     * @param options The options for upserting the records.
     * @return A Mono emitting a collection of keys of the upserted records.
     */
    @Override
    public Mono<List<String>> upsertBatchAsync(List<Record> data, UpsertRecordOptions options) {
        Pipeline pipeline = client.pipelined();
        List<String> keys = new ArrayList<>(data.size());

        data.forEach(record -> {
            Entry<String, Object> redisObject = this.vectorStoreRecordMapper
                .mapRecordToStorageModel(record);
            String redisKey = getRedisKey(redisObject.getKey(), collectionName);

            keys.add(redisObject.getKey());
            pipeline.jsonSet(redisKey, redisObject.getValue());
        });

        return Mono.fromRunnable(pipeline::sync)
            .subscribeOn(Schedulers.boundedElastic())
            .thenReturn(keys);
    }

    /**
     * Deletes a record from the store.
     *
     * @param key       The key of the record to delete.
     * @param options The options for deleting the record.
     * @return A Mono representing the completion of the deletion operation.
     */
    @Override
    public Mono<Void> deleteAsync(String key, DeleteRecordOptions options) {
        String redisKey = getRedisKey(key, collectionName);

        return Mono.fromRunnable(() -> client.del(redisKey))
            .subscribeOn(Schedulers.boundedElastic())
            .then();
    }

    /**
     * Deletes a batch of records from the store.
     *
     * @param strings The keys of the records to delete.
     * @param options The options for deleting the records.
     * @return A Mono representing the completion of the deletion operation.
     */
    @Override
    public Mono<Void> deleteBatchAsync(List<String> strings, DeleteRecordOptions options) {
        Pipeline pipeline = client.pipelined();
        strings.forEach(key -> {
            String redisKey = getRedisKey(key, collectionName);
            pipeline.del(redisKey);
        });

        return Mono.fromRunnable(pipeline::sync)
            .subscribeOn(Schedulers.boundedElastic())
            .then();
    }
}
