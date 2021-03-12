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

import java.awt.*
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener
import java.util.List

/**
 *
 */
@CompileStatic
trait MouseListenersTrait implements MouseListener, MouseMotionListener {

    private List<MouseListener> mouseListeners = new LinkedList<>()

    public void mltAddMouseListener(MouseListener mouseListener) {
        this.mouseListeners.add(mouseListener)
    }

    public void mltRemoveMouseListener(MouseListener mouseListener) {
        this.mouseListeners.remove(mouseListener)
    }

    public void forwardMouseClicked(MouseEvent mouseEvent, Component source) {
        MouseEvent mouseEvent2 = new MouseEvent(source, mouseEvent.ID, mouseEvent.when, mouseEvent.modifiers, mouseEvent.x, mouseEvent.y,
                mouseEvent.clickCount, mouseEvent.popupTrigger, mouseEvent.button)
        this.mouseListeners.each { MouseListener listener ->
            listener.mouseClicked(mouseEvent2)
        }
    }

    public void forwardMouseEntered(MouseEvent mouseEvent, Component source) {
        MouseEvent mouseEvent2 = new MouseEvent(source, mouseEvent.ID, mouseEvent.when, mouseEvent.modifiers, mouseEvent.x, mouseEvent.y,
                mouseEvent.clickCount, mouseEvent.popupTrigger, mouseEvent.button)
        this.mouseListeners.each { MouseListener listener ->
            listener.mouseEntered(mouseEvent2)
        }
    }

    public void forwardMouseExited(MouseEvent mouseEvent, Component source) {
        MouseEvent mouseEvent2 = new MouseEvent(source, mouseEvent.ID, mouseEvent.when, mouseEvent.modifiers, mouseEvent.x, mouseEvent.y,
                mouseEvent.clickCount, mouseEvent.popupTrigger, mouseEvent.button)
        this.mouseListeners.each { MouseListener listener ->
            listener.mouseExited(mouseEvent2)
        }
    }

    public void forwardMousePressed(MouseEvent mouseEvent, Component source) {
        MouseEvent mouseEvent2 = new MouseEvent(source, mouseEvent.ID, mouseEvent.when, mouseEvent.modifiers, mouseEvent.x, mouseEvent.y,
                mouseEvent.clickCount, mouseEvent.popupTrigger, mouseEvent.button)
        this.mouseListeners.each { MouseListener listener ->
            listener.mousePressed(mouseEvent2)
        }
    }

    public void forwardMouseReleased(MouseEvent mouseEvent, Component source) {
        MouseEvent mouseEvent2 = new MouseEvent(source, mouseEvent.ID, mouseEvent.when, mouseEvent.modifiers, mouseEvent.x, mouseEvent.y,
                mouseEvent.clickCount, mouseEvent.popupTrigger, mouseEvent.button)
        this.mouseListeners.each { MouseListener listener ->
            listener.mouseReleased(mouseEvent2)
        }
    }

    //
    // Dummy implementations to override.
    //

    /**
     * Invoked when the mouse button has been clicked (pressed
     * and released) on a component.
     */
    @Override
    void mouseClicked(final MouseEvent e) {}

    /**
     * Invoked when a mouse button has been pressed on a component.
     */
    @Override
    void mousePressed(final MouseEvent e) {}

    /**
     * Invoked when a mouse button has been released on a component.
     */
    @Override
    void mouseReleased(final MouseEvent e) {}

    /**
     * Invoked when the mouse enters a component.
     */
    @Override
    void mouseEntered(final MouseEvent e) {}

    /**
     * Invoked when the mouse exits a component.
     */
    @Override
    void mouseExited(final MouseEvent e) {}

    /**
     * Invoked when a mouse button is pressed on a component and then
     * dragged.  <code>MOUSE_DRAGGED</code> events will continue to be
     * delivered to the component where the drag originated until the
     * mouse button is released (regardless of whether the mouse position
     * is within the bounds of the component).
     * <p>
     * Due to platform-dependent Drag&amp;Drop implementations,
     * <code>MOUSE_DRAGGED</code> events may not be delivered during a native
     * Drag&amp;Drop operation.
     */
    @Override
    void mouseDragged(final MouseEvent e) {}

    /**
     * Invoked when the mouse cursor has been moved onto a component
     * but no buttons have been pushed.
     */
    @Override
    void mouseMoved(final MouseEvent e) {}

}
