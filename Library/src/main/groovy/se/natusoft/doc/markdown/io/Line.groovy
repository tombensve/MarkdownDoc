/* 
 * 
 * PROJECT
 *     Name
 *         MarkdownDoc Library
 *     
 *     Code Version
 *         1.0
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

/**
 * This represents a line of text.
 */
class Line {
    //
    // Private Members
    //

    /** The original String passed to us. */
    protected String origLine = null

    /** The words of the line. */
    protected def words = []

    /** The current word index. */
    protected int currentWord = -1

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
    public Line(String line, int lineNumber) {
        this.origLine = line
        StringTokenizer st = new StringTokenizer(line, " ")
        while (st.hasMoreTokens()) {
            words << st.nextToken()
        }
        this.lineNumber = lineNumber
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
        new Line(text, this.lineNumber)
    }

    /**
     * Removes all occurrences of the specified text.
     *
     * @param text The text to remove.
     *
     * @return A new Line instance.
     */
    public Line removeAll(String text) {
        newLine(this.origLine.replaceAll(text, ""))
    }

    /**
     * Replaces the specified regex with the specified text and return a new Line containing the result.
     *
     * @param regex what to replace.
     * @param with What to replace with.
     */
    public Line replaceAll(String regex, String with) {
        newLine(this.origLine.replaceAll(regex, with))
    }

    /**
     * Returns true if there are more words in this line.
     */
    public boolean hasMoreWords() {
        return (this.currentWord + 1) < this.words.size()
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
    public String getNextWord() {
        String str = ""

        if (hasMoreWords()) {
            str = this.words[++this.currentWord]
        }

        return str
    }

    /**
     * Returns the previous word or "" if already at first word.
     */
    public String getPrevWord() {
        String str = ""

        if (this.currentWord > 0) {
            str = this.words[--this.currentWord]
        }

        return str
    }

    /**
     * Returns the current word in the line. If this is called directly after the Line has
     * been constructed it will return the first word.
     */
    public String getCurrentWord() {
        if (this.currentWord < 0) this.currentWord = 0
        return this.words[this.currentWord]
    }

    /**
     * Moves to the first word.
     * <p/>
     * Please note that after this call a call to getNextWord() will return the second word
     * and is thus not the same state as when the Line instance have been constructed! To
     * achieve that call resetLine() instead.
     */
    public String getFirstWord() {
        this.currentWord = 0
        return this.words[this.currentWord]
    }

    /**
     * Moves to the last word.
     */
    public String getLastWord() {
        this.currentWord = words.size() - 1
        return this.words[this.currentWord]
    }

    /**
     * Reset the word position state to the same as after construction.
     */
    public void resetLine() {
        this.currentWord = -1
    }

    /**
     * Returns the number of words in the line.
     */
    public int getNumberOfWords() {
        return this.words.size()
    }

    /**
     * Returns the word at the specified position.
     *
     * @param word The position of the word to get.
     */
    public String getWord(int word) {
        return this.words[word];
    }

    /**
     * Returns the poition of the current word.
     */
    public int getCurrentWordPosition() {
        return this.currentWord
    }

    /**
     * Sets the current word position.
     *
     * @param position The position to set.
     */
    public void setCurrentWordPosition(int position) {
        this.currentWord = position
    }

    /**
     * Returns the number of leading spaces.
     */
    public int getLeadingSpaces() {
        int leading = 0

        while (this.origLine[leading] == " ") {
            ++leading
            if (leading == this.origLine.size()) break
        }

        return leading
    }

    /**
     * Removes any leading spaces.
     */
    public Line removeLeadingSpaces() {
        Line nl = this

        int ls = getLeadingSpaces()
        if (ls > 0) {
            nl = newLine(this.origLine.substring(ls))
        }

        return nl
    }

    /**
     * Calls a closure with each word in the line.
     *
     * @param wordClosure The closure to call.
     */
    public void eachWord(Closure wordClosure) {
        this.words.each wordClosure
    }

    /**
     * Returns true if this line starts with the specified text.
     *
     * @param startsWith The text to check for.
     */
    public boolean startsWith(String startsWith) {
        return this.origLine.startsWith(startsWith)
    }

    /**
     * Returns true if this line ends with the specified text.
     *
     * @param endsWith The text to check for.
     */
    public boolean endsWith(String endsWith) {
        return this.origLine.endsWith(endsWith)
    }

    /**
     * Returns true if the line contains the specified text.
     *
     * @param contains The text to check for.
     */
    public boolean contains(String contains) {
        return this.origLine.indexOf(contains) >= 0
    }

    /**
     * @return true if this line is empty.
     */
    public boolean isEmpty() {
        return this.origLine.trim().length() == 0
    }

    /**
     * Returns a new Line having everything but the first word.
     */
    public Line removeFirstWord() {
        String space = ""
        boolean first = true
        String text = ""
        eachWord { word ->
            if (!first) {
                text += space + word
                space = " "
            }
            first = false
        }

        return newLine(text)
    }

    /**
     * Returns the specified character.
     *
     * @param index The index of the char to get.
     */
    public char charAt(int index) {
        return this.origLine.charAt(index)
    }

    /**
     * Returns this Line as a String (as originally read from file!)
     */
    @Override
    public String toString() {
        return this.origLine
    }
}
