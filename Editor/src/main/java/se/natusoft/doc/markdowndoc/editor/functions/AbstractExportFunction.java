/* 
 * 
 * PROJECT
 *     Name
 *         MarkdownDocEditor
 *     
 *     Code Version
 *         1.3
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
 *         2013-06-06: Created!
 *         
 */
package se.natusoft.doc.markdowndoc.editor.functions;

import se.natusoft.doc.markdown.api.Parser;
import se.natusoft.doc.markdown.exception.ParseException;
import se.natusoft.doc.markdown.model.Doc;
import se.natusoft.doc.markdown.parser.MarkdownParser;
import se.natusoft.doc.markdowndoc.editor.api.Editor;
import se.natusoft.doc.markdowndoc.editor.api.EditorFunction;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * Common code for export.
 */
public abstract class AbstractExportFunction implements EditorFunction {

    //
    // Private Members
    //

    /** The editor instance we provide function for. */
    protected Editor editor;

    /** The property key for saving and loading defaults. */
    protected String defaultsPropKey;

    /** The currently generated file. */
    protected File exportFile = null;

    //
    // Constructor
    //

    AbstractExportFunction(String defaultsPropKey) {
        this.defaultsPropKey = defaultsPropKey;
    }

    //
    // Methods
    //

    /**
     * Receives the instance of the editor we provide functionality for.
     *
     * @param editor The received editor instance.
     */
    @Override
    public void setEditor(Editor editor) {
        this.editor = editor;
    }

    protected class ExportData {
        /** After call to loadPDFData(File) this contains all below fields of ExportDataValue type. */
        protected List<ExportDataValue> exportDataValues = null;

        /**
         * Loads the values of the fields in this class from the specified properties file.
         *
         * @param file The properties file to load from.
         */
        protected void loadExportData(File file) {
            Properties props = AbstractExportFunction.this.editor.getPersistentProps().load(fileToPropertiesName(file));
            if (props != null) {
                for (String propName : props.stringPropertyNames()) {
                    for (ExportDataValue exportDataValue : exportDataValues) {
                        if (exportDataValue.getKey().equals(propName)) {
                            exportDataValue.setValue(props.getProperty(propName));
                        }
                    }
                }
            }
        }

        /**
         * Saves the values of the fields in this class to the specified properties file.
         *
         * @param file The properties file to save to.
         */
        protected void saveExportData(File file) {
            Properties props = new Properties();
            for (ExportDataValue exportDataValue : exportDataValues) {
                props.setProperty(exportDataValue.getKey(), exportDataValue.getValue());
            }
            props.setProperty(AbstractExportFunction.this.defaultsPropKey,
                    AbstractExportFunction.this.exportFile.getAbsolutePath());
            AbstractExportFunction.this.editor.getPersistentProps().save(fileToPropertiesName(file), props);
        }
    }

    /**
     * Base of all field values in PDFData
     */
    protected abstract class ExportDataValue {
        protected JLabel label;
        protected JComponent value;

        public ExportDataValue(String labelText) {
            this.label = new JLabel("    " + labelText + " ");
        }

        public String getKey() {
            return this.label.getText().trim().toLowerCase().replaceAll(" ", "-").replaceAll(":", "");
        }

        public abstract String getValue();
        public abstract void setValue(String value);
    }

    /**
     * PDFData text value fields.
     */
    protected class ExportDataTextValue extends ExportDataValue {
        public ExportDataTextValue(String labelText) {
            super(labelText);
            this.value = new JTextField(25);
        }

        public ExportDataTextValue(String labelText, String defaultValue) {
            this(labelText);
            ((JTextField)value).setText(defaultValue);
        }

        public String getValue() {
            return ((JTextField)this.value).getText();
        }

        public void setValue(String value) {
            ((JTextField)this.value).setText(value);
        }
    }

