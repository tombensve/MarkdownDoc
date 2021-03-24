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
 *         2012-11-16: Created!
 *
 */
package se.natusoft.doc.markdown.model

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

/**
 * This defines all available formats represented by the different models.
 * All DocItem subclasses available in a Doc will have one of these
 * in DocItem.format.
 * <p/>
 * There are other models that are sub models to one of these. All those
 * have null in DocItem.format!
 */
@CompileStatic
enum DocFormat {

    Header,
    AutoLink,
    BlockQuote,
    Code,
    CodeBlock,
    Emphasis,
    Strong,
    List,
    Link,
    Image,
    Paragraph,
    PlainText,
    HorizontalRule,
    Comment,
    Space,
    Div,
    NOT_RELEVANT
}
