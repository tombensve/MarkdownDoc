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
 * Provides an insert heading function.
 */
@CompileStatic
@TypeChecked
class InsertHeadingFunction implements EditorFunction, Configurable {
    //
    // Private Members
    //

    private JButton headerButton

    //
    // Properties
    //

    /** The editor to which this function is bound. */
    @Nullable Editor editor

    //
    // Config
    //

    private static final KeyConfigEntry keyboardShortcutConfig =
            new KeyConfigEntry("editor.function.format.heading.keyboard.shortcut", "Heading format keyboard shortcut",
                    new KeyboardKey("Ctrl+T"), CONFIG_GROUP_KEYBOARD)

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

    InsertHeadingFunction() {
        final Icon headingIcon = new ImageIcon(ClassLoader.getSystemResource("icons/mdd2heading.png"))
        this.headerButton = new JButton(headingIcon)
        headerButton.addActionListener(new ActionListener() {
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
        headerButton.setToolTipText("Heading (" + keyboardShortcutConfig.getKeyboardKey() + ")")
    }

    @Override
    @NotNull String getGroup() {
        ToolBarGroups.FORMAT.name()
    }

    @Override
    @NotNull String getName() {
        "Insert heading format"
    }

    @Override
    @NotNull JComponent getToolBarButton() {
        this.headerButton
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
        this.editor.insertText("#")
        this.editor.requestEditorFocus()
    }

    /**
     * Cleanup and unregister any configs.
     */
    @Override
    void close() {}
}
