/*
 *
 * PROJECT
 *     Name
 *         MarkdownDoc Library
 *     
 *     Code Version
 *         1.4.2
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
 *         2012-11-16: Created!
 *
 */
package se.natusoft.doc.markdown.generator.options

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.doc.markdown.api.Options
import se.natusoft.tools.optionsmgr.annotations.*

/**
 * This provides options for the Markdown generator.
 */
@CompileStatic
@TypeChecked
@OptionsModel
class MarkdownGeneratorOptions implements Options {

    @Option
    @Name("resultFile")
    @Description("Where to write the result.")
    @Required
    String resultFile

    @Option
    @Name("makeFileLinksRelativeTo")
    @Description("The path file links should be relative to.")
    String makeFileLinksRelativeTo = null;

    @Option
    @Name("help")
    @Description("Shows help.")
    @Optional
    @Flag
    boolean help;

}
