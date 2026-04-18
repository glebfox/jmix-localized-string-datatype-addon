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

package com.glebfox.jmix.locstr.validation.constraints.impl;

import com.glebfox.jmix.locstr.datatype.LocalizedString;

import java.util.Collection;
import java.util.List;

final class LocalizedStringValidation {

    private LocalizedStringValidation() {
    }

    static Collection<String> values(LocalizedString localizedString) {
        Collection<String> values = localizedString.getValues().values();

        return values.isEmpty()
                ? List.of("")
                : values.stream()
                .map(value -> value != null ? value : "")
                .toList();
    }
}
