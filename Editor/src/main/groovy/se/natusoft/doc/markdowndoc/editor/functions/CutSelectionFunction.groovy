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
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2013-05-27: Created!
 *
 */
package se.natusoft.doc.markdowndoc.editor.functions

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.doc.markdowndoc.editor.api.Editor
import se.natusoft.doc.markdowndoc.editor.api.EditorFunction
import se.natusoft.doc.markdowndoc.editor.config.KeyboardKey
import se.natusoft.doc.markdowndoc.editor.exceptions.FunctionException

import javax.swing.*

/**
 * This provides a function that cuts the currently selected text.
 */
@CompileStatic
@TypeChecked
class CutSelectionFunction implements EditorFunction {
    //
    // Properties
    //

    Editor editor

    //
    // Methods
    //

    @Override
    String getGroup() {
        null
    }

    @Override
    String getName() {
        "Cut"
    }

    @Override
    JComponent getToolBarButton() {
        null
    }

    @Override
    KeyboardKey getKeyboardShortcut() {
        new KeyboardKey("Meta+X")
    }

    @Override
    void perform() throws FunctionException {
        this.editor.cut()
    }

    /**
     * Cleanup and unregister any configs.
     */
    void close() {}
}
