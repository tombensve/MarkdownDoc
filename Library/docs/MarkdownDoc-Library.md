# Library

The library is made up of a document model representing all formats of markdown, a markdown parser and
a HTML and PDF generator. This design adds the possibility for both more parsers and generators.

Ideas I have is an HTML parser and a markdown generator. I'm also considering an ODT parser since
that is relatively easy to parse. Since markdown is about the basic primitives of formatting which
is more than good enough in most cases any parser of formats that contain more will have to ignore
the more parts and stick to the basics.

## Usage

In package se.natusoft.doc.markdown.api there are 3 API classes:

__Options__ - This represents options for a generator. It should be seen as a narrow variant of Object
              representing only generator options, but any such. It has one method common to all
              `public boolean isHelp()`. Implementations should have a default constructor.
              
__Generator__ - This represents a generator.

	public interface Generator {
    	public Class getOptionsClass();
    	public void generate(Doc document, Options options, File rootDir) 
    	    throws IOException, GenerateException;
	}

_getOptionsClass()_ returns the class implementing Options and holding all the options for the generator.

_generate(...)_ generates the document provided by _document_ using the specified _options_ and producing
the result in whatever _rootDir_ relative path is specified in the _options_.

__Parser__ - This represents a parser.

	public interface Parser {
		public void parse(Doc document, File parseFile) 
		    throws IOException, ParseException;
	}

The parser gets passed an already created Doc model allowing the document to be built from multiple
source files by parsing into the same document. 

### se.natusoft.doc.markdown.parser.MarkdownParser

This parser parses markdown and only markdown! It ignores HTML with the exception of comments. 

Example usage:

	Parser parser = new MarkdownParser();
	Doc document = new Doc();
	parser.parse(document, parseFile);
	

### se.natusoft.doc.markdown.generator.*Generator

Example usage:

    public static void main(String[] args) {
    	Doc document = new Doc();
    
    	... parsing of document.

		Generator generator = new [PDF|HTML]Generator();
	
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

	




