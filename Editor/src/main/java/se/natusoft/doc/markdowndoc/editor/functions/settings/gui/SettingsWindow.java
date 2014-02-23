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
 *         2014-02-01: Created!
 *         
 */
package se.natusoft.doc.markdowndoc.editor.functions.settings.gui;

import se.natusoft.doc.markdowndoc.editor.adapters.WindowListenerAdapter;
import se.natusoft.doc.markdowndoc.editor.config.ConfigEntry;
import se.natusoft.doc.markdowndoc.editor.gui.SmartFlowLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * The settings window.
 */
public class SettingsWindow extends JFrame {

    //
    // Private Members
    //

    /** The tabbed pane containing the group panels. */
    private JTabbedPane groupPane = null;

    /** The group panels for easy lookup using group name. */
    private Map<String, JPanel> groupPanels = new HashMap<>();


    //
    // Constructors
    //

    /**
     * Creates a new SettingsWindow.
     */
    public SettingsWindow() {
        super("MarkdownDoc Editor Settings");
        setLayout(new BorderLayout());
        addWindowListener(new WindowListenerAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cancelSettings();
            }
        });

        this.groupPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
        add(this.groupPane, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout());

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveSettings();
                setVisible(false);
            }
        });
        buttons.add(saveButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelSettings();
                setVisible(false);
            }
        });
        buttons.add(cancelButton);

        add(buttons, BorderLayout.SOUTH);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = ((int)screenSize.getWidth() / 2) - 330;
        int y = ((int)screenSize.getHeight() / 2) - 200;
        setBounds(x, y, 660, 430);
    }

    //
    // Methods
    //

    /**
     * Adds a configuration to the settings window.
     *
     * @param configEntry The config entry to add.
     */
    public void addConfig(ConfigEntry configEntry) {
        JPanel groupPanel = this.groupPanels.get(configEntry.getConfigGroup());
        if (groupPanel == null) {
            groupPanel = new JPanel();
            groupPanel.setLayout(new SmartFlowLayout());
            this.groupPanels.put(configEntry.getConfigGroup(), groupPanel);

            JScrollPane scrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setViewportView(groupPanel);

            this.groupPane.addTab(configEntry.getConfigGroup(), scrollPane);
        }

        ConfigEditPanel configEditPanel = new ConfigEditPanel(configEntry, this);
        groupPanel.add(configEditPanel);
    }

    /**
     * Should be overridden to handle save.
     */
    protected void saveSettings() {}

    /**
     * Should be overridden to handle cancel.
     */
    protected void cancelSettings() {}

}
