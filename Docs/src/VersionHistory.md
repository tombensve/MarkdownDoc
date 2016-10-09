
# Version history

About versions, they are hell! After personal experience of having different versions for each module / produced jar which was close to impossible to keep track of which was compatible with which, I decided to go with just one and the same version for all modules of the tool. This has the side effect of changes in any submodule, even the editor, which probably not everyone uses, will change the version for all even though no changes have been done for some modules. What have changed for each version is documented below so that you can determine if upgrading to the latest version is wanted/needed or not.

## 2.0.0

PDFBox is now used instead of iText to generate PDF. This required some non backwards compatible changes so thereby the version is bumped to 2.0.0. Note that the incompatibilities are small, and most likely this version will work without changes for many.

- Keywords are gone. 

- Footer is no longer supported. Can be added if enough wants it. I have had no use for it myself.

- pageSize is no longer an option, but an MSS setting. This was a decision I made due to now being responsible for all rendering on the page and thus having more control over things like margins (now also settable in MSS), etc.

- There is a difference in image types handled.  iText supports JPEG, JPEG2000, GIF, PNG, BMP, WMF, TIFF, CCITT and JBIG2 images. I can't find a clear list of image types supported by PDFBox (which in general is bady documented, I had to use Stack Exchange a lot!), but from MarkdownDoc:s perspective those image types supported by AWT are supported. The image types supported by PDFBox, not using AWT, like TIFF are not supported since that API only allows loading images from disk! This works badly together with URLs. Yes, it would be possible to download an image to disk first, then pass it to the API, and then delete it locally or cache it for later reuse. But I decided agains that now. 

- The "hr" MSS value for headings have been renamed "underlined", which is by far more clear. This has nothing to do with anything else, just a decision I made since other things have been changed, why not fix this also. I also added an "underline\_offset" to set how many points below the text to draw the underline. 

- Page size is no longer supplied as an option! This is now set in the MSS file used. Default is A4. Margins now defaults to what I can determine from googling is the default for A4: 2.54 cm. These can also be set in MSS.

- I no longer use labels like "Author:" (on front page) or "Page" before page number, etc. I don't miss them, and it does not look strange without them IMHO. This also means the "label" settings for these texts are not needed.

I added some features in MSS:

- Boxed. Current default.mss uses this for _code_ style. A box of choosen color is rendered below text.

- Positioning of images on page.

- Allowing text to flow around images. When an image is added to a page a "hole" the size of the image is defined in the page, and any text rendering will skip the hole and continue after it. This is optional behavior. 

- Setting page size (A4, LETTER, etc).

- Overriding default page margins. 

See the MSS section of the documentation for more info.

The reason for this change is that I discovered that iText is using a GPL license! Now you might think, "What the heck is he talking about ?, the GPL license text have been included in the docs all the time!". Well, that information is generated automatically by another of my tools: CodeLicenseManager. It finds all licence information in pom:s and include license texts. I haven't looked that closely at what licenses are included. Obvioulsy I should have. It however hit me this summer and I decided to go looking for antother Java PDF library, and found Apache PDFBox. PDFBox is of course under the very sensible "Apache Software License 2.0",
the same license I'm releasing MarkdownDoc under. I suspect that the way the GPL is used today was not the intention of Mr Stallman. The GPL nowmore tends to make non free software look free, and that is exactly how iText is using it.

<!-- @PageBreak -->

PDBox however have some pluses and some minuses:

### +

Lower level, closer to PDF. This gave me much more flexibility and I can now generate everything only once since I now can insert the table of contents at the top of the document after generating the contents, which is needed to get the page numbers for the table of contents. With iText I had to make a dummy generation to a null stream first, just to get page numbers. 

Since it is so low level it does not have the type of bugs that iText have. Now all bugs should be mine :-). That is good since then I can do something about them. 

It was now easy to render boxed backgrounds for preformatted text. I always wanted to do that, but I could not figure out how to do it with iText since iText never gave me the coordinates of the text. Now I have full control over the coordinates of everything. 

### -

PDFBox is slower than iText especially when images are used.

PDFBox unfortunately uses AWT for handling most images! This has consequences! Whenever PDFBox is dealing with a PNG, JPG, etc a small window is opened. It is of course closed again when it is done with the image handling. But if run on a server to generate som PDF report then the server process needs access to an X server if running on a unix system! This is however only if images are used.  

### JDK Level

This version is built with JDK 1.8! The Groovy code might still produce 1.5 compatible bytecode, but the maven plugin is written in Java and thus requires 1.8+ to run. The editor also have less bugs when run with 1.8. 1.7 went 6 feet under over a year ago, so you shouldn't be using antything lower than 1.8 anyhow.

