/* 
 * 
 * PROJECT
 *     Name
 *         MarkdownDocEditor
 *     
 *     Code Version
 *         1.3.8
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
package se.natusoft.doc.markdowndoc.editor.functions.settings.gui

import groovy.transform.CompileStatic
import se.natusoft.doc.markdowndoc.editor.config.*

import javax.swing.*
import javax.swing.border.TitledBorder
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.KeyEvent
import java.awt.event.KeyListener

/**
 * Manages and edits one ConfigEntry.
 */
@CompileStatic
public class ConfigEditPanel extends JPanel implements ActionListener {
    //
    // Private Members
    //

    /** The edited config entry.  */
    private ConfigEntry configEntry

    /** The settings window. */
    private JFrame parent

    /** The minimum width. */
    private int minWidth = 100

    //
    // Constructors
    //

    public ConfigEditPanel(ConfigEntry configEntry, JFrame parent) {
        this.configEntry = configEntry
        this.parent = parent

        setLayout(new GridLayout(1,1))

        if (this.configEntry instanceof BooleanConfigEntry) {
            setupBooleanConfigEntry((BooleanConfigEntry) configEntry)
        }
        else if (this.configEntry instanceof ValidSelectionConfigEntry) {
            setupValidSelectionConfigEntry((ValidSelectionConfigEntry) configEntry)
        }
        else if (this.configEntry instanceof ColorConfigEntry) {
            setupColorConfigEntry((ColorConfigEntry) configEntry)
        }
        else if (this.configEntry instanceof DoubleConfigEntry) {
            setupDoubleConfigEntry((DoubleConfigEntry) configEntry)
        }
        else if (this.configEntry instanceof IntegerConfigEntry) {
            setupIntegerConfigEntry((IntegerConfigEntry) configEntry)
        }
        else if (this.configEntry instanceof KeyConfigEntry) {
            setupKeyboardKeyConfigEntry((KeyConfigEntry) configEntry)
        }
        else {
            setupDefaultConfigEntry(configEntry)
        }

        TitledBorder border = new TitledBorder(configEntry.getDescription())
        setBorder(border)

        FontMetrics fm = getFontMetrics(getFont())
        minWidth = fm.stringWidth(configEntry.getDescription()) + 30
    }

    //
    // Methods
    //


    public void refresh() {
        if (this.configEntry instanceof BooleanConfigEntry) {
            refreshBooleanConfigEntry((BooleanConfigEntry) configEntry)
        }
        else if (this.configEntry instanceof ValidSelectionConfigEntry) {
            refreshValidSelectionConfigEntry((ValidSelectionConfigEntry) configEntry)
        }
        else if (this.configEntry instanceof ColorConfigEntry) {
            refreshColorConfigEntry((ColorConfigEntry) configEntry)
        }
        else if (this.configEntry instanceof DoubleConfigEntry) {
            refreshDoubleConfigEntry((DoubleConfigEntry) configEntry)
        }
        else if (this.configEntry instanceof IntegerConfigEntry) {
            refreshIntegerConfigEntry((IntegerConfigEntry) configEntry)
        }
        else if (this.configEntry instanceof KeyConfigEntry) {
            refreshKeyboardKeyConfigEntry(configEntry)
        }
        else {
            refreshDefaultConfigEntry(configEntry)
        }
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension preferredSize = super.getPreferredSize()
        if (preferredSize.width < this.minWidth) {
            preferredSize.size = new Dimension(minWidth, (int)preferredSize.height)
        }

        return preferredSize
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize()
    }

    // ------ Boolean ConfigProvider Entry ------

    // Yes, this wastes a very small amount of memory by sitting on several component references of which
    // only one will be instantiated. Its worth it IMHO.

    private JCheckBox checkBox

    private void setupBooleanConfigEntry(BooleanConfigEntry configEntry) {
        this.checkBox = new JCheckBox()
        add(this.checkBox)
        this.checkBox.addActionListener(this)
        refreshBooleanConfigEntry(configEntry)
    }

    private void refreshBooleanConfigEntry(BooleanConfigEntry configEntry) {
        this.checkBox.setSelected(configEntry.getBoolValue())
    }

    // ------ Valid Selection ConfigProvider Entry ------

    JComboBox<ValidSelectionConfigEntry.Value> comboBox

    private void setupValidSelectionConfigEntry(ValidSelectionConfigEntry configEntry) {
        this.comboBox = new JComboBox<>(configEntry.getValidValues())
        //this.comboBox.setToolTipText(configEntry.getDescription())
        add(this.comboBox)
        this.comboBox.addActionListener(this)
        refreshValidSelectionConfigEntry(configEntry)
    }

    private void refreshValidSelectionConfigEntry(ValidSelectionConfigEntry configEntry) {
        this.comboBox.setSelectedItem(new ValidSelectionConfigEntry.Value(configEntry.getShowValue()))
    }

    // ------ Color ConfigProvider Entry -------

    private JSpinner redSpinner
    private JSpinner greenSpinner
    private JSpinner blueSpinner

