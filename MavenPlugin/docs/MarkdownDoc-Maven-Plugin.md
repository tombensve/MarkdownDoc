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

If the `<inputPaths>...</inputPaths>` section only contain one file of type _.mddoc_ then no other parameters need to be specified, not even `<generator>...</generator>`! In this case all information needed to generate final documents resides in the  _.mddoc_ file. See _’The _mddoc_ file type’_ elsewhere in this document for more information on this file type.

The current valid argument for `<generator>...</generator>` are _pdf_, _html_, and _md_.

The input paths are comma separated and are always relative to the root of the maven project. To clarify that, for a multi module maven build it is always the top root with the top pom that is the root even if you start the build at a lower level. This root is resolved by starting at _${basedir}_ and going up until the parent directory does not have a pom. I have found no way to let maven tell me this path. 

The paths can have wildcards in form of regular expressions for the file names. There is also a special directory name \*\* that means any level of subdirectories.

All the input paths are parsed into the same document model that then gets passed to the generator. They are parsed in the order they are specified. When it comes to wildcards it is hard to say which order they will be in. It might differ on different platforms.

## Example

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
                        <generator>pdf|html|md</generator>
                        <inputPaths>
                            ...
                        </inputPaths>
                    </generatorOptions>
                    
                    <pdf|html|mdGeneratorOptions>
                        ...
                    </pdf|html|mdGeneratorOptions>

                </configuration>
            </execution>
        </executions>
    </plugin>

See the "Options / Settings" part elsewhere in this document for all the specific options.