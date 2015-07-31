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

import com.sun.xml.internal.ws.api.config.management.policy.ManagementAssertion
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.jetbrains.annotations.NotNull
import se.natusoft.doc.markdowndoc.editor.OS
import se.natusoft.doc.markdowndoc.editor.Services
import se.natusoft.doc.markdowndoc.editor.ToolBarGroups
import se.natusoft.doc.markdowndoc.editor.api.*
import se.natusoft.doc.markdowndoc.editor.config.ConfigEntry
import se.natusoft.doc.markdowndoc.editor.config.KeyConfigEntry
import se.natusoft.doc.markdowndoc.editor.config.KeyboardKey
import se.natusoft.doc.markdowndoc.editor.exceptions.FunctionException
import se.natusoft.doc.markdowndoc.editor.functions.utils.FileWindowProps
import se.natusoft.doc.markdowndoc.editor.gui.PopupWindowConfig
import se.natusoft.doc.markdowndoc.editor.gui.SettingsWindow

import javax.swing.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

import static se.natusoft.doc.markdowndoc.editor.config.Constants.CONFIG_GROUP_KEYBOARD

/**
 * Provides editorPane setting function.
 */
@CompileStatic
@TypeChecked
class SettingsFunction implements EditorFunction, Configurable, DelayedInitializer, OS {
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

    private float settingsOpacity = 1.0f

    private int bottomMargin = -1
    private int topMargin = -1

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

    private Closure keyboardShortcutConfigChanged = { final ConfigEntry ignored ->
        updateTooltipText()
    }

    private Closure settingsOpacityChanged = { @NotNull final ConfigEntry ce ->
        final int ival = Integer.valueOf(ce.value)
        final float fval = ((ival as float) / 100.0f) as float
        updateOpacity(fval)
    }

    private Closure settingsWindowTopMarginConfigChanged = { final ConfigEntry ce ->
        updateWindowTopMargin(Integer.valueOf(ce.value))
    }

    private Closure settingsWindowBottomMarginConfigChanged = { final ConfigEntry ce ->
        updateWindowBottomMargin(Integer.valueOf(ce.value))
    }

    /**
     * Register configurations.
     *
     * @param configProvider The config provider to register with.
     */
    @Override
    void registerConfigs(@NotNull final ConfigProvider configProvider) {
        configProvider.registerConfig(keyboardShortcutConfig, keyboardShortcutConfigChanged)
        configProvider.registerConfig(PopupWindowConfig.popupOpacityConfig, settingsOpacityChanged)
        configProvider.registerConfig(PopupWindowConfig.screenTopMargin, settingsWindowTopMarginConfigChanged)
        configProvider.registerConfig(PopupWindowConfig.screenBottomMargin, settingsWindowBottomMarginConfigChanged)
    }

    /**
     * Unregister configurations.
     *
     * @param configProvider The config provider to unregister with.
     */
    @Override
    void unregisterConfigs(@NotNull final ConfigProvider configProvider) {
        configProvider.unregisterConfig(keyboardShortcutConfig, keyboardShortcutConfigChanged)
        configProvider.unregisterConfig(PopupWindowConfig.popupOpacityConfig, settingsOpacityChanged)
        configProvider.unregisterConfig(PopupWindowConfig.screenTopMargin, settingsWindowTopMarginConfigChanged)
        configProvider.unregisterConfig(PopupWindowConfig.screenBottomMargin, settingsWindowBottomMarginConfigChanged)
    }

    //
    // Constructors
    //

