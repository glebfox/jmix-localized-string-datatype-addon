[![license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)
![CI Pipeline](https://github.com/glebfox/jmix-localized-string-datatype-addon/actions/workflows/test.yml/badge.svg)
[![GitHub release](https://img.shields.io/github/release/glebfox/jmix-localized-string-datatype-addon.svg)](https://github.com/glebfox/jmix-localized-string-datatype-addon/releases)

# Jmix LocalizedString Datatype

This add-on provides a custom [Datatype](https://docs.jmix.io/jmix/data-model/data-types.html), sorting support, Bean Validation constraints, and a related action for the [ValuePicker](https://docs.jmix.io/jmix/flow-ui/vc/components/valuePicker.html) component for storing and editing localized string values.

The value displayed in visual components depends on the user's current locale.

![Localized Value 1](/doc/img/localized-value-1.png)
![Localized Value 2](/doc/img/localized-value-2.png)

## Installation

The following table shows which version of the add-on is compatible with which version of the platform:

| Jmix Version | Add-on Version | Implementation                                                       |
|--------------|----------------|----------------------------------------------------------------------|
| 2.3.0+       | 1.0.0          | com.glebfox.jmix.locstr:jmix-localized-string-datatype-starter:1.0.0 |
| 2.8.1+       | 1.1.0          | com.glebfox.jmix.locstr:jmix-localized-string-datatype-starter:1.1.0 |

For manual installation, add the following dependency to your `build.gradle`:

```groovy
implementation 'com.glebfox.jmix.locstr:jmix-localized-string-datatype-starter:<addon-version>'
```

## Using the Add-on

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

To edit a localized value, add an action with `type="value_localizedStringEdit"` to the `ValuePicker` component. For example:

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

## Sorting

Sorting by `LocalizedString` attributes is supported. The add-on sorts by the value for the user's current locale. If a `LocalizedString` does not contain a value for the current locale, an empty string is used for sorting.

This works for database-backed sorting and for in-memory sorting in Flow UI data containers.

### HSQLDB

HSQLDB has a database-level limitation: it cannot apply the locale-specific sorting expression to `CLOB` columns. For HSQLDB, the add-on falls back to sorting the raw JSON string stored in the column. As a result, database-level sorting order may differ from the order for the user's current locale. In-memory sorting still uses the value for the user's current locale.

### Sorting Configuration

The add-on registers global Jmix sorting customizations by default. If an application provides its own `JpqlSortExpressionProvider` or `SorterFactory`, these customizations can be disabled:

```properties
jmix.locstr.sorting.database.enabled=false
jmix.locstr.sorting.in-memory.enabled=false
```

## LocalizedStringEditAction

The `value_localizedStringEdit` action is represented by [LocalizedStringEditAction.java](jmix-localized-string-datatype/src/main/java/com/glebfox/jmix/locstr/action/LocalizedStringEditAction.java) and opens a dialog that edits a localized string value represented by the `LocalizedString` datatype.

### Properties

* `multiline` - sets whether to use a multi-line text input component. `TextArea` is used for multi-line text input, `TextField` otherwise. If the property is not set explicitly and the entity attribute is annotated with `@Lob`, multi-line text input is used. For example:

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

The add-on provides Bean Validation annotations for `LocalizedString` attributes:

* [`@LocalizedStringSize`](jmix-localized-string-datatype/src/main/java/com/glebfox/jmix/locstr/validation/constraints/LocalizedStringSize.java) checks that every localized value length is between `min` and `max`. A `null` `LocalizedString` is valid.
* [`@LocalizedStringLength`](jmix-localized-string-datatype/src/main/java/com/glebfox/jmix/locstr/validation/constraints/LocalizedStringLength.java) checks that every localized value length is between `min` and `max`. A `null` `LocalizedString` is valid.
* [`@LocalizedStringPattern`](jmix-localized-string-datatype/src/main/java/com/glebfox/jmix/locstr/validation/constraints/LocalizedStringPattern.java) checks that every localized value matches `regexp`. A `null` `LocalizedString` is valid.
* [`@LocalizedStringNotNull`](jmix-localized-string-datatype/src/main/java/com/glebfox/jmix/locstr/validation/constraints/LocalizedStringNotNull.java) checks that a non-null localized value is stored for every configured application locale.
* [`@LocalizedStringNotEmpty`](jmix-localized-string-datatype/src/main/java/com/glebfox/jmix/locstr/validation/constraints/LocalizedStringNotEmpty.java) checks that every localized value is not empty. A `null` `LocalizedString` and an empty set of localized values are invalid.
* [`@LocalizedStringNotBlank`](jmix-localized-string-datatype/src/main/java/com/glebfox/jmix/locstr/validation/constraints/LocalizedStringNotBlank.java) checks that every localized value contains at least one non-whitespace character. A `null` `LocalizedString` and an empty set of localized values are invalid.

For example:

```java
@LocalizedStringNotBlank
@LocalizedStringSize(max = 100)
@Column(name = "NAME")
private LocalizedString name;
```

When the edit dialog is used for an entity attribute, these constraints are applied to each locale field separately. Standard `@NotNull` can still be used for the attribute itself when only the `LocalizedString` object reference should be checked.

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

It's possible to change edit fields by setting the field provider, which will return a component for a given `FieldGenerationContext`. For example:

```java
@Install(to = "descriptionField.localizedStringEdit", subject = "fieldProvider")
private HasValueAndElement<?, String> descriptionFieldFieldProvider(final FieldGenerationContext context) {
    RichTextEditor richTextEditor = uiComponents.create(RichTextEditor.class);
    richTextEditor.addThemeVariants(RichTextEditorVariant.COMPACT);
    return richTextEditor;
}
```

![Field Provider](/doc/img/field-provider.png)

## Limitations

Since the actual value stored in the database is a JSON string (`CLOB`), the following Jmix functionalities do not work with `LocalizedString`:

* `GenericFilter` and `PropertyFilter` components.

## License

Code is under the [Apache Licence 2.0](http://www.apache.org/licenses/LICENSE-2.0).
