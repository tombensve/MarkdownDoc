/* 
 * 
 * PROJECT
 *     Name
 *         MarkdownDocEditor
 *     
 *     Code Version
 *         1.3.7
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
import java.lang.String

/**
 * This is a ConfigEntry with a set of valid values.
 */
@CompileStatic
public class ValidSelectionConfigEntry extends ConfigEntry {
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
    public ValidSelectionConfigEntry(String key, String description, ValidValues validValues, String configGroup) {
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
    public ValidSelectionConfigEntry(String key, String description, String defaultValue, ValidValues validValues, String configGroup) {
        super(key, description, defaultValue, configGroup)
        this.validValues = validValues
    }

    /**
     * Returns the valid values.
     */
    public Value[] getValidValues() {
        return this.validValues.validValues()
    }

    /**
     * Returns a show adapted valueComp for display in settings.
     */
    public String getShowValue() {
        String value = getValue()
        if (value.indexOf('.') > 0) {
            value = value.substring(value.lastIndexOf('.') + 1)
        }

        return value
    }

    //
    // Static utilities
    //

    /**
     * A convenience to convert a String List to a String array.
     *
     * @param strings The String List to convert.
     */
    public static String[] stringListToArray(List<String> strings) {
        String[] array = new String[strings.size()]
        return strings.toArray(array)
    }

    /**
     * Convenience to convert a String array to a Value array.
     *
     * @param stringValues The string array to convert.
     */
    public static Value[] convertToValues(String[] stringValues) {
        Value[] values = new Value[stringValues.length]
        for (int i = 0; i < stringValues.length; i++) {
            values[i] = new Value(stringValues[i])
        }

        return values
    }

    /**
     * Convenience to convert a String array to a Value array providing both use and show by
     * making show be the last part after the last occurance of the specified character.
     *
     * @param stringValues The string array to convert.
     * @param cutAtLast The character to find last of and use text after as show text.
     */
    public static Value[] convertToValues(String[] stringValues, String cutAtLast) {
        Value[] values = new Value[stringValues.length]
        for (int i = 0; i < stringValues.length; i++) {
            String use = stringValues[i]
            String show = use.substring(use.lastIndexOf(cutAtLast) + 1)
            values[i] = new Value(show, use)
        }

        return values
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
    public static class Value {
        private String show
        private String use

        /**
         * Creates a new Value.
         *
         * @param show The part of the valueComp to show in the gui.
         * @param use The real/full valueComp to store in config and use.
         */
        public Value(String show, String use) {
            this.show = show
            this.use = use
        }

        /**
         * Creates a new Value.
         *
         * @param use The real valueComp to use. This is also set as show valueComp.
         */
        public Value(String use) {
            this.show = use
            this.use = use
        }

        /**
         * Returns the show valueComp.
         */
        public String getShow() {
            return this.show
        }

        /**
         * Returns the use valueComp.
         */
        public String getUse() {
            return this.use
        }

        /**
         * Returns the show valueComp. This so that a Value array can be passed as values to a JComboBox and it
         * will only display the show valueComp in the gui.
         */
        public String toString() {
            return this.show
        }

        /**
         * This is needed for JComboBox.setSelectedItem(...) to match existing Value instance with new Value wrapped around loaded config
         * valueComp. This is why it compares on show and only show.
         *
         * @param o The object to compare to.
         */
        public boolean equals(Object o) {
            return o instanceof Value && ((Value) o).show.equals(this.show)
        }
    }

    /**
     * Defines the valid values.
     */
    public interface ValidValues {
        Value[] validValues()
    }
}
