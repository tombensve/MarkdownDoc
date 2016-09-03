/*
 *
 * PROJECT
 *     Name
 *         MarkdownDoc Library
 *
 *     Code Version
 *         1.5.0
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
import groovy.transform.ToString
import groovy.transform.TypeChecked
import se.natusoft.doc.markdown.api.Options
import se.natusoft.doc.markdown.generator.pdfbox.PDFBoxDocRenderer
import se.natusoft.tools.optionsmgr.annotations.*

/**
 * Options for the PDFGenerator.
 */
@CompileStatic
@TypeChecked
@OptionsModel
@ToString(includeNames = true)
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
    @Name("titlePageImage")
    @Description("Put an image on the title page. Format: <path/URL>:x:y")
    @Optional
    String titlePageImage

//    @Option
//    @Name("keywords")
//    @Description("Meta keywords")
//    @Optional
//    String keywords = null

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
    @Name("authorLabel")
    @Description("The label text for 'Author:'. ")
    @Optional
    String authorLabel = "Author:"

    @Option
    @Name("versionLabel")
    @Description("The label text for 'Version:'.")
    @Optional
    String versionLabel = "Version:"

    @Option
    @Name("pageLabel")
    @Description("The label text for 'Page'.")
    @Optional
    String pageLabel = "Page"

    @Option
    @Name("tableOfContentsLabel")
    @Description("The text for 'Table of Contents'.")
    @Optional
    String tableOfContentsLabel = "Table of Contents"

    @Option
    @Name("hideLinks")
    @Description("If true then links are not rendered as link the link text will be rendered as plain text.")
    @Optional
    boolean hideLinks = false

    @Option
    @Name("unorderedListItemPrefix")
    @Description("What item marking to use for unordered lists. Default is '- '.")
    @Optional
    String unorderedListItemPrefix = "• "

//    @Option
//    @Name("firstLineParagraphIndent")
//    @Description("If true then the first line of each paragraph is indented. Default is false.")
//    @Optional
//    boolean firstLineParagraphIndent = false

    @Option
    @Name("mss")
    @Description("This specifies the path to an .mss file to use for setting fonts and colors and image styling of the generated document.")
    @Optional
    String mss

    @Option
    @Name("generateSectionNumbers")
    @Description("If true all chapters and sections will be numbered. This was the only option before version 1.4.")
    @Optional
    boolean generateSectionNumbers = true

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
