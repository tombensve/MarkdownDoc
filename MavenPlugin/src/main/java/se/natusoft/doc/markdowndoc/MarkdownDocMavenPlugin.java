/*
 *
 * PROJECT
 *     Name
 *         MarkdownDoc Maven Plugin
 *
 *     Description
 *         A maven plugin for generating documentation from markdown.
 *
 * COPYRIGHTS
 *     Copyright (C) 2012 by Natusoft AB All rights reserved.
 *
 * LICENSE
 *     Apache 2.0 (Open Source)
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 * AUTHORS
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2012-11-18: Created!
 *
 */
package se.natusoft.doc.markdowndoc;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import se.natusoft.doc.markdown.api.Generator;
import se.natusoft.doc.markdown.api.Options;
import se.natusoft.doc.markdown.api.Parser;
import se.natusoft.doc.markdown.exception.GenerateException;
import se.natusoft.doc.markdown.exception.ParseException;
import se.natusoft.doc.markdown.generator.GeneratorProvider;
import se.natusoft.doc.markdown.generator.options.GeneratorOptions;
import se.natusoft.doc.markdown.generator.options.HTMLGeneratorOptions;
import se.natusoft.doc.markdown.generator.options.MarkdownGeneratorOptions;
import se.natusoft.doc.markdown.generator.options.PDFGeneratorOptions;
import se.natusoft.doc.markdown.model.Doc;
import se.natusoft.doc.markdown.parser.MarkdownParser;
import se.natusoft.doc.markdown.parser.ParserProvider;
import se.natusoft.doc.markdown.util.MDDocFileHandler;
import se.natusoft.doc.markdown.util.SourcePaths;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Properties;

/**
 * Goal which touches a timestamp file.
 *
 * @phase generate-sources
 */