## 1.4.4

### Editor

The settings poppup is now popped up to the right of the window, not over it, unless in fullscreen mode then the popup will still be to the right, but on top of the editor window obviously :-). This makes it easier to se the result when playing with settings.

## 1.4.3

### Library

Implemented a suggestion from Mikhail Kopylov that also allows images to be referenced relative from the .md file they are part of.

Added information about options for referencing images in _MarkdowndDoc_ under the _Markdown Reference_ section.

## 1.4.2

### Editor

The editor has been updated:

* The popup window of all open files is no longer accessable by moving the mouse pointer to the left side of the window. This was a really bad idea! It was very easy to unintentionally trigger this popup. This function has now gotten an icon and been added to the toolbar instead. It already had a keyboard shortcut before and still does.

* All toolbar icons have been redone.

* It is now also possible to open the same .fs files as supported by the maven plugin. All markdown documents referenced in the .fs file will be opened in the editor. If an .fs file also points out a java source file for javadoc parsing it will be ignored. Only markdown documents are opened.

## 1.4.1

Only fixes in editor!

* The popup windows now only popup over the editor window. The fullscreen popups worked badly on different platforms which reserves different parts of the screen.

* Disabled rounded corner popup windows since they also worked with different quality on different platforms.

* Added editor function to _Alt-Tab or something_ around opened files in the editor. For this to work at all you need to open configuration and set a keyboard compination that triggers this. There is no failsafe default that works on all platforms. The config is called "content switch keyboard shortcut".

## 1.4

* Added support for what I call _Markdown Style Sheet_ or MSS for short. This is only applicable to PDF generation. For HTML there is CSS, and generating CSS from the MSS is a bad idea. The MSS is relatively simple and JSON based.

  * It supports ttf, otf, and any other format supported by iText for external fonts.

  * It allows for image configuration like scaling, rotating, and alignment. Before all images were alinged to the left. Now they can be alingned to the  left, middle, or right. In previous versions all images was scaled to 60 percent due to iText rendering images very much bigger than any other image viewer (that I have at least).  This scaling can now be set with MSS.

* Added support for `<div class="..">...</div>`. This tool is mainly for writing documentation and generating PDF, but I wanted to add more flexibility for generating HTML pages also. Even though you probably want to keep a common style throughout a document, I did add div support to MSS. Divs within divs inherit styles upward. This was relatively simple to do. Note that the "Options / Settings" section uses a div with slightly different formatting than the rest of the document. Each option is a level 3 heading (H3) which is why it is part of the TOC, but styled with a smaller font with a different color.

* Added possibility to also have an image on the title page.

* Added annotations within a comment block. Most of the options for the PDF generator can now be specified with annotations in the document. For example `@PDFPageSize("A4")`. This means for example that the title page can be part of the document. This comment with annotations should preferrably be the first thing in the document. The reason for the very Javaish format of the annotation is that it is explicit enough to not be accidentally and unwantedly matched.

* Added labels in options for all previously hardcoded text strings in PDFGenerator. It should now be possible to completely generate a document in a different language than English. These can also be set with comment annotations as mentioned above.

* Added Undo / Redo to editor. Swing apparently provides support for this for a JTextPane/JEditorPane, you just have to register an UndoManager. __However__, it reacts on all changes, including styling. Since styling is applied afterward, and on the whole paragraph since it is not only about the current character, this is also registered as a change in the undo manager. So undos will undo styling also. So after undoing what you wanted to undo, do a Ctrl/Cmd+R to fix styling again.

* Editor updated quite a lot!
   * All _known_ bugs fixed.
   * Now handles multipe files in one editor window. It actually only supports one window now, but you switch between the open files by moving mouse to the left window egde which will popup a list of all open files and you just click on the one you want to work on.
   * It is possible to specify a directory as input to editor. In this case it will scan the directory for markdown files and open all found. It is actually possible to specify multiple directories just as it is possible to specify multiple files.
   * When the toolbar is shown at the top of the window, the name of the current file is also shown att the bottom of the window.
   * The editor GUI has gone though some cosmetics.

The addition of MSS made huge changes to the code. To be as backwards compatible as possible, the defaults for the MSS settings are as things looked before. There is also a _default.mss_ file that gets used if you don't supply your own. This has settings that mimics the previous styles.

Also note that the PDF UserGuide now shows off the new features, mostly for that purpose :-).


## 1.3.9

Only bugfix in editor when generating HTML directly from editor, which caused an NPE.

## 1.3.8

