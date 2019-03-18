/* 
 * 
 * PROJECT
 *     Name
 *         MarkdownDoc Library
 *     
 *     Code Version
 *         2.1.1
 *     
 *     Description
 *         Parses markdown and generates HTML and PDF.
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
 *         2016-10-15: Created!
 *         
 */
package se.natusoft.doc.markdown.util

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

/**
 * This represents a set of text.
 */
@CompileStatic
@TypeChecked
class Text {
    //
    // Private Members
    //

    /** The words of this text. */
    private List<Word> words = []

    //
    // Properties
    //

    /** The string content of this text. */
    String content
    void setContent(String content) {
        this.content = content
        parseContent()
    }

    //
    // Methods
    //

    private void parseContent() {
        char[] chars = this.content.toCharArray()
        int current = 0

        // Make any leading whitespace a word
        int start = current
        current = skipWhiteSpace(chars, current)
        if (current > start) {
            this.words << new Word(startPos: start, endPos: current, text: this)
        }

        while (current < chars.length) {
            start = current
            current = findNextWhiteSpace(chars, current)
            current = skipWhiteSpace(chars, current)
            this.words << new Word(startPos: start, endPos: current, text: this)
        }

    }

    private static int skipWhiteSpace(char[] chars, int pos) {
        while (pos < chars.length && (chars[pos] == ' ' as char || chars[pos] == '\t' as char)) {
            ++pos
        }
        pos
    }

    private static int findNextWhiteSpace(char[] chars, int pos) {
        while (pos < chars.length && (chars[pos] != ' ' as char && chars[pos] != '\t' as char)) {
            ++pos
        }
        pos
    }

    List<Word> getWords() {
        List<Word> _words = new LinkedList<>()
        _words.addAll(this.words)
        _words
    }
}

/**
 * Represents a word within a Text.
 */
@CompileStatic
@TypeChecked
class Word {

    //
    // Properties
    //

    /** The start position of the word in the text. */
    int startPos = 0

    /** The end position of the word in the text. */
    int endPos = 0

    /** The text the word belongs to. */
    Text text

    //
    // Methods
    //

    String toString() {
        toString(false)
    }

    String toString(boolean asIs) {
        if (asIs) {
            text.content.substring(this.startPos, this.endPos)
        }
        else {
            text.content.substring(this.startPos, this.endPos).trim() + (text.content.charAt(this.endPos - 1) == ' ' as char ? ' ' : '')
        }
    }
}
