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
 *     tommy ()
 *         Changes:
 *         2015-08-03: Created!
 *
 */
package se.natusoft.doc.markdowndoc.editor.functions

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import se.natusoft.doc.markdowndoc.editor.ToolBarGroups
import se.natusoft.doc.markdowndoc.editor.api.ConfigProvider
import se.natusoft.doc.markdowndoc.editor.api.Configurable
import se.natusoft.doc.markdowndoc.editor.api.Editor
import se.natusoft.doc.markdowndoc.editor.api.EditorFunction
import se.natusoft.doc.markdowndoc.editor.config.ConfigEntry
import se.natusoft.doc.markdowndoc.editor.config.KeyConfigEntry
import se.natusoft.doc.markdowndoc.editor.config.KeyboardKey
import se.natusoft.doc.markdowndoc.editor.exceptions.FunctionException
import se.natusoft.doc.markdowndoc.editor.gui.EditableSelectorPopup
import se.natusoft.doc.markdowndoc.editor.gui.PopupLock

import javax.swing.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionListener

import static se.natusoft.doc.markdowndoc.editor.config.Constants.CONFIG_GROUP_KEYBOARD

/**
 * This function pops up a list of all loaded editables and lets the user select one
 * to edit.
 */
@CompileStatic
@TypeChecked
class SelectEditableFunction implements EditorFunction, Configurable {
    //
    // Private Members
    //

    private Editor editor

    private MouseMotionListener mouseMotionListener = null

    private EditableSelectorPopup popup = null

    private ConfigProvider configProvider = null

    private JButton selectEditableButton = null

    //
    // Configs
    //

    private static final KeyConfigEntry keyboardShortcutConfig =
            new KeyConfigEntry("editor.function.editables.keyboard.shortcut", "Editable files list keyboard shortcut",
                    new KeyboardKey("Ctrl+1"), CONFIG_GROUP_KEYBOARD)

    private Closure keyboardShortcutConfigChanged = { final ConfigEntry ce ->
        updateTooltipText()
    }

    /**
     * Register configurations.
     *
     * @param configProvider The config provider to register with.
     */
    @Override
    void registerConfigs(@NotNull final ConfigProvider configProvider) {
        configProvider.registerConfig(keyboardShortcutConfig, keyboardShortcutConfigChanged)
        this.configProvider = configProvider
    }

    /**
     * Unregister configurations.
     *
     * @param configProvider The config provider to unregister with.
     */
    @Override
    void unregisterConfigs(@NotNull final ConfigProvider configProvider) {
        configProvider.unregisterConfig(keyboardShortcutConfig, keyboardShortcutConfigChanged)
        if (this.popup != null) {
            this.popup.unregisterConfigs(configProvider)
        }
    }

    //
    // Constructors
    //

    public SelectEditableFunction() {
        final Icon selectEditableIcon = new ImageIcon(ClassLoader.getSystemResource("icons/mdd2openfiles.png"))
        this.selectEditableButton = new JButton(selectEditableIcon)
        this.selectEditableButton.addActionListener(new ActionListener() {
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

    /**
     * Returns the group in the tool bar this functions should be placed in.
     * A new group will be created if the named group does not exist.
     */
    @Override
    String getGroup() {
        ToolBarGroups.FILE.name()
    }

    /**
     * Returns the name of the function.
     */
    @Override
    String getName() {
        "Select file to work with"
    }

    /**
     * Returns this functions toolbar button or null if it does not have one.
     */
    @Override
    JComponent getToolBarButton() {
        this.selectEditableButton
    }

    /**
     * Returns the keyboard shortcut for triggering the function via keyboard.
     */
    @Override
    KeyboardKey getKeyboardShortcut() {
        keyboardShortcutConfig.keyboardKey
    }

    /**
     * Performs the function.
     *
     * @throws FunctionException
     */
    @Override
    void perform() throws FunctionException {
        this.popup = new EditableSelectorPopup(editor: this.editor, closer: { close() } )
        this.popup.registerConfigs(this.configProvider)
        this.configProvider.refreshConfigs()
        this.popup.showWindow()
    }

    /**
     * Sets the editorPane for the component to use.
     *
     * @param editor The editorPane to set.
     */
    @Override
    void setEditor(@Nullable final Editor editor) {
        this.editor = editor
    }

    private void updateTooltipText() {
        this.selectEditableButton.setToolTipText("Select current file (" + keyboardShortcutConfig.getKeyboardKey() + ")")
    }

    /**
     * Called when instance is no longer needed.
     */
    @Override
    void close() {
        if (this.popup != null) {
            this.popup.visible = false
            this.popup.unregisterConfigs(this.configProvider)
            this.popup = null
        }
    }

}
