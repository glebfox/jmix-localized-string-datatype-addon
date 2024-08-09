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

package com.glebfox.jmix.locstr.demo.view.product;

import com.glebfox.jmix.locstr.action.FieldGenerationContext;
import com.glebfox.jmix.locstr.demo.entity.Product;
import com.glebfox.jmix.locstr.demo.view.main.MainView;
import com.glebfox.jmix.locstr.validation.ValidationContext;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.richtexteditor.RichTextEditor;
import io.jmix.flowui.exception.ValidationException;
import io.jmix.flowui.kit.component.richtexteditor.RichTextEditorVariant;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "products/:id", layout = MainView.class)
@ViewController("locstr_Product.detail")
@ViewDescriptor("product-detail-view.xml")
@EditedEntityContainer("productDc")
public class ProductDetailView extends StandardDetailView<Product> {

    @Autowired
    private UiComponents uiComponents;

    /*@Install(to = "descriptionField.localizedStringEdit", subject = "validator")
    private void descriptionFieldValidator(final ValidationContext validationContext) {
        String value = validationContext.value();
        if (value != null && value.length() < 5) {
            throw new ValidationException("Must be at least 5 characters");
        }
    }*/

    /*@Install(to = "descriptionField.localizedStringEdit", subject = "fieldProvider")
    private HasValueAndElement<?, String> descriptionFieldFieldProvider(final FieldGenerationContext context) {
        RichTextEditor richTextEditor = uiComponents.create(RichTextEditor.class);
        richTextEditor.addThemeVariants(RichTextEditorVariant.COMPACT);
        return richTextEditor;
    }*/
}