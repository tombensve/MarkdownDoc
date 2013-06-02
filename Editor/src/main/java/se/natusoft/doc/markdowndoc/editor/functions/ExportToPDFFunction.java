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

import se.natusoft.doc.markdown.api.Generator;
import se.natusoft.doc.markdown.api.Parser;
import se.natusoft.doc.markdown.exception.GenerateException;
import se.natusoft.doc.markdown.exception.ParseException;
import se.natusoft.doc.markdown.generator.PDFGenerator;
import se.natusoft.doc.markdown.generator.options.PDFGeneratorOptions;
import se.natusoft.doc.markdown.model.Doc;
import se.natusoft.doc.markdown.parser.MarkdownParser;
import se.natusoft.doc.markdowndoc.editor.ToolBarGroups;
import se.natusoft.doc.markdowndoc.editor.api.Editor;
import se.natusoft.doc.markdowndoc.editor.api.EditorFunction;
import se.natusoft.doc.markdowndoc.editor.exceptions.FunctionException;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.*;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * Provides a function that exports to PDF.
 */
public class ExportToPDFFunction implements EditorFunction {
    //
    // Constants
    //

    /** The property key for the last generated PDF file. */
    private static final String GENERATED_PDF_FILE = "generated.pdf.file";

    //
    // Private Members
    //

    /** The editor instance we provide function for. */
    private Editor editor = null;

    /** The toolbar button. */
    private JButton pdfToolbarButton = null;

    /** Holds all input values for the generate PDF meta data dialog. */
    private PDFData pdfData = new PDFData();

    // The following are referenced from GUI callbacks and thus must be part of the instance.

    /** The currently generated PDF file. */
    private File pdfFile = null;

    /** The PDF meta data / options dialog. */
    private JFrame pdfMetaDataDialog = null;

    /**
     * This holds components that will be added to the pdfMetaDataDialog in perform().
     */
    private class PDFData {
        /** After call to loadPDFData(File) this contains all below fields of PDFDataValue type. */
        private List<PDFDataValue> pdfDataValues = null;

        private PDFDataValue pageSize = new PDFDataTextValue("Page size:", "A4");
        private PDFDataValue title = new PDFDataTextValue("Title:");
        private PDFDataValue subject = new PDFDataTextValue("Subject:");
        private PDFDataValue keywords = new PDFDataTextValue("Keywords:");
        private PDFDataValue author = new PDFDataTextValue("Author:");
        private PDFDataValue version = new PDFDataTextValue("Version:");
        private PDFDataValue copyrightYear = new PDFDataTextValue("Copyright year:");
        private PDFDataValue copyrightBy = new PDFDataTextValue("Copyright by:");
        private PDFDataValue generateTitlePage = new PDFDataSelectValue("Generate title page:");
        private PDFDataValue generateTOC = new PDFDataSelectValue("Generate TOC:");
        private PDFDataValue openResult = new PDFDataSelectValue("Open result:");

        /**
         * Initializes the pdfDataValues list with all the fields
         * for easier dynamic access.
         */
        private void loadPDFDataValues() {
            pdfDataValues = new LinkedList<PDFDataValue>();
            for (Field field : PDFData.class.getDeclaredFields()) {
                if (field.getType() == PDFDataValue.class) {
                    field.setAccessible(true);
                    try {
                        pdfDataValues.add((PDFDataValue)field.get(ExportToPDFFunction.this.pdfData));
                    }
                    catch (Exception e) {
                        System.err.println("ERROR: " + e.getMessage());
                    }
                }
            }
        }

