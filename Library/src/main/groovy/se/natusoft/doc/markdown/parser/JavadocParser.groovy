/* 
 * 
 * PROJECT
 *     Name
 *         MarkdownDoc Library
 *     
 *     Code Version
 *         1.1.2
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

import se.natusoft.doc.markdown.api.Parser
import se.natusoft.doc.markdown.exception.ParseException
import se.natusoft.doc.markdown.model.*

import java.util.List

/**
 * This parses javadoc comments into the markdown model.
 * <p/>
 * Markdown wise the generated document model will have the following format:
 * <pre>
 *     public _class/interface_ __class-name__ extends something [package] {
 *     > class javadoc
 *
 *     __full method declaration__
 *     > method javadoc
 *     _Returns_
 *     > description
 *     _Parameters_
 *     > _param_ - description
 *     _Throws_
 *     > _exception_ - description
 *     _See_
 *     > description
 *
 *     ...
 *     }
 * </pre>
 * The "Returns", "Parameters", "Throws", and "See" parts are only included if there are any entries for them.
 * <p/>
 * Please note that this parser does not try to parse java code more than trying to identify the beginning of a class and method.
 * All other information comes from the javadoc comment block. If there is no javadoc the resulting information will be quite poor.
 */
class JavadocParser implements Parser {

    //
    // Private Members
    //

    /** Indicates if we are currently in a javadoc block. */
    private boolean inJavadocBlock = false

    /** Indicates if we are currently in a declaration. */
    private boolean inDeclarationBlock = false

    /** The package of the currently parsed java file. */
    private String pkg = ""

    /** The latest found javadoc block. */
    private List<String> javadoc = null

    /** The current declaration found. */
    private String declaration = null

    //
    // Methods
    //

    /**
     * Parses a java source file and adds its javadoc comments as appropriate markdown models to the passed Doc.
     *
     * @param doc The parsed result is added to this.
     * @param parseFile The file whose content to parse.
     *
     * @throws IOException on failure.
     * @throws ParseException on parse failures.
     */
    @Override
    void parse(Doc document, File parseFile) throws IOException, ParseException {
        this.inJavadocBlock = false
        this.inDeclarationBlock = false
        this.pkg = ""
        this.javadoc = null
        this.declaration = null

        parseFile.eachLine { line ->
            if (!inJavadocBlock && !inDeclarationBlock && line.trim().startsWith("package")) {
                this.pkg = line.replaceFirst("package ", "").replace(';', ' ').trim()
            }
            else if (!inJavadocBlock && !inDeclarationBlock && line.trim().startsWith("/**")) {
                saveJavadocLine(line)
                if (!line.trim().endsWith("*/")) {
                    inJavadocBlock = true;
                }
            }
            else if (inJavadocBlock && !line.trim().endsWith("*/")) {
                saveJavadocLine(line)
            }
            else if (inJavadocBlock && line.trim().endsWith("*/")) {
                inJavadocBlock = false;
            }
            else if (
                    !inJavadocBlock && !inDeclarationBlock &&
                    (line.trim().endsWith(";") || line.trim().endsWith("{")) &&
                    this.javadoc != null
            ) {
                parseDeclarationLine(document, line)
                if (!(line.trim().endsWith(";") || line.trim().endsWith("{"))) {
                    inDeclarationBlock = true;
                }
            }
            else if (!inJavadocBlock && inDeclarationBlock) {
                parseDeclarationLine(document, line)
            }

            return null // Apparently the closure must return something even though it does not make any sense in such a case as this.
        }

        Paragraph p = new Paragraph()
        p.addItem(new PlainText(text: "}"))
        document.addItem(p)

        p = new Paragraph()
        p.addItem("    ")
        document.addItem(p)
    }

    /**
     * Saves a javadoc line. They are filtered from comment characters and then added to the javadoc list to be parsed later.
     *
     * @param line The javadoc line to save for later.
     */
    private void saveJavadocLine(String line) {
        String sline = line.trim()
        if (sline.startsWith("/**")) {
            sline = sline.substring(3)
        }
        else if (sline.startsWith("*")) {
            if (sline.length() > 2) {
                sline = sline.substring(2)
            }
            else {
                sline = ""
            }
        }
        if (sline.endsWith("*/")) {
            sline = sline.substring(0, sline.length() -2)
        }
        if (this.javadoc == null) {
            this.javadoc = new LinkedList<>()
        }
        this.javadoc.add(sline)
    }

