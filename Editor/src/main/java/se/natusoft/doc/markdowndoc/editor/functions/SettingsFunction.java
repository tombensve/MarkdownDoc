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
import se.natusoft.doc.markdowndoc.editor.api.Editor;
import se.natusoft.doc.markdowndoc.editor.api.EditorFunction;
import se.natusoft.doc.markdowndoc.editor.config.ConfigEntry;
import se.natusoft.doc.markdowndoc.editor.exceptions.FunctionException;
import se.natusoft.doc.markdowndoc.editor.functions.settings.gui.ConfigEditPanel;
import se.natusoft.doc.markdowndoc.editor.functions.settings.gui.SettingsWindow;
import se.natusoft.doc.markdowndoc.editor.functions.utils.FileWindowProps;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.*;

/**
 * Provides editor setting function.
 */
public class SettingsFunction implements EditorFunction {
    //
    // Constants
    //

    private static final String SETTINGS_PROP_NAME = "editor-general-settings";

    //
    // Private Members
    //

    private Editor editor = null;
    private JButton settingsButton = null;
    private SettingsWindow settingsWindow = null;

    private Map<String, String> cancelValues = null;

    private List<ConfigEditPanel> configEditPanels = new LinkedList<ConfigEditPanel>();

    //
    // Constructors
    //

    public SettingsFunction() {
        Icon settingsIcon = new ImageIcon(ClassLoader.getSystemResource("icons/mddsettings.png"));
        this.settingsButton = new JButton(settingsIcon);
        this.settingsButton.setToolTipText("Settings (Alt-S)");
        this.settingsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                perform();
            }
        });
    }

    //
    // Methods
    //

    @Override
    public void setEditor(Editor editor) {
        this.editor = editor;

        load();
    }

    @Override
    public String getGroup() {
        return ToolBarGroups.config.name();
    }

    @Override
    public String getName() {
        return "Open settings";
    }

    @Override
    public JComponent getToolBarButton() {
        return this.settingsButton;
    }

    @Override
    public int getDownKeyMask() {
        return KeyEvent.CTRL_MASK;
    }

    @Override
    public int getKeyCode() {
        return KeyEvent.VK_S;
    }

    @Override
    public void perform() throws FunctionException {
        if (this.settingsWindow == null) {
            this.settingsWindow = new SettingsWindow() {

                @Override
                protected void cancelSettings() {
                    cancel();
                }

                @Override
                protected void saveSettings() {
                    save();
                }
            };

            for (ConfigEntry configEntry : this.editor.getConfigProvider().getConfigs()) {
                this.settingsWindow.addConfig(configEntry);
            }
        }
        else {
            for (ConfigEditPanel configEditPanel : this.configEditPanels) {
                configEditPanel.refresh();
            }
        }

        this.cancelValues = new HashMap<String, String>();
        for (ConfigEntry configEntry : this.editor.getConfigProvider().getConfigs()) {
            this.cancelValues.put(configEntry.getKey(), configEntry.getValue());
        }

        this.settingsWindow.setVisible(true);
    }

    private void cancel() {
        this.settingsWindow.setVisible(false);

        for (ConfigEntry configEntry : this.editor.getConfigProvider().getConfigs()) {
            configEntry.setValue(this.cancelValues.get(configEntry.getKey()));
        }

    }

    private void save() {
        Properties props = new Properties();
        for (ConfigEntry configEntry : this.editor.getConfigProvider().getConfigs()) {
            props.setProperty(configEntry.getKey(), configEntry.getValue());
        }

        this.editor.getPersistentProps().save(SETTINGS_PROP_NAME, props);

        FileWindowProps fileWindowProps = new FileWindowProps();
        fileWindowProps.setBounds(this.editor.getGUI().getWindowFrame().getBounds());
        fileWindowProps.saveBounds(this.editor);
    }

    private void load() {
        Properties props = this.editor.getPersistentProps().load(SETTINGS_PROP_NAME);
        if (props != null) {
            for (String propName : props.stringPropertyNames()) {
                String propValue = props.getProperty(propName);
                ConfigEntry configEntry = this.editor.getConfigProvider().lookupConfig(propName);
                if (configEntry != null) {
                    configEntry.setValue(propValue);
                }
            }
        }
        else {
            for (ConfigEntry configEntry : this.editor.getConfigProvider().getConfigs()) {
                configEntry.setValue(configEntry.getValue()); // force gui update
            }
        }

        SwingUtilities.updateComponentTreeUI(this.editor.getGUI().getWindowFrame());

        FileWindowProps fileWindowProps = new FileWindowProps();
        fileWindowProps.load(this.editor);
        if (fileWindowProps.hasProperties()) {
            this.editor.getGUI().getWindowFrame().setBounds(fileWindowProps.getBounds());
        }

    }

    /**
     * Cleanup and unregister any configs.
     */
    public void close() {

    }
}
