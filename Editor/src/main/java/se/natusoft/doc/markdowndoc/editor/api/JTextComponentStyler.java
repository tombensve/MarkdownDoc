/* 
 * 
 * PROJECT
 *     Name
 *         MarkdownDocEditor
 *     
 *     Code Version
 *         1.3
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
package se.natusoft.doc.markdowndoc.editor.api;

import javax.swing.*;

/**
 * General API for edit time styling a JTextComponent.
 */
public interface JTextComponentStyler {
    /**
     * Initializes the Styler with a component to style.
     *
     * @param textComponentToStyle The component to style.
     */
    void init(JTextPane textComponentToStyle);

    /**
     * Enables styling (on by default)
     */
    void enable();

    /**
     * Disables styling (should be done while loading document!)
     */
    void disable();

    /**
     * Styles the whole document.
     */
    void styleDocument();

    /**
     * Styles the current paragraph.
     */
    void styleCurrentParagraph();

    /**
     * Provides the bounds withing the document model for the paragraph to style.
     */
    public static class ParagraphBounds {
        public int start = 0;
        public int end = 0;

        public String toString() {
            return "Bounds: " + start + ":" + end;
        }
    }
}
