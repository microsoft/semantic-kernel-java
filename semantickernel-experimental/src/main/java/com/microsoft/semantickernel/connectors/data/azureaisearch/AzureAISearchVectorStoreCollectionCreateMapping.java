// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.connectors.data.azureaisearch;

import com.azure.search.documents.indexes.models.ExhaustiveKnnAlgorithmConfiguration;
import com.azure.search.documents.indexes.models.ExhaustiveKnnParameters;
import com.azure.search.documents.indexes.models.HnswAlgorithmConfiguration;
import com.azure.search.documents.indexes.models.HnswParameters;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.azure.search.documents.indexes.models.VectorSearchAlgorithmConfiguration;
import com.azure.search.documents.indexes.models.VectorSearchAlgorithmMetric;
import com.azure.search.documents.indexes.models.VectorSearchProfile;
import com.microsoft.semantickernel.data.vectorstorage.definition.DistanceFunction;
import com.microsoft.semantickernel.data.vectorstorage.definition.IndexKind;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordDataField;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordKeyField;
import com.microsoft.semantickernel.data.vectorstorage.definition.VectorStoreRecordVectorField;
import com.microsoft.semantickernel.exceptions.SKException;

import java.time.OffsetDateTime;
import java.util.List;
import javax.annotation.Nonnull;

class AzureAISearchVectorStoreCollectionCreateMapping {

    private static String getVectorSearchProfileName(VectorStoreRecordVectorField vectorField) {
        return vectorField.getEffectiveStorageName() + "Profile";
    }

    private static String getAlgorithmConfigName(VectorStoreRecordVectorField vectorField) {
        return vectorField.getEffectiveStorageName() + "AlgorithmConfig";
    }

    private static VectorSearchAlgorithmMetric getAlgorithmMetric(
        @Nonnull VectorStoreRecordVectorField vectorField) {
        if (vectorField.getDistanceFunction() == DistanceFunction.UNDEFINED) {
            return VectorSearchAlgorithmMetric.COSINE;
        }

        switch (vectorField.getDistanceFunction()) {
            case COSINE_SIMILARITY:
                return VectorSearchAlgorithmMetric.COSINE;
            case DOT_PRODUCT:
                return VectorSearchAlgorithmMetric.DOT_PRODUCT;
            case EUCLIDEAN_DISTANCE:
                return VectorSearchAlgorithmMetric.EUCLIDEAN;
            default:
                throw new SKException(
                    "Unsupported distance function: " + vectorField.getDistanceFunction());
        }
    }

    private static VectorSearchAlgorithmConfiguration getAlgorithmConfig(
        @Nonnull VectorStoreRecordVectorField vectorField) {
        if (vectorField.getIndexKind() == IndexKind.UNDEFINED) {
            return new HnswAlgorithmConfiguration(getAlgorithmConfigName(vectorField))
                .setParameters(new HnswParameters().setMetric(getAlgorithmMetric(vectorField)));
        }

        switch (vectorField.getIndexKind()) {
            case HNSW:
                return new HnswAlgorithmConfiguration(getAlgorithmConfigName(vectorField))
                    .setParameters(new HnswParameters().setMetric(getAlgorithmMetric(vectorField)));
            case FLAT:
                return new ExhaustiveKnnAlgorithmConfiguration(getAlgorithmConfigName(vectorField))
                    .setParameters(
                        new ExhaustiveKnnParameters().setMetric(getAlgorithmMetric(vectorField)));
            default:
                throw new SKException(
                    "Unsupported index kind: " + vectorField.getIndexKind());
        }
    }

    public static SearchField mapKeyField(VectorStoreRecordKeyField keyField) {
        return new SearchField(keyField.getEffectiveStorageName(), SearchFieldDataType.STRING)
            .setKey(true)
            .setFilterable(true);
    }

    public static SearchField mapDataField(VectorStoreRecordDataField dataField) {
        if (dataField.getFieldType() == null) {
            throw new SKException(
                "Field type is required: " + dataField.getEffectiveStorageName());
        }

        return new SearchField(dataField.getEffectiveStorageName(),
            getSearchFieldDataType(dataField.getFieldType()))
            .setFilterable(dataField.isFilterable())
            .setSearchable(dataField.isFullTextSearchable());
    }

    public static SearchField mapVectorField(VectorStoreRecordVectorField vectorField) {
        return new SearchField(vectorField.getEffectiveStorageName(),
            SearchFieldDataType.collection(SearchFieldDataType.SINGLE))
            .setSearchable(true)
            .setVectorSearchDimensions(vectorField.getDimensions())
            .setVectorSearchProfileName(getVectorSearchProfileName(vectorField));
    }

    public static void updateVectorSearchParameters(
        List<VectorSearchAlgorithmConfiguration> algorithms,
        List<VectorSearchProfile> profiles,
        VectorStoreRecordVectorField vectorField) {
        if (vectorField.getDimensions() <= 0) {
            throw new SKException("Vector field dimensions must be greater than 0");
        }

        algorithms.add(getAlgorithmConfig(vectorField));
        profiles.add(new VectorSearchProfile(
            getVectorSearchProfileName(vectorField), getAlgorithmConfigName(vectorField)));
    }

    public static SearchFieldDataType getSearchFieldDataType(Class<?> fieldType) {
        if (fieldType == String.class) {
            return SearchFieldDataType.STRING;
        } else if (fieldType == Integer.class || fieldType == int.class) {
            return SearchFieldDataType.INT32;
        } else if (fieldType == Long.class || fieldType == long.class) {
            return SearchFieldDataType.INT64;
        } else if (fieldType == Float.class || fieldType == float.class) {
            return SearchFieldDataType.DOUBLE;
        } else if (fieldType == Double.class || fieldType == double.class) {
            return SearchFieldDataType.DOUBLE;
        } else if (fieldType == Boolean.class || fieldType == boolean.class) {
            return SearchFieldDataType.BOOLEAN;
        } else if (fieldType == OffsetDateTime.class) {
            return SearchFieldDataType.DATE_TIME_OFFSET;
        } else {
            throw new SKException("Unsupported field type: " + fieldType.getName());
        }
    }
}
