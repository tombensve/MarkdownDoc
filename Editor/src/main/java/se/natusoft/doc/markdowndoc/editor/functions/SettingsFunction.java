/* 
 * 
 * PROJECT
 *     Name
 *         Editor
 *     
 *     Code Version
 *         1.2.6
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
import se.natusoft.doc.markdowndoc.editor.adapters.WindowListenerAdapter;
import se.natusoft.doc.markdowndoc.editor.api.Editor;
import se.natusoft.doc.markdowndoc.editor.api.EditorFunction;
import se.natusoft.doc.markdowndoc.editor.config.*;
import se.natusoft.doc.markdowndoc.editor.exceptions.FunctionException;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.util.*;
import java.util.List;

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
    private JFrame settingsWindow = null;

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
            this.settingsWindow = new JFrame("Markdown Editor Settings");
            this.settingsWindow.setLayout(new BorderLayout());
            this.settingsWindow.addWindowListener(new WindowListenerAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    cancel();
                }
            });

            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new GridLayout(this.editor.getConfigProvider().getConfigs().size(), 1));
            for (ConfigEntry configEntry : this.editor.getConfigProvider().getConfigs()) {
                ConfigEditPanel configEditPanel = new ConfigEditPanel(configEntry);
                contentPanel.add(configEditPanel);
                this.configEditPanels.add(configEditPanel);
            }

            JScrollPane scrollPane = new JScrollPane();
            scrollPane.setViewportView(contentPanel);
            this.settingsWindow.add(scrollPane, BorderLayout.CENTER);

            JPanel buttons = new JPanel(new FlowLayout());

            JButton saveButton = new JButton("Save");
            saveButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    save();
                    SettingsFunction.this.settingsWindow.setVisible(false);
                }
            });
            buttons.add(saveButton);

            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    cancel();
                }
            });
            buttons.add(cancelButton);

            this.settingsWindow.add(buttons, BorderLayout.SOUTH);
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int x = ((int)screenSize.getWidth() / 2) - 200;
            int y = ((int)screenSize.getHeight() / 2) - 200;
            this.settingsWindow.setBounds(x, y, 420, 420);
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
        for (ConfigEntry configEntry : this.editor.getConfigProvider().getConfigs()) {
            configEntry.setValue(this.cancelValues.get(configEntry.getKey()));
        }

        this.settingsWindow.setVisible(false);
    }

    private void save() {
        Properties props = new Properties();
        for (ConfigEntry configEntry : this.editor.getConfigProvider().getConfigs()) {
            props.setProperty(configEntry.getKey(), configEntry.getValue());
        }

        this.editor.getPersistentProps().save(SETTINGS_PROP_NAME, props);
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
    }

    //
    // Inner Classes
    //

    /**
     * Manages and edits one CofnigEntry.
     */
    private class ConfigEditPanel extends JPanel implements ActionListener {
        //
        // Private Members
        //

        private ConfigEntry configEntry;

        //
        // Constructors
        //

        public ConfigEditPanel(ConfigEntry configEntry) {
            this.configEntry = configEntry;

            setLayout(new GridLayout(2,1));

            JLabel description = new JLabel(configEntry.getDescription());
            add(description);
            if (this.configEntry instanceof BooleanConfigEntry) {
                setupBooleanConfigEntry((BooleanConfigEntry)configEntry);
            }
            else if (this.configEntry instanceof ValidSelectionConfigEntry) {
                setupValidSelectionConfigEntry((ValidSelectionConfigEntry) configEntry);
            }
            else if (this.configEntry instanceof ColorConfigEntry) {
                setupColorConfigEntry((ColorConfigEntry) configEntry);
            }
            else if (this.configEntry instanceof DoubleConfigEntry) {
                setupDoubleConfigEntry((DoubleConfigEntry) configEntry);
            }
            else if (this.configEntry instanceof IntegerConfigEntry) {
                setupIntegerConfigEntry((IntegerConfigEntry)configEntry);
            }
            else {
                setupDefaultConfigEntry(configEntry);
            }

            setBorder(new BevelBorder(BevelBorder.LOWERED));
        }

        //
        // Methods
        //

        public void refresh() {
            if (this.configEntry instanceof BooleanConfigEntry) {
                refreshBooleanConfigEntry((BooleanConfigEntry) configEntry);
            }
            else if (this.configEntry instanceof ValidSelectionConfigEntry) {
                refreshValidSelectionConfigEntry((ValidSelectionConfigEntry) configEntry);
            }
            else if (this.configEntry instanceof ColorConfigEntry) {
                refreshColorConfigEntry((ColorConfigEntry) configEntry);
            }
            else if (this.configEntry instanceof DoubleConfigEntry) {
                refreshDoubleConfigEntry((DoubleConfigEntry) configEntry);
            }
            else if (this.configEntry instanceof IntegerConfigEntry) {
                refreshIntegerConfigEntry((IntegerConfigEntry) configEntry);
            }
            else {
                refreshDefaultConfigEntry(configEntry);
            }
        }

        // ------ Boolean ConfigProvider Entry ------

        private JCheckBox checkBox;

        private void setupBooleanConfigEntry(BooleanConfigEntry configEntry) {
            this.checkBox = new JCheckBox();
            add(this.checkBox);
            this.checkBox.addActionListener(this);
            refreshBooleanConfigEntry(configEntry);
        }

        private void refreshBooleanConfigEntry(BooleanConfigEntry configEntry) {
            this.checkBox.setSelected(configEntry.getBoolValue());
        }

        // ------ Valid Selection ConfigProvider Entry ------

        JComboBox comboBox;

        private void setupValidSelectionConfigEntry(ValidSelectionConfigEntry configEntry) {
            this.comboBox = new JComboBox(configEntry.getValidValues());
            add(this.comboBox);
            this.comboBox.addActionListener(this);
            refreshValidSelectionConfigEntry(configEntry);
        }

        private void refreshValidSelectionConfigEntry(ValidSelectionConfigEntry configEntry) {
            this.comboBox.setSelectedItem(configEntry.getValue());
        }

        // ------ Color ConfigProvider Entry -------

        private JSpinner redSpinner;
        private JSpinner greenSpinner;
        private JSpinner blueSpinner;

        private void setupColorConfigEntry(final ColorConfigEntry colorConfigEntry) {
            JPanel colorPanel = new JPanel();
            colorPanel.setLayout(new FlowLayout());
            add(colorPanel);

            this.redSpinner = new JSpinner(new SpinnerNumberModel(colorConfigEntry.getRed(), 0,255, 1));
            colorPanel.add(this.redSpinner);
            this.redSpinner.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    JSpinner redSpinner = (JSpinner)e.getSource();
                    colorConfigEntry.setRed((Integer)redSpinner.getValue());
                }
            });

            this.greenSpinner = new JSpinner(new SpinnerNumberModel(colorConfigEntry.getGreen(), 0,255, 1));
            colorPanel.add(this.greenSpinner);
            this.greenSpinner.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    JSpinner greenSpinner = (JSpinner)e.getSource();
                    colorConfigEntry.setGreen((Integer) greenSpinner.getValue());
                }
            });

            this.blueSpinner = new JSpinner(new SpinnerNumberModel(colorConfigEntry.getBlue(), 0,255, 1));
            colorPanel.add(this.blueSpinner);
            this.blueSpinner.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    JSpinner blueSpinner = (JSpinner)e.getSource();
                    colorConfigEntry.setBlue((Integer) blueSpinner.getValue());
                }
            });

            JButton colorChooserButton = new JButton("...");
            colorPanel.add(colorChooserButton);
            colorChooserButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Color selectedColor =
                            JColorChooser.showDialog(SettingsFunction.this.settingsWindow,
                                    "", new ConfigColor(colorConfigEntry));
                    colorConfigEntry.setRed(selectedColor.getRed());
                    colorConfigEntry.setGreen(selectedColor.getGreen());
                    colorConfigEntry.setBlue(selectedColor.getBlue());
                    redSpinner.setValue(selectedColor.getRed());
                    greenSpinner.setValue(selectedColor.getGreen());
                    blueSpinner.setValue(selectedColor.getBlue());
                }
            });
        }

        private void refreshColorConfigEntry(ColorConfigEntry configEntry) {
            this.redSpinner.setValue(configEntry.getRed());
            this.greenSpinner.setValue(configEntry.getGreen());
            this.blueSpinner.setValue(configEntry.getBlue());
        }

        // ------ Double ConfigProvider Entry ------

        private JSpinner doubleSpinner;

        private void setupDoubleConfigEntry(final DoubleConfigEntry configEntry) {
            SpinnerNumberModel snm = new SpinnerNumberModel(configEntry.getDoubleValue(),
                    configEntry.getMinValue(), configEntry.getMaxValue(), 1.0);
            this.doubleSpinner = new JSpinner(snm);
            JPanel panel = new JPanel(new FlowLayout());
            panel.add(this.doubleSpinner);
            add(panel);
            this.doubleSpinner.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    JSpinner doubleSpinner = (JSpinner)e.getSource();
                    configEntry.setDoubleValue((Double) doubleSpinner.getValue());
                }
            });
        }

        private void refreshDoubleConfigEntry(DoubleConfigEntry configEntry) {
            this.doubleSpinner.setValue(configEntry.getDoubleValue());
        }

        // ------ Integer ConfigProvider Entry ------

        private JSpinner intSpinner;

        private void setupIntegerConfigEntry(final IntegerConfigEntry configEntry) {
            SpinnerNumberModel snm = new SpinnerNumberModel(configEntry.getIntValue(),
                    configEntry.getMinValue(), configEntry.getMaxValue(), 1);
            this.intSpinner = new JSpinner(snm);
            JPanel panel = new JPanel(new FlowLayout());
            panel.add(this.intSpinner);
            add(panel);
            this.intSpinner.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    JSpinner intSpinner = (JSpinner) e.getSource();
                    configEntry.setIntValue((Integer) intSpinner.getValue());
                }
            });
        }

        private void refreshIntegerConfigEntry(IntegerConfigEntry configEntry) {
            this.intSpinner.setValue(configEntry.getIntValue());
        }

        // ------ Default ConfigProvider Entry ------

        private JTextField textField;

        private void setupDefaultConfigEntry(ConfigEntry configEntry) {
            this.textField = new JTextField();
            add(this.textField);
            this.textField.addActionListener(this);
            refreshDefaultConfigEntry(configEntry);
        }

        private void refreshDefaultConfigEntry(ConfigEntry configEntry) {
            this.textField.setText(configEntry.getValue());
        }

        /**
         * Invoked when an action occurs.
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() instanceof JComboBox) {
                JComboBox comboBox = (JComboBox)e.getSource();
                this.configEntry.setValue(comboBox.getSelectedItem().toString());
            }
            else {
                this.configEntry.setValue(e.getActionCommand());
            }
        }
    }

    /**
     * Cleanup and unregister any configs.
     */
    public void close() {}
}
