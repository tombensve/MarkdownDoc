# Settings / Options

The options for each generator are represented by a JavaBean model using [OptionsManager](http://github.com/tombensve/OptionsManager) annotations. Independent of how you run a generator it is the same options model that is used the only difference is in how it is populated. If it is from code you just use setter methods to set values. If it is from the command line jar then each option name is prefixed with -- and passed as command line argument. In this case it is OptionsManager that will populate the model from arguments. If it is from the maven plugin then each option is set in the pom with xml tags of the same name as the options. In this case it is maven that populates the options.

The options will be described here in general, not centric to any way of running.

__(R)__ after an option means it is required.

__(O)__ after an option means it is optional.

Note that values of __Boolean__ type should have a value of either "true" or "false". They do need a value when specified from command line! Just a _--firstLineParagraphIndent_ will not work! 

## Common options

<!-- @Div("options") -->

### generator (R)

Specifies the generator to run. Current valid values are:

- pdf
- html
- markdown

### parser (O)

Selects the parser to run. Valid values are:

- markdown
- byext\[ension\]  _This is the defalt value!_

The latter selects parser based on extension of file being parsed. This is in general a good idea to use since there are currently both a markdown parser and a javadoc parser. By using _byext_ it ispossible to pass both .md and .java (and .groovy) files. Each parser is registered as a standard Java service and loaded with ServiceLoader. Each parser also provides which file extensions it recognizes. This is how a parser is resolved. 

### inputPaths (R)

A comma separated list of files and paths. A path can look like this: `MavenPlugin/docs/.\*.md`. A path supports [regular expressions](https://en.wikipedia.org/wiki/Regular_expression). 

Files will be parsed in the order they are specified. When regular expressions are used to include multiple files the order is unspecified.  

### parserOptions (O)

This is a comma separated list of `name=value`. 

#### JavaDoc2MDParser options

Just setting __markdownJavadoc__ to any value will make it take the javadoc text as markdown and parse that also.

#### MarkdownParser options

This parser currently has no options.

## PDFGenerator options

### resultFile : String (R)

The path to the PDF file to write.

### rootDir : String (O)

A root dir to make image paths relative to.

### pageSize : String (O)

The pagesize name like LETTER or A4. Default is A4.

### title : String (O)

The title of the document. This is used if __generateTitlePage__ is set to true.

### subject : String (O)

The subject of the document.

### titlePageImage : String (O)

Put an image on the title page. Format: \<path/URL\>:x:y

### keywords : String (O)

Meta keywords

### author : String (O)

The author of the document.

### version : String (O)

The version to put on the title page. Must be specified to be rendered!

### copyright : String (O)

The copyright message to put on the title page. Must be specified to be rendered!

### authorLabel : String (O)

The label text for 'Author:'. 

### versionLabel : String (O)

The label text for 'Version:'.

### pageLabel : String (O)

The label text for 'Page'.

### tableOfContentsLabel : String (O)

The text for 'Table of Contents'.

### hideLinks : Boolean (O)

If true then links are not rendered as link the link text will be rendered as plain text.

### unorderedListItemPrefix : String (O)

What item marking to use for unordered lists. Default is '- '.

### firstLineParagraphIndent : Boolean (O)

If true then the first line of each paragraph is indented. Default is false.

### backgroundColor : String (O)

__DEPRECATED__! Use an .mss file instead! The background color of the document in "R:G:B" format where each R, G, and B are a number 0 - 255.

### blockQuoteColor : String (O)

__DEPRECATED__! Use an .mss file instead! The blockquote color to use in this document in "R:G:B" format where each R, G, and B are a number 0 - 255.

### codeColor : String (O)

__DEPRECATED__! Use an .mss file instead! The code color to use in this document in "R:G:B" format where each R, G, and B are a number 0 - 255.

### mss : String (O)

This specifies the path to an .mss file to use for setting fonts and colors and image styling of the generated document.

### generateSectionNumbers : Boolean (O)

If true all chapters and sections will be numbered. This was the only option before version 1.4.

### generateTOC : Boolean (O)

This generates a table of contents. Default is false!

### generateTitlePage : Boolean (O)

This will generate one first page with a title, version, author, and copyright. Default is false.

### help (Only from command line!)

Shows help.

<!-- @EndDiv -->

### Comment block annotation setting of options

The PDF generator have a special feature to be able to set options via an annotation in a comment block. The annotations look like this:

    <!--
    
        @PDF<option name>(<option value>)
        @PDF<option name>("<option value>")
    
    -->

The following annotation options are available:

- @PDFTitle(title)
- @PDFSubject(subject)
- @PDFKeywords(keywords)
- @PDFAuthor(author)
- @PDFVersion(version)
- @PDFCopyright(copyright line)
- @PDFAuthorLabel(label)
- @PDFVersionLabel(label)
- @PDFPageLabel(label)
- @PDFTableOfContentsLabel(label)
- @PDFPageSize(size)
- @PDFHideLinks(true/false)
- @PDFUnorderedListItemPrefix(prefix)
- @PDFFirstLineParagraphIndent(true/false)
- @PDFGenerateSectionNumbers(true/false)
- @PDFGenerateTOC(true/false)
- @PDFGenerateTitlePage(true/false)
- @PDFTitlePageImage(imageref)

Put this comment block at the top of the document! The optoins provided this way will not have an effect until the comment block have been processed by the generator, and the annotations found. Thereby it is also theoretically possible to change options further down the document. This should be seen as a side effect rather than a feature!!

<!-- @Div("options") -->

## HTMLGenerator options

### resultFile : String (R)

Where to write the result.

### inlineCSS : Boolean (O)

If true then the css will be included in the generated HTML.

### css : String (O)

The path to a CSS file.

### primitiveHTML : Boolean (O)

When true very primitive HMTL will be generated. This will work for rendering with JEditorPane.

### makeFileLinksRelativeTo : String (O)

The path file links should be relative to.

### help (Only from command line!) 

Shows help.


## MarkdownGenerator options

### resultFile : String (R)

Where to write the result.

### makeFileLinksRelativeTo : String (O)

The path file links should be relative to.

### help (Only from command line!)

Shows help.


<!-- @EndDiv -->

