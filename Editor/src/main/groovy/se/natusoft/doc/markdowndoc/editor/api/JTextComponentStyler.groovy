/*
 *
 * PROJECT
 *     Name
 *         MarkdownDocEditor
 *     
 *     Code Version
 *         2.0.0
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
 *         2014-02-01: Created!
 *
 */
package se.natusoft.doc.markdowndoc.editor.api

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

import javax.swing.*
import javax.swing.text.StyledDocument

/**
 * General API for edit time styling a JTextComponent.
 */
@CompileStatic
@TypeChecked
interface JTextComponentStyler {

    /**
     * Creates a new styled and initialized document model.
     */
    @NotNull StyledDocument createDocumentModel()

    /**
     * Returns the currently styled component.
     */
    @NotNull JTextPane getStylee()

    /**
     * Enables styling (on by default)
     */
    void enable()

    /**
     * Disables styling (should be done while loading document!)
     */
    void disable()

    /**
     * Returns true if styling is enabled.
     */
    boolean isEnabled()

    /**
     * Styles the whole document.
     */
    void styleDocument()

    /**
     * Styles the current paragraph.
     */
    void styleCurrentParagraph()

    /**
     * Provides the bounds withing the document model for the paragraph to style.
     */
    static class ParagraphBounds {
        int start = 0
        int end = 0

        @NotNull String toString() {
            return "Bounds: " + start + ":" + end
        }
    }
}
