/* 
 * 
 * PROJECT
 *     Name
 *         MarkdownDoc Library
 *     
 *     Code Version
 *         1.2.5
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
 * A utility for reading text files, line by line and allows for look ahead and push back.
 */
public class LineReader {
    //
    // Private Members
    //

    /** We wrap the passed reader in this. */
    private BufferedReader reader = null

    /** For read ahead data. */
    private Deque<Line> readAhead = new LinkedList<Line>()

    /** The current line number. */
    private int lineNo = 1

    //
    // Constructors
    //

    /**
     * Creates a new LineReader.
     *
     * @param reader The reader to wrap.
     */
    public LineReader(Reader reader) {
        this.reader = new BufferedReader(reader)
    }

    //
    // Methods
    //

    /**
     * Creates a new Line of text.
     *
     * @param text The text to wrap in a Line.
     */
    protected Line createLine(String text) {
        return new Line(text, this.lineNo)
    }

    /**
     * Reads the next line.
     *
     * @return The line or null on end-of-file.
     *
     * @throws IOException on any I/O failure.
     */
    public Line readLine() throws IOException {
        Line line = null

        if (!this.readAhead.isEmpty()) {
            line = this.readAhead.pollFirst()
        }
        else {
            String str = this.reader.readLine()
            if (str != null) {
                line = createLine(str.replaceAll("\t", "    "))
            }
        }

        ++this.lineNo

        return line
    }

    /**
     * Pushed the specified line back to be returned again on next readLine().
     *
     * @param line The line to push back.
     */
    public void pushBackLine(Line line) {
        this.readAhead.offerLast(line)
        --this.lineNo
    }

    /**
     * Takes a peek at the next line without "reading" it. The next call to readLine() will still return it.
     *
     * @return The next line or null if end-of-file.
     *
     * @throws IOException on any I/O failure.
     */
    public Line peekNextLine() throws IOException {
        Line line = readLine()
        if (line != null) {
            pushBackLine(line)
        }

        return line
    }

    /**
     * Returns true if this reader has any line to read. When this returns false end-of-file has been reached.
     */
    public boolean hasLine() {
        return peekNextLine() != null
    }

    // Wanted to test this, and it works fine, but is not optimal for parsing ...
    /**
     * Calls closure for each line providing current line and next line.
     *
     * @param lineClosure The closure to call.
     */
    public void each(Closure lineClosure) {
        while (hasLine()) {
            lineClosure.call(readLine(), peekNextLine())
        }
    }

    /**
     * Returns the line number of the current line starting count at 0.
     */
    public int getLineNo() {
        return this.lineNo
    }

    /**
     * Closes the reader.
     *
     * @throws IOException on any I/O failure.
     */
    public void close() throws IOException {
        this.readAhead.clear()
        this.reader.close();
    }
}
