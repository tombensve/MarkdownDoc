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
package se.natusoft.doc.markdowndoc.editor.api

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import se.natusoft.doc.markdowndoc.editor.config.ConfigEntry

/**
 * Allows for registering and getting config values.
 */
@CompileStatic
@TypeChecked
interface ConfigProvider {

    /**
     * This will populate the registered config entry with a user selected valueComp.
     *
     * @param configEntry The config entry to make available and get populated.
     * @param configChanged A ConfigChanged callback to add to the list of callbacks for the config.
     */
    void registerConfig(@NotNull ConfigEntry configEntry, @Nullable Closure configChanged)

    /**
     * Unregisters a configuration.
     *
     * @param configEntry The config entry to unregister a ConfigChanged callback for.
     * @param configChanged The ConfigChanged callback to unregister.
     */
    void unregisterConfig(@NotNull ConfigEntry configEntry, @Nullable Closure configChanged)

    /**
     * Returns a list of all registered configs.
     */
    @NotNull List<ConfigEntry> getConfigs()

    /**
     * Looks up a config entry by its key.
     *
     * @param key The key of the config entry to get.
     */
    @Nullable ConfigEntry lookupConfig(@NotNull String key)

    /**
     * Looks up all ConfigChanged callbacks for the config entry.
     *
     * @param configEntry The config entry to lookup ConfigChanged callbacks for.
     */
    @Nullable List<Closure> lookupConfigChanged(@NotNull ConfigEntry configEntry)

    /**
     * Refreshes all configs by triggering callbacks.
     */
    void refreshConfigs()

}
