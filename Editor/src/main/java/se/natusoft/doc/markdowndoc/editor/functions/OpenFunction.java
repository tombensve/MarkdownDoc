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
import se.natusoft.doc.markdowndoc.editor.api.Editor;
import se.natusoft.doc.markdowndoc.editor.api.EditorFunction;
import se.natusoft.doc.markdowndoc.editor.exceptions.FunctionException;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;

/**
 * Provides an open function.
 */
public class OpenFunction implements EditorFunction {
    //
    // Private Members
    //

    private Editor editor;
    private JButton openButton;

    //
    // Constructors
    //

    public OpenFunction() {
        Icon openIcon = new ImageIcon(ClassLoader.getSystemResource("icons/mddopen.png"));
        this.openButton = new JButton(openIcon);
        this.openButton.setToolTipText("Open (Meta-O)");
        this.openButton.addActionListener(new ActionListener() {
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
     * Sets the editor for the function to use.
     *
     * @param editor The editor to set.
     */
    @Override
    public void setEditor(Editor editor) {
        this.editor = editor;
    }

    @Override
    public String getGroup() {
        return ToolBarGroups.file.name();
    }

    @Override
    public String getName() {
        return "Open file";
    }

    @Override
    public JComponent getToolBarButton() {
        return this.openButton;
    }

    @Override
    public int getDownKeyMask() {
        return KeyEvent.META_MASK;
    }

    @Override
    public int getKeyCode() {
        return KeyEvent.VK_O;
    }

    @Override
    public void perform() throws FunctionException {
        try {
            open();
        }
        catch (IOException ioe) {
            throw new FunctionException(ioe.getMessage(), ioe);
        }
    }

    /**
     * Opens a new file using a file chooser.
     */
    private void open() throws IOException {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Markdown", "md", "markdown");
        fileChooser.setFileFilter(filter);
        int returnVal = fileChooser.showOpenDialog(this.editor.getGUI().getWindowFrame());
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            this.editor.loadFile(fileChooser.getSelectedFile());
        }
    }


    /**
     * Cleanup and unregister any configs.
     */
    public void close() {}
}
