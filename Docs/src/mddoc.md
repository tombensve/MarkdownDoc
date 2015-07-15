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
    
    ...
    
    # --- HTML ---
    
    # The name of the file to produce.
    html.resultFile: MyDoc.html
    
    # The path to the css file for the generated html file. Required.
    html.css: css/my.css
    
    ...    
    
    # --- Markdown ---
    
    # The name of the file to produce.
    markdown.resultFile: MyDoc.md
    
    # This affects links and images. When specified the resulting file: URLs in the
    # result will be relative to the path specified by "path" if the absolute path
    # of the URL starts with the specified path. If a plus sign (+) and a prefix
    # path is specified it will be prefixed to the final URL. Optional.
    markdown.makeFileLinksRelativeTo: path[+prefix]

As you can see pdf options are prefixed with "pdf.", html options are prefixed with "html.", and markdown options are prefixed with "markdown.". After the prefix are the same options as documented under the "Options / Settings" section. 