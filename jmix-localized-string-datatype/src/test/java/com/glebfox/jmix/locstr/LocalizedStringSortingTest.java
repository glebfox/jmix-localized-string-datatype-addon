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
import com.glebfox.jmix.locstr.entity.LocalizedStringTestEntity;
import io.jmix.core.DataManager;
import io.jmix.core.Sort;
import io.jmix.core.security.ClientDetails;
import io.jmix.core.security.SecurityContextHelper;
import io.jmix.core.security.SystemAuthenticationToken;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.DataComponents;
import org.apache.commons.lang3.LocaleUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class LocalizedStringSortingTest {

    private static final Locale LOCALE_RU = LocaleUtils.toLocale("ru_RU");

    @Autowired
    DataManager dataManager;

    @Autowired
    DataComponents dataComponents;

    List<LocalizedStringTestEntity> savedEntities = new ArrayList<>();

    Authentication initialAuthentication;

    @BeforeEach
    void setUp() {
        initialAuthentication = SecurityContextHelper.getAuthentication();

        authenticateWithLocale(LOCALE_RU);
    }

    @Test
    void sortByLocalizedStringPropertyShouldUseAuthenticationLocaleValue() {
        LocalizedStringTestEntity entity1 = saveEntity(Map.of(
                Locale.ENGLISH, "Zulu",
                LOCALE_RU, "Alpha"
        ));
        LocalizedStringTestEntity entity2 = saveEntity(Map.of(
                Locale.ENGLISH, "Alpha",
                LOCALE_RU, "Zulu"
        ));

        List<LocalizedStringTestEntity> loadedEntities =
                loadByIdsSortedByName(entity1, entity2);

        assertThat(entityIds(loadedEntities))
                .containsExactly(entity1.getId(), entity2.getId());
        assertThat(localizedNames(loadedEntities, LOCALE_RU))
                .containsExactly("Alpha", "Zulu");

        authenticateWithLocale(Locale.ENGLISH);

        loadedEntities = loadByIdsSortedByName(entity1, entity2);

        assertThat(entityIds(loadedEntities))
                .containsExactly(entity2.getId(), entity1.getId());
        assertThat(localizedNames(loadedEntities, Locale.ENGLISH))
                .containsExactly("Alpha", "Zulu");
    }

    @Test
    void sortByLocalizedStringPropertyShouldUseEmptyStringForMissingLocaleValue() {
        LocalizedStringTestEntity entity1 = saveEntity(Map.of(
                Locale.ENGLISH, "ru_RU is not a localized value key"
        ));
        LocalizedStringTestEntity entity2 = saveEntity(Map.of(
                Locale.ENGLISH, "Alpha",
                LOCALE_RU, "Charlie"
        ));
        LocalizedStringTestEntity entity3 = saveEntity(Map.of(
                Locale.ENGLISH, "Beta",
                LOCALE_RU, "Bravo"
        ));

        List<LocalizedStringTestEntity> loadedEntities =
                loadByIdsSortedByName(entity1, entity2, entity3);

        assertThat(localizedNames(loadedEntities, LOCALE_RU))
                .containsExactly("", "Bravo", "Charlie");
    }

    @Test
    void sortInMemoryByLocalizedStringPropertyShouldUseAuthenticationLocaleValue() {
        LocalizedStringTestEntity entity1 = saveEntity(Map.of(
                Locale.ENGLISH, "Zulu",
                LOCALE_RU, "Alpha"
        ));
        LocalizedStringTestEntity entity2 = saveEntity(Map.of(
                Locale.ENGLISH, "Alpha",
                LOCALE_RU, "Zulu"
        ));

        CollectionContainer<LocalizedStringTestEntity> container =
                dataComponents.createCollectionContainer(LocalizedStringTestEntity.class);
        container.setItems(List.of(entity2, entity1));

        sortContainerByName(container);

        assertThat(entityIds(container.getItems()))
                .containsExactly(entity1.getId(), entity2.getId());
        assertThat(localizedNames(container.getItems(), LOCALE_RU))
                .containsExactly("Alpha", "Zulu");
    }

    @Test
    void sortInMemoryByLocalizedStringPropertyShouldUseEmptyStringForMissingLocaleValue() {
        LocalizedStringTestEntity entity1 = saveEntity(Map.of(
                Locale.ENGLISH, "ru_RU is not a localized value key"
        ));
        LocalizedStringTestEntity entity2 = saveEntity(Map.of(
                Locale.ENGLISH, "Alpha",
                LOCALE_RU, "Charlie"
        ));
        LocalizedStringTestEntity entity3 = saveEntity(Map.of(
                Locale.ENGLISH, "Beta",
                LOCALE_RU, "Bravo"
        ));

        CollectionContainer<LocalizedStringTestEntity> container =
                dataComponents.createCollectionContainer(LocalizedStringTestEntity.class);
        container.setItems(List.of(entity2, entity3, entity1));

        sortContainerByName(container);

        assertThat(localizedNames(container.getItems(), LOCALE_RU))
                .containsExactly("", "Bravo", "Charlie");
    }

    @AfterEach
    void tearDown() {
        try {
            savedEntities.forEach(entity -> dataManager.remove(entity));
        } finally {
            SecurityContextHelper.setAuthentication(initialAuthentication);
        }
    }

    private void authenticateWithLocale(Locale locale) {
        SystemAuthenticationToken authentication =
                new SystemAuthenticationToken("test-user", List.of());
        authentication.setDetails(ClientDetails.builder().locale(locale).build());

        SecurityContextHelper.setAuthentication(authentication);
    }

    private LocalizedStringTestEntity saveEntity(Map<Locale, String> names) {
        LocalizedStringTestEntity entity = dataManager.create(LocalizedStringTestEntity.class);
        entity.setName(new LocalizedString(names));

        LocalizedStringTestEntity savedEntity = dataManager.save(entity);
        savedEntities.add(savedEntity);

        return savedEntity;
    }

    private List<LocalizedStringTestEntity> loadByIdsSortedByName(LocalizedStringTestEntity... entities) {
        List<UUID> entityIds = Arrays.stream(entities)
                .map(LocalizedStringTestEntity::getId)
                .toList();

        return dataManager.load(LocalizedStringTestEntity.class)
                .query("select e from locstr_LocalizedStringTestEntity e where e.id in :entityIds")
                .parameter("entityIds", entityIds)
                .sort(Sort.by("name"))
                .list();
    }

    private void sortContainerByName(CollectionContainer<LocalizedStringTestEntity> container) {
        Objects.requireNonNull(container.getSorter()).sort(Sort.by("name"));
    }

    private List<String> localizedNames(List<LocalizedStringTestEntity> entities, Locale locale) {
        return entities.stream()
                .map(entity -> entity.getName().getValue(locale))
                .toList();
    }

    private List<UUID> entityIds(List<LocalizedStringTestEntity> entities) {
        return entities.stream()
                .map(LocalizedStringTestEntity::getId)
                .toList();
    }
}
