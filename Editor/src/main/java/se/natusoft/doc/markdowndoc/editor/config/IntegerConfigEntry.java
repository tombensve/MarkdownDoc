/* 
 * 
 * PROJECT
 *     Name
 *         MarkdownDocEditor
 *     
 *     Code Version
 *         1.2.9
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
 * This is a integer config entry.
 */
public class IntegerConfigEntry extends ConfigEntry {
    //
    // Private Members
    //

    private int min = Integer.MIN_VALUE;
    private int max = Integer.MAX_VALUE;

    //
    // Constructors
    //

    /**
     * Creates a new ConfigEntry.
     *
     * @param key The config key.
     * @param description The description of the config.
     */
    public IntegerConfigEntry(String key, String description) {
        super(key, description);
    }

    /**
     * Creates a new ConfigEntry.
     *
     * @param key The config key.
     * @param description The description of the config.
     * @param defaultValue The default value of the config.
     * @param min The minimum value.
     * @param max The maximum value.
     */
    public IntegerConfigEntry(String key, String description, int defaultValue, int min, int max) {
        super(key, description, "" + defaultValue);
        this.min = min;
        this.max = max;
    }

    //
    // Methods
    //

    /**
     * Returns the value as an int.
     */
    public int getIntValue() {
        return Integer.valueOf(getValue());
    }

    /**
     * Sets the value as an int.
     *
     * @param value The int value to set.
     */
    public void setIntValue(int value) {
        setValue("" + value);
    }

    /**
     * Returns the minimum value.
     */
    public int getMinValue() {
        return this.min;
    }

    /**
     * Returns the maximum value.
     */
    public int getMaxValue() {
        return this.max;
    }
}
