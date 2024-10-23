// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.contextvariables.converters;

import static com.microsoft.semantickernel.contextvariables.ContextVariableTypes.convert;

import com.microsoft.semantickernel.contextvariables.ContextVariable;
import com.microsoft.semantickernel.contextvariables.ContextVariableTypeConverter;
import com.microsoft.semantickernel.contextvariables.ContextVariableTypes;
import javax.annotation.Nullable;

/**
 * A {@link ContextVariableTypeConverter} for {@code java.lang.String} variables. Use
 * {@code ContextVariableTypes.getGlobalVariableTypeForClass(String.class)} to get an instance of
 * this class.
 *
 * @see ContextVariableTypes#getGlobalVariableTypeForClass(Class)
 */
public class StringVariableContextVariableTypeConverter extends
    ContextVariableTypeConverter<String> {

    /**
     * Creates a new instance of the {@link StringVariableContextVariableTypeConverter} class.
     */
    public StringVariableContextVariableTypeConverter() {
        super(
            String.class,
            StringVariableContextVariableTypeConverter::convertToString,
            ContextVariableTypeConverter::escapeXmlString,
            s -> s);
    }

    /**
     * Converts the specified object to a string.
     * Has special handling for {@link ContextVariable} objects and 
     * for objects that look like an object reference
     * @param s the object to convert
     * @return the string representation of the object, or {@code null} 
     * if the object cannot be converted to a string or is an object reference. 
     */
    @Nullable
    public static String convertToString(@Nullable Object s) {
        String converted = convert(s, String.class);
        if (converted != null) {
            return converted;
        }

        if (s instanceof ContextVariable) {
            s = ((ContextVariable<?>) s).getValue();
        }

        if (s != null) {
            String str = s.toString();
            // ignore if this looks like an object reference
            if (!str.matches(".*@[a-f0-9]+$")) {
                return str;
            }
        }
        return null;
    }
}
