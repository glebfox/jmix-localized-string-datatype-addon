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

import com.glebfox.jmix.locstr.datatype.LocalizedString;
import com.glebfox.jmix.locstr.validation.Validator;
import com.glebfox.jmix.locstr.validation.ValidatorAdapter;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.dom.PropertyChangeEvent;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.CoreProperties;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.DialogAction;
import io.jmix.flowui.action.valuepicker.PickerAction;
import io.jmix.flowui.component.HasRequired;
import io.jmix.flowui.component.PickerComponent;
import io.jmix.flowui.component.SupportsValidation;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.data.EntityValueSource;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.kit.action.ActionVariant;
import io.jmix.flowui.kit.component.ComponentUtils;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Opens a dialog that edits a localized string value represented by the {@link LocalizedString} datatype.
 */
@ActionType(LocalizedStringEditAction.ID)
public class LocalizedStringEditAction
        extends PickerAction<LocalizedStringEditAction, PickerComponent<LocalizedString>, LocalizedString> {

    public static final String ID = "value_localizedStringEdit";

    protected ApplicationContext applicationContext;
    protected Dialogs dialogs;
    protected Messages messages;
    protected UiComponents uiComponents;
    protected MessageTools messageTools;

    protected Dialog dialog;
    protected Button saveButton;
    protected Button cancelButton;

    protected LinkedHashMap<Locale, String> availableLocales;
    protected Cache<Locale, HasValueAndElement<?, String>> fieldCache;
    protected List<Validator> validators;

    protected Boolean multiline;
    protected String multilineFieldHeight = "var(--localized-string-editor-multiline-field-height, 6.5em)";
    protected String multilineFieldMinHeight;
    protected String multilineFieldMaxHeight;

    protected boolean hasUnsavedChanges;
    protected boolean checkForUnsavedChanges = true;

    protected ValueProvider<FieldGenerationContext, HasValueAndElement<?, String>> fieldProvider;

    public LocalizedStringEditAction() {
        this(ID);
    }

    public LocalizedStringEditAction(String id) {
        super(id);

        initAction();
    }

    protected void initAction() {
        this.icon = ComponentUtils.convertToIcon(VaadinIcon.GLOBE);
        initDialog();
    }

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;

        CoreProperties coreProperties = applicationContext.getBean(CoreProperties.class);
        MessageTools messageTools = applicationContext.getBean(MessageTools.class);
        availableLocales = coreProperties.getAvailableLocales().stream()
                .collect(Collectors.toMap(Function.identity(), messageTools::getLocaleDisplayName,
                        (s1, s2) -> s1, LinkedHashMap::new));
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
        this.text = messages.getMessage("actions.localizedStringEdit.description");

        String title = messages.getMessage("actions.localizedStringEdit.editor.title");
        dialog.setHeaderTitle(title);
        dialog.getElement().setAttribute("aria-label", title);
        saveButton.setText(messages.getMessage("actions.Save"));
        cancelButton.setText(messages.getMessage("actions.Cancel"));
    }

    @Autowired
    public void setUiComponents(UiComponents uiComponents) {
        this.uiComponents = uiComponents;
    }

    @Autowired
    public void setMessageTools(MessageTools messageTools) {
        this.messageTools = messageTools;
    }

    @Autowired
    public void setDialogs(Dialogs dialogs) {
        this.dialogs = dialogs;
    }

    @Override
    public void setTarget(@Nullable PickerComponent<LocalizedString> target) {
        Preconditions.checkArgument(target == null || target instanceof HasValue<?, ?>,
                "Target must implement " + HasValue.class.getName());
        super.setTarget(target);
    }

    /**
     * Returns whether a multi-line text input component is used
     * for each localized value editing.
     *
     * @return whether a multi-line text input component is used
     * for each localized value editing
     */
    public boolean isMultiline() {
        return Boolean.TRUE.equals(multiline);
    }

    /**
     * Sets whether to use a multi-line text input component.
     * <p>
     * If not set explicitly, the presence of the {@link Lob}
     * annotation is checked. A single-line text input component
     * is used by default.
     *
     * @param multiline {@code true} to use a multi-line text
     *                  input component, {@code false} otherwise
     * @apiNote this setting is applied before the first time the edit
     * dialog is opened, as fields are cached. {@link TextArea} is used
     * for multi-line text input, {@link TextField} otherwise
     */
    public void setMultiline(boolean multiline) {
        this.multiline = multiline;
    }

    /**
     * Sets whether to use a multi-line text input component.
     * {@code false} by default.
     *
     * @param multiline {@code true} to use a multi-line text
     *                  input component, {@code false} otherwise
     * @return this object
     * @apiNote this setting is applied before the first time the edit
     * dialog is opened, as fields are cached. {@link TextArea} is used
     * for multi-line text input, {@link TextField} otherwise
     */
    public LocalizedStringEditAction withMultiline(boolean multiline) {
        setMultiline(multiline);
        return this;
    }

    /**
     * Returns whether this action should prevent the closing of the edit
     * dialog if there are unsaved changes.
     *
     * @return whether this action should prevent the closing of the edit
     * dialog if there are unsaved changes
     */
    public boolean isCheckForUnsavedChanges() {
        return checkForUnsavedChanges;
    }

    /**
     * Sets whether this action should prevent the closing of the edit
     * dialog if there are unsaved changes. {@code true} by default.
     *
     * @param checkForUnsavedChanges {@code true} if this action should prevent
     *                               the closing of the edit dialog if there are
     *                               unsaved changes, {@code false} otherwise
     */
    public void setCheckForUnsavedChanges(boolean checkForUnsavedChanges) {
        this.checkForUnsavedChanges = checkForUnsavedChanges;
    }

    /**
     * Sets whether this action should prevent the closing of the edit
     * dialog if there are unsaved changes. {@code true} by default.
     *
     * @param checkForUnsavedChanges {@code true} if this action should prevent
     *                               the closing of the edit dialog if there are
     *                               unsaved changes, {@code false} otherwise
     * @return this object
     */
    public LocalizedStringEditAction withCheckForUnsavedChanges(boolean checkForUnsavedChanges) {
        setCheckForUnsavedChanges(checkForUnsavedChanges);
        return this;
    }

    /**
     * Sets the width of the edit dialog.
     * <p>
     * The width should be in a format understood by the browser, e.g. "100px"
     * or "2.5em". Default width is {@code var(--localized-string-editor-width, 30em)},
     * i.e. default width is "30em", but can be globally changed by setting value
     * to the {@code --localized-string-editor-width} CSS variable.
     * <p>
     * If the provided {@code width} value is {@code null} then width is
     * removed.
     *
     * @param width the width to set, may be {@code null}
     */
    public void setWidth(String width) {
        dialog.setWidth(width);
    }

    /**
     * Sets the width of the edit dialog.
     * <p>
     * The width should be in a format understood by the browser, e.g. "100px"
     * or "2.5em". Default width is {@code var(--localized-string-editor-width, 30em)},
     * i.e. default width is "30em", but can be globally changed by setting value
     * to the {@code --localized-string-editor-width} CSS variable.
     * <p>
     * If the provided {@code width} value is {@code null} then width is
     * removed.
     *
     * @param width the width to set, may be {@code null}
     * @return this object
     */
    public LocalizedStringEditAction withWidth(String width) {
        setWidth(width);
        return this;
    }

    /**
     * Sets the min-width of the edit dialog.
     * <p>
     * The width should be in a format understood by the browser, e.g. "100px"
     * or "2.5em".
     * <p>
     * If the provided {@code minWidth} value is {@code null} then min-width
     * is removed.
     *
     * @param minWidth the min-width value, may be {@code null}
     */
    public void setMinWidth(String minWidth) {
        dialog.setMinWidth(minWidth);
    }

    /**
     * Sets the min-width of the edit dialog.
     * <p>
     * The width should be in a format understood by the browser, e.g. "100px"
     * or "2.5em".
     * <p>
     * If the provided {@code minWidth} value is {@code null} then min-width
     * is removed.
     *
     * @param minWidth the min-width value, may be {@code null}
     * @return this object
     */
    public LocalizedStringEditAction withMinWidth(String minWidth) {
        setMinWidth(minWidth);
        return this;
    }

    /**
     * Sets the max-width of the edit dialog.
     * <p>
     * The width should be in a format understood by the browser, e.g. "100px"
     * or "2.5em".
     * <p>
     * If the provided {@code maxWidth} value is {@code null} then max-width
     * is removed.
     *
     * @param maxWidth the max-width value, may be {@code null}
     */
    public void setMaxWidth(String maxWidth) {
        dialog.setMaxWidth(maxWidth);
    }

    /**
     * Sets the max-width of the edit dialog.
     * <p>
     * The width should be in a format understood by the browser, e.g. "100px"
     * or "2.5em".
     * <p>
     * If the provided {@code maxWidth} value is {@code null} then max-width
     * is removed.
     *
     * @param maxWidth the max-width value, may be {@code null}
     * @return this object
     */
    public LocalizedStringEditAction withMaxWidth(String maxWidth) {
        setMaxWidth(maxWidth);
        return this;
    }

    /**
     * Sets the height of the edit dialog.
     * <p>
     * The height should be in a format understood by the browser, e.g. "100px"
     * or "2.5em".
     * <p>
     * If the provided {@code height} value is {@code null} then height is
     * removed.
     *
     * @param height the height to set, may be {@code null}
     */
    public void setHeight(String height) {
        dialog.setHeight(height);
    }

    /**
     * Sets the height of the edit dialog.
     * <p>
     * The height should be in a format understood by the browser, e.g. "100px"
     * or "2.5em".
     * <p>
     * If the provided {@code height} value is {@code null} then height is
     * removed.
     *
     * @param height the height to set, may be {@code null}
     * @return this object
     */
    public LocalizedStringEditAction withHeight(String height) {
        setHeight(height);
        return this;
    }

    /**
     * Sets the min-height of the edit dialog.
     * <p>
     * The height should be in a format understood by the browser, e.g. "100px"
     * or "2.5em".
     * <p>
     * If the provided {@code minHeight} value is {@code null} then
     * min-height is removed.
     *
     * @param minHeight the min-height value, may be {@code null}
     */
    public void setMinHeight(@Nullable String minHeight) {
        dialog.setMinHeight(minHeight);
    }

    /**
     * Sets the min-height of the edit dialog.
     * <p>
     * The height should be in a format understood by the browser, e.g. "100px"
     * or "2.5em".
     * <p>
     * If the provided {@code minHeight} value is {@code null} then
     * min-height is removed.
     *
     * @param minHeight the min-height value, may be {@code null}
     * @return this object
     */
    public LocalizedStringEditAction withMinHeight(@Nullable String minHeight) {
        setMinHeight(minHeight);
        return this;
    }

    /**
     * Sets the max-height of the edit dialog.
     * <p>
     * The height should be in a format understood by the browser, e.g. "100px"
     * or "2.5em".
     * <p>
     * If the provided {@code maxHeight} value is {@code null} then
     * max-height is removed.
     *
     * @param maxHeight the max-height value, may be {@code null}
     */
    public void setMaxHeight(@Nullable String maxHeight) {
        dialog.setMaxHeight(maxHeight);
    }

    /**
     * Sets the max-height of the edit dialog.
     * <p>
     * The height should be in a format understood by the browser, e.g. "100px"
     * or "2.5em".
     * <p>
     * If the provided {@code maxHeight} value is {@code null} then
     * max-height is removed.
     *
     * @param maxHeight the max-height value, may be {@code null}
     * @return this object
     */
    public LocalizedStringEditAction withMaxHeight(@Nullable String maxHeight) {
        setMaxHeight(maxHeight);
        return this;
    }

    /**
     * Sets the height of the multi-line text input component if used.
     * <p>
     * The height should be in a format understood by the browser, e.g. "100px"
     * or "2.5em". Default height is {@code var(--localized-string-editor-multiline-field-height, 6.5em)},
     * i.e. default height is "6.5em", but can be globally changed by setting value
     * to the {@code --localized-string-editor-multiline-field-height} CSS variable.
     * <p>
     * If the provided {@code height} value is {@code null} then height is
     * removed.
     *
     * @param height the height to set, may be {@code null}
     * @see #setMultiline(boolean)
     */
    public void setMultilineFieldHeight(@Nullable String height) {
        this.multilineFieldHeight = height;
    }

    /**
     * Sets the height of the multi-line text input component if used.
     * <p>
     * The height should be in a format understood by the browser, e.g. "100px"
     * or "2.5em". Default height is {@code var(--localized-string-editor-multiline-field-height, 6.5em)},
     * i.e. default height is "6.5em", but can be globally changed by setting value
     * to the {@code --localized-string-editor-multiline-field-height} CSS variable.
     * <p>
     * If the provided {@code height} value is {@code null} then height is
     * removed.
     *
     * @param height the height to set, may be {@code null}
     * @return this object
     * @see #setMultiline(boolean)
     */
    public LocalizedStringEditAction withMultilineFieldHeight(@Nullable String height) {
        setMultilineFieldHeight(height);
        return this;
    }

    /**
     * Sets the min-height of the multi-line text input component if used.
     * <p>
     * The height should be in a format understood by the browser, e.g. "100px"
     * or "2.5em".
     * <p>
     * If the provided {@code minHeight} value is {@code null} then
     * min-height is removed.
     *
     * @param minHeight the min-height value, may be {@code null}
     * @see #setMultiline(boolean)
     */
    public void setMultilineFieldMinHeight(@Nullable String minHeight) {
        this.multilineFieldMinHeight = minHeight;
    }

    /**
     * Sets the min-height of the multi-line text input component if used.
     * <p>
     * The height should be in a format understood by the browser, e.g. "100px"
     * or "2.5em".
     * <p>
     * If the provided {@code minHeight} value is {@code null} then
     * min-height is removed.
     *
     * @param minHeight the min-height value, may be {@code null}
     * @return this object
     * @see #setMultiline(boolean)
     */
    public LocalizedStringEditAction withMultilineFieldMinHeight(@Nullable String minHeight) {
        setMultilineFieldMinHeight(minHeight);
        return this;
    }

    /**
     * Sets the max-height of the multi-line text input component if used.
     * <p>
     * The height should be in a format understood by the browser, e.g. "100px"
     * or "2.5em".
     * <p>
     * If the provided {@code maxHeight} value is {@code null} then
     * max-height is removed.
     *
     * @param maxHeight the max-height value, may be {@code null}
     * @see #setMultiline(boolean)
     */
    public void setMultilineFieldMaxHeight(@Nullable String maxHeight) {
        this.multilineFieldMaxHeight = maxHeight;
    }

    /**
     * Sets the max-height of the multi-line text input component if used.
     * <p>
     * The height should be in a format understood by the browser, e.g. "100px"
     * or "2.5em".
     * <p>
     * If the provided {@code maxHeight} value is {@code null} then
     * max-height is removed.
     *
     * @param maxHeight the max-height value, may be {@code null}
     * @return this object
     * @see #setMultiline(boolean)
     */
    public LocalizedStringEditAction withMultilineFieldMaxHeight(@Nullable String maxHeight) {
        setMultilineFieldMaxHeight(maxHeight);
        return this;
    }

    /**
     * Sets the CSS class name of the edit dialog. This method overwrites any
     * previous set class names. "localized-string-editor" is set by default.
     *
     * @param className a space-separated string of class names to set, or
     *                  {@code null} to remove all class names
     */
    public void setClassName(@Nullable String className) {
        dialog.setClassName(className);
    }

    /**
     * Sets the CSS class name of the edit dialog. This method overwrites any
     * previous set class names. "localized-string-editor" is set by default.
     *
     * @param className a space-separated string of class names to set, or
     *                  {@code null} to remove all class names
     * @return this object
     */
    public LocalizedStringEditAction withClassName(@Nullable String className) {
        dialog.setClassName(className);
        return this;
    }

    /**
     * Sets Save button text.
     *
     * @param text text to set or {@code null} to remove existing text
     */
    public void setSaveText(@Nullable String text) {
        saveButton.setText(text);
    }

    /**
     * Sets Save button text.
     *
     * @param text text to set or {@code null} to remove existing text
     * @return this object
     */
    public LocalizedStringEditAction withSaveText(@Nullable String text) {
        setSaveText(text);
        return this;
    }

    /**
     * Sets the given component as the icon of Save button.
     *
     * @param icon component to be used as an icon, may be {@code null} to
     *             only remove the current icon, can't be a text-node
     */
    public void setSaveIcon(@Nullable Component icon) {
        this.saveButton.setIcon(icon);
    }

    /**
     * Sets the given component as the icon of Save button.
     *
     * @param icon component to be used as an icon, may be {@code null} to
     *             only remove the current icon, can't be a text-node
     * @return this object
     */
    public LocalizedStringEditAction withSaveIcon(@Nullable Component icon) {
        setSaveIcon(icon);
        return this;
    }

    /**
     * Sets the theme names of Save button. This method overwrites any
     * previous set theme names.
     *
     * @param themeName a space-separated string of theme names to set,
     *                  or {@code null} to remove all theme names
     */
    public void setSaveButtonTheme(@Nullable String themeName) {
        saveButton.setThemeName(themeName);
    }

    /**
     * Sets the theme names of Save button. This method overwrites any
     * previous set theme names.
     *
     * @param themeName a space-separated string of theme names to set,
     *                  or {@code null} to remove all theme names
     * @return this object
     */
    public LocalizedStringEditAction withSaveButtonTheme(@Nullable String themeName) {
        saveButton.setThemeName(themeName);
        return this;
    }

    /**
     * Sets Cancel button text.
     *
     * @param text text to set or {@code null} to remove existing text
     */
    public void setCancelText(@Nullable String text) {
        cancelButton.setText(text);
    }

    /**
     * Sets Cancel button text.
     *
     * @param text text to set or {@code null} to remove existing text
     * @return this object
     */
    public LocalizedStringEditAction withCancelText(@Nullable String text) {
        setCancelText(text);
        return this;
    }

    /**
     * Sets the given component as the icon of Cancel button.
     *
     * @param icon component to be used as an icon, may be {@code null} to
     *             only remove the current icon, can't be a text-node
     */
    public void setCancelIcon(@Nullable Component icon) {
        cancelButton.setIcon(icon);
    }

    /**
     * Sets the given component as the icon of Cancel button.
     *
     * @param icon component to be used as an icon, may be {@code null} to
     *             only remove the current icon, can't be a text-node
     * @return this object
     */
    public LocalizedStringEditAction withCancelIcon(@Nullable Component icon) {
        setCancelIcon(icon);
        return this;
    }

    /**
     * Sets the theme names of Cancel button. This method overwrites any
     * previous set theme names.
     *
     * @param themeName a space-separated string of theme names to set,
     *                  or {@code null} to remove all theme names
     */
    public void setCancelButtonTheme(@Nullable String themeName) {
        cancelButton.setThemeName(themeName);
    }

    /**
     * Sets the theme names of Cancel button. This method overwrites any
     * previous set theme names.
     *
     * @param themeName a space-separated string of theme names to set,
     *                  or {@code null} to remove all theme names
     * @return this object
     */
    public LocalizedStringEditAction withCancelButtonTheme(@Nullable String themeName) {
        cancelButton.setThemeName(themeName);
        return this;
    }

    /**
     * Adds a validator that checks the value of every field in the edit dialog.
     * <p>
     * Should throw {@link io.jmix.flowui.exception.ValidationException} if the value is incorrect.
     * <p>
     * Example:
     * <pre>{@code
     * @Install(to = "locNameField.localizedStringEdit", subject = "validator")
     * private void fieldValidator(final ValidationContext validationContext) {
     *     String value = validationContext.value();
     *     if (value != null && value.length() < 10) {
     *         throw new ValidationException("Must be at least 10 characters");
     *     }
     * }
     * }</pre>
     *
     * @param validator a validator to add
     * @return handler to remove the validator
     */
    public Registration addValidator(Validator validator) {
        if (validators == null) {
            validators = new ArrayList<>();
        }
        validators.add(validator);
        return () -> validators.remove(validator);
    }

    /**
     * Adds validators that check the value of every field in the edit dialog.
     * <p>
     * Each validator should throw {@link io.jmix.flowui.exception.ValidationException} if the value is incorrect.
     *
     * @param validators validators to add
     * @return handler to remove the validator
     * @see #addValidator(Validator)
     */
    public LocalizedStringEditAction withValidators(Validator... validators) {
        for (Validator validator : validators) {
            addValidator(validator);
        }

        return this;
    }

    /**
     * Sets the field provider that will return a component for the given
     * {@link FieldGenerationContext}.
     * <p>
     * Example:
     * <pre>{@code
     * @Install(to = "locNameField.localizedStringEdit", subject = "fieldProvider")
     * private HasValueAndElement<?, String> editFieldProvider(final FieldGenerationContext context) {
     *     return uiComponents.create(RichTextEditor.class);
     * }
     * }</pre>
     *
     * @param fieldProvider the field provider that will return a component
     *                      for the given {@link FieldGenerationContext}
     */
    public void setFieldProvider(
            @Nullable ValueProvider<FieldGenerationContext, HasValueAndElement<?, String>> fieldProvider) {
        this.fieldProvider = fieldProvider;
    }

    /**
     * Sets the field provider that will return a component for the given
     * {@link FieldGenerationContext}.
     * <p>
     * Example:
     * <pre>{@code
     * @Install(to = "locNameField.localizedStringEdit", subject = "fieldProvider")
     * private HasValueAndElement<?, String> editFieldProvider(final FieldGenerationContext context) {
     *     return uiComponents.create(RichTextEditor.class);
     * }
     * }</pre>
     *
     * @param fieldProvider the field provider that will return a component
     *                      for the given {@link FieldGenerationContext}
     * @return this object
     */
    public LocalizedStringEditAction withFieldProvider(
            @Nullable ValueProvider<FieldGenerationContext, HasValueAndElement<?, String>> fieldProvider) {
        setFieldProvider(fieldProvider);
        return this;
    }

    @Override
    public void execute() {
        checkTarget();

        // Fields are not recreated because they are cached, but
        // they are correctly initialized with new values
        dialog.removeAll();
        dialog.add(createContent());

        // Clear flag after content is created because fields are
        // initialized with a default value
        hasUnsavedChanges = false;

        dialog.open();
    }

    protected void initDialog() {
        dialog = new Dialog();
        dialog.setWidth("var(--localized-string-editor-width, 30em)");
        dialog.setClassName("localized-string-editor");
        dialog.setCloseOnOutsideClick(false);
        // Add 'ESCAPE' shortcut to the Close button in order to reset focus on
        // the active element before triggering the shortcut event handler.
        dialog.setCloseOnEsc(false);
        dialog.addOpenedChangeListener(this::onDialogOpenedChanged);

        saveButton = createSaveButton();
        dialog.getFooter().add(saveButton);

        cancelButton = createCancelButton();
        dialog.getFooter().add(cancelButton);
    }

    protected Button createSaveButton() {
        Button saveButton = new Button();
        saveButton.setIcon(ComponentUtils.convertToIcon(VaadinIcon.CHECK));
        saveButton.addClickListener(this::doSave);
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClassName("localized-string-editor-save");
        addButtonClickShortcut(saveButton, Key.ENTER, KeyModifier.CONTROL);
        if (isMac()) {
            addButtonClickShortcut(saveButton, Key.ENTER, KeyModifier.META);
        }

        return saveButton;
    }

    protected Button createCancelButton() {
        Button cancelButton = new Button();
        cancelButton.setIcon(ComponentUtils.convertToIcon(VaadinIcon.BAN));
        cancelButton.addClickListener(this::doClose);
        cancelButton.addClassName("localized-string-editor-cancel");
        addButtonClickShortcut(cancelButton, Key.ESCAPE);

        return cancelButton;
    }

    protected void addButtonClickShortcut(Button button, Key key, KeyModifier... keyModifiers) {
        ShortcutRegistration shortcutRegistration = button.addClickShortcut(key, keyModifiers);
        shortcutRegistration.setResetFocusOnActiveElement(true);
        shortcutRegistration.bindLifecycleTo(dialog);
    }

    protected void doSave(ClickEvent<Button> event) {
        Map<Locale, String> localizedValues = getFields().asMap()
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> entry.getValue().getValue())
                );

        target.setValueFromClient(new LocalizedString(localizedValues));
        hasUnsavedChanges = false;
        closeInternal();
    }

    protected void doClose(ClickEvent<Button> event) {
        closeInternal();
    }

    protected void closeInternal() {
        if (isCheckForUnsavedChanges() && hasUnsavedChanges) {
            showUnsavedChangesDialog();
        } else {
            dialog.close();
        }
    }

    protected void showUnsavedChangesDialog() {
        dialogs.createOptionDialog()
                .withHeader(messages.getMessage("dialogs.closeUnsaved.title"))
                .withText(messages.getMessage("dialogs.closeUnsaved.message"))
                .withActions(
                        new DialogAction(DialogAction.Type.YES)
                                .withHandler(__ -> dialog.close()),
                        new DialogAction(DialogAction.Type.NO)
                                .withHandler(__ ->
                                        UiComponentUtils.findFocusComponent(dialog)
                                                .ifPresent(Focusable::focus))
                                .withVariant(ActionVariant.PRIMARY)
                )
                .open();
    }

    protected Component createContent() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.setAlignItems(FlexComponent.Alignment.STRETCH);
        layout.setClassName("localized-string-editor-content");

        availableLocales.keySet().stream()
                .map(locale -> ((Component) getField(locale)))
                .forEach(layout::add);

        return layout;
    }

    protected HasValueAndElement<?, String> getField(Locale locale) {
        HasValueAndElement<?, String> field = getFields().getIfPresent(locale);
        if (field == null) {
            field = createField(locale);
            getFields().put(locale, field);
        }

        field.setValue(getInitialValue(locale));
        return field;
    }

    protected HasValueAndElement<?, String> createField(Locale locale) {
        MetaPropertyPath metaPropertyPath = findMetaPropertyPath();

        HasValueAndElement<?, String> field;
        if (fieldProvider != null) {
            field = fieldProvider.apply(new FieldGenerationContext(locale, metaPropertyPath));
        } else if (isMultilineInternal(metaPropertyPath)) {
            field = uiComponents.create(TextArea.class);
            initMultilineField(field);
        } else {
            field = uiComponents.create(TextField.class);
        }

        initField(field, locale, metaPropertyPath);

        return field;
    }

    protected void initField(HasValueAndElement<?, String> field,
                             Locale locale,
                             @Nullable MetaPropertyPath metaPropertyPath) {
        field.addValueChangeListener(event -> hasUnsavedChanges = true);
        field.getElement()
                .addPropertyChangeListener("invalid", this::onFieldInvalidChanged);

        if (field instanceof HasLabel hasLabel) {
            hasLabel.setLabel(availableLocales.get(locale));
        }

        if (field instanceof HasStyle hasStyle) {
            hasStyle.addClassName("localized-string-editor-field");
        }

        initRequired(field, metaPropertyPath);
        initValidators(field, locale);
    }

    protected void onFieldInvalidChanged(PropertyChangeEvent propertyChangeEvent) {
        saveButton.setEnabled(!hasInvalidFields());
    }

    protected boolean hasInvalidFields() {
        return getFields().asMap().entrySet()
                .stream()
                .anyMatch(entry ->
                        entry.getValue() instanceof HasValidation hasValidation
                                && hasValidation.isInvalid()
                );
    }

    protected void initMultilineField(HasValueAndElement<?, String> field) {
        if (field instanceof HasSize hasSize) {
            hasSize.setHeight(multilineFieldHeight);
            hasSize.setMinHeight(multilineFieldMinHeight);
            hasSize.setMaxHeight(multilineFieldMaxHeight);
        }
    }

    @Nullable
    protected MetaPropertyPath findMetaPropertyPath() {
        ValueSource<LocalizedString> valueSource = target.getValueSource();
        MetaPropertyPath metaPropertyPath = null;
        return valueSource instanceof EntityValueSource<?, ?> entityValueSource
                ? entityValueSource.getMetaPropertyPath()
                : null;
    }

    protected boolean isMultilineInternal(@Nullable MetaPropertyPath metaPropertyPath) {
        // if 'multiline' is set explicitly, it takes precedence
        if (multiline != null) {
            return multiline;
        } else if (metaPropertyPath != null) {
            Annotation lob = metaPropertyPath
                    .getMetaProperty()
                    .getAnnotatedElement()
                    .getAnnotation(Lob.class);
            return lob != null;
        } else {
            return false;
        }
    }

    protected void initRequired(HasValueAndElement<?, String> field,
                                @Nullable MetaPropertyPath metaPropertyPath) {
        if (metaPropertyPath == null) {
            return;
        }

        MetaProperty metaProperty = metaPropertyPath.getMetaProperty();

        boolean required = metaProperty.isMandatory();
        Object notNullUiComponent = metaProperty.getAnnotations()
                .get(NotNull.class.getName() + "_notnull_ui_component");
        if (Boolean.TRUE.equals(notNullUiComponent)) {
            required = true;
        }

        if (required) {
            field.setRequiredIndicatorVisible(true);
        }

        if (field instanceof HasRequired hasRequired) {
            String errorMessage = hasRequired.getRequiredMessage();

            if (Strings.isNullOrEmpty(errorMessage)) {
                String defaultRequiredMessage = messageTools.getDefaultRequiredMessage(
                        metaPropertyPath.getMetaClass(),
                        metaPropertyPath.toPathString());
                hasRequired.setRequiredMessage(defaultRequiredMessage);
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void initValidators(HasValueAndElement<?, String> field, Locale locale) {
        if (validators != null
                && field instanceof SupportsValidation) {
            validators.forEach(validator ->
                    ((SupportsValidation<String>) field).addValidator(new ValidatorAdapter(validator, locale)));
        }
    }

    @SuppressWarnings("unchecked")
    protected String getInitialValue(Locale locale) {
        LocalizedString localizedString = ((HasValue<?, LocalizedString>) target).getValue();
        return localizedString != null ? localizedString.getValue(locale) : "";
    }

    protected Cache<Locale, HasValueAndElement<?, String>> getFields() {
        if (fieldCache == null) {
            fieldCache = CacheBuilder.newBuilder()
                    .maximumSize(availableLocales.size())
                    .build();
        }

        return fieldCache;
    }

    protected boolean isMac() {
        return System.getProperty("os.name").contains("Mac");
    }

    protected void onDialogOpenedChanged(Dialog.OpenedChangeEvent openedChangeEvent) {
        if (openedChangeEvent.isOpened()) {
            UiComponentUtils.findFocusComponent(dialog)
                    .ifPresent(Focusable::focus);
        }
    }
}
