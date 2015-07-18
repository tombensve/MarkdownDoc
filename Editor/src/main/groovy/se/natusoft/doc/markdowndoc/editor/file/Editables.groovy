package se.natusoft.doc.markdowndoc.editor.file

import groovy.transform.CompileStatic
import groovy.transform.ToString
import groovy.transform.TypeChecked
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import se.natusoft.doc.markdowndoc.editor.api.Editable
import se.natusoft.doc.markdowndoc.editor.api.JTextComponentStyler

/**
 * This handles all Editables.
 */
@CompileStatic
@TypeChecked
@ToString(includeNames = true)
class Editables {
    //
    // Private Members
    //

    /** The current loaded editables. */
    private Map<File, Editable> editables = new HashMap<>()

    //
    // Properties
    //

    /** The styler instance. */
    @NotNull /* userOf */ JTextComponentStyler styler

    //
    // Methods
    //

    /**
     * Loads a new file creating an Editable associated with the file.
     *
     * @param file The file to load.
     *
     * @throws IOException on any IO failure.
     */
    void load(@NotNull File file) throws IOException {
        if (!this.editables.containsKey(file)) {
            Editable editable = new EditableProvider(file: file, editorStyler: this.styler)
            this.editables.put(file, editable)
            editable.load(file)
        }
    }

    /**
     * Saves the specified Editable.
     *
     * @param editable The Editable to save.
     *
     * @throws Exception on any IO failure.
     */
    void save(Editable editable) throws Exception {
        if (this.editables.containsValue(editable)) {
            editable.save()
        }
        else {
            System.err.println("Tried to save unknown Editable! [${editable}]")
        }
    }

    /**
     * Saves the Editable belonging to the specified file.
     *
     * @param file The file whose Editable to save.
     *
     * @throws IOException on fly colliding with a cow.
     */
    void save(@NotNull File file) throws IOException {
        save(this.editables.get(file))
    }

    /**
     * Closes the specified file without saving!
     *
     * @param file The file to close.
     */
    void close(File file) {
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
        this.editables.keySet().first()
    }
}
