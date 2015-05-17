/* 
 * 
 * PROJECT
 *     Name
 *         MarkdownDocEditor
 *     
 *     Code Version
 *         1.3.9
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
import se.natusoft.doc.markdowndoc.editor.api.Editor

import java.awt.*

/**
 * Wraps properties for editorPane window position and size.
 */
@CompileStatic
public class FileWindowProps {
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
    public FileWindowProps() {
        this.props = new Properties()
    }

    //
    // Methods
    //

    /**
     * Saves the specified bounds in the properties.
     *
     * @param bounds The bounds to save.
     */
    public void setBounds(Rectangle bounds) {
        this.props.setProperty(X, "" + (int)bounds.getX())
        this.props.setProperty(Y, "" + (int)bounds.getY())
        this.props.setProperty(WIDTH, "" + (int)bounds.getWidth())
        this.props.setProperty(HEIGHT, "" + (int)bounds.getHeight())
    }

    /**
     * Returns the bounds in the properties as a Rectangle.
     */
    public Rectangle getBounds() {
        Rectangle rectangle = new Rectangle(
            Integer.valueOf(this.props.getProperty(X)),
            Integer.valueOf(this.props.getProperty(Y)),
            Integer.valueOf(this.props.getProperty(WIDTH)),
            Integer.valueOf(this.props.getProperty(HEIGHT))
        )

        return rectangle
    }

    /**
     * Saves the properties.
     *
     * @param editor The editorPane to save for.
     */
    public void saveBounds(Editor editor) {
        String boundsNamePart = "default"
        if (editor.getCurrentFile() != null) {
            boundsNamePart = editor.getCurrentFile().getName().replace(".", "_")
        }

        editor.getPersistentProps().save(boundsNamePart + "_bounds", this.props)
    }

    /**
     * Loads the properties.
     *
     * @param editor The editorPane to load for.
     */
    public void load(Editor editor) {
        String boundsNamePart = "default"
        if (editor.getCurrentFile() != null) {
            boundsNamePart = editor.getCurrentFile().getName().replace(".", "_")
        }
        this.props = editor.getPersistentProps().load(boundsNamePart + "_bounds")
        if (this.props == null) {
            this.props = new Properties()
        }
    }

    /**
     * Returns true if there are properties available.
     */
    public boolean hasProperties() {
        return !this.props.isEmpty()
    }
}
