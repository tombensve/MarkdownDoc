/*
 *
 * PROJECT
 *     Name
 *         MarkdownDocEditor
 *     
 *     Code Version
 *         2.0.1
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
package se.natusoft.doc.markdowndoc.editor.gui

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.jetbrains.annotations.NotNull
import se.natusoft.doc.markdowndoc.editor.config.*

import javax.swing.*
import javax.swing.border.TitledBorder
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import java.awt.*
import java.awt.event.*

/**
 * GUI component that manages and edits one ConfigEntry.
 */
@CompileStatic
@TypeChecked
class ConfigValueEditor extends JPanel implements ActionListener, ColorsTrait {
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

    ConfigValueEditor(@NotNull final ConfigEntry configEntry, @NotNull final JFrame parent) {
        this.configEntry = configEntry
        this.parent = parent
        updateColors(this)

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

        final TitledBorder border = new MDETitledBorder(title: configEntry.getDescription())
        border.setTitleColor(defaultForegroundColor)
        setBorder(border)

        final FontMetrics fm = getFontMetrics(getFont())
        minWidth = fm.stringWidth(configEntry.getDescription()) + 30
    }

    //
    // Methods
    //

    @SuppressWarnings("GroovyUnusedDeclaration")
    void refresh() {
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
    @NotNull Dimension getPreferredSize() {
        final Dimension preferredSize = super.getPreferredSize()
        if (preferredSize.width < this.minWidth) {
            preferredSize.size = new Dimension(minWidth, preferredSize.height as int)
        }

        return preferredSize
    }

    @Override
    @NotNull Dimension getMinimumSize() {
        getPreferredSize()
    }

    // ------ Boolean ConfigProvider Entry ------

    // Yes, this wastes a very small amount of memory by sitting on several component references of which
    // only one will be instantiated. Its worth it IMHO.

    private JCheckBox checkBox

    private void setupBooleanConfigEntry(@NotNull final BooleanConfigEntry configEntry) {
        this.checkBox = new JCheckBox()
        updateColors(this.checkBox)
        add(this.checkBox)
        this.checkBox.addActionListener(this)
        refreshBooleanConfigEntry(configEntry)
    }

    private void refreshBooleanConfigEntry(@NotNull final BooleanConfigEntry configEntry) {
        this.checkBox.setSelected(configEntry.getBoolValue())
    }

    // ------ Valid Selection ConfigProvider Entry ------

    private JComboBox<ValidSelectionConfigEntry.Value> comboBox

    private void setupValidSelectionConfigEntry(@NotNull final ValidSelectionConfigEntry configEntry) {
        this.comboBox = new JComboBox<>(configEntry.getValidValues())
        this.comboBox.setForeground(Color.BLACK)
        //this.comboBox.setToolTipText(configEntry.getDescription())
        add(this.comboBox)
        this.comboBox.addActionListener(this)
        refreshValidSelectionConfigEntry(configEntry)
    }

    private void refreshValidSelectionConfigEntry(@NotNull final ValidSelectionConfigEntry configEntry) {
        this.comboBox.setSelectedItem(new ValidSelectionConfigEntry.Value(configEntry.getShowValue()))
    }

    // ------ Color ConfigProvider Entry -------

    private JSpinner redSpinner
    private JSpinner greenSpinner
    private JSpinner blueSpinner

    private void setupColorConfigEntry(@NotNull final ColorConfigEntry colorConfigEntry) {
        final JPanel colorPanel = new JPanel()
        updateColors(colorPanel)
        colorPanel.setLayout(new FlowLayout())
        add(colorPanel)

        this.redSpinner = new JSpinner(new SpinnerNumberModel(colorConfigEntry.getRed(), 0,255, 1))
        updateColors(this.redSpinner)
        colorPanel.add(this.redSpinner)
        this.redSpinner.addChangeListener(new ChangeListener() {
            @Override
            void stateChanged(@NotNull final ChangeEvent e) {
                final JSpinner redSpinner = (JSpinner)e.getSource()
                colorConfigEntry.setRed((Integer)redSpinner.getValue())
            }
        })

        this.greenSpinner = new JSpinner(new SpinnerNumberModel(colorConfigEntry.getGreen(), 0,255, 1))
        updateColors(this.greenSpinner)
        colorPanel.add(this.greenSpinner)
        this.greenSpinner.addChangeListener(new ChangeListener() {
            @Override
            void stateChanged(@NotNull final ChangeEvent e) {
                final JSpinner greenSpinner = (JSpinner)e.getSource()
                colorConfigEntry.setGreen((Integer) greenSpinner.getValue())
            }
        })

        this.blueSpinner = new JSpinner(new SpinnerNumberModel(colorConfigEntry.getBlue(), 0,255, 1))
        updateColors(this.blueSpinner)
        colorPanel.add(this.blueSpinner)
        this.blueSpinner.addChangeListener(new ChangeListener() {
            @Override
            void stateChanged(@NotNull final ChangeEvent e) {
                final JSpinner blueSpinner = (JSpinner)e.getSource()
                colorConfigEntry.setBlue((Integer) blueSpinner.getValue())
            }
        })

        final JButton colorChooserButton = new JButton("...")
//        updateColors(colorChooserButton)
        colorPanel.add(colorChooserButton)
        colorChooserButton.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(@NotNull final ActionEvent e) {
                final Color selectedColor =
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

    private void refreshColorConfigEntry(@NotNull final ColorConfigEntry configEntry) {
        this.redSpinner.setValue(configEntry.getRed())
        this.greenSpinner.setValue(configEntry.getGreen())
        this.blueSpinner.setValue(configEntry.getBlue())
    }

    // ------ Double ConfigProvider Entry ------

    private JSpinner doubleSpinner

    private void setupDoubleConfigEntry(@NotNull final DoubleConfigEntry configEntry) {
        final SpinnerNumberModel snm = new SpinnerNumberModel(configEntry.getDoubleValue(),
                configEntry.getMinValue(), configEntry.getMaxValue(), 1.0d)
        this.doubleSpinner = new JSpinner(snm)
        final JPanel panel = new JPanel(new FlowLayout())
        panel.add(this.doubleSpinner)
        updateColors(panel)
        add(panel)
        this.doubleSpinner.addChangeListener(new ChangeListener() {
            @Override
            void stateChanged(final ChangeEvent e) {
                final JSpinner doubleSpinner = (JSpinner)e.getSource()
                configEntry.setDoubleValue((Double) doubleSpinner.getValue())
            }
        })
    }

    private void refreshDoubleConfigEntry(@NotNull final DoubleConfigEntry configEntry) {
        this.doubleSpinner.setValue(configEntry.getDoubleValue())
    }

    // ------ Integer ConfigProvider Entry ------

    private JSpinner intSpinner

    private void setupIntegerConfigEntry(@NotNull final IntegerConfigEntry configEntry) {
        final SpinnerNumberModel snm = new SpinnerNumberModel(configEntry.getIntValue(),
                configEntry.getMinValue(), configEntry.getMaxValue(), 1i)
        this.intSpinner = new JSpinner(snm)
        updateColors(this.intSpinner)
        final JPanel panel = new JPanel(new FlowLayout())
        updateColors(panel)
        panel.add(this.intSpinner)
        add(panel)
        this.intSpinner.addChangeListener(new ChangeListener() {
            @Override
            void stateChanged(final ChangeEvent e) {
                final JSpinner intSpinner = (JSpinner) e.getSource()
                configEntry.setIntValue((Integer) intSpinner.getValue())
            }
        })
    }

    private void refreshIntegerConfigEntry(@NotNull final IntegerConfigEntry configEntry) {
        this.intSpinner.setValue(configEntry.getIntValue())
    }

    // ------ KeyboardKey ConfigProvider Entry ------

    private JTextField keyboardKeyField
    private KeyEvent keyPressedEvent

    private void setupKeyboardKeyConfigEntry(@NotNull final KeyConfigEntry configEntry) {
        this.keyboardKeyField = new JTextField()
        updateColors(this.keyboardKeyField)
        //this.keyboardKeyField.setEditable(false)
        final JPanel panel = new JPanel(new GridLayout(1,1))
        updateColors(panel)
        panel.add(this.keyboardKeyField)
        add(panel)
        this.keyboardKeyField.addKeyListener(new KeyListener() {
            @Override
            void keyTyped(final KeyEvent e) {
            }

            @Override
            void keyPressed(final KeyEvent e) {
                keyPressedEvent = e
            }

            @Override
            void keyReleased(final KeyEvent e) {
                final KeyboardKey keyboardKey = new KeyboardKey(keyPressedEvent)
                keyboardKeyField.setText(keyboardKey.toString())
                configEntry.setKeyboardKey(keyboardKey)
            }
        })
        this.keyboardKeyField.addFocusListener(new FocusListener() {
            @Override
            void focusGained(final FocusEvent e) {
                keyboardKeyField.background = ConfigValueEditor.this.defaultForegroundColor
                keyboardKeyField.foreground = ConfigValueEditor.this.defaultBackgroundColor
            }

            @Override
            void focusLost(final FocusEvent e) {
                keyboardKeyField.background = ConfigValueEditor.this.defaultBackgroundColor
                keyboardKeyField.foreground = ConfigValueEditor.this.defaultForegroundColor
            }
        })
        refreshKeyboardKeyConfigEntry(configEntry)
    }

    private void refreshKeyboardKeyConfigEntry(@NotNull final ConfigEntry configEntry) {
        this.keyboardKeyField.setText(configEntry.getValue())
    }

    // ------ Default ConfigProvider Entry ------

    private JTextField textField

    private void setupDefaultConfigEntry(@NotNull final ConfigEntry configEntry) {
        this.textField = new JTextField()
        updateColors(this.textField)
        final JPanel panel = new JPanel(new FlowLayout())
        panel.add(this.textField)
        add(panel)
        this.textField.addActionListener(this)
        this.textField.addFocusListener(new FocusListener() {
            @Override
            void focusGained(final FocusEvent e) {
                textField.background = ConfigValueEditor.this.defaultForegroundColor
                textField.foreground = ConfigValueEditor.this.defaultBackgroundColor
            }

            @Override
            void focusLost(final FocusEvent e) {
                textField.background = ConfigValueEditor.this.defaultBackgroundColor
                textField.foreground = ConfigValueEditor.this.defaultForegroundColor
            }
        })
        refreshDefaultConfigEntry(configEntry)
    }

    private void refreshDefaultConfigEntry(@NotNull final ConfigEntry configEntry) {
        this.textField.setText(configEntry.getValue())
    }

    /**
     * Invoked when an action occurs.
     */
    @Override
    void actionPerformed(@NotNull final ActionEvent e) {
        if (e.getSource() instanceof JComboBox) {
            final JComboBox comboBox = (JComboBox)e.getSource()
            if (comboBox.getSelectedItem() instanceof ValidSelectionConfigEntry.Value) {
                this.configEntry.setValue(((ValidSelectionConfigEntry.Value)comboBox.getSelectedItem()).getUse())
            }
            else {
                this.configEntry.setValue(comboBox.getSelectedItem().toString())
            }
        }
        else if (e.getSource() instanceof JCheckBox) {
            final JCheckBox checkBox = (JCheckBox)e.getSource()
            this.configEntry.setValue("" + checkBox.getModel().isSelected())
        }
        else {
            this.configEntry.setValue(e.getActionCommand())
        }
    }

}
