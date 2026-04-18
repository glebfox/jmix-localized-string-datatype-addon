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

import com.glebfox.jmix.locstr.entity.LocalizedStringTestEntity;
import com.glebfox.jmix.locstr.validation.LocalizedStringBeanPropertyValidatorAdapter;
import io.jmix.core.security.ClientDetails;
import io.jmix.core.security.SecurityContextHelper;
import io.jmix.core.security.SystemAuthenticationToken;
import io.jmix.flowui.component.validation.bean.BeanPropertyValidator;
import io.jmix.flowui.exception.ValidationException;
import org.apache.commons.lang3.LocaleUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
public class LocalizedStringBeanPropertyValidatorAdapterTest {

    private static final Locale LOCALE_RU = LocaleUtils.toLocale("ru_RU");

    @Autowired
    ApplicationContext applicationContext;

    Authentication initialAuthentication;

    @BeforeEach
    void setUp() {
        initialAuthentication = SecurityContextHelper.getAuthentication();

        SystemAuthenticationToken authentication =
                new SystemAuthenticationToken("test-user", List.of());
        authentication.setDetails(ClientDetails.builder().locale(Locale.ENGLISH).build());

        SecurityContextHelper.setAuthentication(authentication);
    }

    @Test
    void shouldValidateEachLocaleFieldIndependently() {
        LocalizedStringBeanPropertyValidatorAdapter englishAdapter =
                new LocalizedStringBeanPropertyValidatorAdapter(beanPropertyValidator(), Locale.ENGLISH);
        LocalizedStringBeanPropertyValidatorAdapter russianAdapter =
                new LocalizedStringBeanPropertyValidatorAdapter(beanPropertyValidator(), LOCALE_RU);

        assertThatThrownBy(() -> englishAdapter.accept("   "))
                .isInstanceOf(ValidationException.class);
        assertThatCode(() -> russianAdapter.accept("Значение"))
                .doesNotThrowAnyException();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHelper.setAuthentication(initialAuthentication);
    }

    private BeanPropertyValidator beanPropertyValidator() {
        return applicationContext.getBean(
                BeanPropertyValidator.class,
                LocalizedStringTestEntity.class,
                "validationName");
    }
}
