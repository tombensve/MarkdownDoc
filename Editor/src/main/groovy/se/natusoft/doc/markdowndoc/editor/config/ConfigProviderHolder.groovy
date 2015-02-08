/* 
 * 
 * PROJECT
 *     Name
 *         MarkdownDocEditor
 *     
 *     Code Version
 *         1.3.7
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
 * This class holds config entries.
 */
@CompileStatic
public class ConfigProviderHolder implements ConfigProvider, Iterable<ConfigEntry> {
    //
    // Private Members
    //

    /** Keeps the configs in order of registration. */
    private List<ConfigEntry> configList = new LinkedList<ConfigEntry>()

    /** The unique config entries saved on config key. */
    private Map<String, ConfigEntry> configMap = new HashMap<String, ConfigEntry>()

    /** Callbacks per config entry. */
    private Map<ConfigEntry, List<Closure>> configChangedCallbacks = new HashMap<ConfigEntry, List<Closure>>()

    //
    // Constructors
    //

    /**
     * Creates a new ConfigProviderHolder.
     */
    public ConfigProviderHolder() {}

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
        return this.configList.iterator()
    }

    /**
     * This will populate the registered config entry with a user selected valueComp.
     *
     * @param configEntry The config entry to make available and get populated.
     * @param configChanged A ConfigChanged callback to add to the list of callbacks for the config.
     */
    @Override
    public void registerConfig(ConfigEntry configEntry, Closure configChanged) {
        if (!this.configMap.containsKey(configEntry.getKey())) {
            configEntry.setConfigProvider(this)
            this.configMap.put(configEntry.getKey(), configEntry)
            this.configList.add(configEntry)
        }
        List<Closure> configChangedEntries = this.configChangedCallbacks.get(configEntry)
        if (configChangedEntries == null) {
            configChangedEntries = new LinkedList<Closure>()
            this.configChangedCallbacks.put(configEntry, configChangedEntries)
        }
        if (configChanged != null && !configChangedEntries.contains(configChanged)) {
            configChangedEntries.add(configChanged)
        }
    }

    /**
     * Unregisters a configuration.
     *
     * @param configEntry The config entry to unregister a ConfigChanged callback for.
     * @param configChanged The ConfigChanged callback to unregister.
     */
    @Override
    public void unregisterConfig(ConfigEntry configEntry, Closure configChanged) {
        List<Closure> configChangedEntries = this.configChangedCallbacks.get(configEntry)
        if (configChangedEntries != null && configChanged != null) {
            configChangedEntries.remove(configChanged)
        }
    }

    /**
     * Returns a list of all registered configs.
     */
    @Override
    public List<ConfigEntry> getConfigs() {
        return this.configList
    }

    /**
     * Looks up a config entry by its key.
     *
     * @param key The key of the config entry to get.
     */
    @Override
    public ConfigEntry lookupConfig(String key) {
        return this.configMap.get(key)
    }

    /**
     * Looks up all ConfigChanged callbacks for the config entry.
     *
     * @param configEntry The config entry to lookup ConfigChanged callbacks for.
     */
    @Override
    public List<Closure> lookupConfigChanged(ConfigEntry configEntry) {
        return this.configChangedCallbacks.get(configEntry)
    }

    /**
     * Refreshes all configs by triggering callbacks.
     */
    public void refreshConfigs() {
        getConfigs().each { ConfigEntry ce ->
            ce.setValue(ce.getValue())
        }
    }
}
