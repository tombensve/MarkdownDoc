/*
 *
 * PROJECT
 *     Name
 *         MarkdownDocEditor
 *     
 *     Code Version
 *         2.0.0
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
 *         2015-08-03: Created!
 *
 */
package se.natusoft.doc.markdowndoc.editor.file

import groovy.transform.CompileStatic
import groovy.transform.ToString
import groovy.transform.TypeChecked
import org.jetbrains.annotations.NotNull
import se.natusoft.doc.markdowndoc.editor.OSTrait
import se.natusoft.doc.markdowndoc.editor.api.*

import javax.swing.*
import javax.swing.event.UndoableEditEvent
import javax.swing.event.UndoableEditListener
import javax.swing.text.Document
import javax.swing.undo.CannotRedoException
import javax.swing.undo.CannotUndoException
import javax.swing.undo.UndoManager
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.MouseMotionListener

/**
 * Provides a implementation of EditorFile.
 */
@CompileStatic
@TypeChecked
@ToString(includeNames = true)
class EditableProvider implements Editable, OSTrait {
    //
    // Private Members
    //

    private UndoManager undoManager

    /** The actual editor instance for this file. */
    private JTextPane editorPane = new JTextPane() {

        @Override
        Dimension getMinimumSize() {
            return getPreferredSize()
        }
    }

    /** The currently loaded file or null if none. */
    private File file

    /** The styler for this editable. */
    private JTextComponentStyler styler

    //
    // Properties
    //

    /**
     * The saved state of this editable. This will be changed to false when editable is active in editor and
     * text is entered.
     */
    boolean saved = true

    //
    // Constructors
    //

    /**
     * Creates a new EditableProvider instance.
     *
     * @param file The file of this editable.
     * @param stylerFactory A JTextComponentStylerFactory for creating a styler for the editable.
     * @param configurables Required by the JTextComponentStylerFactory when creating styler.
     * @param configProvider Required by the JTextComponentStylerFactory when creating styler.
     *
     * @throws IOException on failure to load file.
     */
    EditableProvider(
            final @NotNull File file, @NotNull final JTextComponentStylerFactory stylerFactory) throws IOException {

        this.file = file
        this.styler = stylerFactory.createStyler(this)

        // Attach undo manager to document.
        final Document doc = this.styler.createDocumentModel()

        String undoKey = "control Z"
        String redoKey = "control Y"

        if (macOSXOS) {
            undoKey = "meta Z"
            redoKey = "shift meta Z"
        }

        this.undoManager = new UndoManager()

        doc.addUndoableEditListener(new UndoableEditListener() {
            public void undoableEditHappened(final UndoableEditEvent evt) {
                EditableProvider.this.undoManager.addEdit(evt.edit);
            }
        })

        // This is incredibly important since only this document model have insertString(...)
        // and remove() overridden to automatically update styling as the model is updated.
        // Also only this model has the configured styling fonts and sizes.
        this.editorPane.document = doc

        this.editorPane.getActionMap().put("Undo", new AbstractAction("Undo") {
            public void actionPerformed(final ActionEvent evt) {
                try
                {
                    if (EditableProvider.this.undoManager.canUndo()) {
                        EditableProvider.this.undoManager.undo()
                    }
                }
                catch (final CannotUndoException cue) {
                    System.err.println("Undo problem: ${cue.message}")
                }
            }
        })
        this.editorPane.getInputMap().put(KeyStroke.getKeyStroke(undoKey), "Undo")

        this.editorPane.getActionMap().put("Redo", new AbstractAction("Redo") {
            public void actionPerformed(final ActionEvent evt) {
                try {
                    if (EditableProvider.this.undoManager.canRedo()) {
                        EditableProvider.this.undoManager.redo()
                    }
                }
                catch (final CannotRedoException cre) {
                    System.err.println("Redo problem: ${cre.message}")
                }
            }
        })
        this.editorPane.getInputMap().put(KeyStroke.getKeyStroke(redoKey), "Redo")

        load()
    }

    //
    // Methods
    //

    /**
     * Returns the editorPane instance.
     */
    JTextPane getEditorPane() {
        this.editorPane
    }

    /**
     * Returns the styler.
     */
    JTextComponentStyler getStyler() {
        this.styler
    }

    /**
     * Returns the file.
     */
    File getFile() {
        this.file
    }

    /**
     * Loads a file and creates an editor pane. After this call getEditorPane() should be non null.
     * @param file
     * @throws IOException
     */
    @Override
    void load() throws IOException {
        final StringBuilder sb = new StringBuilder()
        file.withReader('UTF-8') { final BufferedReader reader ->
            // If you are using IDEA 14.1.4 the "String" part below will be marked as an error.
            // It isn't! BufferedReader.eachLine() do provide a String. IDEA have a general
            // problem with closures taking a String argument. Other types seem to work OK.
            reader.eachLine { final String line ->
                // Translate a special italicized quote that some markdown editors like to use into a
                // standard quote.
                final String modifiedLine = line.replace("‚Äù", "\"")
                sb.append(modifiedLine)
                sb.append("\n")
            }
        }

        this.styler.disable()
        this.editorPane.setText(sb.toString())
        this.styler.enable()
        this.styler.styleDocument()
        saved = true
    }

    /**
     * Saves the file.
     *
     * @throws IOException on failure to selectNewFile.
     */
    @Override
    void save() throws IOException {
        file.withWriter('UTF-8') { final BufferedWriter writer ->
            writer.write(this.editorPane.text)
        }
        saved = true
    }

    /**
     * Adds a mouse motion listener to receive mouse motion events.
     *
     * @param listener The listener to add.
     */
    void addMouseMotionListener(@NotNull final MouseMotionListener listener) {
        this.editorPane.addMouseMotionListener(listener)
    }

    /**
     * Removes a mouse motion listener.
     *
     * @param listener The listener to remove.
     */
    void removeMouseMotionListener(@NotNull final MouseMotionListener listener) {
        this.editorPane.removeMouseMotionListener(listener)
    }

}
