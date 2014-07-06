/* 
 * 
 * PROJECT
 *     Name
 *         MarkdownDocEditor
 *     
 *     Code Version
 *         1.3.3
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
 *     Licensed under the Apache License, Version 2.0 (the "License")
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

/**
 * This is a double config entry.
 */
@CompileStatic
public class DoubleConfigEntry extends ConfigEntry {
    //
    // Private Members
    //

    private double min = Double.MIN_VALUE
    private double max = Double.MAX_VALUE

    //
    // Constructors
    //

    /**
     * Creates a new DoubleConfigEntry.
     *
     * @param key The config key.
     * @param description The description of the config.
     * @param defaultValue The default valueComp of the config.
     * @param min The minimum valueComp.
     * @param max The maximum valueComp.
     * @param configGroup The config group this config belongs to.
     */
    public DoubleConfigEntry(String key, String description, double defaultValue, double min, double max, String configGroup) {
        super(key, description, "" + defaultValue, configGroup)
        this.min = min
        this.max = max
    }

    //
    // Methods
    //

    /**
     * Returns the valueComp as a double.
     */
    public double getDoubleValue() {
        return Double.valueOf(getValue())
    }

    /**
     * Sets the valueComp as a double.
     *
     * @param value The double valueComp to set.
     */
    public void setDoubleValue(double value) {
        setValue("" + value)
    }

    /**
     * Returns the minimum valueComp.
     */
    public double getMinValue() {
        return this.min
    }

    /**
     * Returns the maximum valueComp.
     */
    public double getMaxValue() {
        return this.max
    }
}
