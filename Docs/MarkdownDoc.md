## Introduction

MarkdownDoc is a tool that basically does what the name sounds like. My intention with this tool was to be able to document my java opensource tools in markdown and be able to generate both html and PDF from it using a maven plugin.

So why not use mavens site plugin which does support markdown ? These days generating a whole site for your project seems a bit much. Both Bitbucket and GitHub supports markdown documentation right off in a nice and easy way. I want to choose where to put my documentation (ok, most locations in maven can be configured) and I also had the following requirements:

* Be able to generate one PDF document from a whole collection of separate markdown documents so that I can spread them out in different subproject for multi maven project projects. If you are reading this in PDF format this PDF have been put together from multiple sources. 

* Be able to generate a table of contents and a title page. 

* I just wanted to do it my way OK! :-)

It does also provide a java -jar executable variant. The main functionality is available as a library.

In short MarkdownDoc provides the following:

* Markdown document model.
* Markdown parser.
* Javadoc comment parser.
* PDF generator.
* HTML generator.
* Markdown generator.
* java -jar commandline executable.
* Markdown editor that formats Markdown while writing with preview and PDF + HTML generation. Can be run with java -jar.
* Maven plugin.

### Binaries

Version 1.2.9 was available in maven central.

Binaries for newer versions will be made available at [Bintray](https://bintray.com/tommy/maven/MarkdownDoc/view) and Bintrays JCenter repository: http://jcenter.bintray.com which should also contain everything in maven central.

### Thanks

Thanks to [John Gruber](http://www.daringfireball.net) for the brilliant [markdown](http://daringfireball.net/projects/markdown) document format, and to [iText Software Corp.](http://itextpdf.com) for making an excellent easy to use PDF library and making it available as open source. 

### Version history

#### 1.3.3

Bug fixes.



#### 1.3.2

Added markdown formatting as you write. 

#### 1.3.1

Bug fixes. Monospaced font now rendering correctly. 

Deleting text with backspace have strange effects on text layout. That is, the place where a senetence is broken to the right and moved down to the next line keeps moving around while deleting text, in some completely different paragraph! This is entirely handled by JTextPane. I have tried to find a way to intercept the delete key and handle delete myself, but I have not been successful in finding a way to do that if it is even possible. Continuing writing new text after deleting text seems to restore the layout. This oddity has no effect on the final text, it is just the layout while editing that is affacted. You will also only see this if you write paragraphs as one block of text that wraps around into multiple lines without pressing return until the end of the paragraph.

#### 1.3

Made big changes to the editor, finally making it into what I want, with some markdown formatting as you write, and far more configuration in settings dialog, which have also been redone.

Bug fixes. 

#### 1.2.10

Added support for &amp;lt;, &amp;gt;, and &amp;amp;.

#### 1.2.9

Added markdown file reading feature by allowing markdown files to be dropped on the editor in preview mode, in wihch case the dropped file will be formatted and displayed without changeing the content of the editor. Exiting preview and doing a preview again will again preview the editor content.

#### 1.2.8

Headings can now **not** be more than one line (not include LF/CRLF). Before they were treated like paragraphs. This to be more compatible with other Markdown tools and Markdown documents.

#### 1.2.7

Added settings for specifying top, bottom, left, and right margins in editor. Please note that I've been a bit lazy here. The sizes are in pixels, not characters/lines! 

#### 1.2.6

Added the new _.mddoc_ format, which makes command line usage easier, but it is also supported by the maven plugin and the library has a utility that completely handles this format.

Added a Java Swing based editor for editing markdown with support.

#### 1.2.5 

Added _parserOptions_ now used by JavadocParser to markdown parse javadoc comments if markdownJavadoc=true is provided. The Parser API is thus also updated to take a Properties object for the parser options.

#### 1.2.4

Added _makeFileLinksRelativeTo_ option for HTMLGenerator and MarkdownGenerator mostly to be able to manipulate _file:_ references to images in the generated result so that the image paths still work in source when editing with a markdown tool and is still correct when generated to a different path. 

#### 1.2.3

If image paths are not absolute and not http referenced then they are now looked for relative to the source markdown file first, and then the are looked for relative to the result file as before. This makes it easier to generate a big document for a whole project containing several subproject with local makdown documents and referenced images. The image reference can still be relative to the subproject local markdown file.

#### 1.2.2

Added support for _&nbsp_ to be able to indent text. This is one more exception to no html passtrhough.

### How markdown is MarkdownDoc ?

Well, it implements the "specification" as documented on [daringfireball.net](http://daringfireball.net/projects/markdown/syntax).
This specification however is not extremely exact so there might be some differences.

The known (and intentional) differences are:

* No HMTL pass-through! Well, there is a small exception to that. HTML comments are passed along. Mostly because there is no markdown comment format and I wanted to be able to put comments in my documents. "\&\n\b\s\p\;" is also passed through to create indents that are not code blocks. The reason for no HTML pass-through is that MarkdownDoc takes it directly from markdown to PDF without any HTML rendering in between. The main purpose of this tool is to write documentation not generate HTML sites.

* Escaping with '\\'. In MarkdownDoc you can escape any character with \\ and it will be passed through as is
  without being acted on if it has markdown meaning.

* No entity encoding of email addresses.

* No multiple block quote levels (as of now).

## File specifications

With both the maven plugin and the command line execution jar file you can specify a set of files to use
as input. These are basically a comma separated list of files, but with the following additions:

/my/path

> All files in the directory pointed to by the path.

/my/path/\*\*

> All files in the directory pointed to by the path and sub directories.

/my/path/\*\*/_regexp pattern_

> All files matching the pattern in the directory pointed to by the path and sub directories.

/my/path/_regexp pattern_

> All files matching the pattern in the directory pointed to by the path.

/my/path/fileset.fs

> The above rules are applied to all file specifications in files having the .fs extension. # are comment lines within .fs files.

