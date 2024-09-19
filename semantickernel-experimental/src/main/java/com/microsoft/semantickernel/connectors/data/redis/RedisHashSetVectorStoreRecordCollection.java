// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.semantickernel.data.vectorsearch.VectorSearchResult;
import com.microsoft.semantickernel.data.vectorsearch.VectorizedSearch;
import com.microsoft.semantickernel.data.vectorsearch.queries.VectorSearchQuery;
import com.microsoft.semantickernel.data.vectorsearch.queries.VectorizedSearchQuery;
import com.microsoft.semantickernel.data.vectorstorage.VectorStoreRecordCollection;
import com.microsoft.semantickernel.data.vectorstorage.VectorStoreRecordMapper;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordDefinition;
import com.microsoft.semantickernel.data.vectorstorage.options.DeleteRecordOptions;
import com.microsoft.semantickernel.data.vectorstorage.options.GetRecordOptions;
import com.microsoft.semantickernel.data.vectorstorage.options.UpsertRecordOptions;
import com.microsoft.semantickernel.data.vectorstorage.options.VectorSearchOptions;
import com.microsoft.semantickernel.exceptions.SKException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.tuple.Pair;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.search.FTSearchParams;
import redis.clients.jedis.search.IndexDefinition;
import redis.clients.jedis.search.IndexOptions;
import redis.clients.jedis.search.Schema;
import redis.clients.jedis.search.SearchResult;

