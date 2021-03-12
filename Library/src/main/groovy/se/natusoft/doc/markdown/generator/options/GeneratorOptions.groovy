/*
 *
 * PROJECT
 *     Name
 *         MarkdownDoc Library
 *
 *     Description
 *         Parses markdown and generates HTML and PDF.
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
 *         2012-11-19: Created!
 *
 */
package se.natusoft.doc.markdown.generator.options

import groovy.transform.CompileStatic
import se.natusoft.tools.optionsmgr.annotations.*

/**
 * This provides options for selecting generator.
 */
@CompileStatic
@OptionsModel(name="generatorOptions")
class GeneratorOptions {

    @Option
    @Name("generator")
    @Description("The generator to run. \"html\" or \"pdf\".")
    @Required
    String generator

    @Option
    @Name("parser")
    @Description("The name of the parser to use. Default:\"byExtension\".")
    @Optional
    String parser = "byExtension"

    @Option
    @Name("inputPaths")
    @Description("A comma separated list of paths of files to parse as input for one output.")
    @Required
    String inputPaths

    @Option
    @Name("parserOptions")
    @Description("This will be passed to each parser and is in the format \"name=value,...,name=value\" no spaces!")
    @Optional
    String parserOptions

}
