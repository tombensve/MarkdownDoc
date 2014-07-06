package se.natusoft.doc.markdowndoc.editor.api

import groovy.transform.CompileStatic

/**
 * This should be implemented by components needing initializing after all
 * components have been created.
 */
@CompileStatic
public interface DelayedInitializer {

    /**
     * Initializes the component.
     */
    public void init()
}
