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
 *     tommy ()
 *         Changes:
 *         2013-06-01: Created!
 *
 */
package se.natusoft.doc.markdowndoc.editor.functions

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import se.natusoft.doc.markdowndoc.editor.ToolBarGroups
import se.natusoft.doc.markdowndoc.editor.api.ConfigProvider
import se.natusoft.doc.markdowndoc.editor.api.Configurable
import se.natusoft.doc.markdowndoc.editor.api.Editor
import se.natusoft.doc.markdowndoc.editor.api.EditorFunction
import se.natusoft.doc.markdowndoc.editor.config.ConfigEntry
import se.natusoft.doc.markdowndoc.editor.config.KeyConfigEntry
import se.natusoft.doc.markdowndoc.editor.config.KeyboardKey
import se.natusoft.doc.markdowndoc.editor.exceptions.FunctionException
import se.natusoft.doc.markdowndoc.editor.gui.ColorsTrait
import se.natusoft.doc.markdowndoc.editor.gui.GuiEnvToolsTrait
import se.natusoft.doc.markdowndoc.editor.gui.MDETitledBorder
import se.natusoft.doc.markdowndoc.editor.gui.PopupWindow

import javax.swing.*
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.FocusEvent
import java.awt.event.FocusListener

import static se.natusoft.doc.markdowndoc.editor.config.Constants.CONFIG_GROUP_KEYBOARD

/**
 * This provides an "insert image" function.
 */
@CompileStatic
@TypeChecked
class InsertImageFunction implements EditorFunction, Configurable, GuiEnvToolsTrait, ColorsTrait {
    //
    // Private Members
    //

    private JButton imageButton
    private JTextField imageAltText
    private JTextField imageURL
    private JTextField imageTitle
    private JWindow inputDialog

    private float popupOpacity = 1.0f

    //
    // Properties
    //

    /** The editor to which this function is bound. */
    @Nullable Editor editor

    //
    // Config
    //

    private static final KeyConfigEntry keyboardShortcutConfig =
            new KeyConfigEntry("editor.function.insert.image.keyboard.shortcut", "Insert image keyboard shortcut",
                    new KeyboardKey("Ctrl+M"), CONFIG_GROUP_KEYBOARD)

    private Closure keyboardShortcutConfigChanged = { final ConfigEntry ce ->
        updateTooltipText()
    }

