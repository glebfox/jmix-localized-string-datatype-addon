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

    /**
     * Root configuration prefix for the add-on.
     */
    public static final String PREFIX = "jmix.locstr";

    /**
     * Configuration prefix for database-level sorting settings.
     */
    public static final String SORTING_DATABASE_PREFIX = PREFIX + ".sorting.database";

    /**
     * Configuration prefix for in-memory sorting settings.
     */
    public static final String SORTING_IN_MEMORY_PREFIX = PREFIX + ".sorting.in-memory";

    /**
     * Sorting customization settings.
     */
    private Sorting sorting = new Sorting();

    /**
     * Returns sorting customization settings.
     *
     * @return sorting settings
     */
    public Sorting getSorting() {
        return sorting;
    }

    /**
     * Sets sorting customization settings.
     *
     * @param sorting sorting settings
     */
    public void setSorting(Sorting sorting) {
        this.sorting = sorting;
    }

    /**
     * Groups sorting customization settings.
     */
    public static class Sorting {

        private Database database = new Database();
        private InMemory inMemory = new InMemory();

        /**
         * Returns database-level sorting settings.
         *
         * @return database-level sorting settings
         */
        public Database getDatabase() {
            return database;
        }

        /**
         * Sets database-level sorting settings.
         *
         * @param database database-level sorting settings
         */
        public void setDatabase(Database database) {
            this.database = database;
        }

        /**
         * Returns in-memory sorting settings.
         *
         * @return in-memory sorting settings
         */
        public InMemory getInMemory() {
            return inMemory;
        }

        /**
         * Sets in-memory sorting settings.
         *
         * @param inMemory in-memory sorting settings
         */
        public void setInMemory(InMemory inMemory) {
            this.inMemory = inMemory;
        }
    }

    /**
     * Configures database-level sorting for localized string attributes.
     */
    public static class Database {

        /**
         * Whether to register the add-on database-level sorting customization for LocalizedString attributes.
         */
        private boolean enabled = true;

        /**
         * Returns whether database-level sorting customization is enabled.
         *
         * @return {@code true} if database-level sorting customization is
         * enabled
         */
        public boolean isEnabled() {
            return enabled;
        }

        /**
         * Sets whether database-level sorting customization is enabled.
         *
         * @param enabled {@code true} to enable database-level sorting
         *                customization
         */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    /**
     * Configures in-memory sorting for localized string attributes.
     */
    public static class InMemory {

        /**
         * Whether to register the add-on in-memory sorting customization for LocalizedString attributes.
         */
        private boolean enabled = true;

        /**
         * Returns whether in-memory sorting customization is enabled.
         *
         * @return {@code true} if in-memory sorting customization is enabled
         */
        public boolean isEnabled() {
            return enabled;
        }

        /**
         * Sets whether in-memory sorting customization is enabled.
         *
         * @param enabled {@code true} to enable in-memory sorting customization
         */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
