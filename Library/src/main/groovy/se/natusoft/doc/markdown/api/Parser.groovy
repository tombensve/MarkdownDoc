/*
 *
 * PROJECT
 *     Name
 *         MarkdownDoc Library
 *     
 *     Code Version
 *         2.0.0
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
package se.natusoft.doc.markdown.api

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import se.natusoft.doc.markdown.exception.ParseException
import se.natusoft.doc.markdown.model.Doc

/**
 * This defines the API of a parser.
 */
@CompileStatic
@TypeChecked
interface Parser {

    /**
     * Parses a file and adds its document structure to the passed Doc.
     *
     * @param document The parsed result is added to this.
     * @param parseFile The file whose content to parse.
     * @param parserOptions options to pass on to the parser.
     *
     * @throws IOException on failure.
     * @throws ParseException on parse failures.
     */
    void parse(@NotNull Doc document, @NotNull final File parseFile,
               @Nullable final Properties parserOptions) throws IOException, ParseException

    /**
     * Parses a file and adds its document structure to the passed Doc.
     *
     * @param document The parsed result is added to this.
     * @param parseStream The stream whose content to parse.
     * @param parserOptions options to pass on to the parser.
     *
     * @throws IOException on failure.
     * @throws ParseException on parse failures.
     */
    void parse(@NotNull Doc document, @NotNull InputStream parseStream,
               @Nullable final Properties parserOptions) throws IOException, ParseException

    /**
     * Returns true if extension of the passed fileName is valid for this parser.
     *
     * @param fileName The fileName whose extension to test.
     *
     * @return true or false.
     */
    boolean validFileExtension(@NotNull String fileName)

}
