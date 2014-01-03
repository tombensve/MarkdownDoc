/* 
 * 
 * PROJECT
 *     Name
 *         MarkdownDocEditor
 *     
 *     Code Version
 *         1.2.10
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
import se.natusoft.doc.markdowndoc.editor.api.Editor;
import se.natusoft.doc.markdowndoc.editor.api.EditorFunction;
import se.natusoft.doc.markdowndoc.editor.exceptions.FunctionException;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 * This provides a function that inserts formatting for italics.
 */
public class InsertItalicsFunction implements EditorFunction {
    //
    // Private Members
    //

    private Editor editor;
    private JButton italicsButton;

    //
    // Constructors
    //

    public InsertItalicsFunction() {
        Icon italicsIcon = new ImageIcon(ClassLoader.getSystemResource("icons/mdditalics.png"));
        this.italicsButton = new JButton(italicsIcon);
        italicsButton.setToolTipText("Italics (Meta-I)");
        italicsButton.addActionListener(new ActionListener() {
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
    }

    @Override
    public String getGroup() {
        return ToolBarGroups.format.name();
    }

    @Override
    public String getName() {
        return "Insert italics format";
    }

    @Override
    public JComponent getToolBarButton() {
        return this.italicsButton;
    }

    @Override
    public int getDownKeyMask() {
        return KeyEvent.META_MASK;
    }

    @Override
    public int getKeyCode() {
        return KeyEvent.VK_I;
    }

    @Override
    public void perform() throws FunctionException {
        this.editor.insertText("__");
        this.editor.moveCaretBack(1);
        this.editor.requestEditorFocus();
    }

    /**
     * Cleanup and unregister any configs.
     */
    public void close() {}
}
