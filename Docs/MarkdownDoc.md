## Introduction

MarkdownDoc is a tool that basically does what the name sounds like. My intention with this tool was to be able to
document my java opensource tools in markdown and be able to generate both html and PDF from it using a maven plugin.

So why not use mavens site plugin which does support markdown ? Since maven3 the site plugin is not what it used to 
be and these days generating a whole site for your project seems a bit much. Both Bitbucket and GitHub supports 
markdown documentation right off in a nice and easy way. I want to choose where to put my documentation (ok, most
locations in maven can be configured) and I also had the following requirements:

* Be able to generate one PDF document from a whole collection of separate markdown documents so that I can spread
  them out in different subproject for multi maven project projects. If you are reading this in PDF format this
  PDF have been put together from multiple sources. 

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
* Maven plugin.

Thanks to [John Gruber](http://www.daringfireball.net) for the brilliant [markdown](http://daringfireball.net/projects/markdown) document format, and to [iText Software Corp.](http://itextpdf.com) for making an excellent easy to use PDF library and making it available as open source. 

### Version history

#### 1.2.6

Added the new _.mddoc_ format, which makes command line useage easier, but it is also supported by the maven plugin and the library has a utility that completely handles this format.

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

* No HMTL pass-through! Well, there is a small exception to that. HTML comments are passed along. Mostly because there is no markdown comment format and I wanted to be able to put comments in my documents. ”\&\n\b\s\p\;” is also passed through to create indents that are not code blocks. The reason for no HTML pass-through is that MarkdownDoc takes it directly from markdown to PDF without any HTML rendering in between. The main purpose of this tool is to write documentation not generate HTML sites.

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

