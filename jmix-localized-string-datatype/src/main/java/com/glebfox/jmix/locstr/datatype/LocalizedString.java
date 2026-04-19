/*
 * Copyright 2024 Gleb Gorelov.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.glebfox.jmix.locstr.datatype;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Serializable value object that stores text values by locale.
 * <p>
 * The object is immutable from the caller's point of view: the map passed to the
 * constructor is copied and no mutable map is exposed.
 */
public class LocalizedString implements Serializable {

    private static final TypeReference<HashMap<Locale, String>> TYPE_REFERENCE = new TypeReference<>() {
    };

    private final Map<Locale, String> values;

    /**
     * Creates a localized string from the provided locale-to-value map.
     *
     * @param values localized values keyed by locale
     */
    public LocalizedString(Map<Locale, String> values) {
        this.values = new HashMap<>(values);
    }

    /**
     * Returns the value for the given locale.
     *
     * @param locale locale to look up
     * @return localized value, or an empty string if the locale is not present
     */
    public String getValue(Locale locale) {
        return values.getOrDefault(locale, "");
    }

    /**
     * Returns all stored localized values.
     *
     * @return unmodifiable map of locale to localized value
     */
    public Map<Locale, String> getValues() {
        return Collections.unmodifiableMap(values);
    }

    /**
     * Serializes this object to JSON.
     *
     * @return JSON representation of the locale-to-value map
     * @throws RuntimeException if the value cannot be serialized
     */
    public String toJson() {
        try {
            return new ObjectMapper().writeValueAsString(values);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Cannot convert to Json", e);
        }
    }

    /**
     * Deserializes a localized string from JSON created by {@link #toJson()}.
     *
     * @param json JSON representation of the locale-to-value map
     * @return localized string instance
     * @throws RuntimeException if the JSON value cannot be deserialized
     */
    public static LocalizedString fromJson(String json) {
        try {
            Map<Locale, String> values = new ObjectMapper().readValue(json, TYPE_REFERENCE);
            return new LocalizedString(values);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Cannot convert from Json", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LocalizedString that = (LocalizedString) o;
        return values.equals(that.values);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return values.hashCode();
    }

    /**
     * Returns the JSON representation of this localized string.
     *
     * @return JSON representation of the locale-to-value map
     */
    @Override
    public String toString() {
        return toJson();
    }
}
