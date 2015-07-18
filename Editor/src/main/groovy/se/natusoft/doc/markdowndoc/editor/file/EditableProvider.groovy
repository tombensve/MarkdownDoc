package se.natusoft.doc.markdowndoc.editor.file

import groovy.transform.CompileStatic
import groovy.transform.ToString
import groovy.transform.TypeChecked
import net.iharder.dnd.FileDrop
import org.jetbrains.annotations.NotNull
import se.natusoft.doc.markdowndoc.editor.api.Editable
import se.natusoft.doc.markdowndoc.editor.api.JTextComponentStyler

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
class EditableProvider implements Editable {
    //
    // Private Members
    //

    private UndoManager undoManager

    //
    // Properties
    //

    /** The actual editor instance for this file. */
    JTextPane editorPane = new JTextPane() {
        @Override
        Dimension getPreferredSize() {
            Dimension dim = super.getPreferredSize()
            dim.setSize(getWidth(), dim.getHeight())

            return dim
        }
        @Override
        Dimension getMinimumSize() {
            return getPreferredSize()
        }
    }

    /** The currently loaded file or null if none. */
    File file

    /** The styler for the file. */
    JTextComponentStyler editorStyler
    void setEditorStyler(JTextComponentStyler styler) {
        this.editorStyler = styler
        configureEditorPane()
    }

    /** The saved state of this editable. */
    boolean saved

    //
    // Methods
    //

    /**
     * Configures the editorPane with a StyledDocument and an undo manager.
     */
    private void configureEditorPane() {
        this.editorStyler.init(this.editorPane)

        // Attach undo manager to document.
        Document doc = this.editorPane.getDocument()

        String undoKey = "control Z"
        String redoKey = "control Y"

        String osName = System.getProperty("os.name").toUpperCase()
        if (osName.contains("MAC")) {
            undoKey = "meta Z"
            redoKey = "shift meta Z"
        }

        this.undoManager = new UndoManager()

        doc.addUndoableEditListener(new UndoableEditListener() {
            public void undoableEditHappened(UndoableEditEvent evt) {
                this.undoManager.addEdit(evt.edit);
            }
        })

        this.editorPane.getActionMap().put("Undo", new AbstractAction("Undo") {
            public void actionPerformed(ActionEvent evt) {
                try
                {
                    if (this.undoManager.canUndo()) {
                        this.undoManager.undo()
                    }
                }
                catch (CannotUndoException cue) {
                    System.err.println("Undo problem: ${cue.message}")
                }
            }
        })
        this.editorPane.getInputMap().put(KeyStroke.getKeyStroke(undoKey), "Undo")

        this.editorPane.getActionMap().put("Redo", new AbstractAction("Redo") {
            public void actionPerformed(ActionEvent evt) {
                try {
                    if (this.undoManager.canRedo()) {
                        this.undoManager.redo()
                    }
                }
                catch (CannotRedoException cre) {
                    System.err.println("Redo problem: ${cre.message}")
                }
            }
        })
        this.editorPane.getInputMap().put(KeyStroke.getKeyStroke(redoKey), "Redo")
    }


    /**
     * Loads a file and creates an editor pane. After this call getEditorPane() should be non null.
     * @param file
     * @throws IOException
     */
    @Override
    void load(@NotNull File file) throws IOException {
        this.file = file

        StringBuilder sb = new StringBuilder()
        file.withReader('UTF-8') { BufferedReader reader ->
            reader.eachLine { String line ->
                // Translate a special italicized quote that some markdown editors like to use into a
                // standard quote.
                line = line.replace("‚Äù", "\"")
                sb.append(line)
                sb.append("\n")
            }
        }

        this.editorPane.setText(sb.toString())
    }

    /**
     * Saves the file.
     *
     * @throws IOException on failure to save.
     */
    @Override
    void save() throws IOException {
        file.withWriter('UTF-8') { BufferedWriter writer ->
            writer.write(this.editorPane.text)
        }
    }

    /**
     * Adds a mouse motion listener to receive mouse motion events.
     *
     * @param listener The listener to add.
     */
    void addMouseMotionListener(@NotNull MouseMotionListener listener) {
        this.editorPane.addMouseMotionListener(listener)
    }

    /**
     * Removes a mouse motion listener.
     *
     * @param listener The listener to remove.
     */
    void removeMouseMotionListener(@NotNull MouseMotionListener listener) {
        this.editorPane.removeMouseMotionListener(listener)
    }

}
