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
import se.natusoft.doc.markdowndoc.editor.api.ConfigProvider
import se.natusoft.doc.markdowndoc.editor.api.Configurable
import se.natusoft.doc.markdowndoc.editor.api.Editor
import se.natusoft.doc.markdowndoc.editor.api.EditorFunction
import se.natusoft.doc.markdowndoc.editor.config.ConfigEntry
import se.natusoft.doc.markdowndoc.editor.config.KeyConfigEntry
import se.natusoft.doc.markdowndoc.editor.config.KeyboardKey
import se.natusoft.doc.markdowndoc.editor.exceptions.FunctionException

import javax.swing.*
import javax.swing.border.SoftBevelBorder
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

import static se.natusoft.doc.markdowndoc.editor.config.Constants.CONFIG_GROUP_KEYBOARD

/**
 * Provides a selectNewFile function.
 */
@CompileStatic
@TypeChecked
class SaveFunction implements EditorFunction, Configurable {
    //
    // Private Members
    //

    private JButton saveButton

    //
    // Properties
    //

    /** The editor this function is bound to. */
    Editor editor

    //
    // Config
    //

    private static final KeyConfigEntry keyboardShortcutConfig =
            new KeyConfigEntry("editor.function.selectNewFile.keyboard.shortcut", "Save keyboard shortcut",
                    new KeyboardKey("Ctrl+S"), CONFIG_GROUP_KEYBOARD)

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

    SaveFunction() {
        Icon saveIcon = new ImageIcon(ClassLoader.getSystemResource("icons/mddsave.png"))
        this.saveButton = new JButton(saveIcon)
        this.saveButton.addActionListener(new ActionListener() {
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
        this.saveButton.setToolTipText("Save (" + keyboardShortcutConfig.getKeyboardKey() + ")")
    }

    @Override
    @NotNull String getGroup() {
        ToolBarGroups.FILE.name()
    }

    @Override
    @NotNull String getName() {
        "Save file"
    }

    @Override
    @NotNull JComponent getToolBarButton() {
        this.saveButton
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
        try {
            this.editor.save()
            showSavedInfo()
            this.editor.requestEditorFocus()
        }
        catch (IOException ioe) {
            JOptionPane.showMessageDialog(
                    this.editor.getGUI().getWindowFrame(), ioe.getMessage(), "Failed to selectNewFile!", JOptionPane.ERROR_MESSAGE)

        }
    }

    /**
     * Shows "Saved!" for 2,5 seconds up in the left cornet of the edit window.
     */
    private void showSavedInfo() {
        new Thread() {
            @SuppressWarnings("UnnecessaryQualifiedReference")
            @Override
            void run() {
                int x = editor.getGUI().getWindowFrame().getX()
                int y = editor.getGUI().getWindowFrame().getY()

                JWindow window = new JWindow(editor.getGUI().getWindowFrame())
                window.setLayout(new BorderLayout())
                JPanel panel = new JPanel(new BorderLayout())
                panel.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED))
                window.add(panel, BorderLayout.CENTER)
                panel.add(new JLabel("Saved!"))
                window.setLocation(x + 10, y + 30)
                window.setVisible(true)
                window.setSize(window.getPreferredSize())
                try { Thread.sleep(2500) } catch (Exception ignored) {}
                window.setVisible(false)
            }
        }.start()
    }

    /**
     * Cleanup and unregister any configs.
     */
    void close() {}
}
