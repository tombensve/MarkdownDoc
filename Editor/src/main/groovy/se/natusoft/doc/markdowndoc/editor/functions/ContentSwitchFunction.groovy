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
import se.natusoft.doc.markdowndoc.editor.config.KeyConfigEntry
import se.natusoft.doc.markdowndoc.editor.config.KeyboardKey
import se.natusoft.doc.markdowndoc.editor.exceptions.FunctionException
import se.natusoft.doc.markdowndoc.editor.file.Editables
import se.natusoft.doc.markdowndoc.editor.gui.ColorsTrait
import se.natusoft.doc.markdowndoc.editor.gui.FileNamePopup
import se.natusoft.doc.markdowndoc.editor.gui.GuiEnvToolsTrait

import javax.swing.*

import static se.natusoft.doc.markdowndoc.editor.config.Constants.CONFIG_GROUP_KEYBOARD

/**
 * For the lack of a better name this function allows the user to keyboard wise rotate among the open files.
 */
@CompileStatic
@TypeChecked
class ContentSwitchFunction implements EditorFunction, Configurable, GuiEnvToolsTrait, ColorsTrait {

    //
    // Config
    //

    private static final KeyConfigEntry keyboardShortcutConfig =
            new KeyConfigEntry("editor.function.content-switch.keyboard.shortcut", "Content switch keyboard shortcut",
                    new KeyboardKey("Alt+Tab"), CONFIG_GROUP_KEYBOARD)

    /**
     * Register configurations.
     *
     * @param configProvider The config provider to register with.
     */
    @Override
    void registerConfigs(@NotNull final ConfigProvider configProvider) {
        this.fileNamePopup.registerConfigs(configProvider)
        configProvider.registerConfig(keyboardShortcutConfig, null)
    }

    /**
     * Unregister configurations.
     *
     * @param configProvider The config provider to unregister with.
     */
    @Override
    void unregisterConfigs(@NotNull final ConfigProvider configProvider) {
        this.fileNamePopup.unregisterConfigs(configProvider)
        configProvider.unregisterConfig(keyboardShortcutConfig, null)
    }

    //
    // Private Members
    //

    /** A popup window that shows the current Editable file name. */
    private FileNamePopup fileNamePopup = new FileNamePopup()

    /** The current of all Editables. */
    private int currIndex = 0

    /** A thread that shows and hides the fileNamePopup. */
    private NamePopupThread namePopupThread = new NamePopupThread()

    //
    // Properties
    //

    /** The function triggering edtior. */
    @Nullable Editor editor

    //
    // Constructors
    //

    /**
     * Creates & initializes a new ContentSwitchFunction.
     */
    public ContentSwitchFunction() {
        this.namePopupThread.start()
    }

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
        return "contentSwitch"
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
        return keyboardShortcutConfig.getKeyboardKey()
    }

    /**
     * Performs the function.
     *
     * @throws FunctionException
     */
    @Override
    void perform() throws FunctionException {
        ++this.currIndex
        if (this.currIndex >= Editables.inst.size()) {
            this.currIndex = 0
        }
        this.editor.editable = Editables.inst.getByPosition(this.currIndex)
        this.namePopupThread.show()
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
        }
    }

    /**
     * Called when instance is no longer needed.
     */
    @Override
    void close() {
        this.namePopupThread.markRunning(false)
        this.namePopupThread.join()
    }

    /**
     * This is a thread that will always be running, but will sleep for most of the time.
     * <p/>
     * It reacts on the 'showName' local variable being true in which case it pops up a popup window showing the name of the
     * currently shown editable, and then sets showName to false again. An end time is also set and when that end time is
     * reached the popup is closed. Every time show() is called the end time is prolonged.
     */
    private class NamePopupThread extends Thread {

        //
        // Private Members
        //

        /** The end time after which have passed the popup window will be closed if open. */
        private long endTime

        /**
         * Indicates if the thread is running or not. This is updated and read by synchronized methods
         * markRunning(state) and isRunning().
         */
        private boolean running = false

        /** This tells the thread to popup the current editable file name when true. This acts as a semaphore. */
        private boolean showNameSemaphore = false

        //
        // Methods
        //

        /**
         * This should be called to bring up the current editable file name popup.
         */
        synchronized void show() {
            this.showNameSemaphore = true
            updateEndTime()
        }

        // The following methods basically just wraps private members to make their access synchronized.

        /**
         * This adds 2500 milliseconds to the end time at which the popup will be closed if open.
         */
        synchronized void updateEndTime() {
            this.endTime = new Date().time + 2500
        }

        /**
         * This is called by the thread itself to indicate that it is upp and running. It should be called with a value
         * of false to stop the thread.
         *
         * @param state The state of the thread to set, true to keep running, false to stop thread.
         */
        synchronized void markRunning(boolean state) {
            this.running = state
        }

        /**
         * This is called within the thread after a true value of showNameSemaphore is acknowledged.
         * It will restore the semaphore so that it can be triggered again.
         */
        private synchronized void hide() {
            this.showNameSemaphore = false
        }

        /**
         * This indicates if the showNameSemaphore is active and should be reacted on.
         */
        private synchronized boolean isShow() {
            this.showNameSemaphore
        }

        /**
         * This checks the current running state.
         */
        private synchronized boolean isRunning() {
            return this.running
        }

        /**
         * Returns the end time.
         */
        private synchronized long getEndAt() {
            return this.endTime
        }

        /**
         * This is the whole life of the thread!
         */
        public void run() {
            markRunning(true)
            while (isRunning()) {
                try {
                    if (isShow()) {
                        hide()
                        if (fileNamePopup.showing) {
                            fileNamePopup.setFileName(editor.editable.file.name)
                        } else {
                            fileNamePopup.displayName(editor.editable.file.name)
                        }
                        editor.requestEditorFocus()
                    }

                    if (fileNamePopup.showing && new Date().time >= this.endAt) {
                        fileNamePopup.hideName()
                    }

                    sleep(200)
                }
                catch (Exception e) {
                    e.printStackTrace()
                }
            }
        }
    }
}
