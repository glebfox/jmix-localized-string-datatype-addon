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

package com.glebfox.jmix.locstr.action;

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import org.springframework.lang.Nullable;

import java.util.Locale;

/**
 * Contains information used when creating a field for
 * {@link com.glebfox.jmix.locstr.action.LocalizedStringEditAction}.
 *
 * @param locale           the locale for which a field is created
 * @param metaPropertyPath an object representing a relative path to a property
 *                         from certain {@link MetaClass}, can be {@code null}
 */
public record FieldGenerationContext(Locale locale, @Nullable MetaPropertyPath metaPropertyPath) {
}