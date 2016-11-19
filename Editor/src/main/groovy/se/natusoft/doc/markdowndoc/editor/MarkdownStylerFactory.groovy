/*
 *
 * PROJECT
 *     Name
 *         MarkdownDocEditor
 *     
 *     Code Version
 *         2.0.1
 *     
 *     Description
 *         An editor that supports editing markdown with formatting preview.
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
 *     tommy ()
 *         Changes:
 *         2015-08-03: Created!
 *
 */
package se.natusoft.doc.markdowndoc.editor

import org.jetbrains.annotations.NotNull
import se.natusoft.doc.markdowndoc.editor.api.Configurable
import se.natusoft.doc.markdowndoc.editor.api.Editable
import se.natusoft.doc.markdowndoc.editor.api.JTextComponentStyler
import se.natusoft.doc.markdowndoc.editor.api.JTextComponentStylerFactory

/**
 * A factory for creating markdown styler.
 */
class MarkdownStylerFactory implements JTextComponentStylerFactory {

    @Override
    JTextComponentStyler createStyler(@NotNull final Editable editable) {

        final MarkdownStyler styler = new MarkdownStyler(editable.editorPane)
        if (styler instanceof Configurable) {
            (styler as Configurable).registerConfigs(Services.configs)
            Services.configurables.add(styler as Configurable)
        }

        styler
    }
}
