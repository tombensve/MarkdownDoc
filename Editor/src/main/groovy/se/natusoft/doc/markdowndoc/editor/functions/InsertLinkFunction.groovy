/*
 *
 * PROJECT
 *     Name
 *         MarkdownDocEditor
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
class InsertLinkFunction implements EditorFunction, Configurable, GuiEnvToolsTrait, ColorsTrait {
    //
    // Private Members
    //

    // The toolbar button.
    private JButton linkButton

    // Popup GUI
    private JTextField linkText
    private JTextField linkURL
    private JTextField linkTitle
    private JWindow inputDialog

    float popupOpacity = 1.0f

    //
    // Properties
    //

    // The editor we supply function for.
    @Nullable Editor editor

    //
    // Config
    //

    private static final KeyConfigEntry keyboardShortcutConfig =
            new KeyConfigEntry("editor.function.insert.link.keyboard.shortcut", "Insert link keyboard shortcut",
                    new KeyboardKey("Ctrl+N"), CONFIG_GROUP_KEYBOARD)

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

    InsertLinkFunction() {
        final Icon imageIcon = new ImageIcon(ClassLoader.getSystemResource("icons/mdd2link.png"))
        this.linkButton = new JButton(imageIcon)
        linkButton.addActionListener(new ActionListener() {
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

    void updateTooltipText() {
        linkButton.setToolTipText("Link (" + keyboardShortcutConfig.getKeyboardKey() + ")")
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
        "Insert Link"
    }

    /**
     * Returns this functions toolbar button or null if it does not have one.
     */
    @Override
    @NotNull JComponent getToolBarButton() {
        this.linkButton
    }

    /**
     * Keyboard trigger for the "down" key (shit, ctrl, alt, ...)
     */
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

        final JPanel linkTextPanel = new JPanel(new FlowLayout(FlowLayout.CENTER))
        linkTextPanel.setBorder(new MDETitledBorder(title: "Link text:", titleColor: defaultForegroundColor))
        updateColors(linkTextPanel)
        this.linkText = new JTextField(32)
        this.linkText.addFocusListener(new FocusListener() {
            @Override
            void focusGained(final FocusEvent e) {
                InsertLinkFunction.this.linkText.foreground = InsertLinkFunction.this.defaultBackgroundColor
                InsertLinkFunction.this.linkText.background = InsertLinkFunction.this.defaultForegroundColor
            }

            @Override
            void focusLost(final FocusEvent e) {
                InsertLinkFunction.this.linkText.foreground = InsertLinkFunction.this.defaultForegroundColor
                InsertLinkFunction.this.linkText.background = InsertLinkFunction.this.defaultBackgroundColor
            }
        })
        updateColors(this.linkText)
        linkTextPanel.add(this.linkText)
        vBox.add(linkTextPanel)

        final JPanel linkURLPanel = new JPanel(new FlowLayout(FlowLayout.CENTER))
        linkURLPanel.setBorder(new MDETitledBorder(title: "Link URL:", titleColor: defaultForegroundColor))
        updateColors(linkURLPanel)
        this.linkURL = new JTextField(32)
        this.linkURL.addFocusListener(new FocusListener() {
            @Override
            void focusGained(final FocusEvent e) {
                InsertLinkFunction.this.linkURL.foreground = InsertLinkFunction.this.defaultBackgroundColor
                InsertLinkFunction.this.linkURL.background = InsertLinkFunction.this.defaultForegroundColor
            }

            @Override
            void focusLost(final FocusEvent e) {
                InsertLinkFunction.this.linkURL.foreground = InsertLinkFunction.this.defaultForegroundColor
                InsertLinkFunction.this.linkURL.background = InsertLinkFunction.this.defaultBackgroundColor
            }
        })
        updateColors(this.linkURL)
        linkURLPanel.add(this.linkURL)
        vBox.add(linkURLPanel)

        final JPanel linkTitlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER))
        linkTitlePanel.setBorder(new MDETitledBorder(title: "Link title:", titleColor: defaultForegroundColor))
        updateColors(linkTitlePanel)
        this.linkTitle = new JTextField(32)
        this.linkTitle.addFocusListener(new FocusListener() {
            @Override
            void focusGained(final FocusEvent e) {
                InsertLinkFunction.this.linkTitle.foreground = InsertLinkFunction.this.defaultBackgroundColor
                InsertLinkFunction.this.linkTitle.background = InsertLinkFunction.this.defaultForegroundColor
            }

            @Override
            void focusLost(final FocusEvent e) {
                InsertLinkFunction.this.linkTitle.foreground = InsertLinkFunction.this.defaultForegroundColor
                InsertLinkFunction.this.linkTitle.background = InsertLinkFunction.this.defaultBackgroundColor
            }
        })
        updateColors(this.linkTitle)
        linkTitlePanel.add(this.linkTitle)
        vBox.add(linkTitlePanel)

        final JPanel insertCancelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER))
        updateColors(insertCancelPanel)
        final JButton insertButton = new JButton("Insert")
        insertButton.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(final ActionEvent ignored) {
                inputDialog.setVisible(false)
                linkButton.setEnabled(true)

                if (linkText.getText().trim().length() > 0) {
                    editor.insertText("[" + linkText.getText() + "](" + linkURL.getText() +
                            (linkTitle.getText().trim().length() > 0 ? " \"" + linkTitle.getText() + "\"" : "") + ") ")
                }
                else {
                    editor.insertText("<" + linkURL.getText() + "> ")
                }

                editor.requestEditorFocus()
            }
        })
        insertCancelPanel.add(insertButton)
        final JButton cancelButton = new JButton("Cancel")
        cancelButton.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(final ActionEvent ignored) {
                inputDialog.setVisible(false)
                linkButton.setEnabled(true)
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
        this.inputDialog.add(vBox, BorderLayout.CENTER)
        this.inputDialog.setSize(this.inputDialog.getPreferredSize())
        this.linkButton.setEnabled(false)
        this.inputDialog.setVisible(true)
        final Rectangle mainBounds = this.editor.getGUI().getWindowFrame().getBounds()
        final int x = (int)mainBounds.x + (int)(mainBounds.width / 2) - (int)(this.inputDialog.getWidth() / 2)
        final int y = (int)mainBounds.y + 70
        this.inputDialog.setLocation(x,y)

    }

    /**
     * Cleanup and unregister any configs.
     */
    void close() {}
}
