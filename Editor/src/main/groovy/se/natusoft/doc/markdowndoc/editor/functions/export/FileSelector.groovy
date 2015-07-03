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
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2014-10-12: Created!
 *
 */
package se.natusoft.doc.markdowndoc.editor.functions.export

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

@CompileStatic
@TypeChecked
public class FileSelector extends JPanel implements ActionListener {
    private JTextField fileName = new JTextField(30)
    private JButton selectButton = new JButton("Select")
    private String what
    private DelayedServiceData dsd

    public FileSelector(String what, DelayedServiceData dsd) {
        this.what = what
        this.dsd = dsd
        setLayout(new BorderLayout())
        add(this.fileName, BorderLayout.CENTER)
        add(this.selectButton, BorderLayout.EAST)
        this.selectButton.addActionListener(this)
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        JFileChooser fileChooser = new JFileChooser()
        fileChooser.setDialogTitle("Specify " + what + " file")
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG)
        if (this.fileName.getText() != null && this.fileName.getText().trim().length() > 0) {
            fileChooser.setSelectedFile(new File(this.fileName.getText()))
        }
        FileNameExtensionFilter filter = new FileNameExtensionFilter(this.what, this.what)
        int returnVal = fileChooser.showSaveDialog(dsd.GUI.windowFrame)
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            setFile(fileChooser.getSelectedFile().getAbsolutePath())
        }
    }

    public void setFile(String file) {
        this.fileName.setText(file)
    }

    public String getFile() {
        return this.fileName.getText()
    }

    @Override
    public void setBackground(Color bgColor) {
        super.setBackground(bgColor)
        if (this.fileName != null) {
            this.fileName.setBackground(bgColor)
        }
    }
}
