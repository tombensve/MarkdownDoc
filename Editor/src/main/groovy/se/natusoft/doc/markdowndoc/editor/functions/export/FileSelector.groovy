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
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2014-10-12: Created!
 *
 */
package se.natusoft.doc.markdowndoc.editor.functions.export

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.jetbrains.annotations.NotNull

import javax.swing.*
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.FocusEvent
import java.awt.event.FocusListener

@CompileStatic
@TypeChecked
/**
 * Wraps a JFileChooser.
 */
class FileSelector extends JPanel implements ActionListener {
    //
    // Private Members
    //

    private JTextField fileName = new JTextField(30)
    private JButton selectButton = new JButton("Select")
    private String what
    private DelayedServiceData dsd

    //
    // Constructors
    //

    /**
     * Creates a new FileSelector.
     *
     * @param what The type of file to handle.
     * @param dsd The common service data.
     */
    FileSelector(final String what, final DelayedServiceData dsd) {
        this.what = what
        this.dsd = dsd
        setLayout(new BorderLayout())
        add(this.fileName, BorderLayout.CENTER)
        add(this.selectButton, BorderLayout.EAST)
        this.selectButton.addActionListener(this)

        this.fileName.addFocusListener(new FocusListener() {
            @Override
            void focusGained(final FocusEvent e) {
                final Color bg = FileSelector.this.getBackground()
                final Color fg = FileSelector.this.getForeground()
                FileSelector.this.fileName.setBackground(fg)
                FileSelector.this.fileName.setForeground(bg)
            }

            @Override
            void focusLost(final FocusEvent e) {
                final Color bg = FileSelector.this.getBackground()
                final Color fg = FileSelector.this.getForeground()
                FileSelector.this.fileName.setBackground(bg)
                FileSelector.this.fileName.setForeground(fg)
            }
        })
    }

    //
    // Methods
    //

    /**
     * ActionListener implementation. Pops up a JFileChooser.
     *
     * @param actionEvent The action event that triggered this. I.e a button press.
     */
    @Override
    void actionPerformed(@NotNull final ActionEvent actionEvent) {
        final JFileChooser fileChooser = new JFileChooser()
        fileChooser.setDialogTitle("Specify " + this.what + " file")
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG)
        if (this.fileName.getText() != null && this.fileName.getText().trim().length() > 0) {
            fileChooser.setSelectedFile(new File(this.fileName.getText()))
        }
        //final FileNameExtensionFilter filter = new FileNameExtensionFilter(this.what, this.what)
        final int returnVal = fileChooser.showSaveDialog(dsd.GUI.windowFrame)
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            setFile(fileChooser.getSelectedFile().getAbsolutePath())
        }
    }

    /**
     * Sets the file path.
     *
     * @param file The file path to set.
     */
    void setFile(@NotNull final String file) {
        this.fileName.setText(file)
    }

    /**
     * Returns the file path.
     */
    @NotNull String getFile() {
        this.fileName.getText()
    }

    /**
     * Sets the background of the file path component.
     *
     * @param bgColor The color to set.
     */
    @Override
    void setBackground(@NotNull final Color bgColor) {
        super.setBackground(bgColor)
        if (this.fileName != null) {
            this.fileName.setBackground(bgColor)
        }
    }
}
