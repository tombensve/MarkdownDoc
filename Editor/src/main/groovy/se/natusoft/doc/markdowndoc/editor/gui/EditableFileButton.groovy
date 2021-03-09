/*
 *
 * PROJECT
 *     Name
 *         MarkdownDocEditor
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
 *         2015-08-03: Created!
 *
 */
package se.natusoft.doc.markdowndoc.editor.gui

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.doc.markdowndoc.editor.api.Editable

import javax.swing.*
import java.awt.*
import java.awt.event.MouseEvent

/**
 * This is a button that represents an editable file. It can be clicked on to select that file for editing
 * in the main window.
 */
@CompileStatic
class EditableFileButton extends JComponent implements MouseListenersTrait {

    //
    // Private Members
    //

    private boolean pointedAt = false

    //
    // Properties
    //

    /** The editable this button represents. */
    Editable editable

    //
    // Constructors
    //

    public EditableFileButton() {
        addMouseListener(this)
        setForeground(Color.BLACK)
    }

    //
    // Methods
    //

    /**
     * Renders the file name in white or yellow when mouse is over it.
     *
     * @param g The Graphics to draw with.
     */
    @Override
    public void paintComponent(final Graphics g) {
        g.setColor(this.pointedAt ? Color.GREEN : Color.WHITE)
        g.clearRect(this.x, this.y, this.width, this.height)
        g.drawString(
                this.editable.file.name, ((width / 2) -
                (getFontMetrics(font).stringWidth(this.editable.file.name) / 2)) as int,
                ((height / 2) + font.size / 2) - 2 as int)
    }

    /**
     * Invoked when the mouse enters a component.
     */
    @Override
    void mouseEntered(final MouseEvent ignored) {
        this.pointedAt = true
        repaint()

    }

    /**
     * Invoked when the mouse exits a component.
     */
    @Override
    void mouseExited(final MouseEvent ignored) {
        this.pointedAt = false
        repaint()
    }

    @Override
    Dimension getMinimumSize() {
        new Dimension(getFontMetrics(getFont()).stringWidth(this.editable.file.name) + 20, getFont().size + 8)
    }

    @Override
    Dimension getPreferredSize() {
        getMinimumSize()
    }
}
