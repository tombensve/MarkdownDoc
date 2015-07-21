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
class Editables {

    /**
     * A singleton instance.
     */
    static Editables inst = new Editables()

    //
    // Private Members
    //

    /** The current loaded editables. */
    private Map<File, Editable> editables = new HashMap<>()

    //
    // Methods
    //

    /**
     * Adds an editable to the editables.
     *
     * @param editable The editable to add.
     */
    void addEditable(Editable editable) {
        this.editables.put(editable.file, editable)
    }

    /**
     * Removes the specifed Editable from the Editables.
     *
     * @param file The file referencing the editable to remove.
     */
    void removeEditable(File file) {
        this.editables.remove(file)
    }

    /**
     * Returns the Editable for the specified File, or null if not found.
     *
     * @param file The File to get Editable for.
     */
    @Nullable Editable getEditable(File file) {
        this.editables.get(file)
    }

    @Nullable File getSomeEditable() {
        if (this.editables.isEmpty()) {
            return null
        }
        files.first()
    }

    /**
     * Returns all files.
     */
    Set<File> getFiles() {
        this.editables.keySet()
    }
}
