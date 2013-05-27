/* 
 * 
 * PROJECT
 *     Name
 *         Editor
 *     
 *     Code Version
 *         1.2.6
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
 * This represents one config entry.
 */
public class ConfigEntry {
    //
    // Private Members
    //

    private String key;
    private String description;
    private String value = "";
    private ConfigChanged configChanged = null;

    //
    // Constructors
    //

    /**
     * Creates a new ConfigEntry.
     *
     * @param key The config key.
     * @param description The description of the config.
     * @param configChanged Called when config has changed.
     */
    public ConfigEntry(String key, String description, ConfigChanged configChanged) {
        this.key = key;
        this.description = description;
        this.configChanged = configChanged;
    }

    /**
     * Creates a new ConfigEntry.
     *
     * @param key The config key.
     * @param description The description of the config.
     * @param defaultValue The default value of the config.
     * @param configChanged Called when config has changed.
     */
    public ConfigEntry(String key, String description, String defaultValue, ConfigChanged configChanged) {
        this(key, description, configChanged);
        this.value = defaultValue;
    }

    //
    // Methods
    //

    /**
     * Returns the key of the config.
     */
    public String getKey() {
        return this.key;
    }

    /**
     * Returns the value of the config.
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Returns the value of the config as a boolean.
     */
    public boolean getBoolValue() {
        return Boolean.valueOf(this.value);
    }

    /**
     * Sets the value of the config.
     *
     * @param value The value to set.
     */
    public void setValue(String value) {
        this.value = value;
        this.configChanged.configChanged(this);
    }

    /**
     * Returns the description of the value.
     */
    public String getDescription() {
        return this.description;
    }

    //
    // Inner Classes
    //

    /**
     * This is called when a config value is changed.
     */
    public interface ConfigChanged {
        /**
         * This delivers the changed config entry.
         */
        public void configChanged(ConfigEntry ce);
    }
}
