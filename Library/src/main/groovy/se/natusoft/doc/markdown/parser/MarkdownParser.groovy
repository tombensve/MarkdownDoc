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
 *         2012-11-04: Created!
 *         
 */
package se.natusoft.doc.markdown.parser

import groovy.transform.CompileStatic
import se.natusoft.doc.markdown.api.Parser
import se.natusoft.doc.markdown.exception.ParseException
import se.natusoft.doc.markdown.io.Line
import se.natusoft.doc.markdown.io.LineReader
import se.natusoft.doc.markdown.model.*
import se.natusoft.doc.markdown.parser.markdown.io.MDLine
import se.natusoft.doc.markdown.parser.markdown.io.MDLineReader
import se.natusoft.doc.markdown.parser.markdown.model.MDImage
import se.natusoft.doc.markdown.parser.markdown.model.MDLink
import se.natusoft.doc.markdown.parser.markdown.model.MDList

/**
 * A parser that parses Markdown.
 * <p/>
 * This implements Markdown as documented on http://daringfireball.net/projects/markdown/syntax
 * with the following exceptions:
 * <ul>
 *     <li> No entity encoding of email addresses.</li>
 *     <li> No multiple block quote levels.</li>
 *     <li> '\' will treat the next char as text no matter what it is.</li>
 * </ul>
 * &lt;, &gt;, and &amp; is not handled by this parser but by the HTMLGenerator instead since
 * this tool also can generate PDF such HTML specifics should not be in the parsed text.
 */
@CompileStatic
public class MarkdownParser implements Parser {

    //
    // Private Members
    //

    /** Holds parsed links. Links can be built in 2 different places. */
    private Map<String, Link> links = new HashMap<String, Link>()

    /** The file we are parsing. We save this to pass to ParseException. */
    private File file;

    //
    // Methods
    //

    /**
     * Parses a markdown file and adds its document structure to the passed Doc.
     *
     * @param doc The parsed result is added to this.
     * @param parseFile The file whose content to parse.
     *
     * @throws IOException on failure.
     * @throws ParseException on parse failures.
     */
    @Override
    public void parse(Doc doc, File parseFile, Properties parserOptions) throws IOException, ParseException {
        this.file = parseFile

        parse(doc, new FileInputStream(parseFile), parserOptions);
    }