    private Closure popupOpacityChanged = { final ConfigEntry ce ->
        final int ival = Integer.valueOf(ce.value)
        this.popupOpacity = ((ival as float) / 100.0) as float
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

    InsertImageFunction() {
        final Icon imageIcon = new ImageIcon(ClassLoader.getSystemResource("icons/mdd2image.png"))
        this.imageButton = new JButton(imageIcon)
        imageButton.addActionListener(new ActionListener() {
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
        imageButton.setToolTipText("Image (" + keyboardShortcutConfig.getKeyboardKey() + ")")
    }

    /**
     * Returns the group in the tool bar this functions should be placed in.
     * A new group will be created if the named group does not exist.
     */
    @Override
    @NotNull String getGroup() {
        ToolBarGroups.FORMAT.name()
    }

    /**
     * Returns the name of the function.
     */
    @Override
    @NotNull String getName() {
        "Insert Image"
    }

    /**
     * Returns this functions toolbar button or null if it does not have one.
     */
    @Override
    @NotNull JComponent getToolBarButton() {
        this.imageButton
    }

    /**
     * Returns the keyboard shortcut for the function.
     */
    @Override
    @NotNull KeyboardKey getKeyboardShortcut() {
        keyboardShortcutConfig.getKeyboardKey()
    }

    /**
     * Performs the function.
     *
     * @throws se.natusoft.doc.markdowndoc.editor.exceptions.FunctionException
     *
     */
    @Override
    void perform() throws FunctionException {

        final Box vBox = Box.createVerticalBox()
        updateColors(vBox)

        final JPanel altTextPanel = new JPanel(new FlowLayout(FlowLayout.CENTER))
        altTextPanel.setBorder(new MDETitledBorder(title: "Alt text:", titleColor: defaultForegroundColor))
        updateColors(altTextPanel)
        this.imageAltText = new JTextField(32)
        updateColors(this.imageAltText)
        this.imageAltText.addFocusListener(new FocusListener() {
            @Override
            void focusGained(final FocusEvent e) {
                InsertImageFunction.this.imageAltText.foreground = InsertImageFunction.this.defaultBackgroundColor
                InsertImageFunction.this.imageAltText.background = InsertImageFunction.this.defaultForegroundColor
            }

            @Override
            void focusLost(final FocusEvent e) {
                InsertImageFunction.this.imageAltText.foreground = InsertImageFunction.this.defaultForegroundColor
                InsertImageFunction.this.imageAltText.background = InsertImageFunction.this.defaultBackgroundColor
            }
        })
        altTextPanel.add(this.imageAltText)
        vBox.add(altTextPanel)

        final JPanel imageUrlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER))
        imageUrlPanel.setBorder(new MDETitledBorder(title: "Image URL:", titleColor: defaultForegroundColor))
        updateColors(imageUrlPanel)
        this.imageURL = new JTextField(25)
        updateColors(this.imageURL)
        this.imageURL.addFocusListener(new FocusListener() {
            @Override
            void focusGained(final FocusEvent e) {
                InsertImageFunction.this.imageURL.foreground = InsertImageFunction.this.defaultBackgroundColor
                InsertImageFunction.this.imageURL.background = InsertImageFunction.this.defaultForegroundColor
            }

            @Override
            void focusLost(final FocusEvent e) {
                InsertImageFunction.this.imageURL.foreground = InsertImageFunction.this.defaultForegroundColor
                InsertImageFunction.this.imageURL.background = InsertImageFunction.this.defaultBackgroundColor
            }
        })
        imageUrlPanel.add(this.imageURL)
        final JButton fileSelectButton = new JButton("...")
        fileSelectButton.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(final ActionEvent ignored) {
                final JFileChooser fileChooser = new JFileChooser()
                fileChooser.setDialogType(JFileChooser.OPEN_DIALOG)
                final int returnVal = fileChooser.showOpenDialog(editor.getGUI().getWindowFrame())
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    imageURL.setText("file:" + fileChooser.getSelectedFile())
                    inputDialog.requestFocus()
                }

            }
        })
        imageUrlPanel.add(fileSelectButton)
        vBox.add(imageUrlPanel)

        final JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER))
        titlePanel.setBorder(new MDETitledBorder(title: "Image title:", titleColor: defaultForegroundColor))
        updateColors(titlePanel)
        this.imageTitle = new JTextField(32)
        updateColors(this.imageTitle)
        this.imageTitle.addFocusListener(new FocusListener() {
            @Override
            void focusGained(final FocusEvent e) {
                InsertImageFunction.this.imageTitle.foreground = InsertImageFunction.this.defaultBackgroundColor
                InsertImageFunction.this.imageTitle.background = InsertImageFunction.this.defaultForegroundColor
            }

            @Override
            void focusLost(final FocusEvent e) {
                InsertImageFunction.this.imageTitle.foreground = InsertImageFunction.this.defaultForegroundColor
                InsertImageFunction.this.imageTitle.background = InsertImageFunction.this.defaultBackgroundColor
            }
        })
        titlePanel.add(this.imageTitle)
        vBox.add(titlePanel)

        final JPanel insertCancelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER))
        updateColors(insertCancelPanel)
        final JButton insertButton = new JButton("Insert")
        insertButton.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(final ActionEvent ignored) {
                inputDialog.setVisible(false)
                imageButton.setEnabled(true)
                editor.insertText("![" + imageAltText.getText() + "](" +
                        imageURL.getText() +
                        (imageTitle.getText().trim().length() > 0 ? " \"" + imageTitle.getText() + "\"" : "") + ") ")
                editor.requestEditorFocus()
            }
        })
        insertCancelPanel.add(insertButton)
        final JButton cancelButton = new JButton("Cancel")
        cancelButton.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(final ActionEvent ignored) {
                inputDialog.setVisible(false)
                imageButton.setEnabled(true)
                editor.requestEditorFocus()
            }
        })
        insertCancelPanel.add(cancelButton)
        vBox.add(insertCancelPanel)

        this.inputDialog = new JWindow(this.editor.getGUI().getWindowFrame())
        initGuiEnvTools(this.inputDialog)
        safeOpacity = this.popupOpacity
        safeMakeRoundedRectangleShape()
        updateColors(this.inputDialog)

        this.inputDialog.setLayout(new BorderLayout())
        //vBox.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED))
        this.inputDialog.add(vBox, BorderLayout.CENTER)
        this.inputDialog.setSize(this.inputDialog.getPreferredSize())
        this.imageButton.setEnabled(false)
        this.inputDialog.setVisible(true)
        final Rectangle mainBounds = this.editor.getGUI().getWindowFrame().getBounds()
        final int x = (int)(mainBounds.x + (mainBounds.width / 2) - (this.inputDialog.getWidth() / 2))
        final int y = (int)(mainBounds.y + 70)
        this.inputDialog.setLocation(x,y)
    }

    /**
     * Cleanup and unregister any configs.
     */
    void close() {}
}
