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
import com.glebfox.jmix.locstr.validation.constraints.LocalizedStringSize;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Bean Validation validator for {@link LocalizedStringSize}.
 */
public class LocalizedStringSizeValidator implements ConstraintValidator<LocalizedStringSize, LocalizedString> {

    protected int min;
    protected int max;

    @Override
    public void initialize(LocalizedStringSize constraintAnnotation) {
        min = constraintAnnotation.min();
        max = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(LocalizedString value, ConstraintValidatorContext context) {
        return value == null
                || LocalizedStringValidation.values(value).stream()
                .allMatch(this::isValid);
    }

    protected boolean isValid(String value) {
        int length = value.length();
        return length >= min && length <= max;
    }
}