        /**
         * Loads the values of the fields in this class from the specified properties file.
         *
         * @param file The properties file to load from.
         */
        private void loadPDFData(File file) {
            Properties props = ExportToPDFFunction.this.editor.getPersistentProps().load(fileToPropertiesName(file));
            if (props != null) {
                for (String propName : props.stringPropertyNames()) {
                    for (PDFDataValue pdfDataValue : pdfDataValues) {
                        if (pdfDataValue.getKey().equals(propName)) {
                            pdfDataValue.setValue(props.getProperty(propName));
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
        private void savePDFData(File file) {
            Properties props = new Properties();
            for (PDFDataValue pdfDataValue : pdfDataValues) {
                props.setProperty(pdfDataValue.getKey(), pdfDataValue.getValue());
            }
            props.setProperty(GENERATED_PDF_FILE, ExportToPDFFunction.this.pdfFile.getAbsolutePath());
            ExportToPDFFunction.this.editor.getPersistentProps().save(fileToPropertiesName(file), props);
        }

    }

    /**
     * Base of all field values in PDFData
     */
    private abstract class PDFDataValue {
        protected JLabel label;
        protected JComponent value;

        public PDFDataValue(String labelText) {
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
    private class PDFDataTextValue extends PDFDataValue {
        public PDFDataTextValue(String labelText) {
            super(labelText);
            this.value = new JTextField(25);
        }

        public PDFDataTextValue(String labelText, String defaultValue) {
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
    private class PDFDataSelectValue extends PDFDataValue {
        public PDFDataSelectValue(String labelText) {
            super(labelText);
            this.value = new JCheckBox();
        }

        public PDFDataSelectValue(String label, boolean defValue) {
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

    //
    // Constructors
    //

    /**
     * Creates a new ExportToPDFFunction instance.
     */
    public ExportToPDFFunction() {
        Icon pdfIcon = new ImageIcon(ClassLoader.getSystemResource("icons/mddpdf.png"));
        this.pdfToolbarButton = new JButton(pdfIcon);
        this.pdfToolbarButton.setToolTipText("Export as PDF (Ctrl-P)");
        this.pdfToolbarButton.addActionListener(new ActionListener() {
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
     * Receives the instance of the editor we provide functionality for.
     *
     * @param editor The received editor instance.
     */
    @Override
    public void setEditor(Editor editor) {
        this.editor = editor;
    }

    /**
     * Returns the group name the function belongs to.
     */
    @Override
    public String getGroup() {
        return ToolBarGroups.export.name();
    }

    /**
     * Returns the name of the function.
     */
    @Override
    public String getName() {
        return "Export to PDF";
    }

    /**
     * Returns the function toolbar button.
     */
    @Override
    public JComponent getToolBarButton() {
        return this.pdfToolbarButton;
    }

    /**
     * Returns the down key mast to react on for this function.
     */
    @Override
    public int getDownKeyMask() {
        return KeyEvent.CTRL_MASK;
    }

    /**
     * Returns the key code that activate this function.
     */
    @Override
    public int getKeyCode() {
        return KeyEvent.VK_P;
    }

    /**
     * Executes this function.
     *
     * @throws FunctionException on failure to perform function.
     */
    @Override
    public void perform() throws FunctionException {
        this.pdfFile = getPDFOutputFile();

        if (pdfFile != null) {
            this.pdfMetaDataDialog = new JFrame("PDF document data");
            this.pdfMetaDataDialog.setLayout(new BorderLayout());

            this.pdfData.loadPDFDataValues();

            JPanel dataLabelPanel = new JPanel(new GridLayout(this.pdfData.pdfDataValues.size(),1));
            this.pdfMetaDataDialog.add(dataLabelPanel, BorderLayout.WEST);

            JPanel dataValuePanel = new JPanel(new GridLayout(this.pdfData.pdfDataValues.size(),1));
            this.pdfMetaDataDialog.add(dataValuePanel, BorderLayout.CENTER);


            for (PDFDataValue pdfDataValue : this.pdfData.pdfDataValues) {
                dataLabelPanel.add(pdfDataValue.label);
                dataValuePanel.add(pdfDataValue.value);
            }

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            JButton generateButton = new JButton("Generate");
            generateButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    ExportToPDFFunction.this.pdfMetaDataDialog.setVisible(false);
                    generatePDF();
                }
            });
            buttonPanel.add(generateButton);
            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    ExportToPDFFunction.this.pdfMetaDataDialog.setVisible(false);
                }
            });
            buttonPanel.add(cancelButton);

            this.pdfMetaDataDialog.add(buttonPanel, BorderLayout.SOUTH);

            // Set initial values to last saved values for the specified file.
            if (this.editor.getCurrentFile() != null) {
                this.pdfData.loadPDFData(this.editor.getCurrentFile());
            }

            this.pdfMetaDataDialog.setVisible(true);
            this.pdfMetaDataDialog.setBounds(
                    this.editor.getGUI().getWindowFrame().getX() + 40,
                    this.editor.getGUI().getWindowFrame().getY() + 40,
                    (int) this.pdfMetaDataDialog.getPreferredSize().getWidth(),
                    (int) this.pdfMetaDataDialog.getPreferredSize().getHeight()
            );
        }
    }

    /**
     * Opens i file chooser dialog for specifying the PDF generation target file, and
     * returns the selected file.
     */
    private File getPDFOutputFile() {
        File selectedFile = null;
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Specify file to save PDF to");
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        if (this.editor.getCurrentFile() != null) {
            Properties props = this.editor.getPersistentProps().load(fileToPropertiesName(this.editor.getCurrentFile()));
            if (props != null && props.getProperty(GENERATED_PDF_FILE) != null) {
                fileChooser.setSelectedFile(new File(props.getProperty(GENERATED_PDF_FILE)));
            }
        }
        FileNameExtensionFilter filter = new FileNameExtensionFilter("pdf", "pdf");
        fileChooser.setFileFilter(filter);
        int returnVal = fileChooser.showSaveDialog(this.editor.getGUI().getWindowFrame());
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
        }

        return selectedFile;
    }

    /**
     * Extracts the markdown text in the editor and returns a parsed Doc document model of it.
     */
    private Doc getMarkdownDocument() {
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

    /**
     * Initiates PDF generation and handles any failures with an error dialog.
     */
    private void generatePDF() {
        try {
            _generatePDF();
        }
        catch (RuntimeException re) {
            re.printStackTrace(System.err);
            JOptionPane.showMessageDialog(
                    this.editor.getGUI().getWindowFrame(), re.getMessage(), "Failed to save PDF!", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Actually performs the PDF generation using MarkdownDocs PDF generator.
     */
    private void _generatePDF() {
        Generator generator = new PDFGenerator();
        PDFGeneratorOptions pdfOpts = new PDFGeneratorOptions();
        pdfOpts.setResultFile(this.pdfFile.getAbsolutePath());
        pdfOpts.setAuthor(this.pdfData.author.getValue());
        pdfOpts.setTitle(this.pdfData.title.getValue());
        pdfOpts.setSubject(this.pdfData.subject.getValue());
        pdfOpts.setKeywords(this.pdfData.keywords.getValue());
        pdfOpts.setVersion(this.pdfData.version.getValue());
        pdfOpts.setCopyright("Copyright © " + this.pdfData.copyrightYear.getValue() + " by " +
            this.pdfData.copyrightBy.getValue());
        pdfOpts.setPageSize(this.pdfData.pageSize.getValue());
        pdfOpts.setGenerateTitlePage(Boolean.valueOf(this.pdfData.generateTitlePage.getValue()));
        pdfOpts.setGenerateTOC(Boolean.valueOf(this.pdfData.generateTOC.getValue()));

        BufferedOutputStream pdfStream = null;
        try {
            pdfStream = new BufferedOutputStream(new FileOutputStream(this.pdfFile));
            generator.generate(getMarkdownDocument(), pdfOpts, null, pdfStream);
            pdfStream.flush();
        }
        catch (IOException ioe) {
            throw new RuntimeException(ioe.getMessage(), ioe);
        }
        catch (GenerateException ge) {
            throw new RuntimeException(ge.getMessage(), ge);
        }
        finally {
            try {if (pdfStream!= null) pdfStream.close();} catch (IOException cioe) {}
        }

        if (this.editor.getCurrentFile() != null) {
            this.pdfData.savePDFData(this.editor.getCurrentFile());
        }

        if (this.pdfData.openResult.getValue().equals("true")) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.open(this.pdfFile);
            }
            catch (IOException ioe) {
                JOptionPane.showMessageDialog(
                        this.editor.getGUI().getWindowFrame(), ioe.getMessage(), "Failed to open PDF!", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Converts a File to a properties name. This is used for saving generation meta data for last PDF generation
     * using the File representing the text being edited.
     *
     * @param file The file to convert to properties name.
     */
    private String fileToPropertiesName(File file) {
        return file.getName().replace(".", "_");
    }

    /**
     * Cleanup and unregister any configs.
     */
    public void close() {}
}
