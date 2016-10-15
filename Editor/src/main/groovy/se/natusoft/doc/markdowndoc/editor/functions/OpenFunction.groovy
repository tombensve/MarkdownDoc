/*
 *
 * PROJECT
 *     Name
 *         MarkdownDocEditor
 *     
 *     Code Version
 *         2.0.0
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
import se.natusoft.doc.markdowndoc.editor.MarkdownDocEditor
import se.natusoft.doc.markdowndoc.editor.ToolBarGroups
import se.natusoft.doc.markdowndoc.editor.api.ConfigProvider
import se.natusoft.doc.markdowndoc.editor.api.Configurable
import se.natusoft.doc.markdowndoc.editor.api.Editor
import se.natusoft.doc.markdowndoc.editor.api.EditorFunction
import se.natusoft.doc.markdowndoc.editor.config.ConfigEntry
import se.natusoft.doc.markdowndoc.editor.config.KeyConfigEntry
import se.natusoft.doc.markdowndoc.editor.config.KeyboardKey
import se.natusoft.doc.markdowndoc.editor.exceptions.FunctionException
import se.natusoft.doc.markdowndoc.editor.file.Editables

import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

import static se.natusoft.doc.markdowndoc.editor.config.Constants.CONFIG_GROUP_KEYBOARD

/**
 * Provides an open function.
 */
@CompileStatic
@TypeChecked
class OpenFunction implements EditorFunction, Configurable {
    //
    // Private Members
    //

    private JButton openButton

    //
    // Properties
    //

    /** The editor this function is bound to. */
    Editor editor

    //
    // Config
    //

    private static final KeyConfigEntry keyboardShortcutConfig =
            new KeyConfigEntry("editor.function.open.keyboard.shortcut", "Open keyboard shortcut",
                    new KeyboardKey("Ctrl+O"), CONFIG_GROUP_KEYBOARD)

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

    OpenFunction() {
        final Icon openIcon = new ImageIcon(ClassLoader.getSystemResource("icons/mdd2open.png"))
        this.openButton = new JButton(openIcon)
        this.openButton.addActionListener(new ActionListener() {
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
        this.openButton.setToolTipText("Open ("+ keyboardShortcutConfig.getKeyboardKey() + ")")
    }

    @Override
    @NotNull String getGroup() {
        ToolBarGroups.FILE.name()
    }

    @Override
    @NotNull String getName() {
        "Open file"
    }

    @Override
    @NotNull JComponent getToolBarButton() {
        this.openButton
    }

    @Override
    @NotNull KeyboardKey getKeyboardShortcut() {
        keyboardShortcutConfig.getKeyboardKey()
    }

    @Override
    void perform() throws FunctionException {
        try {
            open()
        }
        catch (final IOException ioe) {
            throw new FunctionException(message: ioe.getMessage(), cause: ioe)
        }
    }

    /**
     * Opens a new file using a file chooser.
     */
    private void open() throws IOException {
        final JFileChooser fileChooser = new JFileChooser()
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG)
        final FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Markdown", "md", "markdown")
        fileChooser.setFileFilter(filter)
        final int returnVal = fileChooser.showOpenDialog(this.editor.getGUI().getWindowFrame())
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            if (Editables.inst.getEditable(fileChooser.selectedFile) == null) {
                MarkdownDocEditor.openFile(fileChooser.selectedFile)
            }
            this.editor.editable = Editables.inst.getEditable(fileChooser.selectedFile)
        }
    }


    /**
     * Cleanup and unregister any configs.
     */
    void close() {}

}
