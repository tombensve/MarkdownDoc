/* 
 * 
 * PROJECT
 *     Name
 *         MarkdownDocEditor
 *     
 *     Code Version
 *         1.3
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
package se.natusoft.doc.markdowndoc.editor.config;

/**
 * This is a ConfigEntry with a set of valid values.
 */
public class ValidSelectionConfigEntry extends ConfigEntry {
    //
    // Private Members
    //

    private ValidValues validValues;

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
        super(key, description, configGroup);
        this.validValues = validValues;
    }

    /**
     * Creates a new ConfigEntry.
     *
     * @param key The config key.
     * @param description The description of the config.
     * @param defaultValue The default value of the config.
     * @param validValues The valid values for the config.
     * @param configGroup The config group this config belongs to.
     */
    public ValidSelectionConfigEntry(String key, String description, String defaultValue, ValidValues validValues, String configGroup) {
        super(key, description, defaultValue, configGroup);
        this.validValues = validValues;
    }

    /**
     * Returns the valid values.
     */
    public String[] getValidValues() {
        return this.validValues.validValues();
    }

    //
    // Inner Classes
    //

    /**
     * Defines the valid values.
     */
    public interface ValidValues {
        String[] validValues();
    }
}
