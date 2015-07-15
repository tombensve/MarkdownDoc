/*
 *
 * PROJECT
 *     Name
 *         MarkdownDocEditor
 *     
 *     Code Version
 *         1.4
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
package se.natusoft.doc.markdowndoc.editor.functions

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.jetbrains.annotations.NotNull
import se.natusoft.doc.markdowndoc.editor.ToolBarGroups
import se.natusoft.doc.markdowndoc.editor.api.*
import se.natusoft.doc.markdowndoc.editor.config.ConfigChanged
import se.natusoft.doc.markdowndoc.editor.config.ConfigEntry
import se.natusoft.doc.markdowndoc.editor.config.KeyConfigEntry
import se.natusoft.doc.markdowndoc.editor.config.KeyboardKey
import se.natusoft.doc.markdowndoc.editor.exceptions.FunctionException
import se.natusoft.doc.markdowndoc.editor.functions.settings.gui.SettingsWindow
import se.natusoft.doc.markdowndoc.editor.functions.utils.FileWindowProps

import javax.swing.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

import static se.natusoft.doc.markdowndoc.editor.config.Constants.CONFIG_GROUP_KEYBOARD

/**
 * Provides editorPane setting function.
 */
@CompileStatic
@TypeChecked
class SettingsFunction implements EditorFunction, Configurable, DelayedInitializer {
    //
    // Constants
    //

    private static final String SETTINGS_PROP_NAME = "editor-general-settings"

    //
    // Private Members
    //

    private JButton settingsButton = null
    private SettingsWindow settingsWindow = null

    private Map<String, String> cancelValues = null

    //
    // Properties
    //

    /** The editor this function is bound to. */
    Editor editor = null

    //
    // Config
    //

    private static final KeyConfigEntry keyboardShortcutConfig =
            new KeyConfigEntry("editor.function.settings.keyboard.shortcut", "Settings keyboard shortcut",
                    new KeyboardKey("Ctrl+E"), CONFIG_GROUP_KEYBOARD)

    private Closure keyboardShortcutConfigChanged = { ConfigEntry ce ->
        updateTooltipText()
    }

    /**
     * Register configurations.
     *
     * @param configProvider The config provider to register with.
     */
    @Override
    void registerConfigs(@NotNull ConfigProvider configProvider) {
        configProvider.registerConfig(keyboardShortcutConfig, keyboardShortcutConfigChanged)
    }

    /**
     * Unregister configurations.
     *
     * @param configProvider The config provider to unregister with.
     */
    @Override
    void unregisterConfigs(@NotNull ConfigProvider configProvider) {
        configProvider.unregisterConfig(keyboardShortcutConfig, keyboardShortcutConfigChanged)
    }

    //
    // Constructors
    //

    SettingsFunction() {
        Icon settingsIcon = new ImageIcon(ClassLoader.getSystemResource("icons/mddsettings.png"))
        this.settingsButton = new JButton(settingsIcon)
        this.settingsButton.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(ActionEvent ignored) {
                perform()
            }
        })
        updateTooltipText()
    }

    //
    // Methods
    //

    private void updateTooltipText() {
        this.settingsButton.setToolTipText("Settings (" + keyboardShortcutConfig.getKeyboardKey() + ")")
    }

    @Override
    @NotNull String getGroup() {
        ToolBarGroups.CONFIG.name()
    }

    @Override
    @NotNull String getName() {
        "Open settings"
    }

    @Override
    @NotNull JComponent getToolBarButton() {
        this.settingsButton
    }

    /**
     * Returns the keyboard shortcut for the function.
     */
    @Override
    @NotNull KeyboardKey getKeyboardShortcut() {
        keyboardShortcutConfig.getKeyboardKey()
    }

    @Override
    void perform() throws FunctionException {
        this.settingsWindow = new SettingsWindow() {

            @Override
            protected void cancelSettings() {
                cancel()
            }

            @Override
            protected void saveSettings() {
                save()
            }
        }

        this.cancelValues = new HashMap<>()
        withAllConfigEntriesDo { ConfigEntry configEntry ->
            this.settingsWindow.addConfig(configEntry)
            this.cancelValues.put(configEntry.getKey(), configEntry.getValue())
        }

        this.settingsWindow.setVisible(true)
    }

    /**
     * Execute specified closure on all configs.
     *
     * @param closure The closure to run.
     */
    private void withAllConfigEntriesDo(@NotNull Closure closure) {
        this.editor.getConfigProvider().getConfigs().each closure
    }

    /**
     * Cancel any changes made.
     */
    protected void cancel() {

        withAllConfigEntriesDo { ConfigEntry configEntry ->
            configEntry.setValue(this.cancelValues.get(configEntry.getKey()))
        }

    }

    /**
     * Saves config to disk.
     */
    protected void save() {
        Properties props = new Properties()
        withAllConfigEntriesDo { ConfigEntry configEntry ->
            props.setProperty(configEntry.getKey(), configEntry.getValue())
        }

        this.editor.getPersistentProps().save(SETTINGS_PROP_NAME, props)

        // When we save, also remember the position and size of the editor window.
        FileWindowProps fileWindowProps = new FileWindowProps()
        fileWindowProps.setBounds(this.editor.getGUI().getWindowFrame().getBounds())
        fileWindowProps.saveBounds(this.editor)
    }

    /**
     * Loads config from disk.
     */
    private void load() {
        Properties props = this.editor.getPersistentProps().load(SETTINGS_PROP_NAME)
        if (props != null) {
            props.stringPropertyNames().each { String propName ->
                String propValue = props.getProperty(propName)
                ConfigEntry configEntry = this.editor.getConfigProvider().lookupConfig(propName)
                if (configEntry != null) {
                    configEntry.setValue(propValue)
                }
            }
        }
        else {
            withAllConfigEntriesDo { ConfigEntry configEntry ->
                configEntry.setValue(configEntry.getValue()) // force gui update
            }
        }

        SwingUtilities.updateComponentTreeUI(this.editor.getGUI().getWindowFrame())

        // Restore window position and size to last saved.
        FileWindowProps fileWindowProps = new FileWindowProps()
        fileWindowProps.load(this.editor)
        if (fileWindowProps.hasProperties()) {
            this.editor.getGUI().getWindowFrame().setBounds(fileWindowProps.getBounds())
        }

    }

    /**
     * Cleanup and unregister any configs.
     */
    void close() {}

    /**
     * Initializes the component.
     */
    @Override
    void init() {
        load()
    }
}
