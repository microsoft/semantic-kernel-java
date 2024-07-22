// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.recorddefinition;

import com.microsoft.semantickernel.data.recordattributes.VectorStoreRecordDataAttribute;
import com.microsoft.semantickernel.data.recordattributes.VectorStoreRecordKeyAttribute;
import com.microsoft.semantickernel.data.recordattributes.VectorStoreRecordVectorAttribute;

import java.lang.reflect.Field;
import java.util.ArrayList;
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

    /**
     * Gets the vector fields in the record definition.
     * @return List of VectorStoreRecordVectorField
     */
    public List<VectorStoreRecordVectorField> getVectorFields() {
        return Collections.unmodifiableList(vectorFields);
    }

    /**
     * Gets all fields in the record definition.
     * @return List of VectorStoreRecordField
     */
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
    public static VectorStoreRecordDefinition fromFields(List<VectorStoreRecordField> fields) {
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
     * @param recordClass The model class to create the definition from.
     * @return VectorStoreRecordDefinition
     */
    public static VectorStoreRecordDefinition fromRecordClass(Class<?> recordClass) {
        List<VectorStoreRecordKeyField> keyFields = new ArrayList<>();
        List<VectorStoreRecordDataField> dataFields = new ArrayList<>();
        List<VectorStoreRecordVectorField> vectorFields = new ArrayList<>();

        for (Field field : recordClass.getDeclaredFields()) {
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

    private static String getSupportedTypesString(HashSet<Class<?>> types) {
        return types.stream().map(Class::getName).collect(Collectors.joining(", "));
    }

    public static void validateSupportedTypes(Class<?> recordClass,
        VectorStoreRecordDefinition recordDefinition, HashSet<Class<?>> keyTypes,
        HashSet<Class<?>> dataTypes, HashSet<Class<?>> vectorTypes) {
        String keyTypesString = getSupportedTypesString(keyTypes);
        String dataTypesString = getSupportedTypesString(dataTypes);
        String vectorTypesString = getSupportedTypesString(vectorTypes);

        for (VectorStoreRecordField field : recordDefinition.getAllFields()) {
            if (field instanceof VectorStoreRecordKeyField) {
                try {
                    Field declaredField = recordClass.getDeclaredField(field.getName());

                    if (!keyTypes.contains(declaredField.getType())) {
                        throw new IllegalArgumentException(
                            "Unsupported key field type: " + declaredField.getType().getName()
                                + ". Supported types are: " + keyTypesString);
                    }
                } catch (NoSuchFieldException e) {
                    throw new IllegalArgumentException(
                        "Key field not found in record class: " + field.getName());
                }
            }

            if (field instanceof VectorStoreRecordDataField) {
                try {
                    Field declaredField = recordClass.getDeclaredField(field.getName());

                    if (!dataTypes.contains(declaredField.getType())) {
                        throw new IllegalArgumentException(
                            "Unsupported data field type: " + declaredField.getType().getName()
                                + ". Supported types are: " + dataTypesString);
                    }
                } catch (NoSuchFieldException e) {
                    throw new IllegalArgumentException(
                        "Data field not found in record class: " + field.getName());
                }
            }

            if (field instanceof VectorStoreRecordVectorField) {
                try {
                    Field declaredField = recordClass.getDeclaredField(field.getName());

                    if (!vectorTypes.contains(declaredField.getType())) {
                        throw new IllegalArgumentException(
                            "Unsupported vector field type: " + declaredField.getType().getName()
                                + ". Supported types are: " + vectorTypesString);
                    }
                } catch (NoSuchFieldException e) {
                    throw new IllegalArgumentException(
                        "Vector field not found in record class: " + field.getName());
                }
            }
        }
    }
}
