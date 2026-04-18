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
import org.apache.commons.lang3.LocaleUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = LocstrTestConfiguration.class)
public class LocalizedStringHsqlSortingTest {

    private static final Locale LOCALE_RU = LocaleUtils.toLocale("ru_RU");

    @Autowired
    DataManager dataManager;

    List<LocalizedStringTestEntity> savedEntities = new ArrayList<>();

    Authentication initialAuthentication;

    @DynamicPropertySource
    static void configureHsql(DynamicPropertyRegistry registry) {
        registry.add(LocstrTestConfiguration.DATABASE_TYPE_PROPERTY, () -> "HSQL");
    }

    @BeforeEach
    void setUp() {
        initialAuthentication = SecurityContextHelper.getAuthentication();

        SystemAuthenticationToken authentication =
                new SystemAuthenticationToken("test-user", List.of());
        authentication.setDetails(ClientDetails.builder().locale(LOCALE_RU).build());

        SecurityContextHelper.setAuthentication(authentication);
    }

    @Test
    void dbSortByLocalizedStringPropertyShouldUseWholeJsonStringOnHsql() {
        LocalizedStringTestEntity entity1 = saveEntity("Zulu", "Alpha");
        LocalizedStringTestEntity entity2 = saveEntity("Alpha", "Zulu");

        List<LocalizedStringTestEntity> loadedEntities = dataManager.load(LocalizedStringTestEntity.class)
                .query("select e from locstr_LocalizedStringTestEntity e where e.id in :entityIds")
                .parameter("entityIds", List.of(entity1.getId(), entity2.getId()))
                .sort(Sort.by("name"))
                .list();

        assertThat(entityIds(loadedEntities))
                .containsExactlyElementsOf(entityIdsSortedByJsonString(entity1, entity2));
    }

    @AfterEach
    void tearDown() {
        try {
            savedEntities.forEach(entity -> dataManager.remove(entity));
        } finally {
            SecurityContextHelper.setAuthentication(initialAuthentication);
        }
    }

    private LocalizedStringTestEntity saveEntity(String englishName, String russianName) {
        LocalizedStringTestEntity entity = dataManager.create(LocalizedStringTestEntity.class);
        entity.setName(new LocalizedString(Map.of(
                Locale.ENGLISH, englishName,
                LOCALE_RU, russianName
        )));

        LocalizedStringTestEntity savedEntity = dataManager.save(entity);
        savedEntities.add(savedEntity);

        return savedEntity;
    }

    private List<UUID> entityIds(List<LocalizedStringTestEntity> entities) {
        return entities.stream()
                .map(LocalizedStringTestEntity::getId)
                .toList();
    }

    private List<UUID> entityIdsSortedByJsonString(LocalizedStringTestEntity... entities) {
        return Arrays.stream(entities)
                .sorted(Comparator.comparing(entity -> entity.getName().toJson()))
                .map(LocalizedStringTestEntity::getId)
                .toList();
    }
}
