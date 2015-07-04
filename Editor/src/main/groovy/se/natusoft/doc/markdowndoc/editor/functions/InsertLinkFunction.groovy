/*
 *
 * PROJECT
 *     Name
 *         MarkdownDocEditor
 *
 *     Code Version
 *         1.3.9
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
import se.natusoft.doc.markdowndoc.editor.ToolBarGroups
import se.natusoft.doc.markdowndoc.editor.api.ConfigProvider
import se.natusoft.doc.markdowndoc.editor.api.Configurable
import se.natusoft.doc.markdowndoc.editor.api.Editor
import se.natusoft.doc.markdowndoc.editor.api.EditorFunction
import se.natusoft.doc.markdowndoc.editor.config.ConfigEntry
import se.natusoft.doc.markdowndoc.editor.config.KeyConfigEntry
import se.natusoft.doc.markdowndoc.editor.config.KeyboardKey
import se.natusoft.doc.markdowndoc.editor.exceptions.FunctionException

import javax.swing.*
import javax.swing.border.SoftBevelBorder
import javax.swing.border.TitledBorder
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

import static se.natusoft.doc.markdowndoc.editor.config.Constants.CONFIG_GROUP_KEYBOARD

/**
 * This provides an "insert image" function.
 */
@CompileStatic
@TypeChecked
class InsertLinkFunction implements EditorFunction, Configurable {
    //
    // Private Members
    //

    // The editorPane we supply function for. Received in attach(Editor).
    private Editor editor

    // The toolbar button.
    private JButton linkButton

    // Popup GUI
    private JTextField linkText
    private JTextField linkURL
    private JTextField linkTitle
    private JWindow inputDialog

    //
    // Config
    //

    private static final KeyConfigEntry keyboardShortcutConfig =
            new KeyConfigEntry("editor.function.insert.link.keyboard.shortcut", "Insert link keyboard shortcut",
                    new KeyboardKey("Ctrl+N"), CONFIG_GROUP_KEYBOARD)

    private Closure keyboardShortcutConfigChanged = { ConfigEntry ce ->
        updateTooltipText()
    }

    /**
     * Register configurations.
     *
     * @param configProvider The config provider to register with.
     */
    @Override
    void registerConfigs(ConfigProvider configProvider) {
        configProvider.registerConfig(keyboardShortcutConfig, keyboardShortcutConfigChanged)
    }

    /**
     * Unregister configurations.
     *
     * @param configProvider The config provider to unregister with.
     */
    @Override
    void unregisterConfigs(ConfigProvider configProvider) {
        configProvider.unregisterConfig(keyboardShortcutConfig, keyboardShortcutConfigChanged)
    }

    //
    // Constructors
    //

    InsertLinkFunction() {
        Icon imageIcon = new ImageIcon(ClassLoader.getSystemResource("icons/mddlink.png"))
        this.linkButton = new JButton(imageIcon)
        linkButton.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(ActionEvent actionEvent) {
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
     * Sets the editorPane for the component to use.
     *
     * @param editor The editorPane to set.
     */
    @Override
    void setEditor(Editor editor) {
        this.editor = editor
    }

    /**
     * Returns the group in the tool bar this functions should be placed in.
     * A new group will be created if the named group does not exist.
     */
    @Override
    String getGroup() {
        ToolBarGroups.FORMAT.name()
    }

    /**
     * Returns the name of the function.
     */
    @Override
    String getName() {
        "Insert Link"
    }

    /**
     * Returns this functions toolbar button or null if it does not have one.
     */
    @Override
    JComponent getToolBarButton() {
        this.linkButton
    }

    /**
     * Keyboard trigger for the "down" key (shit, ctrl, alt, ...)
     */
    /**
     * Returns the keyboard shortcut for the function.
     */
    @Override
    KeyboardKey getKeyboardShortcut() {
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
        Color bgColor = this.editor.getGUI().getWindowFrame().getBackground()

        Box vBox = Box.createVerticalBox()

        JPanel linkTextPanel = new JPanel(new FlowLayout(FlowLayout.CENTER))
        this.linkText = new JTextField(32)
        this.linkText.setBackground(bgColor)
        this.linkText.setBorder(new TitledBorder("Link text:"))
        linkTextPanel.add(this.linkText)
        vBox.add(linkTextPanel)

        JPanel linkURLPanel = new JPanel(new FlowLayout(FlowLayout.CENTER))
        this.linkURL = new JTextField(32)
        this.linkURL.setBackground(bgColor)
        this.linkURL.setBorder(new TitledBorder("Link URL:"))
        linkURLPanel.add(this.linkURL)
        vBox.add(linkURLPanel)

        JPanel linkTitlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER))
        this.linkTitle = new JTextField(32)
        this.linkTitle.setBackground(bgColor)
        this.linkTitle.setBorder(new TitledBorder("Link title:"))
        linkTitlePanel.add(this.linkTitle)
        vBox.add(linkTitlePanel)

        JPanel insertCancelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER))
        JButton insertButton = new JButton("Insert")
        insertButton.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(ActionEvent e) {
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
        JButton cancelButton = new JButton("Cancel")
        cancelButton.addActionListener(new ActionListener() {
            @Override
            void actionPerformed(ActionEvent e) {
                inputDialog.setVisible(false)
                linkButton.setEnabled(true)
                editor.requestEditorFocus()
            }
        })
        insertCancelPanel.add(cancelButton)
        vBox.add(insertCancelPanel)

        this.inputDialog = new JWindow(this.editor.getGUI().getWindowFrame())
        this.inputDialog.setLayout(new BorderLayout())
        vBox.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED))
        this.inputDialog.add(vBox, BorderLayout.CENTER)
        this.inputDialog.setSize(this.inputDialog.getPreferredSize())
        this.linkButton.setEnabled(false)
        this.inputDialog.setVisible(true)
        Rectangle mainBounds = this.editor.getGUI().getWindowFrame().getBounds()
        int x = (int)mainBounds.x + (int)(mainBounds.width / 2) - (int)(this.inputDialog.getWidth() / 2)
        int y = (int)mainBounds.y + 70
        this.inputDialog.setLocation(x,y)

    }

    /**
     * Cleanup and unregister any configs.
     */
    void close() {}
}
