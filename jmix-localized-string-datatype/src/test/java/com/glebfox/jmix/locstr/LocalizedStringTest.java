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

import com.glebfox.jmix.locstr.datatype.LocalizedString;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.LocaleUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class LocalizedStringTest {

    private static final Locale LOCALE_RU = LocaleUtils.toLocale("ru_RU");

    @Test
    void typeTest() {
        LocalizedString localizedString = new LocalizedString(
                ImmutableMap.of(
                        Locale.ENGLISH, "en",
                        LOCALE_RU, "ru"
                )
        );

        assertThat(localizedString.getValue(Locale.ENGLISH)).isEqualTo("en");
        assertThat(localizedString.getValue(LOCALE_RU)).isEqualTo("ru");

        String json = localizedString.toJson();
        assertThat(json).isNotNull();

        localizedString = LocalizedString.fromJson(json);
        assertThat(localizedString.getValue(Locale.ENGLISH)).isEqualTo("en");
        assertThat(localizedString.getValue(LOCALE_RU)).isEqualTo("ru");
    }
}
