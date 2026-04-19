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

import com.glebfox.jmix.locstr.LocstrProperties;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.data.impl.DefaultJpqlSortExpressionProvider;
import io.jmix.data.persistence.DbmsSpecifics;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.function.Supplier;

/**
 * JPQL sort expression provider that sorts {@link LocalizedString} attributes
 * by the value for the current locale.
 * <p>
 * If the current database is not supported by the add-on expression builder,
 * sorting falls back to the default Jmix implementation.
 */
@Primary
@Component("locstr_LocalizedStringJpqlSortExpressionProvider")
@ConditionalOnProperty(
        prefix = LocstrProperties.SORTING_DATABASE_PREFIX,
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class LocalizedStringJpqlSortExpressionProvider extends DefaultJpqlSortExpressionProvider {

    protected final CurrentAuthentication currentAuthentication;
    protected final DbmsSpecifics dbmsSpecifics;

    /**
     * Creates the provider.
     *
     * @param currentAuthentication current authentication used to resolve the
     *                              locale
     * @param dbmsSpecifics         Jmix database-specific metadata
     */
    public LocalizedStringJpqlSortExpressionProvider(CurrentAuthentication currentAuthentication,
                                                     DbmsSpecifics dbmsSpecifics) {
        this.currentAuthentication = currentAuthentication;
        this.dbmsSpecifics = dbmsSpecifics;
    }

    /**
     * Returns a JPQL sort expression for datatype attributes.
     *
     * @param metaPropertyPath entity property path to sort by
     * @param sortDirectionAsc {@code true} for ascending sort direction
     * @return locale-aware sort expression for localized strings, or the
     * default Jmix expression for other datatypes
     */
    @Override
    public String getDatatypeSortExpression(MetaPropertyPath metaPropertyPath, boolean sortDirectionAsc) {
        return sortExpression(
                metaPropertyPath,
                () -> super.getDatatypeSortExpression(metaPropertyPath, sortDirectionAsc));
    }

    /**
     * Returns a JPQL sort expression for LOB attributes.
     *
     * @param metaPropertyPath entity property path to sort by
     * @param sortDirectionAsc {@code true} for ascending sort direction
     * @return locale-aware sort expression for localized strings, or the
     * default Jmix expression for other LOB values
     */
    @Override
    public String getLobSortExpression(MetaPropertyPath metaPropertyPath, boolean sortDirectionAsc) {
        return sortExpression(
                metaPropertyPath,
                () -> super.getLobSortExpression(metaPropertyPath, sortDirectionAsc));
    }

    protected String sortExpression(MetaPropertyPath metaPropertyPath,
                                    Supplier<String> defaultSortExpressionSupplier) {
        return isLocalizedString(metaPropertyPath)
                ? localizedStringSortExpression(metaPropertyPath, defaultSortExpressionSupplier)
                : defaultSortExpressionSupplier.get();
    }

    protected boolean isLocalizedString(MetaPropertyPath metaPropertyPath) {
        return LocalizedString.class.equals(metaPropertyPath.getMetaProperty().getJavaType());
    }

    protected String localizedStringSortExpression(MetaPropertyPath metaPropertyPath,
                                                   Supplier<String> defaultSortExpressionSupplier) {
        String propertyExpression = String.format("{E}.%s", metaPropertyPath);
        String dbmsType = getDbmsType(metaPropertyPath);

        if (isHsql(dbmsType)) {
            return hsqlStringSortExpression(propertyExpression);
        }

        String quoteFunction = getQuoteFunction(dbmsType);
        if (quoteFunction == null) {
            return defaultSortExpressionSupplier.get();
        }

        String localeKey = getCurrentLocaleKey();

        return jsonTextValueExpression(propertyExpression, localeKey, quoteFunction);
    }

    @Nullable
    protected String getQuoteFunction(String dbmsType) {
        return switch (dbmsType) {
            case "h2", "mysql", "sqlServer" -> "char";
            case "oracle", "postgresql" -> "chr";
            default -> null;
        };
    }

    protected boolean isHsql(String dbmsType) {
        return "hsql".equals(dbmsType);
    }

    protected String hsqlStringSortExpression(String propertyExpression) {
        return String.format("function('to_char', %s)", propertyExpression);
    }

    protected String jsonTextValueExpression(String propertyExpression, String localeKey, String quoteFunction) {
        String localizedValuePrefix = jsonValuePrefixExpression(localeKey, quoteFunction);
        String localePosition = String.format("locate(%s, %s)", localizedValuePrefix, propertyExpression);
        String valueStart = String.format("(%s + %s)", localePosition, localeKey.length() + 4);
        String valueEnd = String.format("locate(function('%s', 34), %s, %s)",
                quoteFunction,
                propertyExpression,
                valueStart);

        return String.format(
                "case when %s > 0 then substring(%s, %s, %s - %s) else '' end",
                localePosition,
                propertyExpression,
                valueStart,
                valueEnd,
                valueStart);
    }

    protected String jsonValuePrefixExpression(String localeKey, String quoteFunction) {
        String quote = String.format("function('%s', 34)", quoteFunction);
        String quotedLocaleKey = concat(concat(quote, jpqlString(localeKey)), quote);

        return concat(concat(quotedLocaleKey, "':'"), quote);
    }

    protected String concat(String firstExpression, String secondExpression) {
        return String.format("concat(%s, %s)", firstExpression, secondExpression);
    }

    protected String getCurrentLocaleKey() {
        Locale locale = currentAuthentication.isSet()
                ? currentAuthentication.getLocale()
                : Locale.getDefault();

        return locale.toString();
    }

    protected String getDbmsType(MetaPropertyPath metaPropertyPath) {
        return dbmsSpecifics.getDbmsFeatures(metaPropertyPath.getMetaClass().getStore().getName())
                .getTypeAndVersion();
    }

    protected String jpqlString(String value) {
        return "'" + value.replace("'", "''") + "'";
    }
}
