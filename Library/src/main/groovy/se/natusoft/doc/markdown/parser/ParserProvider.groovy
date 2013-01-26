package se.natusoft.doc.markdown.parser

import se.natusoft.doc.markdown.api.Parser

/**
 * Provides a parser depending on file extension.
 */
class ParserProvider {

    /**
     * Returns a valid parser for the file or null if none match.
     *
     * @param ext The file to get a parser for.
     *
     * @return A parser or null.
     */
    public static Parser getParserForFile(File file) {
        return getParserForFile(file.getName())
    }

    /**
     * Returns a valid parser for the file or null if none match.
     *
     * @param ext The file to get a parser for.
     *
     * @return A parser or null.
     */
    public static Parser getParserForFile(String file) {
        Parser parser = null

        Parser.parsers.each {
            if (it.validFileExtension(file)) {
                parser = it
            }
        }

        return parser;
    }

}
