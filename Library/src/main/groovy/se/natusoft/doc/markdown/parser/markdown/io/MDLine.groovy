/* 
 * 
 * PROJECT
 *     Name
 *         MarkdownDoc Library
 *     
 *     Code Version
 *         1.2.10
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
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2012-11-04: Created!
 *         
 */
package se.natusoft.doc.markdown.parser.markdown.io

import se.natusoft.doc.markdown.io.Line

/**
 * This represents a line of Markdown text.
 */
class MDLine extends Line  {

    //
    // Constructors
    //

    /**
     * Creates a new Line.
     *
     * @param line The content of the line.
     */
    public MDLine(String line, int lineNumber) {
        super(line, lineNumber)
    }

    //
    // Methods
    //

    /**
     * Creates a new Line.
     *
     * @param text The text of the line.
     */
    protected Line newLine(String text) {
        return new MDLine(text, super.lineNumber)
    }

    /**
     * Returns true if this line starts with the specified text. This also accepts one or two spaces
     * before the matching text.
     *
     * @param startsWith The text to check for.
     */
    public boolean startsWith(String startsWith) {
        return super.startsWith(startsWith) || this.origLine.startsWith(" " + startsWith) ||
                this.origLine.startsWith("  " + startsWith)
    }

    /**
     * Returns true if this is part of a code block.
     */
    public boolean isCodeBlock() {
        return super.origLine.length() > 0 && (
            super.origLine.charAt(0) == '\t' || (super.origLine.length() >= 4 && super.origLine.substring(0,4).isAllWhitespace())
        )
    }

    /**
     * Returns true if this is part of a block quote.
     */
    public boolean isBlockQuote() {
        return super.words.size() >= 1 && super.words[0] == ">"
    }

    /**
     * Returns true if this starts a list item.
     */
    public boolean isList() {
        boolean list = false;

        if (super.origLine != null && super.origLine.trim().length() >= 1) {
            StringTokenizer lineTokenizer = new StringTokenizer(super.origLine, " ")
            String firstWord = lineTokenizer.nextToken()

            if (firstWord.endsWith(".")) {
                firstWord = firstWord.substring(0, firstWord.length() - 1)
                boolean onlyDigits = true;
                for (int i = 0; i < firstWord.length(); i++) {
                    if (!firstWord.charAt(i).digit) {
                        onlyDigits = false;
                        break;
                    }
                }

                if (onlyDigits) {
                    list = true;
                }
            }
            else {
                char c = this.origLine.trim().charAt(0)
                char n = 0
                try { n = this.origLine.trim().charAt(1)} catch (IndexOutOfBoundsException iobe) {}
                list = (c == '*' || c == '+' || c == '-') && (n != '*' && n != '+' && n != '-' && !n.isDigit())
            }
        }
        return list
    }

    /**
     * Returns true if this starts an ordered list item.
     */
    public boolean isOrderedList() {
        if (super.origLine != null && super.origLine.trim().length() >= 1) {
            char c = this.origLine.trim().charAt(0)
            return c.isDigit()
        }
        return false
    }

    /**
     * Returns true if this line represents a header.
     */
    public boolean isHeader() {
        return super.origLine.startsWith("#")
    }

    /**
     * Returns true if this line represents a horizontal ruler.
     */
    public boolean isHorizRuler() {
        return this.origLine.startsWith("* * *") || this.origLine.startsWith("***") || this.origLine.startsWith("- - -") ||
                this.origLine.startsWith("---")
    }

    /**
     * Returns true if this line represents a link url specification.
     *
     * @param urls The current known urls.
     */
    public boolean isLinkURLSpec(Map urls) {
        boolean found = false;

        for (String urlText : urls.keySet()) {
            if (this.origLine.trim().startsWith("[" + urlText + "]:")) {
                found = true
                break
            }
        }

        return found
    }

    /**
     * Returns true if this line represents a comment start.
     */
    public boolean isCommentStart() {
        return this.origLine.startsWith("<!--") || this.origLine.startsWith(" <!--") || this.origLine.startsWith("  <!--")
    }

    /**
     * Returns true if this line represents a comment end.
     */
    public boolean isCommentEnd() {
        return this.origLine.trim().endsWith("-->")
    }

    // Both of the following are a bit messy. There is no distinct identifier for a paragraph.

    /**
     * returns true if this line is considered part of the current paragraph.
     *
     * @param urls same as passed to isLinkURLSpec().
     *
     * @return true if all other return false.
     */
    public boolean isPartOfParagraph(Map urls) {
        // Note the distinction: A List contains one or more Paragraph:s. A BlockQuote *is* a Paragraph!
        return !(isList() || isOrderedList() || isHeader() || isHorizRuler() || isLinkURLSpec(urls) ||
                isCommentStart() || isCommentEnd()) || isBlockQuote()
    }

    /**
     * returns true if this line is considered part of the current paragraph (does not check for code block since indented list levels
     * collide with code blocks, but includes block quote in the not list).
     *
     * @param urls same as passed to isLinkURLSpec().
     *
     * @return true if all other return false.
     */
    public boolean isPartOfListParagraph(Map urls) {
        // Again, note: isList() does not mean a list paragraph, but a new list (or list item). Each list item contains
        // a paragraph. It is such a paragraph we are trying to identify here (by the exclusion of other).
        return !(isBlockQuote() || isList() || isOrderedList() || isHeader() || isHorizRuler() || isLinkURLSpec(urls) ||
                isCommentStart() || isCommentEnd())
    }
}
