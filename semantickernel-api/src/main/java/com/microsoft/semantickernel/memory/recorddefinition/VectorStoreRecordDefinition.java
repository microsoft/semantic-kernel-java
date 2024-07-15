// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.memory.recorddefinition;

import com.microsoft.semantickernel.memory.recordattributes.VectorStoreRecordDataAttribute;
import com.microsoft.semantickernel.memory.recordattributes.VectorStoreRecordKeyAttribute;
import com.microsoft.semantickernel.memory.recordattributes.VectorStoreRecordVectorAttribute;

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

    public List<VectorStoreRecordVectorField> getVectorFields() {
        return Collections.unmodifiableList(vectorFields);
    }

    public List<VectorStoreRecordField> getAllFields() {
        List<VectorStoreRecordField> allFields = new ArrayList<>();
        allFields.add(keyField);
        allFields.addAll(dataFields);
        allFields.addAll(vectorFields);
        return allFields;
    }

    public List<VectorStoreRecordField> getNonVectorFields() {
        List<VectorStoreRecordField> nonVectorFields = new ArrayList<>();
        nonVectorFields.add(keyField);
        nonVectorFields.addAll(dataFields);
        return nonVectorFields;
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

    public static void checkFieldsType(HashSet<Class<?>> types, Class<?> recordClass,
        List<VectorStoreRecordField> fields) {
        StringBuilder sb = new StringBuilder();
        types.forEach(t -> sb.append(t.getName()).append(" "));

        for (VectorStoreRecordField field : fields) {
            try {
                if (!types.contains(recordClass.getDeclaredField(field.getName()).getType())) {
                    throw new IllegalArgumentException(
                        "Field " + field.getName() + " must be one of: " + sb);
                }
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }
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
                keyFields.add(new VectorStoreRecordKeyField(field.getName()));
            }

            if (field.isAnnotationPresent(VectorStoreRecordDataAttribute.class)) {
                VectorStoreRecordDataAttribute dataAttribute = field
                    .getAnnotation(VectorStoreRecordDataAttribute.class);
                dataFields.add(new VectorStoreRecordDataField(field.getName(),
                    dataAttribute.hasEmbedding(), dataAttribute.embeddingFieldName()));
            }

            if (field.isAnnotationPresent(VectorStoreRecordVectorAttribute.class)) {
                vectorFields.add(new VectorStoreRecordVectorField(field.getName()));
            }
        }

        return checkFields(keyFields, dataFields, vectorFields);
    }
}
