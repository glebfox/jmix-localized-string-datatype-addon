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
import com.glebfox.jmix.locstr.validation.constraints.LocalizedStringLength;
import com.glebfox.jmix.locstr.validation.constraints.LocalizedStringNotBlank;
import com.glebfox.jmix.locstr.validation.constraints.LocalizedStringNotEmpty;
import com.glebfox.jmix.locstr.validation.constraints.LocalizedStringNotNull;
import com.glebfox.jmix.locstr.validation.constraints.LocalizedStringPattern;
import com.glebfox.jmix.locstr.validation.constraints.LocalizedStringSize;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.apache.commons.lang3.LocaleUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "jmix.core.available-locales=en,ru_RU")
@SuppressWarnings({"unused", "ClassCanBeRecord"})
public class LocalizedStringConstraintsTest {

    private static final Locale LOCALE_RU = LocaleUtils.toLocale("ru_RU");

    @Autowired
    Validator validator;

    @Test
    void sizeShouldValidateEveryLocalizedValue() {
        assertValid(new SizeBean(null));
        assertValid(new SizeBean(localizedString("AB")));
        assertValid(new SizeBean(localizedString("ABCDE")));
        assertValid(new SizeBean(localizedString("AB", "ABCDE")));

        assertInvalid(new SizeBean(emptyLocalizedString()));
        assertInvalid(new SizeBean(localizedString("A")));
        assertInvalid(new SizeBean(localizedString("ABCDEF")));
        assertInvalid(new SizeBean(localizedString("AB", "A")));
    }

    @Test
    void lengthShouldValidateEveryLocalizedValue() {
        assertValid(new LengthBean(null));
        assertValid(new LengthBean(localizedString("AB")));
        assertValid(new LengthBean(localizedString("ABCDE")));
        assertValid(new LengthBean(localizedString("AB", "ABCDE")));

        assertInvalid(new LengthBean(emptyLocalizedString()));
        assertInvalid(new LengthBean(localizedString("A")));
        assertInvalid(new LengthBean(localizedString("ABCDEF")));
        assertInvalid(new LengthBean(localizedString("AB", "A")));
    }

    @Test
    void patternShouldValidateEveryLocalizedValue() {
        assertValid(new PatternBean(null));
        assertValid(new PatternBean(localizedString("ABC-12")));
        assertValid(new PatternBean(localizedString("ABC-12", "XYZ-34")));

        assertInvalid(new PatternBean(emptyLocalizedString()));
        assertInvalid(new PatternBean(localizedString("abc-12")));
        assertInvalid(new PatternBean(localizedString("ABC")));
        assertInvalid(new PatternBean(localizedString("ABC-12", "ABC")));
    }

    @Test
    void validationMessagesShouldBeLocalized() {
        Set<ConstraintViolation<Object>> violations = validator.validate(new PatternBean(localizedString("ABC")));

        assertThat(violations)
                .singleElement()
                .extracting(ConstraintViolation::getMessage)
                .asString()
                .isEqualTo("Localized value must match \"[A-Z]{3}-\\d{2}\"");
    }

    @Test
    void patternShouldSupportFlags() {
        assertValid(new CaseInsensitivePatternBean(localizedString("abc")));
        assertValid(new CaseInsensitivePatternBean(localizedString("abc", "ABC")));
    }

    @Test
    void notNullShouldRequireEveryAvailableLocaleToBeStored() {
        assertValid(new NotNullBean(localizedString("value", "")));

        assertInvalid(new NotNullBean(null));
        assertInvalid(new NotNullBean(emptyLocalizedString()));
        assertInvalid(new NotNullBean(localizedString("value")));
        assertInvalid(new NotNullBean(localizedString("value", null)));
    }

    @Test
    void notEmptyShouldRequireEveryLocalizedValueToHaveText() {
        assertValid(new NotEmptyBean(localizedString("value")));
        assertValid(new NotEmptyBean(localizedString(" ")));
        assertValid(new NotEmptyBean(localizedString("value", " ")));

        assertInvalid(new NotEmptyBean(null));
        assertInvalid(new NotEmptyBean(emptyLocalizedString()));
        assertInvalid(new NotEmptyBean(localizedString("")));
        assertInvalid(new NotEmptyBean(localizedString(null)));
        assertInvalid(new NotEmptyBean(localizedString("value", "")));
    }

