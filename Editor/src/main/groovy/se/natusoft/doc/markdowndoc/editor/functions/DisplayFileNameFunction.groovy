/* 
 * 
 * PROJECT
 *     Name
 *         MarkdownDocEditor
 *     
 *     Code Version
 *         2.0.1
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
 *         2016-01-08: Created!
 *         
 */
package se.natusoft.doc.markdowndoc.editor.functions

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import se.natusoft.doc.markdowndoc.editor.api.ConfigProvider
import se.natusoft.doc.markdowndoc.editor.api.Configurable
import se.natusoft.doc.markdowndoc.editor.api.Editor
import se.natusoft.doc.markdowndoc.editor.api.EditorFunction
import se.natusoft.doc.markdowndoc.editor.config.ConfigEntry
import se.natusoft.doc.markdowndoc.editor.config.KeyboardKey
import se.natusoft.doc.markdowndoc.editor.exceptions.FunctionException
import se.natusoft.doc.markdowndoc.editor.gui.ColorsTrait
import se.natusoft.doc.markdowndoc.editor.gui.FileNamePopup
import se.natusoft.doc.markdowndoc.editor.gui.GuiEnvToolsTrait
import se.natusoft.doc.markdowndoc.editor.gui.PopupLock
import se.natusoft.doc.markdowndoc.editor.gui.PopupWindow

import javax.swing.*
import javax.swing.border.EmptyBorder
import java.awt.*
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionListener

/**
 * This function displays the name of the currently edited file at the same time as the toolbar is visible.
 */
@CompileStatic
@TypeChecked
class DisplayFileNameFunction implements EditorFunction, Configurable, GuiEnvToolsTrait, ColorsTrait {


    /**
     * Register configurations.
     *
     * @param configProvider The config provider to register with.
     */
    @Override
    void registerConfigs(@NotNull final ConfigProvider configProvider) {
        this.fileNamePopup.registerConfigs(configProvider)
    }

    /**
     * Unregister configurations.
     *
     * @param configProvider The config provider to unregister with.
     */
    @Override
    void unregisterConfigs(@NotNull final ConfigProvider configProvider) {
        this.fileNamePopup.unregisterConfigs(configProvider)
    }

    //
    // Private Members
    //

    private FileNamePopup fileNamePopup = new FileNamePopup()

    private MouseMotionListener mouseMotionListener

    //
    // Properties
    //

    /** The function triggering edtior. */
    @Nullable Editor editor

    //
    // Methods
    //

    /**
     * Returns the group in the tool bar this functions should be placed in.
     * A new group will be created if the named group does not exist.
     */
    @Override
    String getGroup() {
        return null
    }

    /**
     * Returns the name of the function.
     */
    @Override
    String getName() {
        return "DisplayFileName"
    }

    /**
     * Returns this functions toolbar button or null if it does not have one.
     */
    @Override
    JComponent getToolBarButton() {
        return null
    }

    /**
     * Returns the keyboard shortcut for triggering the function via keyboard.
     */
    @Override
    KeyboardKey getKeyboardShortcut() {
        return null
    }

    /**
     * Performs the function.
     *
     * @throws FunctionException
     */
    @Override
    void perform() throws FunctionException {

    }

    /**
     * Sets the editorPane for the component to use.
     *
     * @param editor The editorPane to set.
     */
    @Override
    void setEditor(@Nullable final Editor editor) {
        this.editor = editor
        if (this.editor != null) {
            this.fileNamePopup.parent = this.editor.GUI.windowFrame

            this.mouseMotionListener = new MouseMotionListener() {
                @Override
                void mouseDragged(final MouseEvent e) {}

                @Override
                void mouseMoved(final MouseEvent e) {
                    mouseMovedHandler(e)
                }
            }
            this.editor.addMouseMotionListener(this.mouseMotionListener)
        }
    }

    /**
     * Called when instance is no longer needed.
     */
    @Override
    void close() {
        this.editor.removeMouseMotionListener(this.mouseMotionListener)
    }

    protected void mouseMovedHandler(@NotNull final MouseEvent e) {
        if (this.editor == null) return
        final int y = e.y - this.editor.GUI.editorVisibleY
        if (y <= this.editor.topMargin && e.x >= 0 && e.x <= this.editor.width) {
            if (!PopupLock.instance.locked) {
                this.fileNamePopup.displayName(this.editor.editable.file.name)
            }
        }
        else {
            this.fileNamePopup.hideName()
        }
    }

}
