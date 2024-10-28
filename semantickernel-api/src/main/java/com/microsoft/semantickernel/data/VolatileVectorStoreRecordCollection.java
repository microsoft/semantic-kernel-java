// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.semantickernel.data.vectorsearch.VectorOperations;
import com.microsoft.semantickernel.data.vectorsearch.VectorSearchResults;
import com.microsoft.semantickernel.data.vectorstorage.VectorStoreRecordCollection;
import com.microsoft.semantickernel.data.vectorstorage.definition.DistanceFunction;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordDefinition;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordVectorField;
import com.microsoft.semantickernel.data.vectorstorage.options.DeleteRecordOptions;
import com.microsoft.semantickernel.data.vectorstorage.options.GetRecordOptions;
import com.microsoft.semantickernel.data.vectorstorage.options.UpsertRecordOptions;
import com.microsoft.semantickernel.data.vectorstorage.options.VectorSearchOptions;
import com.microsoft.semantickernel.exceptions.SKException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Represents a volatile vector store record collection.
 *
 * @param <Record> The type of record in the collection.
 */
public class VolatileVectorStoreRecordCollection<Record> implements
    VectorStoreRecordCollection<String, Record> {

    private static final HashSet<Class<?>> supportedKeyTypes = new HashSet<>(
        Collections.singletonList(String.class));
    private Map<String, Map<String, ?>> collections;
    private final String collectionName;
    private final VolatileVectorStoreRecordCollectionOptions<Record> options;
    private final VectorStoreRecordDefinition recordDefinition;
    private final ObjectMapper objectMapper;

    /**
     * Creates a new instance of the volatile vector store record collection.
     *
     * @param collectionName The name of the collection.
     * @param options        The options for the collection.
     */
    public VolatileVectorStoreRecordCollection(String collectionName,
        VolatileVectorStoreRecordCollectionOptions<Record> options) {
        this.collectionName = collectionName;
        this.options = options;
        this.collections = new ConcurrentHashMap<>();

        if (options.getRecordDefinition() != null) {
            this.recordDefinition = options.getRecordDefinition();
        } else {
            this.recordDefinition = VectorStoreRecordDefinition
                .fromRecordClass(this.options.getRecordClass());
        }

        if (options.getObjectMapper() == null) {
            this.objectMapper = new ObjectMapper();
        } else {
            this.objectMapper = options.getObjectMapper();
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

    private List<Float> arrayNodeToFloatList(ArrayNode arrayNode) {
        return Stream.iterate(0, i -> i + 1)
            .limit(arrayNode.size())
            .map(i -> arrayNode.get(i).floatValue())
            .collect(Collectors.toList());
    }

    /**
     * Vectorized search. This method searches for records that are similar to the given vector.
     *
     * @param vector  The vector to search with.
     * @param options The options to use for the search.
     * @return A list of search results.
     */
    @Override
    public Mono<VectorSearchResults<Record>> searchAsync(List<Float> vector,
        final VectorSearchOptions options) {
        if (recordDefinition.getVectorFields().isEmpty()) {
            throw new SKException("No vector fields defined. Cannot perform vector search");
        }

        return Mono.fromCallable(() -> {
            VectorStoreRecordVectorField firstVectorField = recordDefinition.getVectorFields()
                .get(0);
            VectorSearchOptions effectiveOptions = options == null
                ? VectorSearchOptions.createDefault(firstVectorField.getName())
                : options;

            VectorStoreRecordVectorField vectorField = effectiveOptions.getVectorFieldName() == null
                ? firstVectorField
                : (VectorStoreRecordVectorField) recordDefinition
                    .getField(effectiveOptions.getVectorFieldName());

            DistanceFunction distanceFunction = vectorField
                .getDistanceFunction() == DistanceFunction.UNDEFINED
                    ? DistanceFunction.EUCLIDEAN_DISTANCE
                    : vectorField.getDistanceFunction();

            List<Record> records = VolatileVectorStoreCollectionSearchMapping.filterRecords(
                new ArrayList<>(getCollection().values()), effectiveOptions.getVectorSearchFilter(),
                recordDefinition, objectMapper);

            return new VectorSearchResults<>(
                VectorOperations.exactSimilaritySearch(records, vector, vectorField,
                    distanceFunction, effectiveOptions));
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
