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

import com.glebfox.jmix.locstr.validation.constraints.impl.LocalizedStringNotBlankValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Checks that every localized value contains at least one non-whitespace character.
 * <p>
 * A {@code null} localized string is invalid. A stored {@code null} localized
 * value is validated as a blank string.
 */
@Documented
@Constraint(validatedBy = LocalizedStringNotBlankValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE,
        ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RUNTIME)
@Repeatable(LocalizedStringNotBlank.List.class)
public @interface LocalizedStringNotBlank {

    /**
     * Defines the validation error message template.
     *
     * @return validation error message template
     */
    String message() default "{msg://validation.constraints.LocalizedStringNotBlank.message}";

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
     * Defines several {@link LocalizedStringNotBlank} constraints on the same
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
        LocalizedStringNotBlank[] value();
    }
}
