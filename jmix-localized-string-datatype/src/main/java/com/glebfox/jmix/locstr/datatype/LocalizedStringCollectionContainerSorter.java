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

import io.jmix.core.Sort;
import io.jmix.core.comparator.EntityValuesComparator;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.model.BaseCollectionLoader;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.impl.CollectionContainerSorter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.lang.Nullable;

import java.util.Comparator;
import java.util.Locale;

public class LocalizedStringCollectionContainerSorter extends CollectionContainerSorter {

    protected final CurrentAuthentication currentAuthentication;

    public LocalizedStringCollectionContainerSorter(CollectionContainer<?> container,
                                                    @Nullable BaseCollectionLoader loader,
                                                    BeanFactory beanFactory,
                                                    CurrentAuthentication currentAuthentication) {
        super(container, loader, beanFactory);
        this.currentAuthentication = currentAuthentication;
    }

    @Override
    protected Comparator<?> createComparator(Sort.Order sortOrder, MetaClass metaClass) {
        MetaPropertyPath propertyPath = metaClass.getPropertyPath(sortOrder.getProperty());
        if (propertyPath == null) {
            throw new IllegalArgumentException("Property " + sortOrder.getProperty() + " is invalid");
        }

        if (!isLocalizedString(propertyPath)) {
            return super.createComparator(sortOrder, metaClass);
        }

        boolean asc = sortOrder.getDirection() == Sort.Direction.ASC;
        Locale locale = getCurrentLocale();
        EntityValuesComparator<String> comparator = new EntityValuesComparator<>(asc, metaClass, beanFactory);

        return Comparator.comparing(
                entity -> getLocalizedValue(entity, propertyPath, locale),
                comparator);
    }

    protected boolean isLocalizedString(MetaPropertyPath propertyPath) {
        return LocalizedString.class.equals(propertyPath.getMetaProperty().getJavaType());
    }

    @Nullable
    protected String getLocalizedValue(Object entity, MetaPropertyPath propertyPath, Locale locale) {
        Object value = EntityValues.getValueEx(entity, propertyPath);

        return value instanceof LocalizedString localizedString
                ? localizedString.getValue(locale)
                : null;
    }

    protected Locale getCurrentLocale() {
        return currentAuthentication.isSet()
                ? currentAuthentication.getLocale()
                : Locale.getDefault();
    }
}
