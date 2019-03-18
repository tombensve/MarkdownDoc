/*
 *
 * PROJECT
 *     Name
 *         MarkdownDocEditor
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

/**
 * This represents a boolean config valueComp.
 */
@CompileStatic
@TypeChecked
class BooleanConfigEntry extends ConfigEntry {

    //
    // Constructors
    //

    /**
     * Creates a new ConfigEntry.
     *
     * @param key The config key.
     * @param description The description of the config.
     * @param defaultValue The default valueComp of the config.
     * @param configGroup The config group this config belongs to.
     */
    BooleanConfigEntry(@NotNull final String key, @NotNull final String description, final boolean defaultValue,
                       @NotNull final String configGroup) {

        super(key, description, "" + defaultValue, configGroup)
    }

    //
    // Methods
    //

    /**
     * Returns the valueComp as a boolean.
     */
    boolean getBooleanValue() {
        return Boolean.valueOf(getValue())
    }

    /**
     * Sets the valueComp as a boolean.
     *
     * @param value The valueComp to set.
     */
    void setBooleanValue(final boolean value) {
        setValue("" + value)
    }
}
