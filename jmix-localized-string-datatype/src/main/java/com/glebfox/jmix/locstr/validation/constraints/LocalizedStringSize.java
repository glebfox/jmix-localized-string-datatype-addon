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

package com.glebfox.jmix.locstr.validation.constraints;

import com.glebfox.jmix.locstr.validation.constraints.impl.LocalizedStringSizeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Checks that every localized value has a length between the specified boundaries.
 * <p>
 * {@code null} localized strings are valid. Stored {@code null} localized
 * values are validated as empty strings.
 */
@Documented
@Constraint(validatedBy = LocalizedStringSizeValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE,
        ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RUNTIME)
@Repeatable(LocalizedStringSize.List.class)
public @interface LocalizedStringSize {

    /**
     * Defines the validation error message template.
     *
     * @return validation error message template
     */
    String message() default "{msg://validation.constraints.LocalizedStringSize.message}";

    /**
     * Defines validation groups for this constraint.
     *
     * @return validation groups
     */
    Class<?>[] groups() default {};

    /**
     * Defines payload objects attached to this constraint.
     *
     * @return constraint payload
     */
    Class<? extends Payload>[] payload() default {};

    /**
     * Defines the minimum accepted localized value length.
     *
     * @return minimum accepted localized value length
     */
    int min() default 0;

    /**
     * Defines the maximum accepted localized value length.
     *
     * @return maximum accepted localized value length
     */
    int max() default Integer.MAX_VALUE;

    /**
     * Defines several {@link LocalizedStringSize} constraints on the same
     * element.
     */
    @Documented
    @Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE,
            ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
    @Retention(RUNTIME)
    @interface List {

        /**
         * Returns constraints to apply.
         *
         * @return constraints to apply
         */
        LocalizedStringSize[] value();
    }
}
