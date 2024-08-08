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

package com.glebfox.jmix.locstr.studio;

import io.jmix.flowui.kit.meta.*;

@StudioUiKit
public interface StudioAction {

    @io.jmix.flowui.kit.meta.StudioAction(
            type = "value_localizedStringEdit",
            description = "Opens a dialog that edits a localized string value",
            classFqn = "com.glebfox.jmix.locstr.action.LocalizedStringEditAction",
            icon = "io/jmix/flowui/kit/meta/icon/action/action.svg",
            documentationLink = "https://github.com/glebfox/localized-string-datatype-addon-addon",
            properties = {
                    @StudioProperty(xmlAttribute = "id", type = StudioPropertyType.COMPONENT_ID, required = true),
                    @StudioProperty(xmlAttribute = "actionVariant", type = StudioPropertyType.ENUMERATION,
                            setMethod = "setVariant", classFqn = "io.jmix.flowui.kit.action.ActionVariant",
                            defaultValue = "DEFAULT", options = {"DEFAULT", "PRIMARY", "DANGER", "SUCCESS"}),
                    @StudioProperty(xmlAttribute = "enabled", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "visible", type = StudioPropertyType.BOOLEAN, defaultValue = "true"),
                    @StudioProperty(xmlAttribute = "icon", type = StudioPropertyType.ICON, required = true,
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon", defaultValue = "GLOBE"),
                    @StudioProperty(xmlAttribute = "text", type = StudioPropertyType.LOCALIZED_STRING,
                            defaultValue = "msg:///actions.localizedStringEdit.description"),
                    @StudioProperty(xmlAttribute = "description", type = StudioPropertyType.LOCALIZED_STRING),
                    @StudioProperty(xmlAttribute = "shortcutCombination", type = StudioPropertyType.SHORTCUT_COMBINATION)

            },
            items = {
                    /* General */
                    @StudioPropertiesItem(xmlAttribute = "multiline", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "false"),
                    @StudioPropertiesItem(xmlAttribute = "checkForUnsavedChanges", type = StudioPropertyType.BOOLEAN,
                            defaultValue = "true"),

                    /* Dialog */
                    @StudioPropertiesItem(xmlAttribute = "width", type = StudioPropertyType.SIZE,
                            options = {"AUTO", "100%"}),
                    @StudioPropertiesItem(xmlAttribute = "minWidth", type = StudioPropertyType.SIZE,
                            options = {"AUTO", "100%"}),
                    @StudioPropertiesItem(xmlAttribute = "maxWidth", type = StudioPropertyType.SIZE,
                            options = {"AUTO", "100%"}),
                    @StudioPropertiesItem(xmlAttribute = "height", type = StudioPropertyType.SIZE,
                            options = {"AUTO", "100%"}),
                    @StudioPropertiesItem(xmlAttribute = "minHeight", type = StudioPropertyType.SIZE,
                            options = {"AUTO", "100%"}),
                    @StudioPropertiesItem(xmlAttribute = "maxHeight", type = StudioPropertyType.SIZE,
                            options = {"AUTO", "100%"}),
                    @StudioPropertiesItem(xmlAttribute = "className", type = StudioPropertyType.STRING),

                    /* MultilineField */
                    @StudioPropertiesItem(xmlAttribute = "multilineFieldHeight", type = StudioPropertyType.SIZE,
                            options = {"AUTO", "100%"}),
                    @StudioPropertiesItem(xmlAttribute = "multilineFieldMinHeight", type = StudioPropertyType.SIZE,
                            options = {"AUTO", "100%"}),
                    @StudioPropertiesItem(xmlAttribute = "multilineFieldMaxHeight", type = StudioPropertyType.SIZE,
                            options = {"AUTO", "100%"}),

                    /* Controls */
                    @StudioPropertiesItem(xmlAttribute = "saveText", type = StudioPropertyType.LOCALIZED_STRING,
                            defaultValue = "msg:///actions.Save"),
                    @StudioPropertiesItem(xmlAttribute = "saveIcon", type = StudioPropertyType.ICON,
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon", defaultValue = "CHECK"),
                    @StudioPropertiesItem(xmlAttribute = "saveButtonTheme", type = StudioPropertyType.VALUES_LIST,
                            options = {"small", "large", "tertiary", "tertiary-inline", "primary", "success", "error",
                                    "contrast", "icon", "contained", "outlined"},
                            defaultValue = "primary"),
                    @StudioPropertiesItem(xmlAttribute = "cancelText", type = StudioPropertyType.LOCALIZED_STRING,
                            defaultValue = "msg:///actions.Cancel"),
                    @StudioPropertiesItem(xmlAttribute = "cancelIcon", type = StudioPropertyType.ICON,
                            setParameterFqn = "com.vaadin.flow.component.icon.Icon", defaultValue = "BAN"),
                    @StudioPropertiesItem(xmlAttribute = "cancelButtonTheme", type = StudioPropertyType.VALUES_LIST,
                            options = {"small", "large", "tertiary", "tertiary-inline", "primary", "success", "error",
                                    "contrast", "icon", "contained", "outlined"})
            }
    )
    void editAction();
}
