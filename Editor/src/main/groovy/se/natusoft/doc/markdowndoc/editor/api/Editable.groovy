package se.natusoft.doc.markdowndoc.editor.api

import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

import javax.swing.JTextPane

/**
 * This represents an object that is editable. Basically a JTextPane and a File.
 */
interface Editable extends MouseMotionProvider {

    /**
     * A JTextPane instance that can be put in the editor main editing view for editing.
     */
    @Nullable JTextPane getEditorPane()

    /**
     * Returns the styler for the document.
     */
    @Nullable JTextComponentStyler getEditorStyler()

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
     * @param extraTasks A closure with extra tasks to do after the loaded text have been set in editor but before
     *                   styling has been enabled again.
     *
     * @throws IOException
     */
    void load(@NotNull File file, Closure<Void> extraTasks) throws IOException

    /**
     * Returns the loaded file or null if no file have been loaded.
     */
    @Nullable File getFile()

    /**
     * Saves the file.
     *
     * @throws IOException on failure to save.
     */
    void save() throws IOException

}
