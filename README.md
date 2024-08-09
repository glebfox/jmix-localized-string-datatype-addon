[![license](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![GitHub release](https://img.shields.io/github/release/glebfox/jmix-localized-string-datatype-addon.svg)](https://github.com/glebfox/jmix-localized-string-datatype-addon/releases)

# Jmix LocalizedString Datatype

This add-on provides a custom [Datatype](https://docs.jmix.io/jmix/data-model/data-types.html) and related action for the [ValuePicker](https://docs.jmix.io/jmix/flow-ui/vc/components/valuePicker.html) component for storing and editing localized string values.

## Installation

The following table shows which version of the add-on is compatible with which version of the platform:

| Jmix Version | Add-on Version | Implementation                                                       |
|--------------|----------------|----------------------------------------------------------------------|
| 2.3.0+       | 1.0.0          | com.glebfox.jmix.locstr:jmix-localized-string-datatype-starter:1.0.0 |

For manual installation, add the following dependencies to your `build.gradle`:

```groovy
implementation 'com.glebfox.jmix.locstr:jmix-localized-string-datatype-starter:<addon-version>'
```

## Using the Addon

The `LocalizedString` datatype is represented by three classes: 

* [LocalizedString.java](jmix-localized-string-datatype/src/main/java/com/glebfox/jmix/locstr/datatype/LocalizedString.java) - custom Java class used as a type of entity attributes. 
* [LocalizedStringDatatype.java](jmix-localized-string-datatype/src/main/java/com/glebfox/jmix/locstr/datatype/LocalizedStringDatatype.java) - a `Datatype` implementation class for `LocalizedString`.
* [LocalizedStringConverter.java](jmix-localized-string-datatype/src/main/java/com/glebfox/jmix/locstr/datatype/LocalizedStringConverter.java) - a class that converts entity attribute state into database column representation and back again.

You can define an entity attribute with the `LocalizedString` datatype using Studio. For example:

![Attribute Datatype](/doc/img/attribute-datatype.png)

As a result, Studio generates the following attribute definition: 

```java
@Column(name = "NAME", nullable = false)
private LocalizedString name;
```

To edit localized value, add action with `type="value_localizedStringEdit"` to the `ValuePicker` component. For example:

```xml
<valuePicker id="nameField" property="name">
    <actions>
        <action id="localizedStringEdit" type="value_localizedStringEdit"/>
        <action id="clear" type="value_clear"/>
    </actions>
</valuePicker>
```

![Edit Dialog](/doc/img/edit-dialog.png)

**NOTE:** The actual value stored in the database is a JSON string. For example: `{"en":"Keyboard","ru_RU":"Клавиатура"}`

## LocalizedStringEditAction

The `value_localizedStringEdit` action is represented by [LocalizedStringEditAction.java](jmix-localized-string-datatype/src/main/java/com/glebfox/jmix/locstr/action/LocalizedStringEditAction.java) and opens a dialog that edits a localized string value represented by the `LocalizedString` datatype.

### Properties

* `multiline` - sets whether to use a multi-line text input component. `TextArea` is used for multi-line text input, `TextField` otherwise. `false` by default. If an entity attribute is annotated with `@Lob`, multi-line text input is used. For example:

```java
@Lob
@Column(name = "DESCRIPTION")
private LocalizedString description;
```

![Multiline Fields](/doc/img/multiline-fields.png)

* `checkForUnsavedChanges` - sets whether this action should prevent the closing of the edit dialog if there are unsaved changes. `true` by default.

* `width` - sets the width of the edit dialog. Default width is `var(--localized-string-editor-width, 30em)`

* `minWidth` - sets the min-width of the edit dialog.

* `maxWidth` - sets the max-width of the edit dialog.

* `height` - sets the height of the edit dialog.

* `minHeight` - sets the min-height of the edit dialog.

* `maxHeight` - sets the max-height of the edit dialog.

* `className` - sets the CSS class name of the edit dialog. This method overwrites any previous set class names. `localized-string-editor` is set by default.

* `multilineFieldHeight` - sets the height of the multi-line text input component if used. Default height is `var(--localized-string-editor-multiline-field-height, 6.5em)`.

* `multilineFieldMinHeight` - sets the min-height of the multi-line text input component if used.

* `multilineFieldMaxHeight` - sets the max-height of the multi-line text input component if used.

* `saveText` - sets **Save** button text.

* `saveIcon` - sets the given component as the icon of **Save** button.

* `saveButtonTheme` - sets the theme names of **Save** button. This method overwrites any previous set theme names.

* `cancelText` - sets **Cancel** button text.

* `cancelIcon` - sets the given component as the icon of **Cancel** button.

* `cancelButtonTheme` - sets the theme names of **Cancel** button. This method overwrites any previous set theme names.

### Validation

If the target `ValuePicker` of `LocalizedStringEditAction` is bound to a required entity attribute, then the edit fields also become required:

![Required Field](/doc/img/required-field.png)

Additionally, you can add a validator that checks the value of every field in the edit dialog. For example:

```java
@Install(to = "descriptionField.localizedStringEdit", subject = "validator")
private void descriptionFieldValidator(final ValidationContext validationContext) {
    String value = validationContext.value();
    if (value != null && value.length() < 5) {
        throw new ValidationException("Must be at least 5 characters");
    }
}
```

![Validator](/doc/img/validator.png)

### Field Provider

It's possible tio change edit fields by setting the field provider, which will return a component for a given `FieldGenerationContext`. For example:

```java
@Install(to = "descriptionField.localizedStringEdit", subject = "fieldProvider")
private HasValueAndElement<?, String> descriptionFieldFieldProvider(final FieldGenerationContext context) {
    RichTextEditor richTextEditor = uiComponents.create(RichTextEditor.class);
    richTextEditor.addThemeVariants(RichTextEditorVariant.COMPACT);
    return richTextEditor;
}
```

![Field Provider](/doc/img/field-provider.png)

## License

Code is under the [Apache Licence 2.0](http://www.apache.org/licenses/LICENSE-2.0).