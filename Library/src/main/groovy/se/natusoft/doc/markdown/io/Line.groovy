/*
 *
 * PROJECT
 *     Name
 *         MarkdownDoc Library
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
package se.natusoft.doc.markdown.io

import groovy.transform.CompileStatic
import org.jetbrains.annotations.NotNull

/**
 * This represents a line of text.
 */
@CompileStatic
@SuppressWarnings("GroovyUnusedDeclaration")
class Line {
    //
    // Private Members
    //

    /** The original String passed to us. */
    protected String origLine = null

    /** The words of the line. */
    protected String[] words = new String[0]

    /** The current word index. */
    protected int currentWord = -1

    //
    // Properties
    //

    /** The line number of this line. */
    int lineNumber = -1

    //
    // Constructors
    //

    /**
     * Creates a new Line.
     *
     * @param line The content of the line.
     * @param lineNumber The line number of the line.
     */
    Line(@NotNull final String line) {
        this(line, 0)
    }

    /**
     * Creates a new Line.
     *
     * @param line The content of the line.
     * @param lineNumber The line number of the line.
     */
    Line(@NotNull final String line, final int lineNumber) {
        this.origLine = line
        this.words = line.split("\\s+")
        this.lineNumber = lineNumber
    }

    //
    // Methods
    //

    /**
     * Returns the text from the beginning up to the specified word number, not including.
     *
     * @param word The word # to go up to, but not include.
     *
     * @return The left side of the line that matches the criteria.
     */
    @NotNull String getTextUpToWord(int word) {
        StringBuilder sb = new StringBuilder()
        if (word > this.words.length) word = this.words.length
        String blank = ""
        (1..word).each { int i ->
            sb.append(blank)
            blank = " "
            sb.append(getWord(i - 1))
        }

        sb.toString()
    }

    /**
     * Returns the text from the specified word # to the end of the line.
     *
     * @param word The word # to start with.
     *
     * @return The right side of the line that matches the criteria.
     */
    @NotNull String getTextFromWord(int word) {
        StringBuilder sb = new StringBuilder()
        if (word > (this.words.length + 1)) return ""
        String blank = ""
        word += 1
        (word..(this.words.length)).each {int i ->
            sb.append(blank)
            blank = " "
            sb.append(getWord(i - 1))
        }

        sb.toString()
    }

    /**
     * Removes the specified text at the beginning of line. Any whitespace before is ignored (and also removed!).
     *
     * @param beg The string to remove.
     */
    @NotNull Line removeBeg(@NotNull final String beg) {
        Line line = this
        if (this.origLine.trim().startsWith(beg)) {
            final int ix = this.origLine.indexOf(beg);
            final String nwLine = this.origLine[(ix+1)..-1]
            line = newLine(nwLine)
        }

        line
    }

    /**
     * Removes any number directly followed by a dot ('.') from the beginning of the string.
     */
    @NotNull Line removeBegNumberDot() {
        Line line = this;
        if (this.origLine.trim() =~ /^[0-9]+\..*/) {
            final int ix = this.origLine.indexOf(".")
            line = newLine(this.origLine[(ix+1)..-1])
        }

        line
    }

    /**
     * Creates a new Line.
     *
     * @param text The text of the line.
     */
    protected @NotNull Line newLine(@NotNull final String text) {
        new Line(text, this.lineNumber)
    }

    /**
     * Removes all occurrences of the specified text.
     *
     * @param text The text to remove.
     *
     * @return A new Line instance.
     */
    @NotNull Line removeAll(@NotNull final String text) {
        newLine(this.origLine.replaceAll(text, ""))
    }

    /**
     * Replaces the specified regex with the specified text and return a new Line containing the result.
     *
     * @param regex what to replace.
     * @param with What to replace with.
     */
    @NotNull Line replaceAll(@NotNull final String regex, @NotNull final String with) {
        newLine(this.origLine.replaceAll(regex, with))
    }

    /**
     * Returns true if there are more words in this line.
     */
    boolean hasMoreWords() {
        (this.currentWord + 1) < this.words.size()
    }

    /**
     * Returns the next word or "" if already at last word.
     * <p/>
     * Please note that when a new Line have just been constructed then this method
     * will return the first word. It can thereby be used in the same way as an
     * Iterator:
     * <pre>
     *   while (line.hasMoreWords) {
     *       System.out.println(line.nextWord)
     *   }
     * </pre>
     * If you however do line.currentWord before this method then this will return the second word.
     */
    @SuppressWarnings("GroovyResultOfIncrementOrDecrementUsed")
    @NotNull String getNextWord() {
        String str = ""

        if (hasMoreWords()) {
            str = this.words[++this.currentWord]
        }

        str
    }

