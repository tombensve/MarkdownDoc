/* 
 * 
 * PROJECT
 *     Name
 *         MarkdownDocEditor
 *     
 *     Code Version
 *         1.3.3
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
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import static se.natusoft.doc.markdowndoc.editor.config.Constants.CONFIG_GROUP_KEYBOARD;

/**
 * This provides an "insert image" function.
 */
public class InsertImageFunction implements EditorFunction, Configurable {
    //
    // Private Members
    //

    private Editor editor;
    private JButton imageButton;
    private JTextField imageAltText;
    private JTextField imageURL;
    private JTextField imageTitle;
    private JWindow inputDialog;

    //
    // Config
    //

    private static final KeyConfigEntry keyboardShortcutConfig =
            new KeyConfigEntry("editor.function.insert.image.keyboard.shortcut", "Insert image keyboard shortcut",
                    new KeyboardKey("Ctrl+M"), CONFIG_GROUP_KEYBOARD);

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

    public InsertImageFunction() {
        Icon imageIcon = new ImageIcon(ClassLoader.getSystemResource("icons/mddimg.png"));
        this.imageButton = new JButton(imageIcon);
        imageButton.addActionListener(new ActionListener() {
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
        imageButton.setToolTipText("Image (" + keyboardShortcutConfig.getKeyboardKey() + ")");
    }

    /**
     * Sets the editor for the component to use.
     *
     * @param editor The editor to set.
     */
    @Override
    public void setEditor(Editor editor) {
        this.editor = editor;
    }

    /**
     * Returns the group in the tool bar this functions should be placed in.
     * A new group will be created if the named group does not exist.
     */
    @Override
    public String getGroup() {
        return ToolBarGroups.FORMAT.name();
    }

    /**
     * Returns the name of the function.
     */
    @Override
    public String getName() {
        return "Insert Image";
    }

    /**
     * Returns this functions toolbar button or null if it does not have one.
     */
    @Override
    public JComponent getToolBarButton() {
        return this.imageButton;
    }

    /**
     * Returns the keyboard shortcut for the function.
     */
    @Override
    public KeyboardKey getKeyboardShortcut() {
        return keyboardShortcutConfig.getKeyboardKey();
    }

    private JPanel createLabelPanel(String text) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel(text));
        return panel;
    }

    private JPanel createTextFieldPanel(JTextField textField) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(textField);
        return panel;
    }

    /**
     * Performs the function.
     *
     * @throws se.natusoft.doc.markdowndoc.editor.exceptions.FunctionException
     *
     */
    @Override
    public void perform() throws FunctionException {
        Color bgColor = this.editor.getGUI().getWindowFrame().getBackground();

        Box vBox = Box.createVerticalBox();

        JPanel altTextPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        this.imageAltText = new JTextField(32);
        this.imageAltText.setBackground(bgColor);
        this.imageAltText.setBorder(new TitledBorder("Alt text:"));
        altTextPanel.add(this.imageAltText);
        vBox.add(altTextPanel);

        JPanel imageUrlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        this.imageURL = new JTextField(25);
        this.imageURL.setBackground(bgColor);
        this.imageURL.setBorder(new TitledBorder("Image URL:"));
        imageUrlPanel.add(this.imageURL);
        JButton fileSelectButton = new JButton("...");
        fileSelectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
                int returnVal = fileChooser.showOpenDialog(editor.getGUI().getWindowFrame());
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    imageURL.setText("file:" + fileChooser.getSelectedFile());
                    inputDialog.requestFocus();
                }

            }
        });
        imageUrlPanel.add(fileSelectButton);
        vBox.add(imageUrlPanel);

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        this.imageTitle = new JTextField(32);
        this.imageTitle.setBackground(bgColor);
        this.imageTitle.setBorder(new TitledBorder("Image title:"));
        titlePanel.add(this.imageTitle);
        vBox.add(titlePanel);

        JPanel insertCancelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton insertButton = new JButton("Insert");
        insertButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inputDialog.setVisible(false);
                imageButton.setEnabled(true);
                editor.insertText("![" + imageAltText.getText() + "](" +
                        imageURL.getText() +
                        (imageTitle.getText().trim().length() > 0 ? " \"" + imageTitle.getText() + "\"" : "") + ") ");
                editor.requestEditorFocus();
            }
        });
        insertCancelPanel.add(insertButton);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inputDialog.setVisible(false);
                imageButton.setEnabled(true);
                editor.requestEditorFocus();
            }
        });
        insertCancelPanel.add(cancelButton);
        vBox.add(insertCancelPanel);

        this.inputDialog = new JWindow(this.editor.getGUI().getWindowFrame());
        this.inputDialog.setLayout(new BorderLayout());
        vBox.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED));
        this.inputDialog.add(vBox, BorderLayout.CENTER);
        this.inputDialog.setSize(this.inputDialog.getPreferredSize());
        this.imageButton.setEnabled(false);
        this.inputDialog.setVisible(true);
        Rectangle mainBounds = this.editor.getGUI().getWindowFrame().getBounds();
        int x = mainBounds.x + (mainBounds.width / 2) - (this.inputDialog.getWidth() / 2);
        int y = mainBounds.y + 70;
        this.inputDialog.setLocation(x,y);
    }

    /**
     * Cleanup and unregister any configs.
     */
    public void close() {}
}
