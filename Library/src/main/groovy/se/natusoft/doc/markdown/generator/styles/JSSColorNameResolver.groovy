package se.natusoft.doc.markdown.generator.styles

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

/**
 * Resolves colors.
 */
@CompileStatic
@TypeChecked
interface JSSColorNameResolver {

    /**
     * Resolves a JSSColor by name or color value.
     *
     * @param name The name of the color to resolve.
     *
     * @return
     */
    JSSColor resolve(String name)
}