@SuppressWarnings({ "unused", "JavaDoc" })
@Mojo(name = "doc", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class MarkdownDocMavenPlugin extends AbstractMojo {

    /**
     * Provides the options for which generator to run.
     */
    @Parameter()
    private GeneratorOptions generatorOptions;

    /**
     * Provides the options for the HTMLGenerator. These are only relevant if
     * generatorOptions.generator == "html".
     */
    @Parameter
    private HTMLGeneratorOptions htmlGeneratorOptions;

    /**
     * Provides the options for the MarkdownGenerator. These are only relevant if
     * generatorOptions.generator == "md".
     */
    @Parameter
    private MarkdownGeneratorOptions mdGeneratorOptions;

    /**
     * Provides the options for the PDFGenerator. These are only relevant if
     * generatorOptions.generator == "pdf".
     */
    @Parameter
    private PDFGeneratorOptions pdfGeneratorOptions;

    /**
     * The projects base directory.
     */
    @Parameter(property = "basedir")
    private String baseDir;

    /**
     * Validates the inputs.
     *
     * @throws MojoExecutionException TODO: Validations should be delegated to each generator.
     */
    private void validate() throws MojoExecutionException {
        if ( this.generatorOptions == null ) {
            throw new MojoExecutionException( "A <generatorOptions>...</generatorOptions> must be specified with a minimum of " +
                    "<inputPaths>...</inputPaths> in it!" );
        }

        if ( this.generatorOptions.getInputPaths() == null || this.generatorOptions.getInputPaths().isEmpty() ) {
            throw new MojoExecutionException( "<inputPaths>...</inputPaths> must be specified!" );
        }

        if ( this.generatorOptions.getGenerator() == null || this.generatorOptions.getGenerator().isEmpty() ) {
            throw new MojoExecutionException( "Missing: <generatorOptions><generator>...</generator></generatorOptions>!" );
        }

        if ( this.generatorOptions.getGenerator().trim().toLowerCase().equals( "pdf" ) ) {
            if ( this.pdfGeneratorOptions == null ) {
                throw new MojoExecutionException( "The PDF generator needs <pdfGeneratorOptions>...</pdfGeneratorOptions> configuration!" );
            }

            if ( this.pdfGeneratorOptions.getResultFile() == null || this.pdfGeneratorOptions.getResultFile().isEmpty() ) {
                throw new MojoExecutionException( "'resultFile' missing: " +
                        "<pdfGeneratorOptions><resultFile>...</resultFile></pdfGeneratorOptions>!" );
            }
        }
        if ( this.generatorOptions.getGenerator().trim().toLowerCase().equals( "html" ) ) {
            if ( this.htmlGeneratorOptions == null ) {
                throw new MojoExecutionException( "The PDF generator needs <htmlGeneratorOptions>...</htmlGeneratorOptions> configuration!" );
            }

            if ( this.htmlGeneratorOptions.getResultFile() == null || this.htmlGeneratorOptions.getResultFile().isEmpty() ) {
                throw new MojoExecutionException( "'resultFile' missing: " +
                        "<htmlGeneratorOptions><resultFile>...</resultFile></htmlGeneratorOptions>!" );
            }

        }
        if ( ( this.generatorOptions.getGenerator().trim().toLowerCase().equals( "md" ) ||
                this.generatorOptions.getGenerator().trim().toLowerCase().equals( "markdown" ) ) ) {
            if ( this.mdGeneratorOptions == null ) {
                throw new MojoExecutionException( "The PDF generator needs <mdGeneratorOptions>...</mdGeneratorOptions> configuration!" );
            }

            if ( this.mdGeneratorOptions.getResultFile() == null || this.mdGeneratorOptions.getResultFile().isEmpty() ) {
                throw new MojoExecutionException( "'resultFile' missing: " +
                        "<mdGeneratorOptions><resultFile>...</resultFile></mdGeneratorOptions>!" );
            }
        }
    }

    /**
     * Executes this mojo.
     *
     * @throws MojoExecutionException on bad config and other failures.
     */
    public void execute() throws MojoExecutionException {
        validate();

        String inputPaths = this.generatorOptions.getInputPaths();
        if ( inputPaths != null && inputPaths.trim().length() > 0 ) {
            if ( inputPaths.indexOf( ',' ) == -1 && inputPaths.endsWith( ".mddoc" ) ) {
                try {
                    MDDocFileHandler.execute( inputPaths, true );
                } catch ( ParseException pe ) {
                    throw new MojoExecutionException( pe.getMessage(), pe );
                }
            } else {
                executeStd();
            }
        } else {
            getLog().error( "No markdown files to generate from have been specified!" );
        }
    }

    /**
     * Executes this mojo.
     *
     * @throws MojoExecutionException on bad config and other failures.
     */
    public void executeStd() throws MojoExecutionException {
        Parser parser = null;
        String selParser = this.generatorOptions.getParser().toLowerCase();
        if ( selParser.equals( "markdown" ) ) {
            parser = new MarkdownParser();
        } else if ( !selParser.startsWith( "byext" ) ) {
            throw new MojoExecutionException( "Unknown parser specified: '" + selParser + "'!" );
        }

        Options options = null;
        String selGenerator = generatorOptions.getGenerator().toLowerCase();
        Generator generator = GeneratorProvider.getGeneratorByName( selGenerator );
        if ( generator == null ) {
            throw new MojoExecutionException( "Unknown generator: '" + selGenerator + "'!" );
        }

        // Hmm ... I can't get around having an options model for a generator declared as a private member and
        // plugin parameter. But with this reflection match we only need to add it above.
        for ( Field field : getClass().getDeclaredFields() ) {
            if ( field.getType().equals( generator.getOptionsClass() ) ) {
                //noinspection EmptyCatchBlock
                try {
                    options = (Options) field.get( this );
                    break;
                } catch ( IllegalAccessException iae ) {
                }
            }
        }

        if ( options == null ) {
            throw new MojoExecutionException( "No options provided for " + generator.getOptionsClass().getSimpleName() + "!" );
        }

        // Parse

        File projRoot = getRootDir();
        Doc document = new Doc();
        SourcePaths sourcePaths = new SourcePaths( projRoot, generatorOptions.getInputPaths() );
        String parserOptsStr = generatorOptions.getParserOptions();
        Properties parserOptions = new Properties();
        if ( parserOptsStr != null ) {
            for ( String parserOpt : parserOptsStr.split( "," ) ) {
                String[] nameValue = parserOpt.split( "=" );
                parserOptions.put( nameValue[ 0 ], nameValue[ 1 ] );
            }
        }
        if ( sourcePaths.hasSourceFiles() ) {
            try {
                getLog().info( "Parsing the following files:" );
                for ( File sourceFile : sourcePaths.getSourceFiles() ) {
                    System.out.println( "    " + sourceFile );
                    Parser fileParser = parser;
                    if ( fileParser == null ) {
                        fileParser = ParserProvider.getParserForFile( sourceFile );
                    }
                    if ( fileParser == null ) {
                        throw new MojoExecutionException( "Don't know how to parse '" + sourceFile.getName() + "'!" );
                    }
                    fileParser.parse( document, sourceFile, parserOptions );
                }
                getLog().info( "All parsed!" );
            } catch ( ParseException pe ) {
                throw new MojoExecutionException( "Parse failure!", pe );
            } catch ( IOException ioe ) {
                throw new MojoExecutionException( "I/O failure while parsing input!", ioe );
            }

            // Generate

            try {
                if ( options.getResultFile().startsWith( projRoot.getAbsolutePath() ) ) {
                    System.out.println( "Generating: " + options.getResultFile() );
                    generator.generate( document, options, null );
                } else {
                    System.out.println( "Generating: " + projRoot.getAbsolutePath() + File.separator + options.getResultFile() );
                    generator.generate( document, options, projRoot );
                }
            } catch ( GenerateException ge ) {
                throw new MojoExecutionException( "Failed to generate html!", ge );
            } catch ( IOException ioe ) {
                throw new MojoExecutionException( "I/O problems when generating!", ioe );
            }
        } else {
            System.out.println( "No document source files were specified, and thus nothing were generated!" );
        }
    }

    /**
     * Returns a File representing the directory whose parent directory does not have a pom.xml.
     * In other words, the root of a multi-module build.
     */
    private File getRootDir() {
        File root = new File( this.baseDir );
        while ( havePOM( root.getParentFile().listFiles() ) ) {
            root = root.getParentFile();
        }

        return root;
    }

    /**
     * Checks if any of the passed files is a pom.xml.
     *
     * @param files The files to check.
     *
     * @return true if found, false otherwise.
     */
    private boolean havePOM( File[] files ) {

        for ( File file : files ) {
            if ( file.getName().toLowerCase().equals( "pom.xml" ) ) {
                return true;
            }
        }

        return false;
    }

}
