/*
 *
 * PROJECT
 *     Name
 *         MarkdownDocEditor
 *     
 *     Code Version
 *         2.1.1
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
import org.jetbrains.annotations.Nullable
import se.natusoft.doc.markdowndoc.editor.api.ConfigProvider

/**
 * This represents one config entry.
 */
@CompileStatic
@TypeChecked
class ConfigEntry {
    //
    // Private Members
    //

    /** The config key. */
    @NotNull String key

    /** Description of the config. */
    @NotNull String description

    /** The config valueComp. */
    @NotNull  String value = ""

    /** The config group this config belongs to. */
    @NotNull String configGroup = "Editor"

    /** The configuration provider. */
    @NotNull ConfigProvider configProvider

    //
    // Constructors
    //

    /**
     * Default constructor.
     */
    ConfigEntry() {}

    /**
     * Creates a new ConfigEntry.
     *
     * @param key         The config key.
     * @param description The description of the config.
     * @param configGroup The configuration group this config belongs to.
     */
    ConfigEntry(@NotNull final String key, @NotNull final String description, @NotNull final String configGroup) {
        this.key = key
        this.description = description
        this.configGroup = configGroup
    }

    /**
     * Creates a new ConfigEntry.
     *
     * @param key          The config key.
     * @param description  The description of the config.
     * @param defaultValue The default valueComp of the config.
     * @param configGroup The configuration group this config belongs to.
     */
    ConfigEntry(@NotNull final String key, @NotNull final String description, @NotNull final String defaultValue,
                @NotNull final String configGroup) {

        this(key, description, configGroup)
        this.value = defaultValue
    }

    //
    // Methods
    //

    /**
     * Returns the valueComp of the config as a boolean.
     */
    boolean getBoolValue() {
        Boolean.valueOf(this.value)
    }

    /**
     * Sets the valueComp of the config.
     *
     * @param value The valueComp to set.
     */
    void setValue(@NotNull final String value) {
        this.value = value
        refresh()
    }

    /**
     * Refresh all listeners of this.
     */
    void refresh() {
        this.configProvider.lookupConfigChanged(this).each { final Closure configChanged ->
            configChanged(this)
        }
    }

}