    /**
     * PDFData boolean value fields.
     */
    protected class ExportDataSelectValue extends ExportDataValue {
        public ExportDataSelectValue(String labelText) {
            super(labelText);
            this.value = new JCheckBox();
        }

        public ExportDataSelectValue(String label, boolean defValue) {
            this(label);
            this.value = new JCheckBox();
            ((JCheckBox)this.value).setSelected(defValue);
        }

        public String getValue() {
            return "" + ((JCheckBox)this.value).isSelected();
        }

        public void setValue(String value) {
            boolean selected = Boolean.valueOf(value);
            ((JCheckBox)this.value).setSelected(selected);
        }
    }

    protected class ExportFileValue extends ExportDataValue {
        public ExportFileValue(String labelText, String whatFile) {
            super(labelText);
            this.value = new FileSelector(whatFile);
        }

        public String getValue() {
            return ((FileSelector)this.value).getFile();
        }

        public void setValue(String value) {
            ((FileSelector)this.value).setFile(value);
        }
    }

    protected class FileSelector extends JPanel implements ActionListener {
        private JTextField fileName = new JTextField(30);
        private JButton selectButton = new JButton("Select");
        private String what;

        public FileSelector(String what) {
            this.what = what;
            setLayout(new BorderLayout());
            add(this.fileName, BorderLayout.CENTER);
            add(this.selectButton, BorderLayout.EAST);
            this.selectButton.addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Specify " + what + " file");
            fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
            if (this.fileName.getText() != null && this.fileName.getText().trim().length() > 0) {
                fileChooser.setSelectedFile(new File(this.fileName.getText()));
            }
            FileNameExtensionFilter filter = new FileNameExtensionFilter(this.what, this.what);
            int returnVal = fileChooser.showSaveDialog(AbstractExportFunction.this.editor.getGUI().getWindowFrame());
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                setFile(fileChooser.getSelectedFile().getAbsolutePath());
            }
        }

        public void setFile(String file) {
            this.fileName.setText(file);
        }

        public String getFile() {
            return this.fileName.getText();
        }
    }

    /**
     * Opens i file chooser dialog for specifying the PDF generation target file, and
     * returns the selected file.
     */
    protected File getExportOutputFile(String type, String def, String... extFilter) {
        File selectedFile = null;
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Specify file to save " + type + " to");
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        if (this.editor.getCurrentFile() != null) {
            Properties props = this.editor.getPersistentProps().load(fileToPropertiesName(this.editor.getCurrentFile()));
            if (props != null && props.getProperty(this.defaultsPropKey) != null) {
                fileChooser.setSelectedFile(new File(props.getProperty(this.defaultsPropKey)));
            }
        }
        FileNameExtensionFilter filter = new FileNameExtensionFilter(def, extFilter);
        fileChooser.setFileFilter(filter);
        int returnVal = fileChooser.showSaveDialog(this.editor.getGUI().getWindowFrame());
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
        }

        return selectedFile;
    }

    /**
     * Converts a File to a properties name. This is used for saving generation meta data for last PDF generation
     * using the File representing the text being edited.
     *
     * @param file The file to convert to properties name.
     */
    protected String fileToPropertiesName(File file) {
        return file.getName().replace(".", "_");
    }

    /**
     * Extracts the markdown text in the editor and returns a parsed Doc document model of it.
     */
    protected Doc getMarkdownDocument() {
        String markdownText = this.editor.getEditorContent();
        ByteArrayInputStream markDownStream = new ByteArrayInputStream(markdownText.getBytes());

        Parser parser = new MarkdownParser();
        Doc document = new Doc();
        Properties parserOptions = new Properties();
        try {
            parser.parse(document, markDownStream, parserOptions);
        }
        catch (ParseException pe) {
            throw new RuntimeException(pe.getMessage(), pe);
        }
        catch (IOException ioe) {
            throw new RuntimeException(ioe.getMessage(), ioe);
        }
        finally {
            try {markDownStream.close();} catch (IOException cioe) {}
        }

        return document;
    }

}
