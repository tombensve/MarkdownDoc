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

import se.natusoft.doc.markdowndoc.editor.api.ConfigProvider;

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

    private ConfigProvider configProvider;

    //
    // Constructors
    //

    /**
     * Creates a new ConfigEntry.
     *
     * @param key         The config key.
     * @param description The description of the config.
     */
    public ConfigEntry(String key, String description) {
        this.key = key;
        this.description = description;
    }

    /**
     * Creates a new ConfigEntry.
     *
     * @param key          The config key.
     * @param description  The description of the config.
     * @param defaultValue The default value of the config.
     */
    public ConfigEntry(String key, String description, String defaultValue) {
        this(key, description);
        this.value = defaultValue;
    }

    //
    // Methods
    //

    /**
     * Receives the config provided instance managing all configs.
     *
     * @param configProvider The received config provider.
     */
    public void setConfigProvider(ConfigProvider configProvider) {
        this.configProvider = configProvider;
    }

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
        for (ConfigChanged callback : this.configProvider.lookupConfigChanged(this)) {
            callback.configChanged(this);
        }
    }

    /**
     * Returns the description of the value.
     */
    public String getDescription() {
        return this.description;
    }

}
