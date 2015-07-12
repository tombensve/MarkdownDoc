<!--

    As of version 1.4 it is possible to specify a lot of the PDFGenerator options as annotations
    within a comment block. This should be at the top of the document or it can have side effects.

    @PDFTitle("MarkdownDoc")
    @PDFSubject("User Guide")
    @PDFKeywords("markdown MarkdownDoc mdd_version_1.4")
    @PDFVersion(1 . 4) Due to the font used I put a space between to make the dot more visible.
    @PDFAuthor("Tommy Svensson")
    @PDFCopyright("Copyright (C) 2012 Natusoft AB")
    
    @PDFPageSize("A4")
    @PDFGenerateTitlePage(true)
    @PDFGenerateTOC(true)
    @PDFGenerateSectionNumbers(false)

-->

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

Binaries are made available at [Bintray](https://bintray.com/tommy/maven/MarkdownDoc/view) and Bintrays JCenter repository: http://jcenter.bintray.com which should also contain everything in maven central.

### Thanks

Thanks to [John Gruber](http://www.daringfireball.net) for the brilliant [markdown](http://daringfireball.net/projects/markdown) document format, and to [iText Software Corp.](http://itextpdf.com) for making an excellent easy to use PDF library and making it available as open source.

### Version history

About versions, they are hell! After personal experience of having different versions for each module / produced jar which was close to impossible to keep track of which was compatible with which, I decided to go with just one and the same version for all modules of the tool. This has the side effect of changes in any submodule, even the editor, which probably not everyone uses, will change the version for all even though no changes have been done for some modules. What have changed for each version is documented below so that you can determine if upgrading to the latest version is wanted/needed or not.

#### 1.4

* Added support for what I call _Markdown Style Sheet_ or MSS for short. This is only applicable to PDF generation. For HTML there is CSS, and generating CSS from the MSS is a bad idea. The MSS is relatively simple an JSON based. This now gives the users of MarkdownDoc full control over styling of PDF documents, that is colors and fonts. It also supports ttf, otf, and any other format supported by iText for external fonts.

* Added support for `<div class="..">...</div>`. This tool is mainly for writing documentation and generating PDF, but I wanted to add more flexibility for generating HTML pages also. Even though you probably want to keep a common style throughout a document, I did add div support to MSS. Divs within divs inherit styles upward. This was relatively simple to do.

#### 1.3.9

Only bugfix in editor when generating HTML directly from editor, which caused an NPE.

#### 1.3.8

Bad internal version dependencies in well ... probably from version 1.3.4 up to 1.3.7. The markdowndoc-maven-plugin were using a too old (hardcoded!!) version of markdown-doc-lib, which is the core of MarkdownDoc! It was pointing to version 1.3.3. This means that fixes in 1.3.4 and 1.3.5 were not available when maven plugin was used! It now uses ${project.version}. The command line jar and the editor have had the correct version dependency.

Very sorry for this!

#### 1.3.7

* Bugfixes in the maven plugin.

* The maven plugin also no longer has any runtime dependency on CodeLicenseManager which is a build only plugin, something maven does not really distinguish.

* Includes a pull request submitted by both komarevsky and iorixxx that fixes an XML error in an example in the user guide. Thanks for seeing that and submitting pull requests!

#### 1.3.6

Bug fixes in MarkdownDocEditor:

- Preformatted styling should now behave correctly.

- Preformatted font (monospace) settings now work. Also defaulted font size of monospace to 14 rather than 16.

#### 1.3.5

What I did not mention in the information for version 1.3.4 is that the editor was converted from Java to Groovy. Here I apparently ran into a Groovy gotcha: What looked to be a member reference were actually a property reference to the same method that tried to reference member. In this case it was an anonymously implemented interface with a getter whose implementation tried to reference the outer class member of same name as getter property, and got the property rather than the member causing a never ending loop resulting in java.lang.StackOverflowError.

This affected only generating of PDF and HTML. The error occured after writing generated output, but before opening the generated output (when told to do so by checkbox setting). This problem is now fixed by this version and is the only thing that differs from previous version.

#### 1.3.4

Fixed a bug with relative path for images using _PDFGenerator_ reported by Maher Gamal. There are now 5 ways to specifiy paths to images for PDF:

1. Absolute path
2. Relative to current directory.
3. Relative to markdown document.
4. Relative to resulting PDF document.
5. Relative to a supplied root dir. This can now be specified in the PDF generator options. If using the library, passing rootDir will override the options rootDir.

These paths will be automatically resolved.

#### 1.3.3

Ironed out all _known_ bugs in editor.

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

* No HMTL pass-through! Well, there is a small exception to that. HTML comments are passed along. Mostly because there is no markdown comment format and I wanted to be able to put comments in my documents. "\&\n\b\s\p\;" is passed through to create indents that are not code blocks. `"<div class=\"...\">...</div>"` is also passed through. The start and end div has to be on their own lines! The reason for no general HTML pass-through is that MarkdownDoc takes it directly from markdown to a document model which is then used to generate PDF without any HTML rendering in between. The main purpose of this tool is to write documentation not generate HTML sites (though that has become easier in version 1.4 with the div support).

* Escaping with '\\'. In MarkdownDoc you can escape any character with \\ and it will be passed through as is without being acted on if it has markdown meaning.

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

