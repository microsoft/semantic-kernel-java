// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.services.textembedding;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Represents a strongly typed vector of numeric data. */
@SuppressFBWarnings("SING_SINGLETON_HAS_NONPRIVATE_CONSTRUCTOR") // This class is not a singleton
public class Embedding {

    // vector is immutable!
    private final List<Float> vector;

    private static final Embedding EMPTY = new Embedding();

    /**
     * Returns an empty {@code Embedding} instance.
     * @return An empty {@code Embedding} instance.
     */
    public static Embedding empty() {
        return EMPTY;
    }

    /** Initializes a new instance of the Embedding class. */
    public Embedding() {
        this.vector = Collections.emptyList();
    }

    /**
     * Initializes a new instance of the Embedding class that contains numeric elements copied from
     * the specified collection
     *
     * @param vector The collection whose elements are copied to the new Embedding
     */
    public Embedding(@Nonnull List<Float> vector) {
        Objects.requireNonNull(vector);
        this.vector = Collections.unmodifiableList(vector);
    }

    /**
     * Initializes a new instance of the Embedding class that contains numeric elements copied from
     * the specified array
     *
     * @param vector The array whose elements are copied to the new Embedding
     */
    public Embedding(@Nonnull float[] vector) {
        Objects.requireNonNull(vector);
        List<Float> list = new ArrayList<>(vector.length);
        for (float f : vector) {
            list.add(f);
        }
        this.vector = Collections.unmodifiableList(list);
    }

    /**
     * Return the embedding vector as a read-only list.
     *
     * @return The embedding vector as a read-only list.
     */
    public List<Float> getVector() {
        return Collections.unmodifiableList(this.vector);
    }
}
