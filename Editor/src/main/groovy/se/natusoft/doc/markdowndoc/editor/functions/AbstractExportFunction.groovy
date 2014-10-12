/*
 *
 * PROJECT
 *     Name
 *         MarkdownDocEditor
 *     
 *     Code Version
 *         1.3.5
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
package se.natusoft.doc.markdowndoc.editor.functions

import groovy.transform.CompileStatic
import se.natusoft.doc.markdown.api.Parser
import se.natusoft.doc.markdown.exception.ParseException
import se.natusoft.doc.markdown.model.Doc
import se.natusoft.doc.markdown.parser.MarkdownParser
import se.natusoft.doc.markdowndoc.editor.api.ConfigProvider
import se.natusoft.doc.markdowndoc.editor.api.Editor
import se.natusoft.doc.markdowndoc.editor.api.EditorFunction
import se.natusoft.doc.markdowndoc.editor.api.GUI
import se.natusoft.doc.markdowndoc.editor.api.PersistentProps
import se.natusoft.doc.markdowndoc.editor.functions.export.DelayedServiceData

import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter

/**
 * Common code for export.
 */
@CompileStatic
public abstract class AbstractExportFunction implements EditorFunction {

    //
    // Bean Members
    //

    /** The editorPane instance we provide function for. */
    Editor editor

    /** The property key for saving and loading defaults. */
    String defaultsPropKey

    /** The currently generated file. */
    File exportFile = null

    //
    // Constructor
    //

    AbstractExportFunction(String defaultsPropKey) {
        this.defaultsPropKey = defaultsPropKey
    }

    //
    // Methods
    //

    /**
     * Returns a local implementation of LocalServiceData.
     */
    protected DelayedServiceData getLocalServiceData() {
        new DelayedServiceData() {
            /**
             * Returns the default property key.
             */
            public String getDefaultsPropKey() {
                return AbstractExportFunction.this.defaultsPropKey
            }

            /**
             *  Returns the file to export to.
             */
            public File getExportFile() {
                return AbstractExportFunction.this.exportFile
            }

            /**
             * Returns the editorPane GUI API.
             */
            public GUI getGUI() {
                return AbstractExportFunction.this.editor.getGUI()
            }

            /**
             * Returns the config API.
             */
            public ConfigProvider getConfigProvider() {
                return AbstractExportFunction.this.editor.getConfigProvider()
            }

            /**
             * Returns the persistent properties provider.
             */
            public PersistentProps getPersistentProps() {
                return AbstractExportFunction.this.editor.getPersistentProps()
            }

        }
    }

    /**
     * Opens i file chooser dialog for specifying the PDF generation target file, and
     * returns the selected file.
     */
    protected File getExportOutputFile(String type, String definition, String... extFilter) {
        File selectedFile = null
        JFileChooser fileChooser = new JFileChooser()
        fileChooser.setDialogTitle("Specify file to save " + type + " to")
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG)
        if (this.editor.getCurrentFile() != null) {
            Properties props = this.editor.getPersistentProps().load(fileToPropertiesName(this.editor.getCurrentFile()))
            if (props != null && props.getProperty(this.defaultsPropKey) != null) {
                fileChooser.setSelectedFile(new File(props.getProperty(this.defaultsPropKey)))
            }
        }
        FileNameExtensionFilter filter = new FileNameExtensionFilter(definition, extFilter)
        fileChooser.setFileFilter(filter)
        int returnVal = fileChooser.showSaveDialog(this.editor.getGUI().getWindowFrame())
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile()
        }

        return selectedFile
    }

    /**
     * Converts a File to a properties name. This is used for saving generation meta data for last PDF generation
     * using the File representing the text being edited.
     *
     * @param file The file to convert to properties name.
     */
    protected String fileToPropertiesName(File file) {
        return file.getName().replace(".", "_")
    }

    /**
     * Extracts the markdown text in the editorPane and returns a parsed Doc document model of it.
     */
    protected Doc getMarkdownDocument() {
        String markdownText = this.editor.getEditorContent()
        ByteArrayInputStream markDownStream = new ByteArrayInputStream(markdownText.getBytes())

        Parser parser = new MarkdownParser()
        Doc document = new Doc()
        Properties parserOptions = new Properties()
        try {
            parser.parse(document, markDownStream, parserOptions)
        }
        catch (ParseException pe) {
            throw new RuntimeException(pe.getMessage(), pe)
        }
        catch (IOException ioe) {
            throw new RuntimeException(ioe.getMessage(), ioe)
        }
        finally {
            try {markDownStream.close()} catch (IOException cioe) {}
        }

        return document
    }

}
