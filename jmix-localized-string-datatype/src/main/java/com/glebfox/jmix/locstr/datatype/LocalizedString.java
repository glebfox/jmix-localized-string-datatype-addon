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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LocalizedString implements Serializable {

    private static final TypeReference<HashMap<Locale, String>> TYPE_REFERENCE = new TypeReference<>() {
    };

    private final Map<Locale, String> values;

    public LocalizedString(Map<Locale, String> values) {
        this.values = new HashMap<>(values);
    }

    public String getValue(Locale locale) {
        return values.getOrDefault(locale, "");
    }

    public String toJson() {
        try {
            return new ObjectMapper().writeValueAsString(values);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Cannot convert to Json", e);
        }
    }

    public static LocalizedString fromJson(String json) {
        try {
            Map<Locale, String> values = new ObjectMapper().readValue(json, TYPE_REFERENCE);
            return new LocalizedString(values);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Cannot convert from Json", e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LocalizedString that = (LocalizedString) o;
        return values.equals(that.values);
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }

    @Override
    public String toString() {
        return toJson();
    }
}