    /**
     * Parses a markdown stream and adds its document structure to the passed Doc.
     *
     * @param doc The parsed result is added to this.
     * @param parseStream The stream whose content to parse.
     *
     * @throws IOException on failure.
     * @throws ParseException on parse failures.
     */
    @Override
    public void parse(Doc doc, InputStream parseStream, Properties parserOptions) throws IOException, ParseException {
        LineReader lineReader = null
        try {
            lineReader = new MDLineReader(new InputStreamReader(parseStream))

            DocItem prevDocItem = null
            Stack<DocItem> hierarchyStack = new Stack<DocItem>();

            while (lineReader.hasLine()) {
                MDLine line = (MDLine)lineReader.readLine()

                if (!line.empty) {
                    DocItem docItem = null

                    def commentStartCase = { MDLine it -> it.commentStart }
                    def headerCase       = { MDLine it -> it.header }
                    def listCase         = { MDLine it -> it.list && (it.leadingSpaces < 4 || (prevDocItem != null && prevDocItem.isHierarchy)) }
                    def codeBlockCase    = { MDLine it -> it.codeBlock }
                    def blockQuoteCase   = { MDLine it -> it.blockQuote }
                    def horizRulerCase   = { MDLine it -> it.horizRuler }
                    def linkUrlCSpecCase = { MDLine it -> it.isLinkURLSpec(this.links) }

                    switch (line) {
                        case commentStartCase : docItem = parseComment    (line, lineReader); break
                        case headerCase       : docItem = parseHeader     (line, lineReader); break
                        case listCase         : docItem = parseList       (line, lineReader); break
                        case codeBlockCase    : docItem = parseCodeBlock  (line, lineReader); break
                        case blockQuoteCase   : docItem = parseBlockQuote (line, lineReader); break
                        case horizRulerCase   : docItem = new HorizontalRule();               break
                        case linkUrlCSpecCase : parseLinkUrlSpec(line);                       break

                        // The annoying underline header format.
                        case { lineReader.hasLine() && (lineReader.peekNextLine().contains("----") ||
                                lineReader.peekNextLine().contains("====")) } :
                            docItem = parseHeader(line, lineReader)
                            break

                        default:
                            Paragraph paragraph = new Paragraph()
                            parseParagraph(paragraph, line, lineReader)
                            docItem = paragraph
                    }

                    // Handle specific DocItem subclass behavior.
                    if (docItem != null && docItem.keepConsecutiveTogether) {
                        if (prevDocItem != null && prevDocItem.isSameType(docItem)) {
                            boolean addItem = true

                            if (docItem.isHierarchy) {
                                if (docItem.isHierarchyDown(prevDocItem)) {
                                    prevDocItem.addItem(docItem)
                                    addItem = false
                                    hierarchyStack.push(prevDocItem)
                                    prevDocItem = docItem
                                }
                                else if (docItem.isHierarchyUp(prevDocItem)) {
                                    while (docItem.isHierarchyUp(prevDocItem)) {
                                        prevDocItem = hierarchyStack.pop()
                                    }
                                }
                            }

                            if (prevDocItem.addBetweenKeepTogether != null) {
                                prevDocItem.addItem(prevDocItem.addBetweenKeepTogether)
                            }

                            if (addItem) {
                                for (DocItem content : docItem.items) {
                                    prevDocItem.addItem(content)
                                }
                            }

                            docItem = null
                        }
                    }

                    if (docItem != null) {
                        setParseFileOnDocItems(docItem, this.file != null ? this.file : null)

                        doc.addItem(docItem)
                        prevDocItem = docItem
                    }
                }
            }
        }
        catch (ParseException pe) {
            throw pe;
        }
        catch (Exception e) {
            throw new ParseException(file: this.file.getAbsolutePath(), line: lineReader.getLastReadLine().toString(), lineNo: lineReader.getLineNo(), message: "Unknown error", cause: e)
        }
        finally {
            if (lineReader != null) {
                lineReader.close()
            }
        }
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

    /**
     * Returns true if the extension of the specified file is a valid extension for this parser.
     *
     * @param fileName The file to check extension of.
     */
    @Override
    boolean validFileExtension(String fileName) {
        return fileName.endsWith(".md") || fileName.endsWith(".markdown") || fileName.endsWith(".mdpart")
    }

/**
     * Parses a html comment
     *
     * @param line The current line.
     * @param lineReader To read more lines from.
     *
     * @return A Comment.
     */
    private DocItem parseComment(Line line, LineReader lineReader) {
        MDLine mdline = (MDLine)line

        StringBuilder sb = new StringBuilder()
        if (!mdline.commentEnd) {
            while (lineReader.hasLine() && !((MDLine)lineReader.peekNextLine()).commentEnd) {
                line = lineReader.readLine()
                sb.append("\n")
                sb.append(line.toString())
            }
            lineReader.readLine() // We have to remove the last "-->"
        }
        else {
            String cmLine = line.toString().substring(5)
            cmLine = cmLine.substring(0, cmLine.length() - 4)
            sb.append(cmLine)
        }

        return new Comment(text: sb.toString())
    }

    /**
     * Parses a header.
     *
     * @param line The current line.
     * @param lineReader To read more lines from.
     *
     * @return A Header.
     *
     * @throws IOException on failure to read input.
     * @throws ParseException On bad format being parsed.
     */
    private DocItem parseHeader(Line line, LineReader lineReader) throws IOException, ParseException {

        String text = line.toString()

        Header.Level level = null;
        switch (text) {
            case { String it -> it.startsWith("######") } : level = Header.Level.H6; break
            case { String it -> it.startsWith("#####")  } : level = Header.Level.H5; break
            case { String it -> it.startsWith("####")   } : level = Header.Level.H4; break
            case { String it -> it.startsWith("###")    } : level = Header.Level.H3; break
            case { String it -> it.startsWith("##")     } : level = Header.Level.H2; break
            case { String it -> it.startsWith("#")      } : level = Header.Level.H1; break

            case { lineReader.hasLine() && lineReader.peekNextLine().contains("===") } :
                level = Header.Level.H1
                lineReader.readLine()
                break;

            case { lineReader.hasLine() && lineReader.peekNextLine().contains("---") } :
                level = Header.Level.H2
                lineReader.readLine()
                break

            default: throw new ParseException(file: this.file != null ? this.file.toString() : "stream", lineNo: lineReader.lineNo,
                    message: "Bad header found in line: '" + line.toString() + "'!")
        }

        Header header = new Header(level: level)

        header.addItem(line.removeAll("#").removeLeadingSpaces())

        // I comment this part out for now since other MD tools seems only allow one line for heading.
        //while (lineReader.hasLine() && !lineReader.peekNextLine().empty) {
        //    line = lineReader.readLine()
        //    header.addItem(" ")
        //    header.addItem(line.removeAll("#").removeLeadingSpaces().toString())
        //}

        return header
    }

    /**
     * Parses a code block.
     *
     * @param line The current line.
     * @param lineReader To read more lines from.
     *
     * @return A CodeBlock.
     *
     * @throws IOException on input failure.
     */
    private DocItem parseCodeBlock(Line line, LineReader lineReader) throws IOException {
        CodeBlock codeBlock = new CodeBlock()
        codeBlock.addItem(line)

        while (lineReader.hasLine() && ((MDLine)lineReader.peekNextLine()).codeBlock) {
            line = lineReader.readLine()

            codeBlock.addItem(line)
        }

        return codeBlock
    }

    /**
     * Parses a block quote.
     *
     * @param line The current line.
     * @param lineReader To read more lines from.
     *
     * @return A BlockQuote.
     *
     * @throws IOException on input failure.
     */
    private DocItem parseBlockQuote(Line line, LineReader lineReader) throws IOException {
        BlockQuote blockQuote = new BlockQuote()
        parseParagraph(blockQuote, line.removeFirstWord(), lineReader, ">")

        return blockQuote
    }

    /**
     * Parses a list entry.
     *
     * @param line The current line.
     * @param lineReader To read more lines from.
     *
     * @return A ListItem.
     *
     * @throws IOException on input failure.
     */
    private DocItem parseList(MDLine line, LineReader lineReader) throws IOException {
        MDList list = new MDList(ordered: line.orderedList, indentLevel: line.leadingSpaces)

        boolean isList = true
        int indent = line.leadingSpaces >= 3 ? line.leadingSpaces : 3

        // QD fix for a hard to find bug. List entries not at top level still retain their "list" char
        // even though they have been identified as list entries.
        line = (MDLine)line.removeBeg("*")
        line = (MDLine)line.removeBeg("+")
        line = (MDLine)line.removeBeg("-")

        ListItem listItem = new ListItem()
        Paragraph liParagraph = new Paragraph()
        parseParagraph(liParagraph, line.removeFirstWord(), lineReader, isList)
        listItem.addItem(liParagraph)

        MDLine peekLine = (MDLine)lineReader.peekNextLine()
        while (peekLine != null && peekLine.leadingSpaces >= indent && !peekLine.isList()) {

            line = (MDLine)lineReader.readLine()

            liParagraph = new Paragraph()
            parseParagraph(liParagraph, line, lineReader, isList)
            listItem.addItem(liParagraph)

            peekLine = (MDLine)lineReader.peekNextLine()
        }

        list.addItem(listItem)

        return list
    }

    /**
     * Adds urls to already parsed links.
     *
     * @param line The current line
     */
    private void parseLinkUrlSpec(Line line) throws ParseException {
        line = line.removeAll("\\[").removeAll("\\]:")
        if (line.numberOfWords < 2) {
            throw new ParseException(file: this.file.getAbsolutePath(), lineNo: line.lineNumber, message: "Bad link url specification: '" +
                    line.toString() + "'!")
        }

        Link link = this.links[line.getWord(0)]
        if (link == null) {
            throw new ParseException(file: this.file.getAbsolutePath(), lineNo: line.lineNumber, message: "The specified link is undefined! '" + line.toString() + "'")
        }
        link.url = line.getWord(1)
        if (line.numberOfWords > 2) {
            line.currentWordPosition = 1
            String space = ""
            link.title = ""
            while (line.hasMoreWords()) {
                link.title = link.title + space + line.nextWord
                space = " "
            }
            link.title = link.title.substring(1, link.title.length() - 1)
        }
    }

    /**
     * Parses a paragraph of text.
     *
     * @param paragraph The paragraph to parse.
     * @param line The current line.
     * @param lineReader To read more lines from.
     *
     * @throws IOException
     * @throws ParseException
     */
    private void parseParagraph(Paragraph paragraph, Line line, LineReader lineReader) throws IOException, ParseException {
        parseParagraph(paragraph, line, lineReader, (String)null)
    }
    /**
     * Parses a paragraph of text.
     *
     * @param paragraph The paragraph to parse.
     * @param line The current line.
     * @param lineReader To read more lines from.
     *
     * @throws IOException
     * @throws ParseException
     */
    private void parseParagraph(Paragraph paragraph, Line line, LineReader lineReader, boolean isList) throws IOException, ParseException {
        parseParagraph(paragraph, line, lineReader, (String)null, isList)
    }

    /**
     * Parses a paragraph of text.
     *
     * @param paragraph The paragraph to parse.
     * @param line The current line.
     * @param lineReader To read more lines from.
     * @param removeBeginningWord if non null and this matches the first word of a line that word is removed.
     *
     * @throws IOException
     * @throws ParseException
     */
    private void parseParagraph(Paragraph paragraph, Line line, LineReader lineReader, String removeBeginningWord) throws IOException, ParseException {
        parseParagraph(paragraph, line, lineReader, removeBeginningWord, false)
    }

    /**
     * Parses a paragraph of text.
     *
     * @param paragraph The paragraph to parse.
     * @param line The current line.
     * @param lineReader To read more lines from.
     * @param removeBeginningWord if non null and this matches the first word of a line that word is removed.
     *
     * @throws IOException
     * @throws ParseException
     */
    private void parseParagraph(Paragraph paragraph, Line line, LineReader lineReader, String removeBeginningWord, boolean isList) throws IOException, ParseException {

        StringBuilder sb = new StringBuilder();

        boolean done = false;
        String space = ""
        while (!done) {
            if (removeBeginningWord != null && line.numberOfWords > 0 && line.getWord(0).equals(removeBeginningWord)) {
                line = line.removeFirstWord()
            }
            line.eachWord {
                sb << space
                sb << it
                space = " "
            }

            line = lineReader.readLine()
            done = line == null || line.isEmpty()
            if (!done && line != null && (isList ? !((MDLine)line).isPartOfListParagraph(links) : !((MDLine)line).isPartOfParagraph(links))) {
                done = true
                lineReader.pushBackLine(line)
            }
        }

        DocItem current = new PlainText(renderPrefixedSpace: false)

        LinkedList<DocItem> itemStack = new LinkedList<DocItem>()

        char p = 0
        boolean escapeChar = false

        for (int i = 0 ; i < sb.length(); i++) {
            int j = (i + 1) < sb.length() ? i + 1 : -1

            char c = sb.charAt(i);
            char n = j > 0 ? sb.charAt(j) : (char)0

            if (escapeChar) {
                current << c
                escapeChar = false
            }
            else {

                switch (c) {
                    case '\\':
                        escapeChar = true;
                        break

                    case { it == '.' && !(current instanceof Link)}:
                    case { it == ',' && !(current instanceof Link)}:
                    case { it == '!' && n != '[' && !(current instanceof Link)} :
                    case { it == '?' && !(current instanceof Link)}:
                        if (n == ' ') {
                            paragraph.addItem(current)
                            current = current.createNewWithSameConfig()
                            paragraph.addItem(new PlainText(text: ("" + c), renderPrefixedSpace: false))
                        }
                        else {
                            current << c
                        }
                        break

                    case {
                        it == '_' &&
                        !(current instanceof Link) &&
                        (
                            (
                                (current instanceof Strong) ||
                                (current instanceof Emphasis)
                            ) ||
                            (
                                (i+2) < sb.length() &&
                                sb.substring(i+2).contains("_")
                            )
                        )
                    }:
                    case {
                        it == '*' &&
                        !(current instanceof Link) &&
                        (
                            (
                                (current instanceof Strong) ||
                                (current instanceof Emphasis)
                            ) ||
                            (
                                (i+2) < sb.length() &&
                                 sb.substring(i+2).contains("*")
                            )
                        )
                    }:
                        paragraph.addItem(current)
                        if (n == '_' || n == '*') {
                            ++i
                            if (current instanceof Strong) {
                                current = itemStack.pop().createNewWithSameConfig()
                            }
                            else {
                                itemStack.push(current)
                                current = new Strong(renderPrefixedSpace: false)
                            }
                        }
                        else {
                            if (current instanceof Emphasis) {
                                current = itemStack.pop().createNewWithSameConfig()
                            }
                            else {
                                itemStack.push(current)
                                current = new Emphasis(renderPrefixedSpace: false)
                            }
                        }
                        break

                    // &nbsp;
                    case {it == "&" && n == "n"} :
                        paragraph.addItem(new Space())
                        i = i + 5
                        break;

                    // &gt;
                    case {it == "&" && n == "g"} :
                        current << ">"
                        i = i + 3
                        break;

                    // &lt;
                    case {it == "&" && n == "l"} :
                        current << "<"
                        i = i + 3
                        break;

                    // &amp;
                    case {it == "&" && n =="a"} :
                        current << "&"
                        i = i + 4
                        break;

                    case '`':
                        paragraph.addItem(current)
                        if (current instanceof Code) {
                            current = itemStack.pop().createNewWithSameConfig()
                        }
                        else {
                            itemStack.push(current)
                            current = new Code(renderPrefixedSpace: false)
                        }
                        break

                    case { it == '[' && p != '!' && !(current instanceof Link)}:
                        paragraph.addItem(current)
                        itemStack.push((DocItem)current)
                        current = new MDLink(renderPrefixedSpace: false)
                        break

                    case { it == '!' && n == '[' && !(current instanceof Link)}:
                        paragraph.addItem(current)
                        itemStack.push((DocItem)current)
                        current = new MDImage(renderPrefixedSpace: false)
                        ++i
                        break

                    case ']':
                        if (current instanceof Link) {
                            if (n != '(') {
                                paragraph.addItem(current)
                                this.links.put(((Link)current).text, (Link)current)
                                current = itemStack.pop().createNewWithSameConfig()
                            }
                        }
                        else {
                            current << c
                        }
                        break;

                    case ')':
                        if (current instanceof Link) {
                            paragraph.addItem(current)
                            this.links.put(((Link)current).text, (Link)current)
                            current = itemStack.pop().createNewWithSameConfig()
                        }
                        else {
                            current << c
                        }
                        break

                    case { it == '<' && (current.class == PlainText.class)} :
                        paragraph.addItem(current)
                        itemStack.push(current)
                        current = new AutoLink(renderPrefixedSpace: false)
                        break;

                    case { it == '>' && (current.class == AutoLink.class)}:
                        paragraph.addItem(current)
                        current =  itemStack.pop().createNewWithSameConfig()
                        break

                    default:
                        current << c
                }
            }

            p = c
        }

        paragraph.addItem(current)
    }

}