    /**
     * This is called when a declaration has been identified and will parse that declaration building a model for it, and
     * then adding it to the document model. After that parseJavadoc(...) is called to add the javadoc information for the
     * declaration to the document model.
     * <p/>
     * In java the javadoc comes before the declaration, but our doc format puts the declaration first and then its javadoc
     * information.
     *
     * @param document The document model to add to.
     * @param line The current declaration line. A declaration might occupy more than one line so until we see the end of
     *             it, we just save the line text possibly adding it to previous text.
     */
    private void parseDeclarationLine(Doc document, String line) {
        if (this.declaration == null) {
            this.declaration = line.trim()
        }
        else {
            this.declaration += " " + line.trim()
        }

        if (this.declaration.trim().endsWith(";") || this.declaration.trim().endsWith("{")) {
            inDeclarationBlock = false

            boolean classOrInterface = false;
            Paragraph p = new Paragraph()
            if (this.declaration.contains("class ") || this.declaration.contains("interface ")) {
                classOrInterface = true
                String[] words = this.declaration.split("\\s+");
                boolean handledName = false
                for (int i = 0; i < words.length; i++) {
                    String word = words[i]

                    if (word.equals("public") || word.equals("protected")) {
                        PlainText pt = new PlainText(text: word)
                        p.addItem(pt)
                    }
                    else if (word.equals("class") || word.equals("interface")) {
                        Emphasis emp = new Emphasis(text: word)
                        p.addItem(emp)
                    }
                    else {
                        if (!handledName) {
                            Strong s = new Strong(text: word + " ")
                            p.addItem(s)
                            handledName = true
                        }
                        else {
                            // Please note that we remove any  '{' at the end since we will add it ourself later
                            // after the [package] specification.
                            PlainText pt = new PlainText(text:  word.replace('{', ' ').trim() + " ")
                            p.addItem(pt)
                        }
                    }
                }
                PlainText pt = new PlainText(text: "[" + this.pkg + "] {")
                p.addItem(pt)
            }
            else {
                if (!this.declaration.trim().startsWith("private") && !this.declaration.trim().startsWith("protected")) {
                    Strong s = new Strong(text: this.declaration.replace(';', ' ').replace('{', ' ').trim())
                    p.addItem(s)
                }
            }
            document.addItem(p)

            if (!classOrInterface && !this.declaration.trim().startsWith("private") && !this.declaration.trim().startsWith("protected")) {
                parseJavadoc(document, classOrInterface)
            }
            else if (classOrInterface) {
                parseJavadoc(document, classOrInterface)
            }

            this.declaration = null
            this.javadoc = null
        }
    }

    /**
     * Here handle previously saved javadoc text.
     *
     * @param document The document model to add javadoc information to.
     * @param classOrInterface true if this javadoc is for a class or interface.
     */
    private void parseJavadoc(Doc document, boolean classOrInterface) {
        List<String> text = new LinkedList<>()
        List<String> params = new LinkedList<>()
        List<String> exceptions = new LinkedList<>()
        String returnDesc = null
        String seeDesc = null

        if (this.javadoc == null || this.javadoc.size() == 0) {
            if (this.javadoc == null) {
                this.javadoc = new LinkedList<>()
            }
            this.javadoc.add("(No Javadoc provided!)")
        }

        boolean textPart = true;
        this.javadoc.each { jdline ->
            if (jdline.contains("@param")) {
                textPart = false;
                params.add(jdline)
            }
            else if (jdline.contains("@exception") || jdline.contains("@throws")) {
                textPart = false
                exceptions.add(jdline)
            }
            else if (jdline.contains("@see")) {
                seeDesc = jdline.replace("@see", "").trim()
            }
            else if (jdline.contains("@return")) {
                returnDesc = jdline.replace("@return", "").trim()
            }
            else if (textPart) {
                text.add(jdline)
            }
        }


        DocItem p = new BlockQuote()
        PlainText format = new PlainText()
        boolean preMode = false

        text.each { line ->
            if (!preMode && line.trim().matches("^<[Pp][Rr][Ee]>.*")) {
                preMode = true
                p.addItem(format)
                document.addItem(p)
                format = new PlainText()
                p = new CodeBlock()
            }
            else if (preMode && line.trim().matches("^</[Pp][Rr][Ee]>.*")) {
                preMode = false
                document.addItem(p)
                p = new BlockQuote()
                format = new PlainText()
            }
            else if (preMode) {
                p.addItem(line)
            }
            else {
                line.split("\\s+|>\\s*|</").each { word ->
                    if (word.matches("^<[Pp].?") ) {
                        p.addItem(format)
                        format = new PlainText()
                        document.addItem(p)
                        p = new BlockQuote()
                    }
                    else if (word.matches("^</[Pp]")) {
                        // ignore
                    }
                    else {
                        format.text += (word + " ")
                    }
                }
            }
        }
        p.addItem(format)
        document.addItem(p)

        if (!classOrInterface) {

            if (returnDesc != null) {
                p = new Paragraph()
                p.addItem(new Emphasis(text: "Returns"))
                document.addItem(p)
                p = new BlockQuote()
                PlainText pt = new PlainText(text: returnDesc)
                p.addItem(pt)
                document.addItem(p)
            }

            if (params.size() > 0) {
                p = new Paragraph()
                p.addItem(new Emphasis(text: "Parameters"))
                document.addItem(p)

                params.each { param ->
                    p = new BlockQuote()
                    String[] words = param.split("\\s+")
                    p.addItem(new Emphasis(text: words[1]))
                    if (words.length > 2) {
                        PlainText pt = new PlainText()
                        pt.text = "- "
                        for (int i = 2; i < words.length; i++) {
                            pt.text += (words[i] + " ")
                        }
                        p.addItem(pt)
                    }
                    document.addItem(p)
                }
            }

            if (exceptions.size() > 0) {
                p = new Paragraph()
                p.addItem(new Emphasis(text: "Throws"))
                document.addItem(p)

                exceptions.each { exc ->
                    p = new BlockQuote()
                    String[] words = exc.split("\\s+")
                    p.addItem(new Emphasis(text: words[1]))
                    if (words.length > 2) {
                        PlainText pt = new PlainText()
                        pt.text = "- "
                        for (int i = 2; i < words.length; i++) {
                            pt.text += (words[i] + " ")
                        }
                        p.addItem(pt)
                    }
                    document.addItem(p)
                }
            }

            if (seeDesc != null) {
                p = new Paragraph()
                p.addItem(new Emphasis(text: "See"))
                document.addItem(p)
                p = new BlockQuote()
                PlainText pt = new PlainText(text: seeDesc.replace('#', '.'))
                p.addItem(pt)
                document.addItem(p)
            }

        }
    }

    /**
     * Returns true if the extension of the specified file is a valid extension for this parser.
     *
     * @param fileName The file to check extension of.
     */
    @Override
    boolean validFileExtension(String fileName) {
        return fileName.endsWith(".java")
    }
}
