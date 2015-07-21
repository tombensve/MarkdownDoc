package se.natusoft.doc.markdowndoc.editor.api

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

import javax.swing.JTextPane

/**
 * This represents an object that is editable. Basically a JTextPane and a File.
 */
@CompileStatic
@TypeChecked
interface Editable extends MouseMotionProvider {

    /**
     * A JTextPane instance that can be put in the editor main editing view for editing.
     */
    @Nullable JTextPane getEditorPane()

    /**
     * Sets saved state to true or false.
     *
     * @param state The state to set.
     */
    void setSaved(boolean state)

    /**
     * Returns the saved state.
     */
    boolean getSaved()

    /**
     * Loads a file and creates an editor pane. After this call getEditorPane() should be non null.
     * @param file
     *
     * @throws IOException
     */
    void load(@NotNull File file) throws IOException

    /**
     * Returns the loaded file or null if no file have been loaded.
     */
    @Nullable File getFile()

    /**
     * Sets the file for this Editable.
     *
     * @param file The file to set.
     */
    void setFile(@NotNull File file)

    /**
     * Saves the file.
     *
     * @throws IOException on failure to selectNewFile.
     */
    void save() throws IOException

}
