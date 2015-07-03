# Maven Plugin

The maven plugin is rather straight forward. It has 3 sets of configuration structures, one
common and one for each generator.

## generatorOptions

There is a config section that is common to all generators and specifys which generator to run
and what input files to include. The following example is from the generation of this manual:

    <generatorOptions>
        <generator>pdf</generator>
        <inputPaths>
            Docs/parts/H1UserGuide.mdpart,
            Docs/MarkdownDoc.md,
            MavenPlugin/docs/.*.md,
            CommandLine/docs/.*.md,
            Library/docs/.*.md,
            Docs/parts/H1Licenses.mdpart,
            Docs/licenses.md,
            Docs/parts/H1LicenseTexts.mdpart,
            Docs/.*-.*.md
        </inputPaths>
        <parserOptions>option=value,....</parserOptions>
    </generatorOptions>

If the `<inputPaths>...</inputPaths>` section only contain one file of type _.mddoc_ then no other parameters need to be specified, not even `<generator>...</generator>`! In this case all information needed to generate final documents resides in the  _.mddoc_ file. See _’The _mddoc_ file type’_ (section 5) for more information on this file type.

The current valid argument for `<generator>...</generator>` are _pdf_, _html_, and _md_.

The input paths are comma separated and are always relative to the root of the maven project. 
To clarify that, for a multi module maven build it is always the top root with the top pom
that is the root even if you start the build at a lower level. This root is resolved by 
starting at _${basedir}_ and going up until the parent directory does not have a pom. I have
found no way to let maven tell me this path. 

The paths can have wildcards in form of regular expressions for the file names. There is
also a special directory name ** that means any level of subdirectories.

All the input paths are parsed into the same document model that then gets passed to the
generator. They are parsed in the order they are specified. When it comes to wildcards
it is hard to say which order they will be in. It might differ on different platforms.

If you are wondering about the `.mdpart` extensions above it is just to hide them from
GitHub. They are just very small header lines that I inject to the PDF document that
I don't want in the main parts of the documentation since they are reused and linked
from the README.md in the root. 

I'm also using this possibility to generate from multiple sources to put the documentation
for each module project in that project. 

The `<parserOptions>option=value,...</parserOptions>` are passed to each parser. Currently only the _JavadocParser_ has an option: `markdownJavadoc=true`. When this is specified then the class and method descriptions are passed to MarkdownParser instead of being added as text. This is useful in conjunction with using a markdown doclet for javadoc.

## pdfGeneratorOptions

Following is a complete plugin specification with all options specified:

    <plugin>
        <groupId>se.natusoft.tools.doc.markdowndoc</groupId>
        <artifactId>markdowndoc-maven-plugin</artifactId>
        <version>n.n[.n]</version>

        <executions>
            <execution>
                <id>generate-docs</id>
                <goals>
                    <goal>doc</goal>
                </goals>
                <phase>install</phase>
                <configuration>

                    <generatorOptions>
                        <generator>pdf</generator>
                        <inputPaths>
                            ...
                        </inputPaths>
                    </generatorOptions>

                    <pdfGeneratorOptions>
                        <!--
                            The path to the pdf document to produce. Path is relative
                            to project root (see comment about root above).
                            Required.
                        -->
                        <resultFile>Docs/MarkdownDoc-User-Guide.pdf</resultFile>
                        
                        <!--
                            The path to an MSS file to use instead of the default one.
                        -->
                        <mss>Docs/doc.mss</mss>
                        
                        <!-- 
                            The page size. For example:A4, LETTER 
                            Optional. Default: A4
                        -->
                        <pageSize>A4</pageSize>
                        
                        <!-- 
                            This will be put in PDF metadata and also rendered on
                            title page.
                            Required if generateTitlePage is true optional otherwise.
                        -->
                        <title>MarkdownDoc</title>
                        
                        <!-- 
                            This will be put in PDF metadata and also rendered on
                            title page.
                            Optional. 
                        -->
                        <subject>User Guide</subject>
                        
                        <!-- 
                            This will be put in the PDF metadata. 
                            Optional.
                        -->
                        <keywords></keywords>
                        
                        <!-- 
                            The version of the document. This will be rendered on the
                            title page.
                            Optional, but recommended if generateTitlePage is true.
                        -->
                        <version>1.0</version>
                        
                        <!-- 
                            The author of the document. This will be put in PDF
                            metadata and also be rendered on title page.
                            Optional.
                        -->
                        <author>Tommy Svensson</author>
                        
                        <!-- 
                            This will be rendered on the title page. 
                            Optional
                        -->
                        <copyright>Copyright © 2012 Natusoft AB</copyright>
                        
                        <!-- 
                            If true then links will render as plain text and not be
                            clickable.
                            Optional. Default: false
                        -->
                        <hideLinks>false</hideLinks>
                        
                        <!-- 
                            Specify this if you want to change the bullet for
                            unordered lists.
                            Optional. Default: • (including space after!)
                        -->
                        <unorderedListItemPrefix>• </unorderedListItemPrefix>
                        
                        <!-- 
                            Specify true here to have the first line of each
                            paragraph indented.
                            Optional. Default: false
                        -->
                        <firstLineParagraphIndent>false</firstLineParagraphIndent>
                        
                        <!--
                            Specify in R:G:B format to change the background color
                            of the document.
                            Optional. Default 255:255:255 (white)
                            
                            NOTE: This is preferably set in a .mss file specified by the mss option.
                                  If set both here and in an .mss file then this will win! (for backwards
                                  compatibility).
                        -->
                        <backgroundColor>255:255:255</backgroundColor>
                        
                        <!--
                            Specify in R:G:B format to change the text color
                            of block quotes.
                            Optional. Default: 128:128:128 (grey)
                            
                            NOTE: This is preferably set in a .mss file specified by the mss option.
                                  If set both here and in an .mss file then this will win! (for backwards
                                  compatibility).
                        -->
                        <blockQuoteColor>128:128:128</blockQuoteColor>
                        
                        <!--
                            Specify in R:G:B format to change the text color
                            of code blocks.
                            Optional. Default: 0:0:0 (black)
                            
                            NOTE: This is preferably set in a .mss file specified by the mss option.
                                  If set both here and in an .mss file then this will win! (for backwards
                                  compatibility).
                        -->
                        <codeColor>0:0:0</codeColor>
                        
                        <!--
                            Set to true to generate a title page. 
                            Optional. Default: false
                        -->
                        <generateTitlePage>true</generateTitlePage>
                        
                        <!-- 
                            Set to true to generate a table of contents.
                            Optional. Default: false
                        -->
                        <generateTOC>true</generateTOC>
                    </pdfGeneratorOptions>

                </configuration>
            </execution>
        </executions>
    </plugin>


