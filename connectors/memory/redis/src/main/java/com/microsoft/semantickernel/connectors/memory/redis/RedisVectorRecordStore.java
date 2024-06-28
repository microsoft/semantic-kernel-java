package com.microsoft.semantickernel.connectors.memory.redis;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.semantickernel.exceptions.SKException;
import com.microsoft.semantickernel.memory.VectorRecordStore;
import com.microsoft.semantickernel.memory.VectorStoreRecordMapper;
import com.microsoft.semantickernel.memory.recorddefinition.VectorStoreRecordDefinition;
import com.microsoft.semantickernel.memory.recordoptions.DeleteRecordOptions;
import com.microsoft.semantickernel.memory.recordoptions.GetRecordOptions;
import com.microsoft.semantickernel.memory.recordoptions.UpsertRecordOptions;
import reactor.core.publisher.Mono;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.AbstractMap.SimpleEntry;
import java.util.stream.Collectors;

public class RedisVectorRecordStore<Record> implements VectorRecordStore<String, Record> {
    private final JedisPooled client;
    private final RedisVectorStoreOptions<Record> options;

    public RedisVectorRecordStore(JedisPooled client, RedisVectorStoreOptions<Record> options) {
        this.client = client;

        // If record definition is not provided, create one from the record class
        VectorStoreRecordDefinition definition = options.getRecordDefinition();
        if (definition == null) {
            definition = VectorStoreRecordDefinition.create(options.getRecordClass());
        }

        // If mapper is not provided, add a default one
        VectorStoreRecordMapper<Record, Map.Entry<String, Object>> mapper = options.getVectorStoreRecordMapper();
        if (mapper == null) {
            mapper = new RedisVectorStoreRecordMapper.Builder<Record>()
                    .keyField(definition.getKeyField().getName())
                    .recordClass(options.getRecordClass())
                    .build();
        }

        this.options = RedisVectorStoreOptions.<Record>builder()
                .withRecordClass(options.getRecordClass())
                .withDefaultCollectionName(options.getDefaultCollectionName())
                .withPrefixCollectionName(options.prefixCollectionName())
                .withRecordDefinition(definition)
                .withVectorStoreRecordMapper(mapper)
                .build();
    }

    private String getRedisKey(String key, String collectionName) {
        return options.prefixCollectionName() ? collectionName + ":" + key : key;
    }

    private String resolveCollectionName(@Nullable String collectionName) {
        if (collectionName != null) {
            return collectionName;
        }
        if (options.getDefaultCollectionName() != null) {
            return options.getDefaultCollectionName();
        }
        throw new SKException("A collection name is required");
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
        String collectionName = resolveCollectionName(
                options != null ? options.getCollectionName() : null);

        String redisKey = getRedisKey(key, collectionName);
        JsonNode jsonNode = new ObjectMapper().valueToTree(client.jsonGet(redisKey));

        return Mono.just(this.options.getVectorStoreRecordMapper().mapStorageModeltoRecord(new SimpleEntry<>(key, jsonNode)));
    }

    /**
     * Gets a batch of records from the store.
     *
     * @param keys The keys of the records to get.
     * @param options The options for getting the records.
     * @return A Mono emitting a collection of records.
     */
    @Override
    public Mono<Collection<Record>> getBatchAsync(Collection<String> keys, GetRecordOptions options) {
        String collectionName = resolveCollectionName(
                options != null ? options.getCollectionName() : null);

        Pipeline pipeline = client.pipelined();
        Map<String, Response<Object>> responses = new HashMap<>();
        keys.forEach(key -> {
            String redisKey = getRedisKey(key, collectionName);
            // Store the response in a map to be processed later
            responses.put(key, pipeline.jsonGet(redisKey));
        });

        pipeline.sync();

        return Mono.just(responses.entrySet().stream()
                .map(entry -> {
                    JsonNode jsonNode = (JsonNode) entry.getValue().get();
                    return this.options.getVectorStoreRecordMapper().mapStorageModeltoRecord(new SimpleEntry<>(entry.getKey(), jsonNode));
                })
                .collect(Collectors.toList()));
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
        String collectionName = resolveCollectionName(
                options != null ? options.getCollectionName() : null);

        Entry<String, Object> redisObject = this.options.getVectorStoreRecordMapper().mapRecordToStorageModel(data);
        String redisKey = getRedisKey(redisObject.getKey(), collectionName);

        client.jsonSet(redisKey, redisObject.getValue());
        return Mono.just(redisObject.getKey());
    }

    /**
     * Inserts or updates a batch of records in the store.
     *
     * @param data    The records to upsert.
     * @param options The options for upserting the records.
     * @return A Mono emitting a collection of keys of the upserted records.
     */
    @Override
    public Mono<Collection<String>> upsertBatchAsync(Collection<Record> data, UpsertRecordOptions options) {
        String collectionName = resolveCollectionName(
                options != null ? options.getCollectionName() : null);

        Pipeline pipeline = client.pipelined();
        List<String> keys = new ArrayList<>(data.size());

        data.forEach(record -> {
            Entry<String, Object> redisObject = this.options.getVectorStoreRecordMapper().mapRecordToStorageModel(record);
            String redisKey = getRedisKey(redisObject.getKey(), collectionName);

            keys.add(redisObject.getKey());
            pipeline.jsonSet(redisKey, redisObject.getValue());
        });

        pipeline.sync();

        return Mono.just(keys);
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
        String collectionName = resolveCollectionName(
                options != null ? options.getCollectionName() : null);

        String redisKey = getRedisKey(key, collectionName);
        client.del(redisKey);

        return Mono.empty();
    }

    /**
     * Deletes a batch of records from the store.
     *
     * @param strings The keys of the records to delete.
     * @param options The options for deleting the records.
     * @return A Mono representing the completion of the deletion operation.
     */
    @Override
    public Mono<Void> deleteBatchAsync(Collection<String> strings, DeleteRecordOptions options) {
        String collectionName = resolveCollectionName(
                options != null ? options.getCollectionName() : null);

        Pipeline pipeline = client.pipelined();
        strings.forEach(key -> {
            String redisKey = getRedisKey(key, collectionName);
            pipeline.del(redisKey);
        });

        pipeline.sync();

        return Mono.empty();
    }
}
