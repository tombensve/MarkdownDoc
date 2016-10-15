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
 *         2013-02-02: Created!
 *
 */
package se.natusoft.doc.markdown.parser

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.jetbrains.annotations.NotNull
import se.natusoft.doc.markdown.api.Parser

/**
 * Provides a parser depending on file extension.
 */
@CompileStatic
@TypeChecked
class ParserProvider {

    private static final ServiceLoader parserLoader = ServiceLoader.load(Parser.class)

    /**
     * Returns a valid parser for the file or null if none match.
     *
     * @param ext The file to get a parser for.
     *
     * @return A parser or null.
     */
    static Parser getParserForFile(@NotNull final File file) {
        getParserForFile(file.getName())
    }

    /**
     * Returns a valid parser for the file or null if none match.
     *
     * @param ext The file to get a parser for.
     *
     * @return A parser or null.
     */
    static Parser getParserForFile(@NotNull final String file) {
        parserLoader.find { final Parser parser -> parser.validFileExtension(file) } as Parser
    }

}
