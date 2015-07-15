<!--

    As of version 1.4 it is possible to specify a lot of the PDFGenerator options as annotations
    within a comment block. This should be at the top of the document or it can have side effects.

    @PDFTitle("MarkdownDoc")
    @PDFSubject("User Guide")
    @PDFKeywords("markdown MarkdownDoc mdd_version_1.4")
    @PDFVersion(1 . 4) Due to the font used I put a space between to make the dot more visible.
    @PDFAuthor("Tommy Svensson")
    @PDFCopyright("Copyright (C) 2012 Natusoft AB")
    @PDFTitlePageImage("Docs/src/MDD_Laptop_2_Fotor.png:200:320")
    
    @PDFPageSize("A4")
    @PDFGenerateTitlePage(true)
    @PDFGenerateTOC(true)
    @PDFGenerateSectionNumbers(false)

-->

# MarkdownDoc User Guide

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

Binaries are made available at [Bintray](https://bintray.com/tommy/maven/MarkdownDoc/view) and Bintrays JCenter repository: <http://jcenter.bintray.com> which should also contain everything in maven central.

### Thanks

Thanks to [John Gruber](http://www.daringfireball.net) for the brilliant [markdown](http://daringfireball.net/projects/markdown) document format, and to [iText Software Corp.](http://itextpdf.com) for making an excellent easy to use PDF library and making it available as open source.

### How markdown is MarkdownDoc ?

Well, it implements the "specification" as documented on [daringfireball.net](http://daringfireball.net/projects/markdown/syntax).
This specification however is not extremely exact so there might be some differences.

The known (and intentional) differences are:

* No HMTL pass-through! Well, there is a small exception to that. HTML comments are passed along. Mostly because there is no markdown comment format and I wanted to be able to put comments in my documents. "\&\n\b\s\p\;" is passed through to create indents that are not code blocks. `"<div class=\"...\">...</div>"` is also passed through. The reason for no general HTML pass-through is that MarkdownDoc takes it directly from markdown to a document model which is then used to generate PDF without any HTML rendering in between. The main purpose of this tool is to write documentation not generate HTML sites (though that has become easier in version 1.4 with the div support).

* Escaping with '\\'. In MarkdownDoc you can escape any character with \\ and it will be passed through as is without being acted on if it has markdown meaning.

* No entity encoding of email addresses.

* No multiple block quote levels (as of now). I've never personally missed having multiple quote levels, which is why I haven't done something about that yet.  

* Does not support any other formatting within strong, emphasized, or header. I personally don't see enough of a problem with this, that I'll prioritize it. 

If you find any of the missing features a problem, I'll happily accept pull requests. :-)

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

