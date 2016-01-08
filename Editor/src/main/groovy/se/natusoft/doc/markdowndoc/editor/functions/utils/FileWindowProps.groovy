/*
 *
 * PROJECT
 *     Name
 *         MarkdownDocEditor
 *     
 *     Code Version
 *         1.4.2
 *     
 *     Description
 *         An editor that supports editing markdown with formatting preview.
 *         
 * COPYRIGHTS
 *     Copyright (C) 2012 by Natusoft AB All rights reserved.
 *     
 * LICENSE
 *     Apache 2.0 (Open Source)
 *     
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *     
 *       http://www.apache.org/licenses/LICENSE-2.0
 *     
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *     
 * AUTHORS
 *     tommy ()
 *         Changes:
 *         2013-06-06: Created!
 *
 */
package se.natusoft.doc.markdowndoc.editor.functions.utils

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.jetbrains.annotations.NotNull
import se.natusoft.doc.markdowndoc.editor.Services

import java.awt.*

/**
 * Wraps properties for editorPane window position and size. Also saves and loads these properties.
 */
@CompileStatic
@TypeChecked
class FileWindowProps {
    //
    // Constants
    //

    private static final String X = "window.x"
    private static final String Y = "window.y"
    private static final String WIDTH = "window.width"
    private static final String HEIGHT = "window.height"

    //
    // Private Members
    //

    /** The properties we are wrapping. */
    private Properties props = null

    //
    // Constructors
    //

    /**
     * Creates a new FileWindowProps instance.
     */
    FileWindowProps() {
        this.props = new Properties()
    }

    //
    // Methods
    //

    /**
     * Saves the specified bounds in the properties.
     *
     * @param bounds The bounds to selectNewFile.
     */
    void setBounds(@NotNull final Rectangle bounds) {
        this.props.setProperty(X, "" + (int)bounds.getX())
        this.props.setProperty(Y, "" + (int)bounds.getY())
        this.props.setProperty(WIDTH, "" + (int)bounds.getWidth())
        this.props.setProperty(HEIGHT, "" + (int)bounds.getHeight())
    }

    /**
     * Returns the bounds in the properties as a Rectangle.
     */
    @NotNull Rectangle getBounds() {
        new Rectangle(
            Integer.valueOf(this.props.getProperty(X)),
            Integer.valueOf(this.props.getProperty(Y)),
            Integer.valueOf(this.props.getProperty(WIDTH)),
            Integer.valueOf(this.props.getProperty(HEIGHT))
        )
    }

    /**
     * Saves the properties.
     *
     * @param editor The editorPane to selectNewFile for.
     */
    void saveBounds() {
        Services.persistentPropertiesProvider.save("default_bounds", this.props)
    }

    /**
     * Loads the properties.
     *
     * @param editor The editorPane to load for.
     */
    void load() {
        this.props = Services.persistentPropertiesProvider.load("default_bounds")
        if (this.props == null) {
            this.props = new Properties()
        }
    }

    /**
     * Returns true if there are properties available.
     */
    boolean hasProperties() {
        !this.props.isEmpty()
    }
}
