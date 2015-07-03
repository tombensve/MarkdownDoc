package se.natusoft.doc.markdown.generator

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

/**
 * This is a general context containing information needed for generating. This needs to
 * be passed along to all classes needing this information.
 */
@CompileStatic
@TypeChecked
class GeneratorContext {
    //
    // Properties
    //

    /** Used to resolve file paths. */
    FileResource fileResource
}
