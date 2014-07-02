/* 
 * 
 * PROJECT
 *     Name
 *         MarkdownDoc Library
 *     
 *     Code Version
 *         1.3.3
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
@CompileStatic
class JavadocParser implements Parser {

    // TODO:
    // Yes, this code is currently a bit messy! To begin with the need for "declaration" to be a member should be fixed. This
    // in turn means the file can't be read line by line!
    // Most of the members shouldn't be there, but a smarter reading of the file could solve most of this.

    //
    // Constants
    //

    private static final String MARKDOWN_JAVADOC = "markdownJavadoc";

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

    /** The parser options. */
    private Properties parserOptions = null;

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
    void parse(Doc document, InputStream parseStream, Properties parserOptions) throws IOException, ParseException {
        throw new ParseException(message: "Parsing from an InputStream is not supported by this parser!")
    }

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
    void parse(Doc document, File parseFile, Properties parserOptions) throws IOException, ParseException {
        this.inJavadocBlock = false
        this.inDeclarationBlock = false
        this.pkg = ""
        this.javadoc = null
        this.declaration = null
        this.parserOptions = parserOptions

        Doc localDoc = new Doc();

        parseFile.eachLine { String line ->
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
                   // (isFieldOrConst(line) || isMethod(line) || isEnumConst(line)) &&
                    this.javadoc != null
            ) {
                parseDeclarationLine(localDoc, line)
                if (!(isFieldOrConst(line) || isMethod(line) || isEnumConst(line))) {
                    inDeclarationBlock = true;
                }
            }
            else if (!inJavadocBlock && inDeclarationBlock) {
                parseDeclarationLine(localDoc, line)
            }

            return null // Apparently the closure must return something even though it does not make any sense in such a case as this.
        }

        Paragraph p = new Paragraph()
        p.addItem(new PlainText(text: "}"))
        localDoc.addItem(p)

        localDoc.addItem(new HorizontalRule())

        p = new Paragraph()
        p.addItem("    ")
        localDoc.addItem(p)

        setParseFileOnDocItems(localDoc, parseFile)

        document.addItems(localDoc.items)
    }

    /**
     * This will provide the parse file to each DocItem created by this parser run.
     *
     * @param docItems
     * @param parseFile
     */
    private void setParseFileOnDocItems(DocItem docItem, File parseFile) {
        docItem.parseFile = parseFile
        if (docItem.hasSubItems()) {
            for (DocItem subDocItem : docItem.items) {
                setParseFileOnDocItems(subDocItem, parseFile)
            }
        }
    }

    private static boolean isFieldOrConst(String line) {
        return line.trim().endsWith(";")
    }

    private static boolean isInterfaceMethod(String line) {
        return (line.contains("(") || line.contains(")")) && line.trim().endsWith(";")
    }

    private static boolean isMethod(String line) {
        return line.trim().endsWith("{") || line.trim().endsWith("{}") || line.trim().endsWith("{ }")
    }

    private static boolean isEnumConst(String line) {
        return line.replace(',', ' ').trim().split(" ").length == 1 && !line.contains("@")
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

        Visibility visibility = new Visibility(this.declaration)
        DeclarationType declType = new DeclarationType(this.declaration)

        if (isFieldOrConst(this.declaration) || isMethod(this.declaration) || isEnumConst(this.declaration)) {
            inDeclarationBlock = false

            boolean classOrInterface = false;
            Paragraph p = new Paragraph()
            if ((declType.isClass() || declType.isInterface() || declType.isEnum()) && (visibility.isPublic() || visibility.isProtected())) {

                classOrInterface = true
                String[] words = this.declaration.split("\\s+");
                boolean handledName = false
                for (int i = 0; i < words.length; i++) {
                    String word = words[i]

                    if (Visibility.isPublic(word) || Visibility.isProtected(word)) {
                        PlainText pt = new PlainText(text: word)
                        p.addItem(pt)
                    }
                    else if (DeclarationType.isDeclarationType(word) || Modifier.isModifier(word)) {
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
                if (visibility.isPublic() || visibility.isProtected()) {
                    Strong s = new Strong(text: removeAnnotations(this.declaration.replace(';', ' ').replace('{', ' ').replace('}', ' ').
                            trim()))
                    p.addItem(s)
                }
                // For interfaces!
                else if (isInterfaceMethod(this.declaration) && !visibility.isPrivate()) {
                    Strong s = new Strong(text: removeAnnotations(this.declaration.replace(';', ' ').replace('{', ' ').replace('}', ' ').
                            trim()))
                    p.addItem(s)
                }
                else if (isEnumConst(this.declaration)) {
                    Strong s = new Strong(text: removeAnnotations(this.declaration.replace(',', ' ').trim()))
                    p.addItem(s)
                }
            }
            document.addItem(p)

            if (!classOrInterface && (visibility.isPublic() || visibility.isProtected())) {
                parseJavadoc(document, classOrInterface)
            }
            else if (isInterfaceMethod(this.declaration) && !visibility.isPrivate()) {
                parseJavadoc(document, classOrInterface)
            }
            else if (isEnumConst(this.declaration)) {
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
     * Removes annotation from the line.
     *
     * @param line The line to remove annotations from.
     *
     * @return The update line without annotations.
     */
    private static String removeAnnotations(String line) {
        return line.replaceAll("(@[A-Z,a-z]+){1} ", "")
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
        this.javadoc.each { String jdline ->
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
            else if (jdline.trim().startsWith("@")) {
                text.add("")
                text.add(jdline)
            }
            else if (textPart) {
                text.add(jdline)
            }
        }

        DocItem p
        if (this.parserOptions.getProperty(MARKDOWN_JAVADOC) != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream()
            PrintStream ps = new PrintStream(baos)
            text.each { line ->
                ps.println(line)
            }
            ps.flush()
            ps.close()
            MarkdownParser mdParser = new MarkdownParser()
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray())
            mdParser.parse(document, bais, this.parserOptions)
        }
        else {
            p = new BlockQuote()
            PlainText format = new PlainText()
            boolean preMode = false

            text.each { String line ->
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
                    line.split("\\s+|>\\s*|</").each { String word ->
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
        }

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

                params.each { String param ->
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

                exceptions.each { String exc ->
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

    /**
     * Utility for handling the visibility of a declaration.
     */
    private static class Visibility {

        private String visibility

        public Visibility(String declaration) {
            if (declaration.trim().length() > 0) {
                this.visibility = declaration.split(" ")[0]
            }
            else {
                this.visibility = ""
            }
        }

        public boolean isPublic() {
            return this.visibility.equals("public")
        }

        public static boolean isPublic(String word) {
            return word.equals("public")
        }

        public boolean isProtected() {
            return this.visibility.equals("protected")
        }

        public static boolean isProtected(String word) {
            return word.equals("protected")
        }

        public boolean isPrivate() {
            return this.visibility.equals("private")
        }

        public static isPrivate(String word) {
            return word.equals("private")
        }

        public boolean isPackage() {
            return !(isPublic() || isProtected() || isPrivate())
        }
    }

    /**
     * Utility for identifying declaration type.
     */
    private static class DeclarationType {

        private String declaration;

        public DeclarationType(String declaration) {
            this.declaration = declaration;
        }

        public boolean isClass() {
            return this.declaration.contains("class ")
        }

        public boolean isInterface() {
            return this.declaration.contains("interface ")
        }

        public boolean isEnum() {
            return this.declaration.contains("enum ")
        }

        public static boolean isDeclarationType(String word) {
            return word.equals("class") || word.equals("interface") || word.equals("enum")
        }
    }

    /**
     * Utility for identifying modifiers.
     */
    private static class Modifier {

        public static boolean isModifier(String word) {
            return word.equals("final") || word.equals("abstract") || word.equals("static")
        }
    }
}
