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
 *     tommy ()
 *         Changes:
 *         2013-06-06: Created!
 *
 */
package se.natusoft.doc.markdowndoc.editor.functions

import groovy.transform.CompileStatic
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import se.natusoft.doc.markdown.api.Parser
import se.natusoft.doc.markdown.exception.ParseException
import se.natusoft.doc.markdown.model.Doc
import se.natusoft.doc.markdown.parser.MarkdownParser
import se.natusoft.doc.markdowndoc.editor.Services
import se.natusoft.doc.markdowndoc.editor.api.Editor
import se.natusoft.doc.markdowndoc.editor.api.EditorFunction
import se.natusoft.doc.markdowndoc.editor.api.GUI
import se.natusoft.doc.markdowndoc.editor.functions.export.DelayedServiceData

import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter

/**
 * Common code for export.
 */
@CompileStatic
abstract class AbstractExportFunction implements EditorFunction {

    //
    // properties
    //

    /** The editor instance we provide function for. */
    @Nullable Editor editor

    /** The property key for saving and loading defaults. */
    @NotNull String defaultsPropKey

    /** The currently generated file. */
    @Nullable File exportFile = null

    //
    // Constructor
    //

    AbstractExportFunction(@NotNull final String defaultsPropKey) {
        this.defaultsPropKey = defaultsPropKey
    }

    //
    // Methods
    //

    /**
     * Returns a local implementation of LocalServiceData.
     */
    protected @NotNull DelayedServiceData getLocalServiceData() {
        new DelayedServiceData() {
            /**
             * Returns the default property key.
             */
            @NotNull String getDefaultsPropKey() {
                AbstractExportFunction.this.defaultsPropKey
            }

            /**
             *  Returns the file to export to.
             */
            @Nullable File getExportFile() {
                AbstractExportFunction.this.exportFile
            }

            /**
             * Returns the editorPane GUI API.
             */
            @NotNull GUI getGUI() {
                AbstractExportFunction.this.editor.getGUI()
            }

        }
    }

    /**
     * Opens i file chooser dialog for specifying the PDF generation target file, and
     * returns the selected file.
     *
     * @param type The type of the file. Example: "HTML" or "PDF".
     * @param definition Passed to swings FileNameExtensionFilter and is a description of the type of file
     *                   being filtered. For example "pdf".
     * @param extFilter The actual extensions of files to make selectable in file chooser. For example "html", "htm".
     */
    protected File getExportOutputFile(@NotNull final String type, @NotNull final String definition,
                                       @NotNull final String... extFilter) {

        File selectedFile = null
        final JFileChooser fileChooser = new JFileChooser()
        fileChooser.setDialogTitle("Specify file to save " + type + " to")
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG)
        if (this.editor.editable.file != null) {
            final Properties props = Services.persistentPropertiesProvider.
                    load(fileToPropertiesName(this.editor.editable.file))
            if (props != null && props.getProperty(this.defaultsPropKey) != null) {
                fileChooser.setSelectedFile(new File(props.getProperty(this.defaultsPropKey)))
            }
        }
        final FileNameExtensionFilter filter = new FileNameExtensionFilter(definition, extFilter)
        fileChooser.setFileFilter(filter)
        final int returnVal = fileChooser.showSaveDialog(this.editor.getGUI().getWindowFrame())
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile()
        }

        selectedFile
    }

    /**
     * Converts a File to a properties name. This is used for saving generation meta data for last PDF generation
     * using the File representing the text being edited.
     *
     * @param file The file to convert to properties name.
     */
    protected static String fileToPropertiesName(@NotNull final File file) {
        file.getName().replace(".", "_")
    }

    /**
     * Extracts the markdown text in the editorPane and returns a parsed Doc document model of it.
     */
    protected @NotNull Doc getMarkdownDocument() {
        final String markdownText = this.editor.getEditorContent()
        final ByteArrayInputStream markDownStream = new ByteArrayInputStream(markdownText.getBytes())

        final Parser parser = new MarkdownParser()
        final Doc document = new Doc()
        final Properties parserOptions = new Properties()
        try {
            parser.parse(document, markDownStream, parserOptions)
        }
        catch (final ParseException pe) {
            throw new RuntimeException(pe.getMessage(), pe)
        }
        catch (final IOException ioe) {
            throw new RuntimeException(ioe.getMessage(), ioe)
        }
        finally {
            try {markDownStream.close()} catch (final IOException ignore) {}
        }

        document
    }

}
