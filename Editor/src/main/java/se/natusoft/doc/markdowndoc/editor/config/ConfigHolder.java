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

import se.natusoft.doc.markdowndoc.editor.api.Config;

import java.util.*;

/**
 * This class holds config entries.
 */
public class ConfigHolder implements Config, Iterable<ConfigEntry> {
    //
    // Private Members
    //

    private List<ConfigEntry> configList = new LinkedList<ConfigEntry>();
    private Map<String, ConfigEntry> configMap = new HashMap<String, ConfigEntry>();

    //
    // Constructors
    //

    /**
     * Creates a new ConfigHolder.
     */
    public ConfigHolder() {}

    //
    // Methods
    //

    /**
     * Returns an iterator over a set of elements of type T.
     *
     * @return an Iterator.
     */
    @Override
    public Iterator<ConfigEntry> iterator() {
        return this.configList.iterator();
    }

    /**
     * This will populate the registered config entry with a user selected value.
     *
     * @param configEntry The config entry to make available and get populated.
     */
    @Override
    public void registerConfig(ConfigEntry configEntry) {
        this.configList.add(configEntry);
        this.configMap.put(configEntry.getKey(), configEntry);
    }

    /**
     * Returns a list of all registered configs.
     */
    @Override
    public List<ConfigEntry> getConfigs() {
        return this.configList;
    }

    /**
     * Looks up a config entry by its key.
     *
     * @param key The key of the config entry to get.
     */
    @Override
    public ConfigEntry lookupConfig(String key) {
        return this.configMap.get(key);
    }
}