    @Test
    void notBlankShouldRequireEveryLocalizedValueToHaveNonBlankText() {
        assertValid(new NotBlankBean(localizedString("value")));
        assertValid(new NotBlankBean(localizedString("value", "text")));

        assertInvalid(new NotBlankBean(null));
        assertInvalid(new NotBlankBean(emptyLocalizedString()));
        assertInvalid(new NotBlankBean(localizedString("")));
        assertInvalid(new NotBlankBean(localizedString("   ")));
        assertInvalid(new NotBlankBean(localizedString(null)));
        assertInvalid(new NotBlankBean(localizedString("value", "   ")));
    }

    @Test
    void validateValueShouldValidateSingleLocaleLocalizedString() {
        assertThat(validator.validateValue(SizeBean.class, "value", localizedString("AB")))
                .isEmpty();
        assertThat(validator.validateValue(SizeBean.class, "value", localizedString("A")))
                .hasSize(1);
        assertThat(validator.validateValue(NotNullBean.class, "value", localizedString("value")))
                .hasSize(1);
    }

    private void assertValid(Object bean) {
        assertThat(validator.validate(bean)).isEmpty();
    }

    private void assertInvalid(Object bean) {
        Set<ConstraintViolation<Object>> violations = validator.validate(bean);
        assertThat(violations).hasSize(1);
    }

    private static LocalizedString emptyLocalizedString() {
        return new LocalizedString(Map.of());
    }

    private static LocalizedString localizedString(String value) {
        Map<Locale, String> values = new HashMap<>();
        values.put(Locale.ENGLISH, value);
        return new LocalizedString(values);
    }

    private static LocalizedString localizedString(String englishValue, String russianValue) {
        Map<Locale, String> values = new HashMap<>();
        values.put(Locale.ENGLISH, englishValue);
        values.put(LOCALE_RU, russianValue);
        return new LocalizedString(values);
    }

    private static class SizeBean {

        private final LocalizedString value;

        private SizeBean(LocalizedString value) {
            this.value = value;
        }

        @LocalizedStringSize(min = 2, max = 5)
        public LocalizedString getValue() {
            return value;
        }
    }

    private static class LengthBean {

        private final LocalizedString value;

        private LengthBean(LocalizedString value) {
            this.value = value;
        }

        @LocalizedStringLength(min = 2, max = 5)
        public LocalizedString getValue() {
            return value;
        }
    }

    private static class PatternBean {

        private final LocalizedString value;

        private PatternBean(LocalizedString value) {
            this.value = value;
        }

        @LocalizedStringPattern(regexp = "[A-Z]{3}-\\d{2}")
        public LocalizedString getValue() {
            return value;
        }
    }

    private static class CaseInsensitivePatternBean {

        private final LocalizedString value;

        private CaseInsensitivePatternBean(LocalizedString value) {
            this.value = value;
        }

        @LocalizedStringPattern(
                regexp = "abc",
                flags = jakarta.validation.constraints.Pattern.Flag.CASE_INSENSITIVE)
        public LocalizedString getValue() {
            return value;
        }
    }

    private static class NotEmptyBean {

        private final LocalizedString value;

        private NotEmptyBean(LocalizedString value) {
            this.value = value;
        }

        @LocalizedStringNotEmpty
        public LocalizedString getValue() {
            return value;
        }
    }

    private static class NotNullBean {

        private final LocalizedString value;

        private NotNullBean(LocalizedString value) {
            this.value = value;
        }

        @LocalizedStringNotNull
        public LocalizedString getValue() {
            return value;
        }
    }

    private static class NotBlankBean {

        private final LocalizedString value;

        private NotBlankBean(LocalizedString value) {
            this.value = value;
        }

        @LocalizedStringNotBlank
        public LocalizedString getValue() {
            return value;
        }
    }
}