Bad internal version dependencies in well ... probably from version 1.3.4 up to 1.3.7. The markdowndoc-maven-plugin were using a too old (hardcoded!!) version of markdown-doc-lib, which is the core of MarkdownDoc! It was pointing to version 1.3.3. This means that fixes in 1.3.4 and 1.3.5 were not available when maven plugin was used! It now uses ${project.version}. The command line jar and the editor have had the correct version dependency.

Very sorry for this!

## 1.3.7

* Bugfixes in the maven plugin.

* The maven plugin also no longer has any runtime dependency on CodeLicenseManager which is a build only plugin, something maven does not really distinguish.

* Includes a pull request submitted by both komarevsky and iorixxx that fixes an XML error in an example in the user guide. Thanks for seeing that and submitting pull requests!

## 1.3.6

Bug fixes in MarkdownDocEditor:

- Preformatted styling should now behave correctly.

- Preformatted font (monospace) settings now work. Also defaulted font size of monospace to 14 rather than 16.

## 1.3.5

What I did not mention in the information for version 1.3.4 is that the editor was converted from Java to Groovy. Here I apparently ran into a Groovy gotcha: What looked to be a member reference were actually a property reference to the same method that tried to reference member. In this case it was an anonymously implemented interface with a getter whose implementation tried to reference the outer class member of same name as getter property, and got the property rather than the member causing a never ending loop resulting in java.lang.StackOverflowError.

This affected only generating of PDF and HTML. The error occured after writing generated output, but before opening the generated output (when told to do so by checkbox setting). This problem is now fixed by this version and is the only thing that differs from previous version.

## 1.3.4

Fixed a bug with relative path for images using _PDFGenerator_ reported by Maher Gamal. There are now 5 ways to specifiy paths to images for PDF:

1. Absolute path
2. Relative to current directory.
3. Relative to markdown document.
4. Relative to resulting PDF document.
5. Relative to a supplied root dir. This can now be specified in the PDF generator options. If using the library, passing rootDir will override the options rootDir.

These paths will be automatically resolved.

## 1.3.3

Ironed out all _known_ bugs in editor.

## 1.3.2

Added markdown formatting as you write.

## 1.3.1

Bug fixes. Monospaced font now rendering correctly.

Deleting text with backspace have strange effects on text layout. That is, the place where a senetence is broken to the right and moved down to the next line keeps moving around while deleting text, in some completely different paragraph! This is entirely handled by JTextPane. I have tried to find a way to intercept the delete key and handle delete myself, but I have not been successful in finding a way to do that if it is even possible. Continuing writing new text after deleting text seems to restore the layout. This oddity has no effect on the final text, it is just the layout while editing that is affacted. You will also only see this if you write paragraphs as one block of text that wraps around into multiple lines without pressing return until the end of the paragraph.

## 1.3

Made big changes to the editor, finally making it into what I want, with some markdown formatting as you write, and far more configuration in settings dialog, which have also been redone.

Bug fixes.

## 1.2.10

Added support for &amp;lt;, &amp;gt;, and &amp;amp;.

## 1.2.9

Added markdown file reading feature by allowing markdown files to be dropped on the editor in preview mode, in wihch case the dropped file will be formatted and displayed without changeing the content of the editor. Exiting preview and doing a preview again will again preview the editor content.

## 1.2.8

Headings can now **not** be more than one line (not include LF/CRLF). Before they were treated like paragraphs. This to be more compatible with other Markdown tools and Markdown documents.

## 1.2.7

Added settings for specifying top, bottom, left, and right margins in editor. Please note that I've been a bit lazy here. The sizes are in pixels, not characters/lines!

## 1.2.6

Added the new _.mddoc_ format, which makes command line usage easier, but it is also supported by the maven plugin and the library has a utility that completely handles this format.

Added a Java Swing based editor for editing markdown with support.

## 1.2.5

Added _parserOptions_ now used by JavadocParser to markdown parse javadoc comments if markdownJavadoc=true is provided. The Parser API is thus also updated to take a Properties object for the parser options.

## 1.2.4

Added _makeFileLinksRelativeTo_ option for HTMLGenerator and MarkdownGenerator mostly to be able to manipulate _file:_ references to images in the generated result so that the image paths still work in source when editing with a markdown tool and is still correct when generated to a different path.

## 1.2.3

If image paths are not absolute and not http referenced then they are now looked for relative to the source markdown file first, and then the are looked for relative to the result file as before. This makes it easier to generate a big document for a whole project containing several subproject with local makdown documents and referenced images. The image reference can still be relative to the subproject local markdown file.

## 1.2.2

Added support for non breaking space (nbsp) to be able to indent text. This is one more exception to no html pass through.

