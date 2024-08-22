// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordDefinition;
import com.microsoft.semantickernel.data.recorddefinition.VectorStoreRecordField;
import com.microsoft.semantickernel.data.recordoptions.DeleteRecordOptions;
import com.microsoft.semantickernel.data.recordoptions.GetRecordOptions;
import com.microsoft.semantickernel.data.recordoptions.UpsertRecordOptions;
import com.microsoft.semantickernel.exceptions.SKException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import reactor.core.publisher.Mono;

public class VolatileVectorStoreRecordCollection<Record> implements
    VectorStoreRecordCollection<String, Record> {

    private static final HashSet<Class<?>> supportedKeyTypes = new HashSet<>(
        Collections.singletonList(String.class));
    private Map<String, Map<String, ?>> collections;
    private final String collectionName;
    private final VolatileVectorStoreRecordCollectionOptions<Record> options;
    private final VectorStoreRecordDefinition recordDefinition;
    private final ObjectMapper objectMapper;

    public VolatileVectorStoreRecordCollection(String collectionName,
        VolatileVectorStoreRecordCollectionOptions<Record> options) {
        this.collectionName = collectionName;
        this.options = options;
        this.collections = new ConcurrentHashMap<>();
        this.objectMapper = new ObjectMapper();

        if (options.getRecordDefinition() != null) {
            this.recordDefinition = options.getRecordDefinition();
        } else {
            this.recordDefinition = VectorStoreRecordDefinition
                .fromRecordClass(this.options.getRecordClass());
        }

        // Validate the key type
        VectorStoreRecordDefinition.validateSupportedTypes(
            Collections.singletonList(recordDefinition.getKeyField()),
            supportedKeyTypes);
    }

    VolatileVectorStoreRecordCollection(String collectionName,
        Map<String, Map<String, ?>> collections,
        VolatileVectorStoreRecordCollectionOptions<Record> options) {
        this(collectionName, options);
        this.collections = collections;
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
        return Mono.fromCallable(() -> collections.containsKey(collectionName));
    }

    /**
     * Creates the collection in the store.
     *
     * @return A Mono representing the completion of the creation operation.
     */
    @Override
    public Mono<VectorStoreRecordCollection<String, Record>> createCollectionAsync() {
        return Mono.fromRunnable(() -> collections.put(collectionName, new ConcurrentHashMap<>()))
            .then(Mono.just(this));
    }

    /**
     * Creates the collection in the store if it does not exist.
     *
     * @return A Mono representing the completion of the creation operation.
     */
    @Override
    public Mono<VectorStoreRecordCollection<String, Record>> createCollectionIfNotExistsAsync() {
        return Mono
            .fromRunnable(() -> collections.putIfAbsent(collectionName, new ConcurrentHashMap<>()))
            .then(Mono.just(this));
    }

    /**
     * Deletes the collection from the store.
     *
     * @return A Mono representing the completion of the deletion operation.
     */
    @Override
    public Mono<Void> deleteCollectionAsync() {
        return Mono.fromRunnable(() -> collections.remove(collectionName));
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
        return Mono.fromCallable(() -> getCollection().get(key));
    }

    /**
     * Gets a batch of records from the store.
     *
     * @param keys    The keys of the records to get.
     * @param options The options for getting the records.
     * @return A Mono emitting a list of records.
     */
    @Override
    public Mono<List<Record>> getBatchAsync(List<String> keys, GetRecordOptions options) {
        return Mono.fromCallable(() -> {
            Map<String, Record> collection = getCollection();
            return keys.stream().map(collection::get).collect(Collectors.toList());
        });
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
        return Mono.fromCallable(() -> {
            try {
                ObjectNode objectNode = objectMapper.valueToTree(data);
                String key = objectNode
                    .get(recordDefinition.getKeyField().getEffectiveStorageName()).asText();

                getCollection().put(key, data);
                return key;
            } catch (Exception e) {
                throw new SKException(
                    "Failure to serialize object. Ensure your model object can be serialized by Jackson, i.e the class is visible, has getters, constructor, annotations etc.",
                    e);
            }
        });
    }

    /**
     * Inserts or updates a batch of records in the store.
     *
     * @param data    The records to upsert.
     * @param options The options for upserting the records.
     * @return A Mono emitting a list of keys of the upserted records.
     */
    @Override
    public Mono<List<String>> upsertBatchAsync(List<Record> data, UpsertRecordOptions options) {
        return Mono.fromCallable(() -> {
            Map<String, Record> collection = getCollection();
            return data.stream().map(record -> {
                try {
                    ObjectNode objectNode = objectMapper.valueToTree(record);
                    String key = objectNode
                        .get(recordDefinition.getKeyField().getEffectiveStorageName()).asText();

                    collection.put(key, record);
                    return key;
                } catch (Exception e) {
                    throw new SKException(
                        "Failure to serialize object. Ensure your model object can be serialized by Jackson, i.e the class is visible, has getters, constructor, annotations etc.",
                        e);
                }
            }).collect(Collectors.toList());
        });
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
        return Mono.fromRunnable(() -> getCollection().remove(key));
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
        return Mono.fromRunnable(() -> {
            Map<String, Record> collection = getCollection();
            strings.forEach(collection::remove);
        });
    }

    private Map<String, Record> getCollection() {
        if (!collections.containsKey(collectionName)) {
            throw new IllegalStateException(
                String.format("Collection %s does not exist.", collectionName));
        }
        return (Map<String, Record>) collections.get(collectionName);
    }
}
