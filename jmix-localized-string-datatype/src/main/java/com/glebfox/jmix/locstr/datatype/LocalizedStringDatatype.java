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

import com.google.common.collect.ImmutableMap;
import io.jmix.core.metamodel.annotation.DatatypeDef;
import io.jmix.core.metamodel.annotation.Ddl;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.security.CurrentAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

import java.text.ParseException;
import java.util.Locale;

/**
 * Jmix datatype that formats and parses {@link LocalizedString} values.
 * <p>
 * Formatting returns the value for the current user locale. Parsing creates a
 * localized string containing the parsed text for the current user locale.
 */
@DatatypeDef(
        id = "localizedString",
        javaClass = LocalizedString.class,
        defaultForClass = true,
        value = "locstr_LocalizedStringDatatype"
)
@Ddl("CLOB")
public class LocalizedStringDatatype implements Datatype<LocalizedString> {

    @Autowired
    protected CurrentAuthentication currentAuthentication;

    /**
     * Formats a localized string for the current user locale.
     *
     * @param value value to format
     * @return localized text, or an empty string if the value is not a
     * {@link LocalizedString}
     */
    @Override
    public String format(@Nullable Object value) {
        return format(value, currentAuthentication.getLocale());
    }

    /**
     * Formats a localized string for the given locale.
     *
     * @param value  value to format
     * @param locale locale whose value should be returned
     * @return localized text, or an empty string if the value is not a
     * {@link LocalizedString}
     */
    @Override
    public String format(@Nullable Object value, Locale locale) {
        return value instanceof LocalizedString localizedString
                ? localizedString.getValue(locale)
                : "";
    }

    /**
     * Parses text as a localized string for the current user locale.
     *
     * @param value text to parse
     * @return localized string containing the text for the current user locale,
     * or {@code null} if the value is {@code null}
     * @throws ParseException never thrown by this implementation
     */
    @Nullable
    @Override
    public LocalizedString parse(@Nullable String value) throws ParseException {
        return parse(value, currentAuthentication.getLocale());
    }

    /**
     * Parses text as a localized string for the given locale.
     *
     * @param value  text to parse
     * @param locale locale to associate with the parsed text
     * @return localized string containing the text for the given locale, or
     * {@code null} if the value is {@code null}
     * @throws ParseException never thrown by this implementation
     */
    @Nullable
    @Override
    public LocalizedString parse(@Nullable String value, Locale locale) throws ParseException {
        if (value == null) {
            return null;
        }

        return new LocalizedString(ImmutableMap.of(locale, value));
    }
}
