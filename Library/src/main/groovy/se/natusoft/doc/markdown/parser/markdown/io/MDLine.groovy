/*
 *
 * PROJECT
 *     Name
 *         MarkdownDoc Library
 *
 *     Code Version
 *         1.4
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

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.jetbrains.annotations.NotNull
import se.natusoft.doc.markdown.io.Line

/**
 * This represents a line of Markdown text.
 */
@CompileStatic
@TypeChecked
class MDLine extends Line  {

    //
    // Constructors
    //

    /**
     * Creates a new Line.
     *
     * @param line The content of the line.
     */
    MDLine(@NotNull String line, int lineNumber) {
        super(line, lineNumber)
    }

    /**
     * Creates a new MDLine from a Line.
     *
     * @param line The original Line to turn into an MDLine.
     */
    MDLine(@NotNull Line line) {
        super(line.toString(), line.lineNumber)
    }

    //
    // Methods
    //

    /**
     * Creates a new Line.
     *
     * @param text The text of the line.
     */
    protected Line newLine(@NotNull String text) {
        new MDLine(text, super.lineNumber)
    }

    /**
     * Returns true if this line starts with the specified text. This also accepts one or two spaces
     * before the matching text.
     *
     * @param startsWith The text to check for.
     */
    boolean startsWith(@NotNull String startsWith) {
        super.startsWith(startsWith) || this.origLine.startsWith(" " + startsWith) ||
                this.origLine.startsWith("  " + startsWith)
    }

    /**
     * Returns true if this is part of a code block.
     */
    boolean isCodeBlock() {
        super.origLine.length() > 0 && (
            super.origLine.charAt(0) == ('\t' as char) ||
                    (super.origLine.length() >= 4 && super.origLine.substring(0,4).isAllWhitespace())
        )
    }

    /**
     * Returns true if this is part of a block quote.
     */
    boolean isBlockQuote() {
        super.words.size() >= 1 && super.words[0] == ">"
    }

    /**
     * Returns true if this starts a list item.
     */
    boolean isList() {
        boolean list = false;

        if (super.origLine != null && super.origLine.trim().length() >= 1) {
            StringTokenizer lineTokenizer = new StringTokenizer(super.origLine, " ")
            String firstWord = lineTokenizer.nextToken()

            if (firstWord.endsWith(".")) {
                firstWord = firstWord.substring(0, firstWord.length() - 1)
                boolean onlyDigits = true;
                for (int i = 0; i < firstWord.length(); i++) {
                    if (!firstWord.charAt(i).isDigit()) {
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
                try { n = this.origLine.trim().charAt(1)} catch (IndexOutOfBoundsException ignore) {}
                list = (c == '*' as char || c == '+' as char || c == '-' as char) &&
                        (n != '*' as char && n != '+' as char && n != '-' as char && !n.isDigit())
            }
        }

        list
    }

    /**
     * Returns true if this starts an ordered list item.
     */
    boolean isOrderedList() {
        if (super.origLine != null && super.origLine.trim().length() >= 1) {
            char c = this.origLine.trim().charAt(0)
            return c.isDigit()
        }
        false
    }

    /**
     * Returns true if this line represents a header.
     */
    boolean isHeader() {
        super.origLine.startsWith("#")
    }

    /**
     * Returns true if this line represents a horizontal ruler.
     */
    boolean isHorizRuler() {
        this.origLine.startsWith("* * *") || this.origLine.startsWith("***") || this.origLine.startsWith("- - -") ||
                this.origLine.startsWith("---")
    }

    /**
     * Returns true if this line represents a link url specification.
     *
     * @param urls The current known urls.
     */
    boolean isLinkURLSpec(@NotNull Map urls) {
        boolean found = false;

        for (String urlText : urls.keySet()) {
            if (this.origLine.trim().startsWith("[" + urlText + "]:")) {
                found = true
                break
            }
        }

        found
    }

    /**
     * Returns true if this line represents a comment start.
     */
    boolean isCommentStart() {
        // Accept up to 3 spaces in front of comment to distinguish it from a code line. This is also
        // the reason for not doing: this.origLine.trim().startsWith("<!--").
        this.origLine.startsWith("<!--") || this.origLine.startsWith(" <!--") || this.origLine.startsWith("  <!--") ||
                this.origLine.startsWith("   <!--")
    }

    /**
     * Returns true if this line represents a comment end.
     */
    boolean isCommentEnd() {
        this.origLine.trim().endsWith("-->")
    }

    // Both of the following are a bit messy. There is no distinct identifier for a paragraph.

    /**
     * returns true if this line is considered part of the current paragraph.
     *
     * @param urls same as passed to isLinkURLSpec().
     *
     * @return true if all other return false.
     */
    boolean isPartOfParagraph(@NotNull Map urls) {
        // Note the distinction: A List contains one or more Paragraph:s. A BlockQuote *is* a Paragraph!
        !(isList() || isOrderedList() || isHeader() || isHorizRuler() || isLinkURLSpec(urls) ||
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
    boolean isPartOfListParagraph(Map urls) {
        // Again, note: isList() does not mean a list paragraph, but a new list (or list item). Each list item contains
        // a paragraph. It is such a paragraph we are trying to identify here (by the exclusion of other).
        !(isBlockQuote() || isList() || isOrderedList() || isHeader() || isHorizRuler() || isLinkURLSpec(urls) ||
                isCommentStart() || isCommentEnd())
    }

    /**
     * @return true if this line is a <div name="..."> or <!-- @Div(class) --> line.
     */
    boolean isStartDiv() {
        // We allow 0 to 3 spaces in front, 4 would make it a code line, and so would a tab! The div also has
        // to be the only thing on the line!
        matches('[ ]?[ ]?[ ]?<div.*class=".*".*>\\s*') || matches('[ ]?[ ]?[ ]?<!--[ ]*@Div\\(".*"\\)[ ]*-->\\s*')
        // Note that when the class name is extracted it is the text between the first and the second " character
        // that is used, and exactly 2 " characters are expected! Therefore the " character is important in the
        // comment version also. The reason for the comment version is only to support making the markdown file
        // readable also on github who does not like to find <div class="...">...</div> in markdown files. It will
        // not markdown format anything between the div tags. So the comment version allows for a readable markdown
        // file on github, but still give extra features to an HTML or PDF version.

        // I wondered why the heck the groovy guys decided to make both "..." and '...' into strings, but
        // I'm however starting to see the point :-). The question is: which is worse: \" or 'x' as char ?
    }

    /**
     * @return true if this line is a </div> or a <!-- @EndDiv -->.
     */
    boolean isEndDiv() {
        startsWithExcludingWhitespace("</div>") || matches("[ ]?[ ]?[ ]?<!--[ ]*@EndDiv[ ]*-->\\s*")
    }
}
