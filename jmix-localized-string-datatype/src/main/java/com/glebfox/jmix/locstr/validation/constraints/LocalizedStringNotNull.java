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

import com.glebfox.jmix.locstr.validation.constraints.impl.LocalizedStringNotNullValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Checks that a localized string contains a non-null value for every configured application locale.
 * <p>
 * If Jmix core properties are not available, the constraint validates against
 * the locales already stored in the value.
 */
@Documented
@Constraint(validatedBy = LocalizedStringNotNullValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE,
        ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RUNTIME)
@Repeatable(LocalizedStringNotNull.List.class)
public @interface LocalizedStringNotNull {

    /**
     * Defines the validation error message template.
     *
     * @return validation error message template
     */
    String message() default "{msg://validation.constraints.LocalizedStringNotNull.message}";

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
     * Defines several {@link LocalizedStringNotNull} constraints on the same
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
        LocalizedStringNotNull[] value();
    }
}
