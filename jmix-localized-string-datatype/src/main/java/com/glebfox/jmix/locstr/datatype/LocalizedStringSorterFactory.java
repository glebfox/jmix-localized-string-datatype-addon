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
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.flowui.model.BaseCollectionLoader;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.Sorter;
import io.jmix.flowui.model.SorterFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Sorter factory that installs locale-aware in-memory sorting for
 * {@link LocalizedString} attributes.
 */
@Primary
@Component("locstr_LocalizedStringSorterFactory")
@ConditionalOnProperty(
        prefix = LocstrProperties.SORTING_IN_MEMORY_PREFIX,
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class LocalizedStringSorterFactory extends SorterFactory {

    protected final CurrentAuthentication currentAuthentication;

    /**
     * Creates the sorter factory.
     *
     * @param currentAuthentication current authentication used by created
     *                              sorters to resolve the locale
     */
    public LocalizedStringSorterFactory(CurrentAuthentication currentAuthentication) {
        this.currentAuthentication = currentAuthentication;
    }

    /**
     * Creates a collection container sorter with localized string support.
     *
     * @param container collection container to sort
     * @param loader    optional collection loader used by the sorter
     * @return sorter that handles localized strings using the current locale
     */
    @Override
    public Sorter createCollectionContainerSorter(CollectionContainer<?> container,
                                                  @Nullable BaseCollectionLoader loader) {
        return new LocalizedStringCollectionContainerSorter(
                container,
                loader,
                beanFactory,
                currentAuthentication);
    }
}
