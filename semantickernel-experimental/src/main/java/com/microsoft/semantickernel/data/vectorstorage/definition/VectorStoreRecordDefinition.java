// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.data.vectorstorage.definition;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.semantickernel.data.vectorstorage.annotations.VectorStoreRecordData;
import com.microsoft.semantickernel.data.vectorstorage.annotations.VectorStoreRecordKey;
import com.microsoft.semantickernel.data.vectorstorage.annotations.VectorStoreRecordVector;
import com.microsoft.semantickernel.exceptions.SKException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a definition of a vector store record.
 */
public class VectorStoreRecordDefinition {

    private final VectorStoreRecordKeyField keyField;
    private final List<VectorStoreRecordDataField> dataFields;
    private final List<VectorStoreRecordVectorField> vectorFields;

    // Cached information
    private final List<VectorStoreRecordField> nonVectorFields;
    private final List<VectorStoreRecordField> allFields;
    private final Map<String, VectorStoreRecordField> allFieldsMap;

    /**
     * Gets the key field in the record definition.
     * @return VectorStoreRecordKeyField
     */
    public VectorStoreRecordKeyField getKeyField() {
        return keyField;
    }

    /**
     * Gets the data fields in the record definition.
     * @return List of VectorStoreRecordDataField
     */
    public List<VectorStoreRecordDataField> getDataFields() {
        return dataFields;
    }

    /**
     * Gets the vector fields in the record definition.
     *
     * @return List of VectorStoreRecordVectorField
     */
    public List<VectorStoreRecordVectorField> getVectorFields() {
        return vectorFields;
    }

    /**
     * Gets all fields in the record definition.
     *
     * @return List of VectorStoreRecordField
     */
    public List<VectorStoreRecordField> getAllFields() {
        return allFields;
    }

    /**
     * Gets the non-vector fields in the record definition.
     *
     * @return List of VectorStoreRecordField
     */
    public List<VectorStoreRecordField> getNonVectorFields() {
        return nonVectorFields;
    }

    /**
     * Checks if the record definition contains a field with the specified name.
     *
     * @param fieldName The name of the field to check.
     * @return boolean
     */
    public boolean containsField(String fieldName) {
        return allFieldsMap.containsKey(fieldName);
    }

    /**
     * Gets the field with the specified name.
     *
     * @param fieldName The name of the field to get.
     * @return VectorStoreRecordField
     */
    public VectorStoreRecordField getField(String fieldName) {
        if (!allFieldsMap.containsKey(fieldName)) {
            throw new SKException("Field not found: " + fieldName);
        }
        return allFieldsMap.get(fieldName);
    }

    private VectorStoreRecordDefinition(
        VectorStoreRecordKeyField keyField,
        List<VectorStoreRecordDataField> dataFields,
        List<VectorStoreRecordVectorField> vectorFields) {
        this.keyField = keyField;
        this.dataFields = Collections.unmodifiableList(dataFields);
        this.vectorFields = Collections.unmodifiableList(vectorFields);
        this.nonVectorFields = Collections
            .unmodifiableList(Stream.concat(Stream.of(keyField), dataFields.stream())
                .collect(Collectors.toList()));
        this.allFields = Collections
            .unmodifiableList(Stream.concat(nonVectorFields.stream(), vectorFields.stream())
                .collect(Collectors.toList()));
        this.allFieldsMap = Collections.unmodifiableMap(allFields.stream()
            .collect(Collectors.toMap(VectorStoreRecordField::getName, p -> p)));
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
     *
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
     *
     * @param recordClass The model class to create the definition from.
     * @return VectorStoreRecordDefinition
     */
    public static VectorStoreRecordDefinition fromRecordClass(Class<?> recordClass) {
        List<VectorStoreRecordKeyField> keyFields = new ArrayList<>();
        List<VectorStoreRecordDataField> dataFields = new ArrayList<>();
        List<VectorStoreRecordVectorField> vectorFields = new ArrayList<>();

        for (Field field : recordClass.getDeclaredFields()) {
            String storageName = null;
            if (field.isAnnotationPresent(JsonProperty.class)) {
                storageName = field.getAnnotation(JsonProperty.class).value();
            }

            if (field.isAnnotationPresent(VectorStoreRecordKey.class)) {
                VectorStoreRecordKey keyAttribute = field
                    .getAnnotation(VectorStoreRecordKey.class);

                if (storageName == null) {
                    storageName = keyAttribute.storageName().isEmpty() ? field.getName()
                        : keyAttribute.storageName();
                }
                keyFields.add(VectorStoreRecordKeyField.builder()
                    .withName(field.getName())
                    .withStorageName(storageName)
                    .withFieldType(field.getType())
                    .build());
            }

            if (field.isAnnotationPresent(VectorStoreRecordData.class)) {
                VectorStoreRecordData dataAttribute = field
                    .getAnnotation(VectorStoreRecordData.class);

                if (storageName == null) {
                    storageName = dataAttribute.storageName().isEmpty() ? field.getName()
                        : dataAttribute.storageName();
                }
                dataFields.add(VectorStoreRecordDataField.builder()
                    .withName(field.getName())
                    .withStorageName(storageName)
                    .withFieldType(field.getType())
                    .isFilterable(dataAttribute.isFilterable())
                    .build());
            }

            if (field.isAnnotationPresent(VectorStoreRecordVector.class)) {
                VectorStoreRecordVector vectorAttribute = field
                    .getAnnotation(VectorStoreRecordVector.class);

                if (storageName == null) {
                    storageName = vectorAttribute.storageName().isEmpty() ? field.getName()
                        : vectorAttribute.storageName();
                }
                vectorFields.add(VectorStoreRecordVectorField.builder()
                    .withName(field.getName())
                    .withStorageName(storageName)
                    .withFieldType(field.getType())
                    .withDimensions(vectorAttribute.dimensions())
                    .withIndexKind(vectorAttribute.indexKind())
                    .withDistanceFunction(vectorAttribute.distanceFunction())
                    .build());
            }
        }

        return checkFields(keyFields, dataFields, vectorFields);
    }

    /**
     * Validate that the record class contains only supported field types.
     * @param fields The declared fields in the record class.
     * @param supportedTypes The supported field types.
     * @throws IllegalArgumentException if unsupported field types are found.
     */
    public static void validateSupportedTypes(List<VectorStoreRecordField> fields,
        Set<Class<?>> supportedTypes) {
        Set<Class<?>> unsupportedTypes = new HashSet<>();
        for (VectorStoreRecordField field : fields) {
            if (!supportedTypes.contains(field.getFieldType())) {
                unsupportedTypes.add(field.getFieldType());
            }
        }
        if (!unsupportedTypes.isEmpty()) {
            throw new IllegalArgumentException(
                String.format(
                    "Unsupported field types found in record class: %s. Supported types: %s",
                    unsupportedTypes.stream().map(Class::getName).collect(Collectors.joining(", ")),
                    supportedTypes.stream().map(Class::getName).collect(Collectors.joining(", "))));
        }
    }
}
