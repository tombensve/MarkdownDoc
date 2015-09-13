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
import se.natusoft.doc.markdowndoc.editor.api.Editable
import se.natusoft.doc.markdowndoc.editor.api.Editor
import se.natusoft.doc.markdowndoc.editor.api.EditorFunction
import se.natusoft.doc.markdowndoc.editor.config.ConfigEntry
import se.natusoft.doc.markdowndoc.editor.config.KeyConfigEntry
import se.natusoft.doc.markdowndoc.editor.config.KeyboardKey
import se.natusoft.doc.markdowndoc.editor.exceptions.FunctionException
import se.natusoft.doc.markdowndoc.editor.file.Editables
import se.natusoft.doc.markdowndoc.editor.gui.GuiEnvToolsTrait
import se.natusoft.doc.markdowndoc.editor.gui.PopupWindow

import javax.swing.*
import javax.swing.border.EmptyBorder
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

import static se.natusoft.doc.markdowndoc.editor.config.Constants.CONFIG_GROUP_KEYBOARD

/**
 * Provides a selectNewFile function.
 */
@CompileStatic
@TypeChecked
class SaveFunction implements EditorFunction, Configurable, GuiEnvToolsTrait {
    //
    // Private Members
    //

    private JButton saveButton

    float popupOpacity = 1.0f

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

    private Closure keyboardShortcutConfigChanged = { final ConfigEntry ce ->
        updateTooltipText()
    }

    private Closure popupOpacityChanged = { final ConfigEntry ce ->
        final int ival = Integer.valueOf(ce.value)
        this.popupOpacity = ((ival as float) / 100.0f) as float
    }

    /**
     * Register configurations.
     *
     * @param configProvider The config provider to register with.
     */
    @Override
    void registerConfigs(@NotNull final ConfigProvider configProvider) {
        configProvider.registerConfig(keyboardShortcutConfig, keyboardShortcutConfigChanged)
        configProvider.registerConfig(PopupWindow.popupOpacityConfig, popupOpacityChanged)
    }

    /**
     * Unregister configurations.
     *
     * @param configProvider The config provider to unregister with.
     */
    @Override
    void unregisterConfigs(@NotNull final ConfigProvider configProvider) {
        configProvider.unregisterConfig(keyboardShortcutConfig, keyboardShortcutConfigChanged)
        configProvider.unregisterConfig(PopupWindow.popupOpacityConfig, popupOpacityChanged)
    }

    //
    // Constructors
    //

    SaveFunction() {
        final Icon saveIcon = new ImageIcon(ClassLoader.getSystemResource("icons/mddsave.png"))
        this.saveButton = new JButton(saveIcon)
        this.saveButton.addActionListener(new ActionListener() {
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
//            this.editor.save()
            int noSaved = 0
            Editables.inst.values().each { final Editable editable ->
                if (!editable.saved) {
                    editable.save()
                    noSaved = noSaved + 1
                }
            }
            showSavedInfo(noSaved)
            this.editor.requestEditorFocus()
        }
        catch (final IOException ioe) {
            JOptionPane.showMessageDialog(
                    this.editor.getGUI().getWindowFrame(),
                    ioe.getMessage(),
                    "Failed to save file(s)!",
                    JOptionPane.ERROR_MESSAGE
            )

        }
    }

    /**
     * Shows "Saved!" for 2,5 seconds up in the left cornet of the edit window.
     */
    private void showSavedInfo(final int noSaved) {
        new Thread() {
            @SuppressWarnings("UnnecessaryQualifiedReference")
            @Override
            void run() {
                final int x = editor.getGUI().getWindowFrame().getX()
                final int y = editor.getGUI().getWindowFrame().getY()

                final JWindow window = new JWindow(editor.getGUI().getWindowFrame())
                initGuiEnvTools(window)
                SaveFunction.this.safeOpacity = SaveFunction.this.popupOpacity
                window.layout = new BorderLayout()

                final JPanel panel = new JPanel(new BorderLayout())
                panel.background = Color.BLACK
                panel.foreground = Color.WHITE
                //panel.border = new SoftBevelBorder(SoftBevelBorder.RAISED)
                panel.border = new EmptyBorder(3, 6, 3, 6)
                window.add(panel, BorderLayout.CENTER)
                final JLabel label = new JLabel("Saved ${noSaved} files!")
                label.background = Color.BLACK
                label.foreground = Color.WHITE
                panel.add(label)
                window.location = new Point(x + 10, y + 30)
                window.visible = true
                window.size = window.preferredSize
                try { Thread.sleep(2500) } catch (final Exception ignored) {}
                window.visible = false
            }
        }.start()
    }

    /**
     * Cleanup and unregister any configs.
     */
    void close() {}
}
