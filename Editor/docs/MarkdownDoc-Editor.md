# MarkdownDoc Editor

This is an editor for editing markdown documents. 

## Features

### New in 1.3

Can markdown format realtime as you are editing! Headings, bold, italics, and monospaced are formatted. This can be turned off in settings. Formats the whole document on load, and after that reformats only the currently edited paragraph for performance reasons.

Formatting characters can be made very tiny while editing by enabling a settings option. Try it to see the effect!

New default keyboard shortcuts for functions that should be more platform independent, and all keyboard shortcuts can now be configured in settings. 

New settings configurable toolbars: The old in window toolbar, and 2 popup toolbar variants that are only visible when the mouse is moved to the top of the editor window. This also means that you can get a text only distraction free fullscreen (real fullscreen on Mac OS X). 

The settings dialog have been redone and now features tabs for different groups of settings, and much better and smarter layout of content in settings dialog. 

Special feature: When in preview mode and you drop a markdown document on the window the dropped markdown document will be formatted and displayed without affecting the currently edited document. When you exit preview mode you have your original document there again, and can preview it again. This is a convenience function to quickly be able to read markdown documents formatted by dropping them on the window in preview mode. 

### Previous version

Toolbar buttons for markdown formats. The toolbar can be dragged to any side of the window. Each toolbar button has a keyboard shortcut (shown in tooltip). 
      
Can switch back and forth between editing and preview mode. The preview will try as much as it can to show the same section of the document as the cursor were at att the time of the previev.

Can generate a PDF document directly from your markdown. Supports table of contents and a title page. All generate settings for a documents are remembered.

Provides list support. Pressing &lt;Return&gt; on a line starting with a list bullet (\*) will automatically produce a new bullet on the next line at the same indent level. Pressing &lt;Return&gt; again without writing anything for the bullet will remove the bullet. Pressing &lt;Tab&gt; on a list bullet line will indent the line 3 spaces. The whole line will be indented and it does not matter where on the line the cursor is. Pressing &lt;Shift&gt;-&lt;Tab&gt; on a list bullet line will do the opposite of &lt;Tab&gt;, unindenting the line.

Provides quote support. Pressing &lt;Return&gt; on a line starting with a quote (&gt;) will produce a new quote (&gt;) on the next line. Pressing &lt;Return&gt; again without writing anything will remove it.

You can modfiy visuals to your liking in settings. Any changes made there are reflected in the editor immediately! If you cancel the revert back to what you had before.

You can load markdown files by drag and drop onto the editing window.

Can be run with java -jar or double clicking the jar on most platforms.

Supports fullscreen on Mac OS X.
  
## Executables

MarkdownDocEditor-n.n.n-App.jar

