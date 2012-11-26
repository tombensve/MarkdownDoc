# Command Line

## General

MarkdownDoc can be run using `java -jar markdowndoc-cmd-line-1.0-exec.jar`. If you just run it
without any arguments you get the following:

    Usage: java -jar markdowndoc-cmd-line-1.0-exec.jar <generator> --help
           or
           java -jar markdowndoc-cmd-line-1.0-exec.jar <generator> <fileSpec> --<generator option> ...

What the generator options are depends on the specified generator.

The markdowndoc-cmd-line-1.0-exec.jar is a jar generated to contain all dependencies in the same jar,
making it easy to execute with java -jar.

__generator__

This should be either _pdf_ or _html_.

__filespec__

This is a comma separated list of paths relative to the current directory. The filename
part of the path can contain regular expressions and the directory part of the path can
specify .../**/... to mean any levels of subdirectories. 

Example: root/**/docs/.*.md

## PDF Generator

__--resultFile text (Required)__
    Where to write the result.

__--pageSize text__
    The pagesize name like LETTER or A4.

__--title text__
    The title of the document

__--subject text__
    The subject of the document. 

__--keywords text__
    Meta keywords 

__--author text__
    The author of the document. 

__--version text__
    The version to put on the title page. Must be specified 
    to be rendered! 

__--copyright text__
    The copyright message to put on the title page. Must be 
    specified to be rendered! 

__--hideLinks true/false__
    If true then links are not rendered as link the link text 
    will be rendered as plain text. 

__--unorderedListItemPrefix text__
    What item marking to use for unuredered lists. Default is 
    '- '. 

__--firstLineParagraphIndent true/false__
    If true then the first line of each paragraph is indented. 
    Default is false. 

__--backgroundColor text__
    The background color of the document in "R:G:B" format where 
    each R, G, and B are number 0 - 255. 

__--blockQuoteColor text__
    The blockquote color to use in this document in "R:G:B" 
    format where each R, G, and B are number 0 - 255. 

__--codeColor text__
    The code color to use in this document in "R:G:B" format 
    where each R, G, and B are number 0 - 255. 

__--generateTOC true/false__
    This generates table of contents. Default is false! 

__--generateTitlePage true/false__
    This will generate one first page with title, version, author, 
    and copyright. Default is false. 


## HTML Generator

__--resultFile text (Required)__
    Where to write the result. 

__--inlineCSS true/false__
    If true then the css will be included in the generated HTML.     

__--css text__
    The path to CSS file. 

