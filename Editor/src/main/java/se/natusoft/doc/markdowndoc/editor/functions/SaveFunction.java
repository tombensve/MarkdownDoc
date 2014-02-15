/* 
 * 
 * PROJECT
 *     Name
 *         MarkdownDocEditor
 *     
 *     Code Version
 *         1.3
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
package se.natusoft.doc.markdowndoc.editor.functions;

import se.natusoft.doc.markdowndoc.editor.ToolBarGroups;
import se.natusoft.doc.markdowndoc.editor.api.ConfigProvider;
import se.natusoft.doc.markdowndoc.editor.api.Configurable;
import se.natusoft.doc.markdowndoc.editor.api.Editor;
import se.natusoft.doc.markdowndoc.editor.api.EditorFunction;
import se.natusoft.doc.markdowndoc.editor.config.ConfigChanged;
import se.natusoft.doc.markdowndoc.editor.config.ConfigEntry;
import se.natusoft.doc.markdowndoc.editor.config.KeyConfigEntry;
import se.natusoft.doc.markdowndoc.editor.config.KeyboardKey;
import se.natusoft.doc.markdowndoc.editor.exceptions.FunctionException;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;

import static se.natusoft.doc.markdowndoc.editor.config.Constants.CONFIG_GROUP_KEYBOARD;

/**
 * Provides a save function.
 */
public class SaveFunction implements EditorFunction, Configurable {
    //
    // Private Members
    //

    private Editor editor;
    private JButton saveButton;

    //
    // Config
    //

    private static final KeyConfigEntry keyboardShortcutConfig =
            new KeyConfigEntry("editor.function.save.keyboard.shortcut", "Save keyboard shortcut",
                    new KeyboardKey("Ctrl+S"), CONFIG_GROUP_KEYBOARD);

    private ConfigChanged keyboardShortcutConfigChanged = new ConfigChanged() {
        @Override
        public void configChanged(ConfigEntry ce) {
            updateTooltipText();
        }
    };

    /**
     * Register configurations.
     *
     * @param configProvider The config provider to register with.
     */
    @Override
    public void registerConfigs(ConfigProvider configProvider) {
        configProvider.registerConfig(keyboardShortcutConfig, keyboardShortcutConfigChanged);
    }

    /**
     * Unregister configurations.
     *
     * @param configProvider The config provider to unregister with.
     */
    @Override
    public void unregisterConfigs(ConfigProvider configProvider) {
        configProvider.unregisterConfig(keyboardShortcutConfig, keyboardShortcutConfigChanged);
    }

    //
    // Constructors
    //

    public SaveFunction() {
        Icon saveIcon = new ImageIcon(ClassLoader.getSystemResource("icons/mddsave.png"));
        this.saveButton = new JButton(saveIcon);
        this.saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                perform();
            }
        });
        updateTooltipText();
    }

    //
    // Methods
    //

    private void updateTooltipText() {
        this.saveButton.setToolTipText("Save (" + keyboardShortcutConfig.getKeyboardKey() + ")");
    }

    /**
     * Sets the editor for the function to use.
     *
     * @param editor The editor to set.
     */
    @Override
    public void setEditor(Editor editor) {
        this.editor = editor;
    }

    @Override
    public String getGroup() {
        return ToolBarGroups.FILE.name();
    }

    @Override
    public String getName() {
        return "Save file";
    }

    @Override
    public JComponent getToolBarButton() {
        return this.saveButton;
    }

    /**
     * Returns the keyboard shortcut for the function.
     */
    @Override
    public KeyboardKey getKeyboardShortcut() {
        return keyboardShortcutConfig.getKeyboardKey();
    }

    @Override
    public void perform() throws FunctionException {
        try {
            if (this.editor.getCurrentFile() != null) {
                this.editor.saveFileAs(this.editor.getCurrentFile());
            }
            else {
                this.editor.save();
            }
            this.editor.requestEditorFocus();
        }
        catch (IOException ioe) {
            JOptionPane.showMessageDialog(
                    this.editor.getGUI().getWindowFrame(), ioe.getMessage(), "Failed to save!", JOptionPane.ERROR_MESSAGE);

        }
    }

    /**
     * Cleanup and unregister any configs.
     */
    public void close() {}
}