    SettingsFunction() {
        final Icon settingsIcon = new ImageIcon(ClassLoader.getSystemResource("icons/mddsettings.png"))
        this.settingsButton = new JButton(settingsIcon)
        this.settingsButton.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(final ActionEvent ignored) {
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

    private void updateOpacity(float newOpacity) {
        if (newOpacity < 0.10f) {
            newOpacity = 0.10f
        }
        this.settingsOpacity = newOpacity
        if (this.settingsWindow != null) {
            this.settingsWindow.safeOpacity = this.settingsOpacity
        }
    }

    private void updateWindowTopMargin(final int topMargin) {
        this.topMargin = topMargin
        if (this.settingsWindow != null) {
            this.settingsWindow.updateBounds()
        }
    }

    private void updateWindowBottomMargin(final int bottomMargin) {
        this.bottomMargin = bottomMargin
        if (this.settingsWindow != null) {
            this.settingsWindow.updateBounds()
        }
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

    private Closure<Float> settingsOpacityProvider = {
        this.settingsOpacity
    }

    private Closure<Integer> bottomMarginProvider = {
        int val = this.bottomMargin
        if (val == -1) {
            if (windowsOS) {
                val = 70
            }
            else if (linuxOS) {
                val = 40
            }
            else {
                val = 0
            }
        }
        val
    }

    private Closure<Integer> topMarginProvider = {
        int val = this.topMargin
        if (val == -1) {
            val = 0
        }
        val
    }

    private Closure<Void> cancelSettingsProvider = {
        cancel()
    }

    private Closure<Void> saveSettingsProvider = {
        save()
    }

    @Override
    void perform() throws FunctionException {
        if (this.settingsWindow == null) {
            this.settingsWindow = new SettingsWindow(
                    windowOpacityProvider: this.settingsOpacityProvider,
                    topMarginProvider: this.topMarginProvider,
                    bottomMarginProvider: this.bottomMarginProvider,
                    cancelSettingsProvider: this.cancelSettingsProvider,
                    saveSettingsProvider: this.saveSettingsProvider
            )

            this.cancelValues = new HashMap<>()
            withAllConfigEntriesDo { final ConfigEntry configEntry ->
                this.settingsWindow.addConfig(configEntry)
                this.cancelValues.put(configEntry.getKey(), configEntry.getValue())
            }

            this.settingsWindow.windowVisibility = true
        }
        else {
            this.settingsWindow.visible = true
            this.settingsWindow.requestFocus()
        }
    }

    /**
     * Execute specified closure on all configs.
     *
     * @param closure The closure to run.
     */
    private static void withAllConfigEntriesDo(@NotNull final Closure closure) {
        Services.configs.getConfigs().each closure
    }

    /**
     * Cancel any changes made.
     */
    protected void cancel() {

        withAllConfigEntriesDo { final ConfigEntry configEntry ->
            configEntry.setValue(this.cancelValues.get(configEntry.getKey()))
        }

        this.settingsWindow = null
    }

    /**
     * Saves config to disk.
     */
    protected void save() {
        final Properties props = new Properties()
        withAllConfigEntriesDo { final ConfigEntry configEntry ->
            props.setProperty(configEntry.getKey(), configEntry.getValue())
        }

        Services.persistentPropertiesProvider.save(SETTINGS_PROP_NAME, props)

        // When we save, also remember the position and size of the editor window.
        final FileWindowProps fileWindowProps = new FileWindowProps()
        fileWindowProps.setBounds(this.editor.getGUI().getWindowFrame().getBounds())
        fileWindowProps.saveBounds()

        this.settingsWindow = null
    }

    /**
     * Loads config from disk.
     */
    private void load() {
        final Properties props = Services.persistentPropertiesProvider.load(SETTINGS_PROP_NAME)
        if (props != null) {
            props.stringPropertyNames().each { final String propName ->
                final String propValue = props.getProperty(propName)
                final ConfigEntry configEntry = Services.configs.lookupConfig(propName)
                if (configEntry != null) {
                    configEntry.setValue(propValue)
                }
            }
        }
        else {
            withAllConfigEntriesDo { final ConfigEntry configEntry ->
                configEntry.setValue(configEntry.getValue()) // force gui update
            }
        }

        SwingUtilities.updateComponentTreeUI(this.editor.getGUI().getWindowFrame())

        // Restore window position and size to last saved.
        final FileWindowProps fileWindowProps = new FileWindowProps()
        fileWindowProps.load()
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
