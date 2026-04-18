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

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the LocalizedString add-on.
 */
@ConfigurationProperties(prefix = LocstrProperties.PREFIX)
public class LocstrProperties {

    public static final String PREFIX = "jmix.locstr";
    public static final String SORTING_DATABASE_PREFIX = PREFIX + ".sorting.database";
    public static final String SORTING_IN_MEMORY_PREFIX = PREFIX + ".sorting.in-memory";

    /**
     * Sorting customization settings.
     */
    private Sorting sorting = new Sorting();

    public Sorting getSorting() {
        return sorting;
    }

    public void setSorting(Sorting sorting) {
        this.sorting = sorting;
    }

    public static class Sorting {

        private Database database = new Database();
        private InMemory inMemory = new InMemory();

        public Database getDatabase() {
            return database;
        }

        public void setDatabase(Database database) {
            this.database = database;
        }

        public InMemory getInMemory() {
            return inMemory;
        }

        public void setInMemory(InMemory inMemory) {
            this.inMemory = inMemory;
        }
    }

    public static class Database {

        /**
         * Whether to register the add-on database-level sorting customization for LocalizedString attributes.
         */
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class InMemory {

        /**
         * Whether to register the add-on in-memory sorting customization for LocalizedString attributes.
         */
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
