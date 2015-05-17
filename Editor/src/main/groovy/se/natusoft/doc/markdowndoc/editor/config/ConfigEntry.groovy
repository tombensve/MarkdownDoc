/* 
 * 
 * PROJECT
 *     Name
 *         MarkdownDocEditor
 *     
 *     Code Version
 *         1.3.9
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
import se.natusoft.doc.markdowndoc.editor.api.ConfigProvider

/**
 * This represents one config entry.
 */
@CompileStatic
public class ConfigEntry {
    //
    // Private Members
    //

    /** The config key. */
    String key
    /** Description of the config. */
    String description
    /** The config valueComp. */
    String value = ""
    /** The config froup this config belongs to. */
    String configGroup = "Editor"

    /** The configuration provider. */
    ConfigProvider configProvider

    //
    // Constructors
    //

    /**
     * Default constructor.
     */
    public ConfigEntry() {}

    /**
     * Creates a new ConfigEntry.
     *
     * @param key         The config key.
     * @param description The description of the config.
     * @param configGroup The configuration group this config belongs to.
     */
    public ConfigEntry(String key, String description, String configGroup) {
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
    public ConfigEntry(String key, String description, String defaultValue, String configGroup) {
        this(key, description, configGroup)
        this.value = defaultValue
    }

    //
    // Methods
    //

    /**
     * Returns the valueComp of the config as a boolean.
     */
    public boolean getBoolValue() {
        return Boolean.valueOf(this.value)
    }

    /**
     * Sets the valueComp of the config.
     *
     * @param value The valueComp to set.
     */
    public void setValue(String value) {
        this.value = value
        this.configProvider.lookupConfigChanged(this).each { Closure configChanged ->
            configChanged(this)
        }
    }

}
