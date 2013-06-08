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
 *     tommy ()
 *         Changes:
 *         2013-06-01: Created!
 *         
 */
package se.natusoft.doc.markdowndoc.editor.functions;

import se.natusoft.doc.markdowndoc.editor.ToolBarGroups;
import se.natusoft.doc.markdowndoc.editor.api.Editor;
import se.natusoft.doc.markdowndoc.editor.api.EditorFunction;
import se.natusoft.doc.markdowndoc.editor.exceptions.FunctionException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 * This provides an "insert image" function.
 */
public class InsertLinkFunction implements EditorFunction {
    //
    // Private Members
    //

    // The editor we supply function for. Received in setEditor(Editor).
    private Editor editor;

    private JButton linkButton;
    private JPanel inputPanel;
    private JTextField linkText;
    private JTextField linkURL;
    private JTextField linkTitle;
    private JFrame inputDialog;

    //
    // Constructors
    //

    public InsertLinkFunction() {
        Icon imageIcon = new ImageIcon(ClassLoader.getSystemResource("icons/mddlink.png"));
        this.linkButton = new JButton(imageIcon);
        linkButton.setToolTipText("Link (Meta-N)");
        linkButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                perform();
            }
        });

    }

    //
    // Methods
    //

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
        return ToolBarGroups.format.name();
    }

    /**
     * Returns the name of the function.
     */
    @Override
    public String getName() {
        return "Insert Link";
    }

    /**
     * Returns this functions toolbar button or null if it does not have one.
     */
    @Override
    public JComponent getToolBarButton() {
        return this.linkButton;
    }

    /**
     * Keyboard trigger for the "down" key (shit, ctrl, alt, ...)
     */
    @Override
    public int getDownKeyMask() {
        return KeyEvent.META_MASK;
    }

    /**
     * The keyboard trigger key code.
     */
    @Override
    public int getKeyCode() {
        return KeyEvent.VK_N;
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
        this.inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JPanel labelPanel = new JPanel(new GridLayout(3, 1));
        labelPanel.add(createLabelPanel("Link text:"));
        labelPanel.add(createLabelPanel("Link URL:"));
        labelPanel.add(createLabelPanel("Link title:"));
        this.inputPanel.add(labelPanel);
        JPanel textInputPanel = new JPanel(new GridLayout(3,1));

        this.linkText = new JTextField(32);
        this.linkURL = new JTextField(32);
        this.linkTitle = new JTextField(32);

        textInputPanel.add(createTextFieldPanel(this.linkText));
        textInputPanel.add(createTextFieldPanel(this.linkURL));
        textInputPanel.add(createTextFieldPanel(this.linkTitle));
        this.inputPanel.add(textInputPanel);

        JButton insertButton = new JButton("Insert");
        this.inputPanel.add(insertButton);
        JButton cancelButton = new JButton("Cancel");
        this.inputPanel.add(cancelButton);

        this.inputDialog = new JFrame("Insert image parameters");
        this.inputDialog.setAlwaysOnTop(true);
        Rectangle mainBounds = this.editor.getGUI().getWindowFrame().getBounds();
        this.inputDialog.setLayout(new BorderLayout());
        this.inputDialog.add(this.inputPanel, BorderLayout.CENTER);

        insertButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inputDialog.setVisible(false);

                if (linkText.getText().trim().length() > 0) {
                    editor.insertText("[" + linkText.getText() + "](" + linkURL.getText() +
                            (linkTitle.getText().trim().length() > 0 ? " \"" + linkTitle.getText() + "\"" : "") + ") ");
                }
                else {
                    editor.insertText("<" + linkURL.getText() + "> ");
                }

                editor.requestEditorFocus();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inputDialog.setVisible(false);
                editor.requestEditorFocus();
            }
        });

        this.inputDialog.setVisible(true);

        // We don't get a correct preferred size until the window has become visible.
        this.inputDialog.setBounds((int) mainBounds.getX(), (int) mainBounds.getY() + 70, (int) mainBounds.getWidth(), (int) this.inputDialog.getPreferredSize().getHeight());
    }

    /**
     * Cleanup and unregister any configs.
     */
    public void close() {}
}
