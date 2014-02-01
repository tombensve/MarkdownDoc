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
 *         2013-05-27: Created!
 *         
 */
package se.natusoft.doc.markdowndoc.editor.api;

import javax.swing.text.BadLocationException;

/**
 * This represents an editor line.
 */
public interface Line {

    /**
     * Returns the text of the line.
     */
    String getText() throws BadLocationException;

    /**
     * Sets the text of the line, replacing any previous text.
     *
     * @param text The text to set.
     */
    void setText(String text);

    /**
     * Returns the next line or null if this is the last line.
     */
    Line getNextLine();

    /**
     * Returns the previous line or null if this is the first line.
     */
    Line getPreviousLine();

    /**
     * Return true if the line is the first line.
     */
    boolean isFirstLine();

    /**
     * Returns true if this line is the last line.
     */
    boolean isLastLine();

    /**
     * Returns the position of the beginning of the line.
     */
    int getLineStartPost();

    /**
     * Returns the position of the end of the line.
     */
    int getLineEndPos();

    /**
     * Same as getText().
     */
    String toString();
}