    private void setupColorConfigEntry(final ColorConfigEntry colorConfigEntry) {
        JPanel colorPanel = new JPanel()
        colorPanel.setLayout(new FlowLayout())
        add(colorPanel)

        this.redSpinner = new JSpinner(new SpinnerNumberModel(colorConfigEntry.getRed(), 0,255, 1))
        colorPanel.add(this.redSpinner)
        this.redSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSpinner redSpinner = (JSpinner)e.getSource()
                colorConfigEntry.setRed((Integer)redSpinner.getValue())
            }
        })

        this.greenSpinner = new JSpinner(new SpinnerNumberModel(colorConfigEntry.getGreen(), 0,255, 1))
        colorPanel.add(this.greenSpinner)
        this.greenSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSpinner greenSpinner = (JSpinner)e.getSource()
                colorConfigEntry.setGreen((Integer) greenSpinner.getValue())
            }
        })

        this.blueSpinner = new JSpinner(new SpinnerNumberModel(colorConfigEntry.getBlue(), 0,255, 1))
        colorPanel.add(this.blueSpinner)
        this.blueSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSpinner blueSpinner = (JSpinner)e.getSource()
                colorConfigEntry.setBlue((Integer) blueSpinner.getValue())
            }
        })

        JButton colorChooserButton = new JButton("...")
        colorPanel.add(colorChooserButton)
        colorChooserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color selectedColor =
                        JColorChooser.showDialog(/*ConfigEditPanel.this.*/parent,
                                "", new ConfigColor(colorConfigEntry))
                if (selectedColor != null) {
                    colorConfigEntry.setRed(selectedColor.getRed())
                    colorConfigEntry.setGreen(selectedColor.getGreen())
                    colorConfigEntry.setBlue(selectedColor.getBlue())
                    redSpinner.setValue(selectedColor.getRed())
                    greenSpinner.setValue(selectedColor.getGreen())
                    blueSpinner.setValue(selectedColor.getBlue())
                }
            }
        })
    }

    private void refreshColorConfigEntry(ColorConfigEntry configEntry) {
        this.redSpinner.setValue(configEntry.getRed())
        this.greenSpinner.setValue(configEntry.getGreen())
        this.blueSpinner.setValue(configEntry.getBlue())
    }

    // ------ Double ConfigProvider Entry ------

    private JSpinner doubleSpinner

    private void setupDoubleConfigEntry(final DoubleConfigEntry configEntry) {
        SpinnerNumberModel snm = new SpinnerNumberModel(configEntry.getDoubleValue(),
                configEntry.getMinValue(), configEntry.getMaxValue(), 1.0)
        this.doubleSpinner = new JSpinner(snm)
        JPanel panel = new JPanel(new FlowLayout())
        panel.add(this.doubleSpinner)
        add(panel)
        this.doubleSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSpinner doubleSpinner = (JSpinner)e.getSource()
                configEntry.setDoubleValue((Double) doubleSpinner.getValue())
            }
        })
    }

    private void refreshDoubleConfigEntry(DoubleConfigEntry configEntry) {
        this.doubleSpinner.setValue(configEntry.getDoubleValue())
    }

    // ------ Integer ConfigProvider Entry ------

    private JSpinner intSpinner

    private void setupIntegerConfigEntry(final IntegerConfigEntry configEntry) {
        SpinnerNumberModel snm = new SpinnerNumberModel(configEntry.getIntValue(),
                configEntry.getMinValue(), configEntry.getMaxValue(), 1)
        this.intSpinner = new JSpinner(snm)
        JPanel panel = new JPanel(new FlowLayout())
        panel.add(this.intSpinner)
        add(panel)
        this.intSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSpinner intSpinner = (JSpinner) e.getSource()
                configEntry.setIntValue((Integer) intSpinner.getValue())
            }
        })
    }

    private void refreshIntegerConfigEntry(IntegerConfigEntry configEntry) {
        this.intSpinner.setValue(configEntry.getIntValue())
    }

    // ------ KeyboardKey ConfigProvider Entry ------

    private JTextField keyboardKeyField
    private KeyEvent keyPressedEvent

    private void setupKeyboardKeyConfigEntry(final KeyConfigEntry configEntry) {
        this.keyboardKeyField = new JTextField()
        //this.keyboardKeyField.setEditable(false)
        JPanel panel = new JPanel(new GridLayout(1,1))
        panel.add(this.keyboardKeyField)
        add(panel)
        this.keyboardKeyField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                keyPressedEvent = e
            }

            @Override
            public void keyReleased(KeyEvent e) {
                KeyboardKey keyboardKey = new KeyboardKey(keyPressedEvent)
                keyboardKeyField.setText(keyboardKey.toString())
                configEntry.setKeyboardKey(keyboardKey)
            }
        })
        refreshKeyboardKeyConfigEntry(configEntry)
    }

    private void refreshKeyboardKeyConfigEntry(ConfigEntry configEntry) {
        this.keyboardKeyField.setText(configEntry.getValue())
    }

    // ------ Default ConfigProvider Entry ------

    private JTextField textField

    private void setupDefaultConfigEntry(ConfigEntry configEntry) {
        this.textField = new JTextField()
        JPanel panel = new JPanel(new FlowLayout())
        panel.add(this.textField)
        add(panel)
        this.textField.addActionListener(this)
        refreshDefaultConfigEntry(configEntry)
    }

    private void refreshDefaultConfigEntry(ConfigEntry configEntry) {
        this.textField.setText(configEntry.getValue())
    }

    /**
     * Invoked when an action occurs.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof JComboBox) {
            JComboBox comboBox = (JComboBox)e.getSource()
            if (comboBox.getSelectedItem() instanceof ValidSelectionConfigEntry.Value) {
                this.configEntry.setValue(((ValidSelectionConfigEntry.Value)comboBox.getSelectedItem()).getUse())
            }
            else {
                this.configEntry.setValue(comboBox.getSelectedItem().toString())
            }
        }
        else if (e.getSource() instanceof JCheckBox) {
            JCheckBox checkBox = (JCheckBox)e.getSource()
            this.configEntry.setValue("" + checkBox.getModel().isSelected())
        }
        else {
            this.configEntry.setValue(e.getActionCommand())
        }
    }

}
