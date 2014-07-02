package se.natusoft.doc.markdowndoc.editor.api;

/**
 * This should be implemented by components needing initializing after all
 * components have been created.
 */
public interface DelayedInitializer {

    /**
     * Initializes the component.
     */
    public void init();
}
