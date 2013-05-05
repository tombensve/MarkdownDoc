# Library

The library is made up of a document model representing all formats of markdown, parsers and
generators. The parsers produce a document model and the generators generate from that model.
The document model represents the markdown formats. Thereby there are no HTML pass-through
from a markdown document! This tool only deals with markdown, not HTML.

The API docs for the library can be found [here](http://apidoc.natusoft.se/MarkdownDoc).

## Usage

In package se.natusoft.doc.markdown.api there are 3 API classes:

__Options__ - This represents options for a generator. It should be seen as a narrow variant of Object
              representing only generator options, but any such. It has one method common to all
              `public boolean isHelp()`. Implementations should have a default constructor.
              
__Parser__ - This represents a parser.

	public interface Parser {
		public void parse(Doc document, File parseFile, Properties parserOptions) 
		    throws IOException, ParseException;
	}

The parser gets passed an already created Doc model allowing the document to be built from multiple
source files by parsing into the same document. 

__Generator__ - This represents a generator.

	public interface Generator {
    	public Class getOptionsClass();
    	public void generate(Doc document, Options options, File rootDir) 
    	    throws IOException, GenerateException;
	}

_getOptionsClass()_ returns the class implementing Options and holding all the options for the generator.

_generate(...)_ generates the document provided by _document_ using the specified _options_ and producing
the result in whatever _rootDir_ relative path is specified in the _options_.

### Parsers

#### se.natusoft.doc.markdown.parser.MarkdownParser

This parser parses markdown and only markdown! It ignores HTML with the exception of comments. 

Example usage:

	Parser parser = new MarkdownParser();
	Doc document = new Doc();
	Properties parserOptions = new Properties();
	parser.parse(document, parseFile, parserOptions);
	
#### se.natusoft.doc.markdown.parser.JavadocParser

This parser parses java source files and extracts class and method declarations and javadoc comment blocks.
it produces a document model looking like this (in markdown format):

    public _class/interface_ __class-name__ extends something [package] {
    > class javadoc

    __full method declaration__
    > method javadoc
    _Returns_
    > description
    _Parameters_
    > _param_ - description
    _Throws_
    > _exception_ - description
    _See_
    > description

    ...
    }

This allows you to include API documentation in your documentation without having to duplicate it. Please note that if `markdownJavadoc=true` parser option have been specified then _class javadoc_ and _method javadoc_ will not be ”\>” formatted but passed to the MarkdownParser instead.

Example usage:

    Parser parser = new JavadocParser();
    Doc document = new Doc();
    Properties parserOptions = new Properties();
    parser.parse(document, parseFile, parserOptions);

#### se.natusoft.doc.markdown.parser.ParserProvider

This is a utility to get a parser based on file extension. ".md", ".markdown", ".mdpart", and ".java" are valid extensions
that will return a parser. If the passed file does not have a valid extension null will be returned.

Example usage:

    Parser parser = ParserProvider.getParserForFile(parseFile);
    Doc document = new Doc();
    Properties parserOptions = new Properties();
    parser.parse(document, parseFie, parserOptions);

### Generators

Example usage:

    public static void main(String[] args) {
    	Doc document = new Doc();

    	... parsing of document.

		Generator generator = new [PDF|HTML|Markdown]Generator();

		// I'm using OptionsManager to load the options in this example.
		// If you use maven or ant then those tools will have loaded
		// the options for you and getOptionsClass() is not relevant
		// in that case.
    	CommandLineOptionsManager<Options> optMgr =
        	new CommandLineOptionsManager<Options>(generator.getOptionsClass());
    	Options options = optMgr.loadOptions("--", args);
    	if (options.isHelp()) {
    		optMgr.printHelpText("--","", System.out);
    	}
    	else {
        	File rootDir = new File();
    		generator.generate(document, options, rootDir);
    	}
    }

Please note that the CommandLineOptionsMangager used in the example is part of the OptionsManager
tool also by me. Available at [github.com/tombensve/OptionsManager](https://github.com/tombensve/OptionsManager).

#### se.natusoft.doc.markdown.generator.PDFGenerator

This generator produces a PDF document.

#### se.natusoft.doc.markdown.generator.HTMLGenerator

This generator produces an HTML document.

#### se.natusoft.doc.markdown.generator.MarkdownGenerator

This generator produces a Markdown document. So why would we want to generate markdown ? Well, it became needed after I added the JavadocParser. Now I can have both markdown and java files as input and the PDF and HTML files contained the whole result including the javadoc information. The original markdown document however does not have the javadoc parts, and this markdown document is read as is on github and will then not be complete. Therefore I added this generator and moved my real source document into docs/src and also generate a markdown version into docs that will be as complete as the pdf and html version.




