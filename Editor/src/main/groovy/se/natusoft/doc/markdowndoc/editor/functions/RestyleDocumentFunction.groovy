/* 
 * 
 * PROJECT
 *     Name
 *         MarkdownDocEditor
 *     
 *     Code Version
 *         1.3.3
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
 *     Licensed under the Apache License, Version 2.0 (the "License")
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
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2013-05-27: Created!
 *         
 */
package se.natusoft.doc.markdowndoc.editor.functions

import groovy.transform.CompileStatic
import se.natusoft.doc.markdowndoc.editor.api.ConfigProvider
import se.natusoft.doc.markdowndoc.editor.api.Configurable
import se.natusoft.doc.markdowndoc.editor.api.Editor
import se.natusoft.doc.markdowndoc.editor.api.EditorFunction
import se.natusoft.doc.markdowndoc.editor.config.ConfigChanged
import se.natusoft.doc.markdowndoc.editor.config.ConfigEntry
import se.natusoft.doc.markdowndoc.editor.config.KeyConfigEntry
import se.natusoft.doc.markdowndoc.editor.config.KeyboardKey
import se.natusoft.doc.markdowndoc.editor.exceptions.FunctionException

import javax.swing.*

import static se.natusoft.doc.markdowndoc.editor.config.Constants.CONFIG_GROUP_KEYBOARD

/**
 * This provides a function that restyles the document.
 */
@CompileStatic
public class RestyleDocumentFunction implements EditorFunction, Configurable {
    //
    // Private Members
    //

    private Editor editor

    //
    // Config
    //

    private static final KeyConfigEntry keyboardShortcutConfig =
            new KeyConfigEntry("editor.function.restyledocument.keyboard.shortcut", "Restyle document keyboard shortcut",
                    new KeyboardKey("Ctrl+R"), CONFIG_GROUP_KEYBOARD)

    private Closure keyboardShortcutConfigChanged = { ConfigEntry ce ->
        // Do nothing.
    }

    /**
     * Register configurations.
     *
     * @param configProvider The config provider to register with.
     */
    @Override
    public void registerConfigs(ConfigProvider configProvider) {
        configProvider.registerConfig(keyboardShortcutConfig, keyboardShortcutConfigChanged)
    }

    /**
     * Unregister configurations.
     *
     * @param configProvider The config provider to unregister with.
     */
    @Override
    public void unregisterConfigs(ConfigProvider configProvider) {
        configProvider.unregisterConfig(keyboardShortcutConfig, keyboardShortcutConfigChanged)
    }

    //
    // Methods
    //

    @Override
    public void setEditor(Editor editor) {
        this.editor = editor
    }

    @Override
    public String getGroup() {
        return null
    }

    @Override
    public String getName() {
        return "Paste"
    }

    @Override
    public JComponent getToolBarButton() {
        return null
    }

    /**
     * Returns the keyboard shortcut for the function.
     */
    @Override
    public KeyboardKey getKeyboardShortcut() {
        return keyboardShortcutConfig.getKeyboardKey()
    }

    @Override
    public void perform() throws FunctionException {
        this.editor.refreshStyling()
//        String text = this.editorPane.getEditorContent()
//        int caretDot = this.editorPane.getCaretDot()
//        this.editorPane.getGUI().getStyler().disable()
//        this.editorPane.setEditorContent(text)
//        this.editorPane.setCaretDot(caretDot)
//        this.editorPane.getGUI().getStyler().enable()
//        this.editorPane.getStyler().styleDocument()
    }

    /**
     * Cleanup and unregister any configs.
     */
    public void close() {}
}
