/* 
 * 
 * PROJECT
 *     Name
 *         MarkdownDoc Library
 *     
 *     Code Version
 *         1.2.2
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

import se.natusoft.doc.markdown.io.LineReader
import se.natusoft.doc.markdown.io.Line
import se.natusoft.doc.markdown.parser.markdown.io.MDLine

/**
 * Extends LineReader to produce MDLine objects instead of Line objects.
 */
public class MDLineReader extends LineReader {

    //
    // Constructors
    //

    /**
     * Creates a new MDLineReader.
     *
     * @param reader The reader to read from.
     */
    public MDLineReader(Reader reader) {
        super(reader)
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
        return new MDLine(text, super.lineNo)
    }
}

