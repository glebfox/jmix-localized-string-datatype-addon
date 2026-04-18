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

package com.glebfox.jmix.locstr;

import com.glebfox.jmix.locstr.datatype.LocalizedStringJpqlSortExpressionProvider;
import com.glebfox.jmix.locstr.datatype.LocalizedStringSorterFactory;
import io.jmix.data.persistence.JpqlSortExpressionProvider;
import io.jmix.flowui.model.SorterFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = LocstrTestConfiguration.class)
public class LocalizedStringSortingBeansTest {

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    JpqlSortExpressionProvider jpqlSortExpressionProvider;

    @Autowired
    SorterFactory sorterFactory;

    @Autowired
    LocstrProperties locstrProperties;

    @DynamicPropertySource
    static void disableSortingBeans(DynamicPropertyRegistry registry) {
        registry.add(LocstrProperties.SORTING_DATABASE_PREFIX + ".enabled", () -> false);
        registry.add(LocstrProperties.SORTING_IN_MEMORY_PREFIX + ".enabled", () -> false);
    }

    @Test
    void sortingBeansShouldBeDisabledWithProperties() {
        assertThat(locstrProperties.getSorting().getDatabase().isEnabled())
                .isFalse();
        assertThat(locstrProperties.getSorting().getInMemory().isEnabled())
                .isFalse();

        assertThat(applicationContext.containsBean("locstr_LocalizedStringJpqlSortExpressionProvider"))
                .isFalse();
        assertThat(applicationContext.containsBean("locstr_LocalizedStringSorterFactory"))
                .isFalse();

        assertThat(jpqlSortExpressionProvider)
                .isNotInstanceOf(LocalizedStringJpqlSortExpressionProvider.class);
        assertThat(sorterFactory)
                .isNotInstanceOf(LocalizedStringSorterFactory.class);
    }
}