## htmlGeneratorOptions

Following is a complete plugin specification with all options specified:

    <plugin>
        <groupId>se.natusoft.tools.doc.markdowndoc</groupId>
        <artifactId>markdowndoc-maven-plugin</artifactId>
        <version>n.n[.n]</version>

        <executions>
            <execution>
                <id>generate-docs</id>
                <goals>
                    <goal>doc</goal>
                </goals>
                <phase>install</phase>
                <configuration>

                    <generatorOptions>
                        <generator>html</generator>
                        <inputPaths>
                            ...
                        </inputPaths>
                    </generatorOptions>
                    
                    <htmlGeneratorOptions>
                        <!-- 
                            The path to the html document to produce. Path is relative
                            to project root (see comment about root above).
                            Required.
                        -->
                        <resultFile>Docs/MarkdownDoc-User-Guide.html</resultFile>
                        
                        <!--
                            If set to true then the specified css will be inlined
                            in the generated html document. Otherwise the generated
                            html document will reference the specified css.
                            Optional. Default: false
                        -->
                        <inlineCSS>false</inlineCSS>
                        
                        <!--
                            The path to the css file for the generated html file.
                            Required.
                        -->
                        <css>css/my.css</css>
                        
                        <!—
                            This affects links and images. When specified the resulting 
                            file: URLs in the result will be relative to the path specified by
                            ”path” if the absulute path of the URL starts with the specified 
                            path. If a plus sign (+) and a prefix path is specified it will be 
                            prefixed to the final URL.  
                        —>
                         <makeFileLinksRelativeTo>path[+prefix]</makeFileLinksRelativeTo>                         
                         
                    </htmlGeneratorOptions>

                </configuration>
            </execution>
        </executions>
    </plugin>

## mdGeneratorOptions

Following is a complete plugin specification with all options specified:

    <plugin>
        <groupId>se.natusoft.tools.doc.markdowndoc</groupId>
        <artifactId>markdowndoc-maven-plugin</artifactId>
        <version>n.n[.n]</version>

        <executions>
            <execution>
                <id>generate-docs</id>
                <goals>
                    <goal>doc</goal>
                </goals>
                <phase>install</phase>
                <configuration>

                    <generatorOptions>
                        <generator>md</generator>
                        <inputPaths>
                            ...
                        </inputPaths>
                    </generatorOptions>
                    
                    <htmlGeneratorOptions>
                        <!-- 
                            The path to the markdown document to produce. Path is relative
                            to project root (see comment about root above).
                            Required.
                        -->
                        <resultFile>Docs/MarkdownDoc-User-Guide-Complete.md</resultFile>
                        
                        <!—
                            This affects links and images. When specified the resulting 
                            file: URLs in the result will be relative to the path specified by
                            ”path” if the absulute path of the URL starts with the specified 
                            path. If a plus sign (+) and a prefix path is specified it will be 
                            prefixed to the final URL.  
                        —>
                         <makeFileLinksRelativeTo>path[+prefix]</makeFileLinksRelativeTo>                         
                         
                    </htmlGeneratorOptions>

                </configuration>
            </execution>
        </executions>
    </plugin>


