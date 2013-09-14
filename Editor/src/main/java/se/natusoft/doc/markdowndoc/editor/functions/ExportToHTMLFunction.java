/* 
 * 
 * PROJECT
 *     Name
 *         MarkdownDocEditor
 *     
 *     Code Version
 *         1.2.9
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
import se.natusoft.doc.markdown.exception.GenerateException;
import se.natusoft.doc.markdown.generator.HTMLGenerator;
import se.natusoft.doc.markdown.generator.options.HTMLGeneratorOptions;
import se.natusoft.doc.markdowndoc.editor.ToolBarGroups;
import se.natusoft.doc.markdowndoc.editor.api.Editor;
import se.natusoft.doc.markdowndoc.editor.api.EditorFunction;
import se.natusoft.doc.markdowndoc.editor.exceptions.FunctionException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.LinkedList;

/**
 * Provides a function that exports to HTML.
 */
public class ExportToHTMLFunction extends AbstractExportFunction implements EditorFunction {
    //
    // Constants
    //

    /** The property key for the last generated HTML file. */
    private static final String GENERATED_HTML_FILE = "generated.html.file";

    //
    // Private Members
    //

    /** The toolbar button. */
    private JButton htmlButton;

    /** Holds all input values for the generate PDF meta data dialog. */
    private HTMLData htmlData = new HTMLData();

    // The following are referenced from GUI callbacks and thus must be part of the instance.

    /** The PDF meta data / options dialog. */
    private JFrame htmlMetaDataDialog = null;

    /**
     * This holds components that will be added to the htmlMetaDataDialog in perform().
     */
    private class HTMLData extends ExportData {

        private ExportDataValue inlineCSS = new ExportDataSelectValue("inline CSS:");
        private ExportDataValue css = new ExportFileValue("CSS file:", "CSS");
        private ExportDataValue fileLinks = new ExportFileValue("'file:' links relative to:", "File links");
        private ExportDataValue openResult = new ExportDataSelectValue("Open result:");

        /**
         * Initializes the exportDataValues list with all the fields
         * for easier dynamic access.
         */
        private void loadPDFDataValues() {
            exportDataValues = new LinkedList<ExportDataValue>();
            for (Field field : HTMLData.class.getDeclaredFields()) {
                if (field.getType() == ExportDataValue.class) {
                    field.setAccessible(true);
                    try {
                        exportDataValues.add((ExportDataValue)field.get(ExportToHTMLFunction.this.htmlData));
                    }
                    catch (Exception e) {
                        System.err.println("ERROR: " + e.getMessage());
                    }
                }
            }
        }
    }

    //
    // Constructors
    //

    public ExportToHTMLFunction() {
        super(GENERATED_HTML_FILE);
        Icon htmlIcon = new ImageIcon(ClassLoader.getSystemResource("icons/mddhtml.png"));
        this.htmlButton = new JButton(htmlIcon);
        htmlButton.setToolTipText("Export as HTML (Ctrl-H)");
        htmlButton.addActionListener(new ActionListener() {
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
        return ToolBarGroups.export.name();
    }

    @Override
    public String getName() {
        return "Export to HTML";
    }

    @Override
    public JComponent getToolBarButton() {
        return this.htmlButton;
    }

    @Override
    public int getDownKeyMask() {
        return KeyEvent.CTRL_MASK;
    }

    @Override
    public int getKeyCode() {
        return KeyEvent.VK_H;
    }

    /**
     * Executes this function.
     *
     * @throws FunctionException on failure to perform function.
     */
    @Override
    public void perform() throws FunctionException {
        this.exportFile = getExportOutputFile("HTML", "html", "html", "htm");

        if (this.exportFile != null) {
            this.htmlMetaDataDialog = new JFrame("PDF document data");
            this.htmlMetaDataDialog.setLayout(new BorderLayout());

            this.htmlData.loadPDFDataValues();

            JPanel dataLabelPanel = new JPanel(new GridLayout(this.htmlData.exportDataValues.size(),1));
            this.htmlMetaDataDialog.add(dataLabelPanel, BorderLayout.WEST);

            JPanel dataValuePanel = new JPanel(new GridLayout(this.htmlData.exportDataValues.size(),1));
            this.htmlMetaDataDialog.add(dataValuePanel, BorderLayout.CENTER);


            for (ExportDataValue exportDataValue : this.htmlData.exportDataValues) {
                dataLabelPanel.add(exportDataValue.label);
                dataValuePanel.add(exportDataValue.value);
            }

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            JButton generateButton = new JButton("Generate");
            generateButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    ExportToHTMLFunction.this.htmlMetaDataDialog.setVisible(false);
                    generateHTML();
                }
            });
            buttonPanel.add(generateButton);
            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    ExportToHTMLFunction.this.htmlMetaDataDialog.setVisible(false);
                }
            });
            buttonPanel.add(cancelButton);

            this.htmlMetaDataDialog.add(buttonPanel, BorderLayout.SOUTH);

            // Set initial values to last saved values for the specified file.
            if (this.editor.getCurrentFile() != null) {
                this.htmlData.loadExportData(this.editor.getCurrentFile());
            }

            this.htmlMetaDataDialog.setVisible(true);
            this.htmlMetaDataDialog.setBounds(
                    this.editor.getGUI().getWindowFrame().getX() + 40,
                    this.editor.getGUI().getWindowFrame().getY() + 40,
                    (int) this.htmlMetaDataDialog.getPreferredSize().getWidth(),
                    (int) this.htmlMetaDataDialog.getPreferredSize().getHeight()
            );
        }
    }

    /**
     * Initiates HTML generation and handles any failures with an error dialog.
     */
    private void generateHTML() {
        try {
            _generateHTML();
        }
        catch (RuntimeException re) {
            re.printStackTrace(System.err);
            JOptionPane.showMessageDialog(
                    this.editor.getGUI().getWindowFrame(), re.getMessage(), "Failed to save HTML!", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Actually performs the PDF generation using MarkdownDocs PDF generator.
     */
    private void _generateHTML() {
        Generator generator = new HTMLGenerator();
        HTMLGeneratorOptions htmlOpts = new HTMLGeneratorOptions();
        htmlOpts.setInlineCSS(Boolean.valueOf(this.htmlData.inlineCSS.getValue()));
        htmlOpts.setCss(this.htmlData.css.getValue());
        if (this.htmlData.fileLinks.getValue().trim().length() > 0) {
            htmlOpts.setMakeFileLinksRelativeTo(this.htmlData.fileLinks.getValue());
        }

        BufferedOutputStream htmlStream = null;
        try {
            htmlStream = new BufferedOutputStream(new FileOutputStream(this.exportFile));
            generator.generate(getMarkdownDocument(), htmlOpts, null, htmlStream);
            htmlStream.flush();
        }
        catch (IOException ioe) {
            throw new RuntimeException(ioe.getMessage(), ioe);
        }
        catch (GenerateException ge) {
            throw new RuntimeException(ge.getMessage(), ge);
        }
        finally {
            try {if (htmlStream!= null) htmlStream.close();} catch (IOException cioe) {}
        }

        if (this.editor.getCurrentFile() != null) {
            this.htmlData.saveExportData(this.editor.getCurrentFile());
        }

        if (this.htmlData.openResult.getValue().equals("true")) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.open(this.exportFile);
            }
            catch (IOException ioe) {
                JOptionPane.showMessageDialog(
                        this.editor.getGUI().getWindowFrame(), ioe.getMessage(), "Failed to open PDF!", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Cleanup and unregister any configs.
     */
    public void close() {}
}
