// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.recorddefinition;

import com.microsoft.semantickernel.data.recordattributes.VectorStoreRecordDataAttribute;
import com.microsoft.semantickernel.data.recordattributes.VectorStoreRecordKeyAttribute;
import com.microsoft.semantickernel.data.recordattributes.VectorStoreRecordVectorAttribute;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a definition of a vector store record.
 */
public class VectorStoreRecordDefinition {
    private final VectorStoreRecordKeyField keyField;
    private final List<VectorStoreRecordDataField> dataFields;
    private final List<VectorStoreRecordVectorField> vectorFields;

    public VectorStoreRecordKeyField getKeyField() {
        return keyField;
    }

    public List<VectorStoreRecordDataField> getDataFields() {
        return Collections.unmodifiableList(dataFields);
    }

    public List<VectorStoreRecordVectorField> getVectorFields() {
        return Collections.unmodifiableList(vectorFields);
    }

    public List<VectorStoreRecordField> getAllFields() {
        List<VectorStoreRecordField> fields = new ArrayList<>();
        fields.add(keyField);
        fields.addAll(dataFields);
        fields.addAll(vectorFields);
        return fields;
    }

    private VectorStoreRecordDefinition(
        VectorStoreRecordKeyField keyField,
        List<VectorStoreRecordDataField> dataFields,
        List<VectorStoreRecordVectorField> vectorFields) {
        this.keyField = keyField;
        this.dataFields = dataFields;
        this.vectorFields = vectorFields;
    }

    private static VectorStoreRecordDefinition checkFields(
        List<VectorStoreRecordKeyField> keyFields,
        List<VectorStoreRecordDataField> dataFields,
        List<VectorStoreRecordVectorField> vectorFields) {
        if (keyFields.size() != 1) {
            throw new IllegalArgumentException("Exactly one key field is required");
        }

        return new VectorStoreRecordDefinition(keyFields.iterator().next(), dataFields,
            vectorFields);
    }

    /**
     * Create a VectorStoreRecordDefinition from a collection of fields.
     * @param fields The fields to create the definition from.
     * @return VectorStoreRecordDefinition
     */
    public static VectorStoreRecordDefinition create(List<VectorStoreRecordField> fields) {
        List<VectorStoreRecordKeyField> keyFields = fields.stream()
            .filter(p -> p instanceof VectorStoreRecordKeyField)
            .map(p -> (VectorStoreRecordKeyField) p)
            .collect(Collectors.toList());

        List<VectorStoreRecordDataField> dataFields = fields.stream()
            .filter(p -> p instanceof VectorStoreRecordDataField)
            .map(p -> (VectorStoreRecordDataField) p)
            .collect(Collectors.toList());

        List<VectorStoreRecordVectorField> vectorFields = fields.stream()
            .filter(p -> p instanceof VectorStoreRecordVectorField)
            .map(p -> (VectorStoreRecordVectorField) p)
            .collect(Collectors.toList());

        return checkFields(keyFields, dataFields, vectorFields);
    }

    /**
     * Create a VectorStoreRecordDefinition from a model.
     * @param modelClass The model class to create the definition from.
     * @return VectorStoreRecordDefinition
     */
    public static VectorStoreRecordDefinition create(Class<?> modelClass) {
        List<VectorStoreRecordKeyField> keyFields = new ArrayList<>();
        List<VectorStoreRecordDataField> dataFields = new ArrayList<>();
        List<VectorStoreRecordVectorField> vectorFields = new ArrayList<>();

        for (Field field : modelClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(VectorStoreRecordKeyAttribute.class)) {
                VectorStoreRecordKeyAttribute keyAttribute = field
                    .getAnnotation(VectorStoreRecordKeyAttribute.class);

                keyFields.add(VectorStoreRecordKeyField.builder()
                    .withName(field.getName())
                    .withStorageName(keyAttribute.storageName())
                    .build());
            }

            if (field.isAnnotationPresent(VectorStoreRecordDataAttribute.class)) {
                VectorStoreRecordDataAttribute dataAttribute = field
                    .getAnnotation(VectorStoreRecordDataAttribute.class);

                dataFields.add(VectorStoreRecordDataField.builder()
                    .withName(field.getName())
                    .withStorageName(dataAttribute.storageName())
                    .withHasEmbedding(dataAttribute.hasEmbedding())
                    .withEmbeddingFieldName(dataAttribute.embeddingFieldName())
                    .withFieldType(field.getType())
                    .withIsFilterable(dataAttribute.isFilterable())
                    .build());
            }

            if (field.isAnnotationPresent(VectorStoreRecordVectorAttribute.class)) {
                VectorStoreRecordVectorAttribute vectorAttribute = field
                    .getAnnotation(VectorStoreRecordVectorAttribute.class);

                vectorFields.add(VectorStoreRecordVectorField.builder()
                    .withName(field.getName())
                    .withStorageName(vectorAttribute.storageName())
                    .withDimensions(vectorAttribute.dimensions())
                    .withIndexKind(IndexKind.fromString(vectorAttribute.indexKind()))
                    .withDistanceFunction(
                        DistanceFunction.fromString(vectorAttribute.distanceFunction()))
                    .build());
            }
        }

        return checkFields(keyFields, dataFields, vectorFields);
    }
}
