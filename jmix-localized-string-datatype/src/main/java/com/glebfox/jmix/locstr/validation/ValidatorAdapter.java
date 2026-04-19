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

package com.glebfox.jmix.locstr.validation;

import org.springframework.lang.Nullable;

import java.util.Locale;

/**
 * Adapts an add-on {@link Validator} to the Jmix Flow UI validator contract.
 *
 * @param validator validator to invoke
 * @param locale    locale associated with the validated field
 */
public record ValidatorAdapter(Validator validator, Locale locale)
        implements io.jmix.flowui.component.validation.Validator<String> {

    /**
     * Validates a field value by wrapping it into a {@link ValidationContext}.
     *
     * @param value field value to validate, may be {@code null}
     */
    @Override
    public void accept(@Nullable String value) {
        validator.accept(new ValidationContext(locale, value));
    }
}
