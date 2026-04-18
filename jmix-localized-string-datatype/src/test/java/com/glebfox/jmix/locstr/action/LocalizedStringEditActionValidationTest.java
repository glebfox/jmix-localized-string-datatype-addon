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

package com.glebfox.jmix.locstr.action;

import com.glebfox.jmix.locstr.entity.LocalizedStringTestEntity;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.security.ClientDetails;
import io.jmix.core.security.SecurityContextHelper;
import io.jmix.core.security.SystemAuthenticationToken;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.SupportsValidation;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.exception.ValidationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = "jmix.core.available-locales=en,ru_RU")
@SuppressWarnings("SameParameterValue")
public class LocalizedStringEditActionValidationTest {

    @Autowired
    ApplicationContext applicationContext;
    @Autowired
    Messages messages;
    @Autowired
    MessageTools messageTools;
    @Autowired
    MetadataTools metadataTools;
    @Autowired
    Metadata metadata;

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
    void defaultFieldShouldReceiveBeanValidationAdapter() {
        TestLocalizedStringEditAction action = createAction("validationName");

        HasValueAndElement<?, String> field = action.createField(Locale.ENGLISH);

        assertThat(action.getRequestedComponentTypes()).containsExactly(TextField.class);
        assertThat(field).isInstanceOf(SupportsValidation.class);
        field.setValue("   ");
        assertThatThrownBy(() -> executeValidators(field))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void multilineDefaultFieldShouldRespectUiComponentsReplacement() {
        TestLocalizedStringEditAction action = createAction("validationName");
        action.setMultiline(true);

        HasValueAndElement<?, String> field = action.createField(Locale.ENGLISH);

        assertThat(action.getRequestedComponentTypes()).containsExactly(TextArea.class);
        assertThat(field).isInstanceOf(SupportsValidation.class);
    }

    @Test
    void fieldsShouldBeRecreatedBetweenOpenings() {
        TestLocalizedStringEditAction action = createAction("validationName");
        HasValueAndElement<?, String> requiredField = action.openField(Locale.ENGLISH);

        requiredField.setValue("   ");
        assertThatThrownBy(() -> executeValidators(requiredField))
                .isInstanceOf(ValidationException.class);

        action.setMetaPropertyPath(metaPropertyPath("validationOptional"));
        HasValueAndElement<?, String> optionalField = action.openField(Locale.ENGLISH);

        assertThat(optionalField).isNotSameAs(requiredField);
        optionalField.setValue("   ");
        assertThatCode(() -> executeValidators(optionalField))
                .doesNotThrowAnyException();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHelper.setAuthentication(initialAuthentication);
    }

    private TestLocalizedStringEditAction createAction(String propertyName) {
        TestLocalizedStringEditAction action = new TestLocalizedStringEditAction(metaPropertyPath(propertyName));
        TestUiComponents uiComponents = new TestUiComponents(applicationContext);

        action.setApplicationContext(applicationContext);
        action.setMessages(messages);
        action.setUiComponents(uiComponents);
        action.setTestUiComponents(uiComponents);
        action.setMessageTools(messageTools);
        action.setMetadataTools(metadataTools);
        return action;
    }

    private MetaPropertyPath metaPropertyPath(String propertyName) {
        return metadata.getClass(LocalizedStringTestEntity.class).getPropertyPath(propertyName);
    }

    @SuppressWarnings("unchecked")
    private void executeValidators(HasValueAndElement<?, String> field) {
        ((SupportsValidation<String>) field).executeValidators();
    }

    private static class TestUiComponents implements UiComponents {

        private final ApplicationContext applicationContext;
        private final List<Class<? extends Component>> requestedComponentTypes = new ArrayList<>();

        private TestUiComponents(ApplicationContext applicationContext) {
            this.applicationContext = applicationContext;
        }

        @SuppressWarnings("unchecked")
        @NonNull
        @Override
        public <T extends Component> T create(@NonNull Class<T> type) {
            requestedComponentTypes.add(type);

            Class<? extends Component> componentType = type;
            if (type == TextField.class) {
                componentType = TypedTextField.class;
            } else if (type == TextArea.class) {
                componentType = JmixTextArea.class;
            }

            return (T) applicationContext.getAutowireCapableBeanFactory().createBean(componentType);
        }

        private List<Class<? extends Component>> getRequestedComponentTypes() {
            return requestedComponentTypes;
        }
    }

    private static class TestLocalizedStringEditAction extends LocalizedStringEditAction {

        private MetaPropertyPath metaPropertyPath;
        private TestUiComponents testUiComponents;

        private TestLocalizedStringEditAction(MetaPropertyPath metaPropertyPath) {
            this.metaPropertyPath = metaPropertyPath;
        }

        private void setMetaPropertyPath(MetaPropertyPath metaPropertyPath) {
            this.metaPropertyPath = metaPropertyPath;
        }

        private void setTestUiComponents(TestUiComponents testUiComponents) {
            this.testUiComponents = testUiComponents;
        }

        private List<Class<? extends Component>> getRequestedComponentTypes() {
            return testUiComponents.getRequestedComponentTypes();
        }

        private HasValueAndElement<?, String> openField(Locale locale) {
            HasValueAndElement<?, String> field = getField(locale);
            field.setValue("");
            return field;
        }

        @Override
        protected MetaPropertyPath findMetaPropertyPath() {
            return metaPropertyPath;
        }

        @NonNull
        @Override
        protected String getInitialValue(@NonNull Locale locale) {
            return "";
        }
    }
}