import javax.annotation.Nonnull;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RedisHashSetVectorStoreRecordCollection<Record>
    implements VectorStoreRecordCollection<String, Record>,
    VectorizedSearch<Record> {

    private static final HashSet<Class<?>> supportedKeyTypes = new HashSet<>(
        Collections.singletonList(
            String.class));

    private static final HashSet<Class<?>> supportedVectorTypes = new HashSet<>(
        Arrays.asList(
            List.class,
            Collection.class));

    private final JedisPooled client;
    private final String collectionName;
    private final RedisHashSetVectorStoreRecordCollectionOptions<Record> options;
    private final VectorStoreRecordMapper<Record, Map.Entry<String, Map<byte[], byte[]>>> vectorStoreRecordMapper;
    private final VectorStoreRecordDefinition recordDefinition;
    private final byte[][] dataFields;
    private final ObjectMapper objectMapper;

    /**
     * Creates a new instance of the RedisVectorRecordStore.
     *
     * @param client  The Redis client.
     * @param collectionName The name of the collection.
     * @param options The options for the store.
     */
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public RedisHashSetVectorStoreRecordCollection(
        @Nonnull JedisPooled client,
        @Nonnull String collectionName,
        @Nonnull RedisHashSetVectorStoreRecordCollectionOptions<Record> options) {
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
        VectorStoreRecordDefinition.validateSupportedTypes(
            Collections.singletonList(recordDefinition.getKeyField()),
            supportedKeyTypes);
        VectorStoreRecordDefinition.validateSupportedTypes(
            new ArrayList<>(recordDefinition.getVectorFields()),
            supportedVectorTypes);

        // If mapper is not provided, set a default one
        objectMapper = options.getObjectMapper() != null ? options.getObjectMapper()
            : new ObjectMapper();

        // If mapper is not provided, set a default one
        if (options.getVectorStoreRecordMapper() == null) {
            vectorStoreRecordMapper = new RedisHashSetVectorStoreRecordMapper.Builder<Record>()
                .withRecordClass(options.getRecordClass())
                .withVectorStoreRecordDefinition(recordDefinition)
                .withObjectMapper(objectMapper)
                .build();
        } else {
            vectorStoreRecordMapper = options.getVectorStoreRecordMapper();
        }

        // Creates a list of paths to retrieve from Redis when no vectors are requested
        this.dataFields = new byte[recordDefinition.getDataFields().size()][];
        for (int i = 0; i < recordDefinition.getDataFields().size(); i++) {
            this.dataFields[i] = stringToBytes(
                recordDefinition.getDataFields().get(i).getEffectiveStorageName());
        }
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
    public Mono<VectorStoreRecordCollection<String, Record>> createCollectionAsync() {
        return Mono.fromRunnable(() -> {
            Schema schema = RedisVectorStoreCollectionCreateMapping
                .mapToSchema(recordDefinition.getAllFields(), false);

            IndexDefinition indexDefinition = new IndexDefinition(IndexDefinition.Type.HASH)
                .setPrefixes(collectionName + ":");

            client.ftCreate(
                collectionName,
                IndexOptions.defaultOptions().setDefinition(indexDefinition),
                schema);
        })
            .subscribeOn(Schedulers.boundedElastic())
            .then(Mono.just(this));
    }

    /**
     * Creates the collection in the store if it does not exist.
     *
     * @return A Mono representing the completion of the creation operation.
     */
    @Override
    public Mono<VectorStoreRecordCollection<String, Record>> createCollectionIfNotExistsAsync() {
        return collectionExistsAsync().flatMap(exists -> {
            if (!exists) {
                return createCollectionAsync();
            }

            return Mono.just(this);
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

    private String prefixKeyIfNeeded(String key, String collectionName) {
        return options.isPrefixCollectionName() ? collectionName + ":" + key : key;
    }

    private String removeKeyPrefixIfNeeded(String key, String collectionName) {
        if (options.isPrefixCollectionName() && key.startsWith(collectionName + ":")) {
            return key.substring(collectionName.length() + 1);
        }
        return key;
    }

    private byte[] stringToBytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    private Map<byte[], byte[]> addDataFieldNames(List<byte[]> result) {
        Map<byte[], byte[]> dataFields = new HashMap<>();
        for (int i = 0; i < result.size(); i++) {
            if (result.get(i) == null) {
                continue;
            }
            dataFields.put(this.dataFields[i], result.get(i));
        }
        return dataFields;
    }

    /**
     * Gets a record from the store.
     *
     * @param key     The key of the record to get.
     * @param options The options for getting the record.
     * @return A Mono emitting the record.
     */
    @Override
    public Mono<Record> getAsync(String key, GetRecordOptions options) {
        return getBatchAsync(Collections.singletonList(key), options)
            .mapNotNull(records -> {
                if (records.isEmpty()) {
                    return null;
                }

                return records.get(0);
            });
    }

    /**
     * Gets a batch of records from the store.
     *
     * @param keys    The keys of the records to get.
     * @param options The options for getting the records.
     * @return A Mono emitting a list of records.
     */
    @Override
    public Mono<List<Record>> getBatchAsync(List<String> keys,
        GetRecordOptions options) {
        Pipeline pipeline = client.pipelined();
        List<Map.Entry<String, Response<?>>> responses = new ArrayList<>(keys.size());
        keys.forEach(key -> {
            String redisKey = prefixKeyIfNeeded(key, collectionName);

            if (options != null && options.isIncludeVectors()) {
                // Returns Map<byte[], byte[]> with the fields and values
                responses
                    .add(new AbstractMap.SimpleEntry<>(key,
                        pipeline.hgetAll(stringToBytes(redisKey))));
            } else {
                // Returns List<String> with the values of the fields
                responses
                    .add(new AbstractMap.SimpleEntry<>(key,
                        pipeline.hmget(stringToBytes(redisKey), dataFields)));
            }
        });

        return Mono.defer(() -> {
            pipeline.sync();

            try {
                return Mono.just(responses.stream()
                    .map(entry -> {
                        if (options != null && options.isIncludeVectors()) {
                            // Results directly in a Map<byte[], byte[]>
                            return this.vectorStoreRecordMapper
                                .mapStorageModelToRecord(
                                    new AbstractMap.SimpleEntry<>(entry.getKey(),
                                        (Map<byte[], byte[]>) entry.getValue().get()),
                                    options);
                        }

                        // Results in a List<byte[]> with the values of the fields
                        return this.vectorStoreRecordMapper
                            .mapStorageModelToRecord(
                                new AbstractMap.SimpleEntry<>(entry.getKey(),
                                    addDataFieldNames((List<byte[]>) entry.getValue().get())),
                                options);
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
        Map.Entry<String, Map<byte[], byte[]>> redisObject = this.vectorStoreRecordMapper
            .mapRecordToStorageModel(data);
        String redisKey = prefixKeyIfNeeded(redisObject.getKey(), collectionName);

        return Mono.fromRunnable(() -> client.hset(stringToBytes(redisKey), redisObject.getValue()))
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
            Map.Entry<String, Map<byte[], byte[]>> redisObject = this.vectorStoreRecordMapper
                .mapRecordToStorageModel(record);
            String redisKey = prefixKeyIfNeeded(redisObject.getKey(), collectionName);

            keys.add(redisObject.getKey());
            pipeline.hset(stringToBytes(redisKey), redisObject.getValue());
        });

        return Mono.fromRunnable(pipeline::sync)
            .subscribeOn(Schedulers.boundedElastic())
            .thenReturn(keys);
    }

    /**
     * Deletes a record from the store.
     *
     * @param key     The key of the record to delete.
     * @param options The options for deleting the record.
     * @return A Mono representing the completion of the deletion operation.
     */
    @Override
    public Mono<Void> deleteAsync(String key, DeleteRecordOptions options) {
        String redisKey = prefixKeyIfNeeded(key, collectionName);

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
            String redisKey = prefixKeyIfNeeded(key, collectionName);
            pipeline.del(redisKey);
        });

        return Mono.fromRunnable(pipeline::sync)
            .subscribeOn(Schedulers.boundedElastic())
            .then();
    }

    @Override
    public Mono<List<VectorSearchResult<Record>>> searchAsync(VectorSearchQuery query) {
        if (recordDefinition.getVectorFields().isEmpty()) {
            return Mono
                .error(new SKException("No vector fields defined. Cannot perform vector search"));
        }

        return createCollectionIfNotExistsAsync().flatMap(collection -> Mono.fromCallable(() -> {
            if (query instanceof VectorizedSearchQuery) {
                VectorSearchOptions options = query.getSearchOptions();

                Pair<String, FTSearchParams> ftSearchParams = RedisVectorStoreCollectionSearchMapping
                    .buildQuery((VectorizedSearchQuery) query, recordDefinition,
                        RedisStorageType.HASH_SET);

                SearchResult searchResult = client.ftSearch(collectionName,
                    ftSearchParams.getLeft(), ftSearchParams.getRight());

                return searchResult.getDocuments().stream()
                    .map(doc -> {
                        String key = removeKeyPrefixIfNeeded(doc.getId(), collectionName);
                        double score = 0;

                        // Convert from Map<String, Object> to Map<byte[], byte[]>
                        Map<byte[], byte[]> storage = new HashMap<>();
                        for (Map.Entry<String, Object> entry : doc.getProperties()) {
                            if (entry.getValue() instanceof byte[]) {
                                storage.put(stringToBytes(entry.getKey()),
                                    (byte[]) entry.getValue());
                            } else if (entry.getKey().equals(
                                RedisVectorStoreCollectionSearchMapping.VECTOR_SCORE_FIELD)) {
                                score = Double.parseDouble((String) entry.getValue());
                            }
                        }

                        Record record = this.vectorStoreRecordMapper
                            .mapStorageModelToRecord(
                                new AbstractMap.SimpleEntry<>(key, storage),
                                new GetRecordOptions(
                                    options != null && options.isIncludeVectors()));

                        return new VectorSearchResult<>(record, score);
                    })
                    .collect(Collectors.toList());
            }

            throw new SKException("Unsupported query type");
        }).subscribeOn(Schedulers.boundedElastic()));
    }

    /**
     * Vectorized search. This method searches for records that are similar to the given vector.
     *
     * @param vector  The vector to search with.
     * @param options The options to use for the search.
     * @return A list of search results.
     */
    @Override
    public Mono<List<VectorSearchResult<Record>>> searchAsync(List<Float> vector,
        VectorSearchOptions options) {
        return this.searchAsync(VectorSearchQuery.createQuery(vector, options));
    }
}
