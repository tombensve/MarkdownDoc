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

import se.natusoft.doc.markdowndoc.editor.config.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Manages and edits one ConfigEntry.
 */
public class ConfigEditPanel extends JPanel implements ActionListener {
    //
    // Private Members
    //

    private ConfigEntry configEntry;

    private JFrame parent;

    //
    // Constructors
    //

    public ConfigEditPanel(ConfigEntry configEntry, JFrame parent) {
        this.configEntry = configEntry;
        this.parent = parent;

        setLayout(new GridLayout(1,1));

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

        TitledBorder border = new TitledBorder(configEntry.getDescription());
        setBorder(border);
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

    JComboBox<String> comboBox;

    private void setupValidSelectionConfigEntry(ValidSelectionConfigEntry configEntry) {
        this.comboBox = new JComboBox<>(configEntry.getValidValues());
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
                        JColorChooser.showDialog(ConfigEditPanel.this.parent,
                                "", new ConfigColor(colorConfigEntry));
                if (selectedColor != null) {
                    colorConfigEntry.setRed(selectedColor.getRed());
                    colorConfigEntry.setGreen(selectedColor.getGreen());
                    colorConfigEntry.setBlue(selectedColor.getBlue());
                    redSpinner.setValue(selectedColor.getRed());
                    greenSpinner.setValue(selectedColor.getGreen());
                    blueSpinner.setValue(selectedColor.getBlue());
                }
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
