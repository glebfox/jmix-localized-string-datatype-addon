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

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.lang.Nullable;

@Converter(autoApply = true)
public class LocalizedStringConverter implements AttributeConverter<LocalizedString, String> {

    @Nullable
    @Override
    public String convertToDatabaseColumn(@Nullable LocalizedString localizedString) {
        return localizedString != null
                ? localizedString.toJson()
                : null;
    }

    @Nullable
    @Override
    public LocalizedString convertToEntityAttribute(@Nullable String dbData) {
        return dbData != null
                ? LocalizedString.fromJson(dbData)
                : null;
    }
}
