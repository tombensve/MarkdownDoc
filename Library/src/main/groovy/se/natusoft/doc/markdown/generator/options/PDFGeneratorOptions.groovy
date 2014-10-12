/* 
 * 
 * PROJECT
 *     Name
 *         MarkdownDoc Library
 *     
 *     Code Version
 *         1.3.5
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
 *         2012-11-16: Created!
 *         
 */
package se.natusoft.doc.markdown.generator.options

import groovy.transform.CompileStatic
import se.natusoft.doc.markdown.api.Options
import se.natusoft.tools.optionsmgr.annotations.*

/**
 * Options for the PDFGenerator.
 */
@CompileStatic
@OptionsModel
class PDFGeneratorOptions implements Options {

    @Option
    @Name("resultFile")
    @Description("Where to write the result.")
    @Required
    String resultFile

    @Option
    @Name("rootDir")
    @Description("A root dir to make image paths relative to.")
    @Optional
    String rootDir = null

    @Option
    @Name("pageSize")
    @Description("The pagesize name like LETTER or A4.")
    @Optional
    String pageSize = "A4"

    @Option
    @Name("title")
    @Description("The title of the document")
    @Optional
    String title = null

    @Option
    @Name("subject")
    @Description("The subject of the document.")
    @Optional
    String subject = null

    @Option
    @Name("keywords")
    @Description("Meta keywords")
    @Optional
    String keywords = null

    @Option
    @Name("author")
    @Description("The author of the document.")
    @Optional
    String author = null

    @Option
    @Name("version")
    @Description("The version to put on the title page. Must be specified to be rendered!")
    @Optional
    String version

    @Option
    @Name("copyright")
    @Description("The copyright message to put on the title page. Must be specified to be rendered!")
    @Optional
    String copyright

    @Option
    @Name("hideLinks")
    @Description("If true then links are not rendered as link the link text will be rendered as plain text.")
    @Optional
    boolean hideLinks = false

    @Option
    @Name("unorderedListItemPrefix")
    @Description("What item marking to use for unuredered lists. Default is '- '.")
    @Optional
    String unorderedListItemPrefix = "• "

    @Option
    @Name("firstLineParagraphIndent")
    @Description("If true then the first line of each paragraph is indented. Default is false.")
    @Optional
    boolean firstLineParagraphIndent = false

    @Option
    @Name("backgroundColor")
    @Description("The background color of the document in \"R:G:B\" format where each R, G, and B are a number 0 - 255.")
    @Optional
    String backgroundColor = null

    @Option
    @Name("blockQuoteColor")
    @Description("The blockquote color to use in this document in \"R:G:B\" format where each R, G, and B are a number 0 - 255.")
    @Optional
    String blockQuoteColor = null

    @Option
    @Name("codeColor")
    @Description("The code color to use in this document in \"R:G:B\" format where each R, G, and B are a number 0 - 255.")
    @Optional
    String codeColor = null

    @Option
    @Name("generateTOC")
    @Description("This generates a table of contents. Default is false!")
    @Optional
    boolean generateTOC = false

    @Option
    @Name("generateTitlePage")
    @Description("This will generate one first page with a title, version, author, and copyright. Default is false.")
    @Optional
    boolean generateTitlePage = false

    @Option
    @Name("help")
    @Description("Shows help.")
    @Optional
    @Flag
    boolean help;

}
