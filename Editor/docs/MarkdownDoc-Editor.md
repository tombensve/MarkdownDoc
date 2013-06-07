# MarkdownDoc Editor

This is an editor for editing markdown documents. 

## Features

* Toolbar buttons for markdown formats.
   * The toolbar can be dragged to any side of the window.
   * Each toolbar button has a keyboard shortcut (shown in tooltip):
      * File
         * Save: Meta-S
         * Open: Meta-O
         * New Document: Meta-N
      * Format
         * Heading: Meta-T
         * Bold: Meta-B
         * Italics: Meta-I
         * List: Meta-L
         * Quote: Meta-K
         * Image: Meta-M
         * Link: Meta-N
      * Preview: Meta-P
      * Generate
         * PDF: Ctrl-P
         * HTML: Ctrl-H
      * Settings: Alt-S
   * Please note that on Windows the \<Meta\> key is the \<Windows\> key, and on Mac it is the \<Cmd\> key.
* Can switch back and forth between editing and preview mode, either through toolbar button och Meta-P. 
   * The preview will try as much as it can to show the same section of the document as the cursor were at att the time of the previev.
* Can generate a PDF document directly from your markdown. Supports table of contents and a title page.
   * All generate settings for a documents are remembered.
* List support
   * Pressing \<Return\> on a line starting with a list bullet (*) will automatically produce a new bullet on the next line at the same indent level. Pressing \<Return\> again without writing anything for the bullet will remove the bullet.
   * Pressing \<Tab\> on a list bullet line will indent the line 3 spaces. The whole line will be indented and it does not matter where on the line the cursor is.
   * Pressing \<Shif\t>-\<Tab\> on a list bullet line will do the opposite of \<Tab\>, unindenting the line.
* Quote support
   * Pressing \<Return\> on a line starting with a quote (\>) will produce a new quote (\>) on the next line. Pressing \<Return\> again without writing anything will remove it.
* Can modfiy visuals to your liking in settings. Any changes made there are reflected in the editor immediately! If you cancel the revert back to what you had before.
* Can be run with java -jar or double clicking the jar on most platforms.
* Mac OS X Support:
   * Full screen.
   * .app packaging.

## Executables

MarkdownDocEditor-n.n.n-App.jar (any platform) or MarkdownDocEditor-n.n.n.app (for Mac).

## Bugs

### By Apple

* When the editor is run through the .app packaging fullscreen does not work!

* This being a Java application the .app packaging uses Apples JavaApplicationStub to start the java application. It however refuses to pass along any parameters so associating this editor with .markdown files and double clicking on a markdown file will open this editor but not with the file!