    /**
     * Returns the previous word or "" if already at first word.
     */
    @SuppressWarnings("GroovyResultOfIncrementOrDecrementUsed")
    @NotNull String getPrevWord() {
        String str = ""

        if (this.currentWord > 0) {
            str = this.words[--this.currentWord]
        }

        str
    }

    /**
     * Returns the current word in the line. If this is called directly after the Line has
     * been constructed it will return the first word.
     */
    @NotNull String getCurrentWord() {
        if (this.currentWord < 0) { this.currentWord = 0 }
        this.words[this.currentWord]
    }

    /**
     * Moves to the first word.
     * <p/>
     * _Please note that after this call a call to getNextWord() will return the second word
     * and is thus not the same state as when the Line instance have been constructed! To
     * achieve that call resetLine() instead.
     */
    @NotNull String getFirstWord() {
        this.currentWord = 0
        this.words[this.currentWord]
    }

    /**
     * Moves to the last word.
     */
    @NotNull String getLastWord() {
        this.currentWord = words.size() - 1
        this.words[this.currentWord]
    }

    /**
     * Reset the word position state to the same as after construction.
     */
    void resetLine() {
        this.currentWord = -1
    }

    /**
     * Returns the number of words in the line.
     */
    int getNumberOfWords() {
        this.words.size()
    }

    /**
     * Returns the word at the specified position.
     *
     * @param word The position of the word to get.
     */
    @NotNull String getWord(final int word) {
        this.words[word];
    }

    /**
     * Returns the poition of the current word.
     */
    int getCurrentWordPosition() {
        this.currentWord
    }

    /**
     * Sets the current word position.
     *
     * @param position The position to set.
     */
    void setCurrentWordPosition(final int position) {
        this.currentWord = position
    }

    /**
     * Returns the number of leading spaces.
     */
    int getLeadingSpaces() {
        int leading = 0
        int lpos = 0

        if (this.origLine.length() > 0) {
            while (this.origLine[lpos] == " " || this.origLine[lpos] == "\t") {
                if (this.origLine[lpos] == "\t") {
                    leading += 4
                }
                else {
                    ++leading
                }
                ++lpos
                if (lpos == this.origLine.size()) break
            }
        }

        leading
    }

    /**
     * Removes any leading spaces.
     */
    @NotNull Line removeLeadingSpaces() {
        Line nl = this

        final int ls = getLeadingSpaces()
        if (ls > 0) {
            nl = newLine(this.origLine.substring(ls))
        }

        nl
    }

    /**
     * Calls a closure with each word in the line.
     *
     * @param wordClosure The closure to call.
     */
    void eachWord(@NotNull final Closure wordClosure) {
        this.words.each wordClosure
    }

    /**
     * Returns true if this line starts with the specified text.
     *
     * @param startsWith The text to check for.
     */
    boolean startsWith(@NotNull final String startsWith) {
        this.origLine.startsWith(startsWith)
    }

    /**
     * Returns true if this line starts with the specified text ignoring initial whitespace.
     *
     * @param startsWith The text to check for.
     */
    boolean startsWithExcludingWhitespace(@NotNull final String startsWith) {
        this.origLine.trim().startsWith(startsWith)
    }

    /**
     * Returns true if this line ends with the specified text.
     *
     * @param endsWith The text to check for.
     */
    boolean endsWith(@NotNull final String endsWith) {
        this.origLine.endsWith(endsWith)
    }

    /**
     * Returns true if this line ends with the specified text ignoring whitespace.
     *
     * @param endsWith The text to check for.
     */
    boolean endsWithExcludingWhitespace(@NotNull final String endsWith) {
        this.origLine.trim().endsWith(endsWith)
    }

    /**
     * Returns true if the line contains the specified text.
     *
     * @param contains The text to check for.
     */
    boolean contains(@NotNull final String contains) {
        this.origLine.indexOf(contains) >= 0
    }

    /**
     * Returns true if this line matches the specified regular expression.
     *
     * @param regexp The regexp to match.
     */
    boolean matches(@NotNull final String regexp) {
        this.origLine.matches(regexp)
    }

    /**
     * @return true if this line is empty.
     */
    boolean isEmpty() {
        this.origLine.trim().length() == 0
    }

    /**
     * Returns a new Line having everything but the first word.
     */
    @NotNull Line removeFirstWord() {
        String space = ""
        boolean first = true
        String text = ""
        eachWord { final String word ->
            if (!first) {
                text += space + word
                space = " "
            }
            if (!word.isEmpty()) first = false
        }

        newLine(text)
    }

    /**
     * Returns the specified character.
     *
     * @param index The index of the char to get.
     */
    char charAt(final int index) {
        this.origLine.charAt(index)
    }

    /**
     * Returns this Line as a String (as originally read from file!)
     */
    @Override
    @NotNull String toString() {
        this.origLine
    }
}
