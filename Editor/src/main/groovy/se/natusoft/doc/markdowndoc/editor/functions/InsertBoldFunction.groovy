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

import javax.swing.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

import static se.natusoft.doc.markdowndoc.editor.config.Constants.CONFIG_GROUP_KEYBOARD

/**
 * This provides a function that inserts bold formatting.
 */
@CompileStatic
class InsertBoldFunction implements EditorFunction, Configurable {
    //
    // Private Members
    //

    private JButton boldButton

    //
    // Properties
    //

    /** The editor this function is bound to. */
    @Nullable Editor editor

    //
    // Config
    //

    private static final KeyConfigEntry keyboardShortcutConfig =
            new KeyConfigEntry("editor.function.format.bold.keyboard.shortcut", "Bold format keyboard shortcut",
                    new KeyboardKey("Ctrl+B"), CONFIG_GROUP_KEYBOARD)

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
    }

    /**
     * Unregister configurations.
     *
     * @param configProvider The config provider to unregister with.
     */
    @Override
    void unregisterConfigs(@NotNull final ConfigProvider configProvider) {
        configProvider.unregisterConfig(keyboardShortcutConfig, keyboardShortcutConfigChanged)
    }

    //
    // Constructors
    //

    InsertBoldFunction() {
        final Icon boldIcon = new ImageIcon(ClassLoader.getSystemResource("icons/mdd2bold.png"))
        this.boldButton = new JButton(boldIcon)
        boldButton.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(final ActionEvent actionEvent) {
                perform()
            }
        })
        updateTooltipText()
    }

    //
    // Methods
    //

    private void updateTooltipText() {
        boldButton.setToolTipText("Bold (" + keyboardShortcutConfig.getKeyboardKey() + ")")
    }

    @Override
    @NotNull String getGroup() {
        ToolBarGroups.FORMAT.name()
    }

    @Override
    @NotNull String getName() {
        "Insert bold format"
    }

    @Override
    @NotNull JComponent getToolBarButton() {
        this.boldButton
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
        if (this.editor.getEditorSelection() != null) {
            this.editor.insertText("**" + this.editor.getEditorSelection() + "**")
        }
        else {
            this.editor.insertText("****")
            this.editor.moveCaretBack(2)
        }
        this.editor.requestEditorFocus()
    }

    /**
     * Cleanup and unregister any configs.
     */
    void close() {}
}
