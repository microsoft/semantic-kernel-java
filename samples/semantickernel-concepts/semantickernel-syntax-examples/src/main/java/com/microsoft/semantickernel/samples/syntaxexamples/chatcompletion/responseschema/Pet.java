// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.samples.syntaxexamples.chatcompletion.responseschema;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Pet {

    private final String name;
    private final AnimalType type;
    private final int age;
    private final Weight weight;

    public static enum AnimalType {
        CAT, DOG, FISH
    }

    public static enum WeightUnit {
        KG, LB
    }

    public static class Weight {

        private final double value;
        private final WeightUnit unit;

        @JsonCreator
        public Weight(
            @JsonProperty("value") double value,
            @JsonProperty("unit") WeightUnit unit) {
            this.value = value;
            this.unit = unit;
        }

        public double getValue() {
            return value;
        }

        public WeightUnit getUnit() {
            return unit;
        }
    }

    @JsonCreator
    public Pet(
        @JsonProperty("name") String name,
        @JsonProperty("type") AnimalType type,
        @JsonProperty("age") int age,
        @JsonProperty("weight") Weight weight) {
        this.name = name;
        this.type = type;
        this.age = age;
        this.weight = weight;
    }

    public String getName() {
        return name;
    }

    public AnimalType getType() {
        return type;
    }

    public int getAge() {
        return age;
    }

    public Weight getWeight() {
        return weight;
    }
}