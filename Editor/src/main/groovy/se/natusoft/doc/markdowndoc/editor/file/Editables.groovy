package se.natusoft.doc.markdowndoc.editor.file

import groovy.transform.CompileStatic
import groovy.transform.ToString
import groovy.transform.TypeChecked
import org.jetbrains.annotations.Nullable
import se.natusoft.doc.markdowndoc.editor.api.Editable

/**
 * This handles all Editables.
 */
@CompileStatic
@TypeChecked
@ToString(includeNames = true)
class Editables extends HashMap<File, Editable> {

    /**
     * A singleton instance.
     */
    static Editables inst = new Editables()

    //
    // Methods
    //

    /**
     * Adds an editable to the editables.
     *
     * @param editable The editable to add.
     */
    void addEditable(Editable editable) {
        put(editable.file, editable)
    }

    /**
     * Removes the specifed Editable from the Editables.
     *
     * @param file The file referencing the editable to remove.
     */
    void removeEditable(File file) {
        remove(file)
    }

    /**
     * Returns the Editable for the specified File, or null if not found.
     *
     * @param file The File to get Editable for.
     */
    @Nullable Editable getEditable(File file) {
        get(file)
    }

    @Nullable Editable getSomeEditable() {
        if (isEmpty()) {
            return null
        }
        get(files.first())
    }

    /**
     * Returns all files.
     */
    Set<File> getFiles() {
        keySet()
    }
}
