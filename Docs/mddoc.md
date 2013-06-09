# The mddoc file type

There is a special file type that describes a complete document in any or all of the 3 output formats. It has the extension of _.mddoc_. It is really a properties file with _key: value_ entries. 

A path to an _.mddoc_ file can be specified as only argument to command line variant, or as only file in `<inputPaths>...</inputPaths>` section in maven plugin (no other options/parameters are needed then) to produce output documents as described by the _.mddoc_ file.

## .mddoc format (myfile.mddoc)

    # --- Generators to run ---
    generate.pdf: true
    generate.html: true
    generate.markdown: true
    
    # A comma separated list of paths to sources. A .fs file can also be
    # specified as an input file in which case it is read for a further
    # set of files to parse. The order of the specified files are important.
    inputPaths: docs/intro.md,docs/install.md,docs/usage.md,docs/appendix.md
    
    # --- PDF ---
    
    # The name of the file to produce.
    pdf.resultFile: MyDoc.pdf
    
    # The page size. For example:A4, LETTER Optional. Default: A4
    pdf.pageSize: A4
    
    # Set to true to generate a title page. Optional. Default: false
    generateTitlePage: true/false
    
    # Set to true to generate a table of contents. Optional. Default: false
    pdf.generateTOC: true/false
    
    # This will be put in PDF metadata and also rendered on title page.
    # Required if generateTitlePage is true optional otherwise.
    pdf.title: MyDoc
    
    # This will be put in PDF metadata and also rendered on title page.
    # Optional.
    pdf.subject: User Guide
    
    # This will be put in the PDF metadata. Optional.
    pdf.keywords: Markdown PDF
    
    # The version of the document. This will be rendered on the title page.
    # Optional, but recommended if generateTitlePage is true.
    pdf.version: 1.0
    
    # The author of the document. This will be put in PDF metadata and also
    # be rendered on title page. Optional.
    pdf.author: Your Name
    
    # This will be rendered on the title page. Optional.
    pdf.copyright: Copyright ?? 2013 Me Myself and I
    
    # If true then links will render as plain text and not be clickable.
    # Optional. Default: false
    pdf.hideLinks: true/false
    
    # Specify this if you want to change the bullet for unordered lists.
    # Optional. Default: ??? (including space after!)
    pdf.unorderedListItemPrefix: *
    
    # Specify true here to have the first line of each paragraph indented.
    # Optional. Default: false
    pdf.firstLineParagraphIndent: true
    
    # Specify in R:G:B format to change the background color of the document.
    # Optional. Default 255:255:255 (white)
    pdf.backgroundColor: 255:255:255
    
    # Specify in R:G:B format to change the text color of block quotes.
    # Optional. Default: 128:128:128 (grey)
    pdf.blockQuoteColor: 128:128:128
    
    # Specify in R:G:B format to change the text color of code blocks.
    # Optional. Default: 0:0:0 (black)
    pdf.codeColor: 0:0:0
    
    # --- HTML ---
    
    # The name of the file to produce.
    html.resultFile: MyDoc.html
    
    # The path to the css file for the generated html file. Required.
    html.css: css/my.css
    
    # If set to true then the specified css will be inlined in the generated html
    # document. Otherwise the generated html document will reference the specified
    # css. Optional. Default: false
    html.inlineCSS: true/false
    
    # This affects links and images. When specified the resulting file: URLs in the
    # result will be relative to the path specified by "path" if the absolute path
    # of the URL starts with the specified path. If a plus sign (+) and a prefix
    # path is specified it will be prefixed to the final URL. Optional.
    html.makeFileLinksRelativeTo: path[+prefix]
    
    # --- Markdown ---
    
    # The name of the file to produce.
    markdown.resultFile: MyDoc.md
    
    # This affects links and images. When specified the resulting file: URLs in the
    # result will be relative to the path specified by "path" if the absolute path
    # of the URL starts with the specified path. If a plus sign (+) and a prefix
    # path is specified it will be prefixed to the final URL. Optional.
    markdown.makeFileLinksRelativeTo: path[+prefix]

