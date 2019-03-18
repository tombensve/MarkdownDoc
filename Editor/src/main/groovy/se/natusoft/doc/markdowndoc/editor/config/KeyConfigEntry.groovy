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
 *         2014-02-15: Created!
 *
 */
package se.natusoft.doc.markdowndoc.editor.config

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.jetbrains.annotations.NotNull

/**
 * This ConfigEntry represents a keyboard key to activate a function.
 */
@CompileStatic
@TypeChecked
class KeyConfigEntry extends ConfigEntry {

    //
    // Constructors
    //

    /**
     * Creates a new KeyConfigEntry.
     *
     * @param key The config key.
     * @param description The description of the config.
     * @param defaultValue The default valueComp.
     * @param configGroup The config group this config belongs to.
     */
    KeyConfigEntry(
            @NotNull final String key,
            @NotNull final String description,
            @NotNull final KeyboardKey defaultValue,
            @NotNull final String configGroup
    ) {
        super(key, description, defaultValue.toString(), configGroup)
    }

    //
    // Methods
    //


    /**
     * Returns the keyboard key.
     */
    @NotNull KeyboardKey getKeyboardKey() {
        new KeyboardKey(getValue())
    }

    /**
     * Sets a keyboard key.
     *
     * @param keyboardKey The keyboard key to set.
     */
    void setKeyboardKey(@NotNull final KeyboardKey keyboardKey) {
        setValue(keyboardKey.toString())
    }
}
