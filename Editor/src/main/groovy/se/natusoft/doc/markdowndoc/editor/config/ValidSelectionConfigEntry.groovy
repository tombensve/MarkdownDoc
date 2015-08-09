/*
 *
 * PROJECT
 *     Name
 *         MarkdownDocEditor
 *
 *     Code Version
 *         1.4
 *
 *     Description
 *         An editor that supports editing markdown with formatting preview.
 *
 * COPYRIGHTS
 *     Copyright (C) 2012 by Natusoft AB All rights reserved.
 *
 * LICENSE
 *     Apache 2.0 (Open Source)
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 * AUTHORS
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2013-05-27: Created!
 *
 */
package se.natusoft.doc.markdowndoc.editor.config

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.jetbrains.annotations.NotNull

import java.lang.String

/**
 * This is a ConfigEntry with a set of valid values.
 */
@CompileStatic
@TypeChecked
class ValidSelectionConfigEntry extends ConfigEntry {
    //
    // Private Members
    //

    private ValidValues validValues

    //
    // Constructors
    //

    /**
     * Creates a new ConfigEntry.
     *
     * @param key The config key.
     * @param description The description of the config.
     * @param validValues The valid values for the config.
     * @param configGroup The config group this config belongs to.
     */
    ValidSelectionConfigEntry(
            @NotNull final String key,
            @NotNull final String description,
            @NotNull final ValidValues validValues,
            @NotNull final String configGroup
    ) {
        super(key, description, configGroup)
        this.validValues = validValues
    }

    /**
     * Creates a new ConfigEntry.
     *
     * @param key The config key.
     * @param description The description of the config.
     * @param defaultValue The default valueComp of the config.
     * @param validValues The valid values for the config.
     * @param configGroup The config group this config belongs to.
     */
    ValidSelectionConfigEntry(
            @NotNull final String key,
            @NotNull final String description,
            @NotNull final String defaultValue,
            @NotNull final ValidValues validValues,
            @NotNull final String configGroup
    ) {
        super(key, description, defaultValue, configGroup)
        this.validValues = validValues
    }

    /**
     * Returns the valid values.
     */
    @NotNull Value[] getValidValues() {
        this.validValues.validValues()
    }

    /**
     * Returns a show adapted valueComp for display in settings.
     */
    @NotNull String getShowValue() {
        String value = getValue()
        if (value.indexOf('.') > 0) {
            value = value.substring(value.lastIndexOf('.') + 1)
        }

        value
    }

    //
    // Static utilities
    //

    /**
     * A convenience to convert a String List to a String array.
     *
     * @param strings The String List to convert.
     */
    static @NotNull String[] stringListToArray(@NotNull final List<String> strings) {
        final String[] array = new String[strings.size()]
        strings.toArray(array)
    }

    /**
     * Convenience to convert a String array to a Value array.
     *
     * @param stringValues The string array to convert.
     */
    static @NotNull Value[] convertToValues(@NotNull final String[] stringValues) {
        final Value[] values = new Value[stringValues.length]
        for (int i = 0; i < stringValues.length; i++) {
            values[i] = new Value(stringValues[i])
        }

        values
    }

    /**
     * Convenience to convert a String array to a Value array providing both use and show by
     * making show be the last part after the last occurance of the specified character.
     *
     * @param stringValues The string array to convert.
     * @param cutAtLast The character to find last of and use text after as show text.
     */
    static @NotNull Value[] convertToValues(@NotNull final String[] stringValues, @NotNull final String cutAtLast) {
        final Value[] values = new Value[stringValues.length]
        for (int i = 0; i < stringValues.length; i++) {
            String use = stringValues[i]
            String show = use.substring(use.lastIndexOf(cutAtLast) + 1)
            values[i] = new Value(show, use)
        }

        values
    }

    //
    // Inner Classes
    //

    /**
     * The point of this is to be able to show a nicer text to the user and associate it with the real
     * but less user friendly valueComp. LookAndFeel and classes loaded with ServiceLoader where there is
     * a choice of which to use are all class names with full package path. In this case the show part
     * is passed as everything after the last '.' character. This makes it slightly more user friendly.
     */
    static class Value {
        private String show
        private String use

        /**
         * Creates a new Value.
         *
         * @param show The part of the valueComp to show in the gui.
         * @param use The real/full valueComp to store in config and use.
         */
        Value(@NotNull final String show, @NotNull final String use) {
            this.show = show
            this.use = use
        }

        /**
         * Creates a new Value.
         *
         * @param use The real valueComp to use. This is also set as show valueComp.
         */
        Value(@NotNull final String use) {
            this.show = use
            this.use = use
        }

        /**
         * Returns the show valueComp.
         */
        @NotNull String getShow() {
            this.show
        }

        /**
         * Returns the use valueComp.
         */
        @NotNull String getUse() {
            this.use
        }

        /**
         * Returns the show valueComp. This so that a Value array can be passed as values to a JComboBox and it
         * will only display the show valueComp in the gui.
         */
        @NotNull String toString() {
            this.show
        }

        /**
         * This is needed for JComboBox.setSelectedItem(...) to match existing Value instance with new Value wrapped around loaded config
         * valueComp. This is why it compares on show and only show.
         *
         * @param o The object to compare to.
         */
        boolean equals(@NotNull final Object o) {
            o instanceof Value && ((Value) o).show.equals(this.show)
        }
    }

    /**
     * Defines the valid values.
     */
    interface ValidValues {
        @NotNull Value[] validValues()
    }
}
