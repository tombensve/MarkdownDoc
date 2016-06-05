# MarkdownDoc Editor

This is an editor for editing markdown documents. 

<div class="editorImage">

![](http://download.natusoft.se/Images/MarkdownDoc/MarkdownDoc-Editor-2.png) 

</div>

## Features

### Styling as you type

Can markdown style as you write. Headings, bold, italics, and monospaced are styled. This can be turned on/off in settings. Styles the whole document on load, and after that restyles only the currently edited paragraph due to performance reasons. There is a function (default Ctrl+R) that restyles the whole document. This is also done on paste if you have mapped the correct paste key in Settings/Keyboard.

### HTML Preview

Can preview in HTML format (toggles between preview and edit mode). This is activated by a toolbar button (the eye) and a keyboard shortcut.

### Editing effects

Can make formatting characters to be made very tiny while editing, by enabling a settings option. Try it to see the effect!

### Undo / Redo (as of 1.4)

Attaches an UndoManager to the document model. Ctrl-Z does an undo, and Ctrl-Y does a redo for all platforms except Mac, which uses Meta-Z for undo, and Shift-Meta-Z for redo.

Do note however that if you enable styling as you type in settings, then the styling actions also gets recorded by the UndoManager! So the first Ctrl-Z might just undo the styling, and the next Ctrl-Z does what you expected it to do. I have currently not found a way around this.

### Generate PDF & HTML

Can generate both PDF and HTML from the editor window. Use toolbar button or keyboard shortcut.

### Configurable

The settings dialog allows you to configure almost anything/everything:

- All keyboard shortcuts. 

   - Don't write the keyboard shortcut in text, just press the keyboard shortcut you want to set.

- Margins.

- Editor font.

- Monospaced font.

- Preview font.

- Font sizes.

- Background color.

- Text color.

- Toolbar variant to use.

### Load file by drag & drop

Instead of using the GUI open dialog you can just drag and drop a file in the editor to edit it.

### Special preview drag & drop feature

While in preview mode, drag and drop a markdown file on the preview window to have it formatted and displayed. This does not affect the edit buffer in any way. Exiting preview mode will bring you back to whatever you have in the editor, and previewing again will preview the editor content. 

But by just opening an empty editor and entering a blank preview you can quickly read multiple markdown documents formatted by just dropping them on the window.

### Fullscreen support

When in fullscreen mode then the settings popup will still popup to the right, but on top of the editor window, so it will still work in that mode. 

#### Mac OS X

When you run this editor on a Mac with Lion+ you will get a litte double arrow in the right corner of the window titlebar, or with Mavericks+ it will be the green dot, which will bring upp the editor window in fullscreen. 

#### Windows (10)

On Windows10 pressing the square button on the top right side of the window will enter some kind of fullscreen. 

#### MarkdownDocEditor.app

The editor is also available in MarkdownDocEditor.app format. The build plugin that creates this .app packaging does however not support passing on arguments to the app when run this way. That means selecting a markdown file and doing "open with" will fail. It will always open the file chooser for you to select the file(s) to edit. 

Also note that since this .app is not signed, Mac OS X Mavericks and upp will not allow you to run the app if you do not open upp for running all types of apps in the security settings.   

Due to GUI bugs in the editor component of earlier versions of Java, Java 1.8 or higher is required to be installed on your system for the app to be able to run, assuming you are using the version I'm providing for download in the README.md document. If you checkout the source and build yourself then it will require Java 1.6 and upp. But be warned: You will be annoyed if you use Java lower than 1.8!

### Version 1.4 usage changes

In prevous versions there were only one file per window and you could open multiple windows. Now there is only one window and you can open multiple files and select which file to work with in the editor window. It is like a tabbed window, but instead of tabs there is a popup list of all open files and you click on the one you want to edit. The reason for this is to have editing distraction free. When you are writing, you only have the text you are working on and nothing else in the window. This works even better with full screen. 

Since the editor now has become very file oriented you can no longer edit an unknown file that you specify a file for later when you save. So when you press the "+" toolbar icon or start the editor without any files specified then a file chooser will popup. In this case you can either navigate to a directory and then enter a name for a new file and it will be created or you can specify existing files and they will be opened. Do note that since you can open multiple files at the same time, the opened files does not necessarily become the current edited file visible in the window! But if you open the files poup you will get the newly opened files in the file list. 
  
## Running

Can be run with java -jar or double clicked on. If you are using Windows 7 or 8 take a look at this page: [http://johann.loefflmann.net/en/software/jarfix/index.html]
(http://johann.loefflmann.net/en/software/jarfix/index.html). 

The executable jar have the following name: MarkdownDocEditor-n.n.n-App.jar

One or more files or directories can be specified as arguments. For a directory all markdown files found in the directory and subdirectories will be loaded. As said above this does not apply if you are running the .app version.

## Requirements

This requires Java 7+!

<div class="imageLeft">

## Functions

This section documents the different functions of the editor, and how to trigger the function.

The images are the toolbar icon for the function. Not all functions install themselves in the toolbar.

### Bringing upp the toolbar

Move the mouse to the top of the editor window and the toolbar will automatically popup. Move the mouse down again and it will go away. 

### Save file(s)

![](http://download.natusoft.se/Images/MarkdownDoc/mdd2save.png) 

Default key: Ctrl+S. This is changeable in the settings. 

This saves all open files that have been modified and not saved. A small pupup appears for a short while in the upper left corner of the window to indicate how many files were saved. If it the number is 0 then there were no modified files needing save.

### Open file

![](http://download.natusoft.se/Images/MarkdownDoc/mdd2open.png)

Default key: Ctrl+O. This is changeable in the settings.

This opens a file chooser to select one markdown file to open. The opened file will be selected for editing in the window.

This function is kind of unneccesarry in this version, but I decided to leave it in anyhow. It differs slightly from Open/Create. It is likely to go away in future versions.

In addition to markdown files the open function will also allow opening the same .fs files as the maven plugin can use. In this case all references to markdown files in the .fs file will be opened.

### Open / Create

![](http://download.natusoft.se/Images/MarkdownDoc/mdd2new.png)

Default key: Ctrl+N. This is changeable in the settings.

This opens a file chooser where you can also enter a filename in the chooser dialog. Here you can navigate to a directory, and then enter the name of a new file that will then be created and opened. In this file chooser you can alternatively navigate to a directory and then select one or more existing files and have all selected files being opened. 

Since there can be more than one file, no file is set as the current edited in the window. You have to bring up the list of open files and select one of the newly added files to edit it. 

The exception to this is when you have started the edtior without any files, which will triger this file chooser then one of the selected files will become the edited file since there always have to be one file in the editor.

In addition to markdown files the open function will also allow opening the same .fs files as the maven plugin can use. In this case all references to markdown files in the .fs file will be opened.

### List of open files popup

![](http://download.natusoft.se/Images/MarkdownDoc/mdd2openfiles.png)

Default key: Ctrl+1. This is changeable in the settings.

This is new in version 1.4.2. This opens the popup window that allows for selecting which open file to work on. This was previously triggered by moving the mouse to the left window edge. 

### Insert heading

![](http://download.natusoft.se/Images/MarkdownDoc/mdd2heading.png)

Default key: Ctrl+T. This is changeable in the settings.

This just adds a # character which is the markdown heading character. Insert as many as the heading level you want, max 6.

### Insert bold

![](http://download.natusoft.se/Images/MarkdownDoc/mdd2bold.png)

Default key: Ctrl+B. This is changeable in the settings.

This adds 4 '\_' characters with the cursor placed between the first 2 and the last 2. 2 underscores before and after makes bold text in markdown. 2 asterisks before and after does the same thing, but the editor uses underscores for this specific help function. 

### Insert italics

![](http://download.natusoft.se/Images/MarkdownDoc/mdd2italics.png) 

Default key: Ctrl+I. This is changeable in the settings.

This adds 2 '\_' characters with the cursor placed between them. 1 underscore before and after makes italic text in markdown. 1 asterisk before and after does the same thing, but the editor uses underscores for this specific help funciton. Asterisks also means other things in markdown so underscores in this case is less confusing. 

### Insert list

![](http://download.natusoft.se/Images/MarkdownDoc/mdd2list.png)

Default key: Ctrl+L. This is changeable in the settings.

This adds and asterisk and a space which is how you make a list entry for an unordered list in markdown. Do note that it is also possible to make a numbered list, in which case you replace the asterisk with a number like 1. See the markdown reference section of this document for more information.

### Insert quote

![](http://download.natusoft.se/Images/MarkdownDoc/mdd2quote.png)

Default key: Ctrl+Q. This is changeable in the settings.

This inserts a '>' character and a space which is how you make quoted text in markdown.

### Insert image

![](http://download.natusoft.se/Images/MarkdownDoc/mdd2image.png)
 
Default key: Ctrl+M. This is changeable in the settings.

This function will open a small popup window where you can enter 3 pieces of information: 1) An alt text. 2) A URL to the image. 3) An image title. Only the URL is required. When you press the "Insert" button in the popup window, then the image reference will be inserted into the text in markdown format:  `![Alt text](url "title")` .

### Insert link

![](http://download.natusoft.se/Images/MarkdownDoc/mdd2link.png)

Default key: Ctrl+N. This is changeable in the settings.

This function will open a small popup window where you can enter 3 pieces of information: 1) The link text. 2) The link URL. 3) A link title. You should provide a link text and an URL as minimum. When you press the "Insert" button in the popup window, then the link will be inserted into the text in markdown format: `[link text](url "title")`. 

### Preview

![](http://download.natusoft.se/Images/MarkdownDoc/mdd2preview.png)

Default key: Ctrl+F. This is changeable in the settings.

This will format the markdown in the editor into HTML and show it in readonly mode instead of the editable content. To go back to editing again do a Ctrl+F again or use the toolbar button. Do note that while in preview mode it is possible to drag and drop markdown files into the window to have them previewed. This does not affect what you are editing in any way. When you go back to edit mode again your edited text will be there and a new preview will preview that text. 

_Please also note that the preview HTML rendering is done by the Java GUI library (Swing) component JEditorPane. This is far from an optimal HTML renderer! It can make things look strange sometimes. It also insists on indenting the beginning of every code block. If anyone knows of a free, open source, swing compatible HTML renderer, that is better please let me know._

### Generate PDF

![](http://download.natusoft.se/Images/MarkdownDoc/mdd2pdf.png)

Default key: Ctrl+P. This is changeable in the settings.

This will first open a file chooser to select target PDF file to generate to. Then a popup window with meta data for the PDF generation will open. 

Press the "Generate" button to actually generate the PDF document. 

![](http://download.natusoft.se/Images/MarkdownDoc/PDFOptions.png)

The choices are:

##### Page size

This is one of the standard paper sizes like A4 or Letter. 

##### Title

This is the title of the document. This will be shown on the front page.

##### Subject

This is an optional subject / subtitle. This will be shown on the front page.

##### Keywords

A space separated set of keywords. These will not be shown anywhere, but will be added as meta data to the PDF document.

##### Author

The author of the document. This will be shown on the front page.

#####  Version

The current version of the document. This will be shown on the front page.

##### Copyright year

The year of copyright. This will be shown on the front page in the format: "Copyright (C) {year} by {by}".

##### Copyright by

The one holding the copyright. 

##### Generate section numbers

When this is selected numbers are generated for each heading. For example: 1, 1.1, 1.3.5, ... This is common for professional documents.

##### Generate title page: 

This will produce a first page with a document title, subject, author, version and copyright.  

##### Generate TOC

This will generate a table of contents. This will come after the title page if that is generated, but before the document. 

##### Open result

If this is selected then the generated PDF will be opened by the system default PDF viewer.

### Generate HTML

![](http://download.natusoft.se/Images/MarkdownDoc/mdd2html.png)

Default key: Ctrl+H. This is changeable in the settings.

This will first open a file chooser to select HTML file to generate to. Then a popup window will be opened containing meta data for generation of the HTML.

![](http://download.natusoft.se/Images/MarkdownDoc/HTMLOptions.png)

##### Inline CSS

If you select this option then the CSS file you point out will be included within the generated HTML file. 

##### CSS file

This is the CSS file to use. Write a path to the CSS file or use the "Select" button to open a file chooser. This is optional and can be skipped, but the resulting HMTL can be rather boring if you don't provide a CSS.

##### file: links relative to

This is a path that file: links in the document isrelative to. This is used to resolve local filesystem images. 

##### Open result

If this is selected then the generated HTML will be opened by the system default browser. 

### Setting

![](http://download.natusoft.se/Images/MarkdownDoc/mdd2settings.png)

Default key: Ctrl+E. This is changeable in the settings.

This opens the settings popup window where you can configure keys, margins, colors, etc. 

### Restyle document

Default key: Ctrl+R. This is changeable in the settings.

This will force a complete restyling of the whole document. 

### Restyle on paste

Default key: Ctrl+V. This is changeable in the settings.

This also forces a restyle of the document, but when paste of text into the document is done. For this to work it must be mapped to the same key as is used for paste. On windows and linux it is Ctrl+V, on Mac it is Cmd+V. This function can be disabled by setting the key to something else than the paste key.

### Goto next open file

This is an alternative to the above function and allows you to jump around the open files. Each time this function is triggered the editor window will switch content. Internally the editor just keeps a list of all open files, and this just jumps to the next file in that list until the last file is reached and it jumps to the first instead. This function will also show the name of the current "jumped to" file at the bottom of the window just like when you move the mouse up to the toolbar area.

Note that this function has no default key configured and must be set in the settings window before it can be used. On Mac Alt+Tab works since it is not used by the system, on Windows and Linux you probably need some other key combination. 

This function is new in version 1.4.1.

</div>

## If you're on a Mac

If you are on a Mac you might want to change the keyboard mappings to use Cmd rather than Ctrl. Do note however that Cmd+H and Cmd+Q are really nasty on Mac OS X! Since these keys immedialtely kills the app these keys are impossible to set in the first place, but you will loose other unsaved settings when you try.

## Currently Missing

Fancy functions like search and replace.

## Laptop power warning

The markdown styling as you type in the editor do pull some CPU since basically the whole paragraph needs to be reformated on every key. My Mac Book Pro marks this app as a heavy energy user. To minimize battery drain you can turn off the styling as you type in the settings.

## Bugs

### By me

All _known_ bugs have been fixed.

### By Oracle

This editor uses the standard Swing component JTextPane. This is unfortunately not an optimal component. Specially for styling it gets slow for large documents. In earlier versions of Java 7 this component had a word wrap problem when deleting text either using backspace or cutting text. In that case it rerendered the text screwing up the format until new text was entered again. _As of Java 8 this bug is fixed_, but other new bugs have been added. They are however smaller and don't occur so often.

Sometimes when the JTextPane is opened the pane will not render at all! Just increase the width of the window util text appears. Then save so that the window size for that file will be remembered. I have one and only one document for which this happens and I cannot tell what it is that causes the problem. This could be a mac only problem. 

### Other

On Windows 7 generating a PDF within the editor throws a NullPointerException! This does not happen on Mac OS X, Linux, nor Windows 10. I see this as a Windows 7 bug. 

