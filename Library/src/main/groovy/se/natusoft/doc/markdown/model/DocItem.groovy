/*
 *
 * PROJECT
 *     Name
 *         MarkdownDoc Library
 *     
 *     Code Version
 *         2.0.2
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
 *         2012-10-28: Created!
 *
 */
package se.natusoft.doc.markdown.model

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import se.natusoft.doc.markdown.io.Line

import java.util.LinkedList as JLinkedList
import java.util.List as JList

/**
 * This is a base API implemented by most of the document structure models.
 */
@CompileStatic
@TypeChecked
abstract class DocItem {
    //
    // Properties
    //

    /** The sub items of this DocItem.  */
    @NotNull JList<DocItem> items = new JLinkedList<DocItem>()

    /** If true the consecutive instances are merged. */
    boolean keepConsecutiveTogether = false

    /** If non null a new item with this content will be added between kept together blocks. */
    @Nullable String addBetweenKeepTogether = null

    /** If true the this belongs in an hierarchy of items. */
    boolean isHierarchy = false;

    /** This is a rendering hint. */
    boolean renderPrefixedSpace = true

    /** The input file this item comes from. */
    @NotNull("Must be set by parsers.")
    File parseFile = null;

    //
    // Methods
    //

    /**
     * Provides the left shift operator.
     *
     * @param object The object to left shift in.
     */
    @NotNull DocItem leftShift(@NotNull final Object object) {
        addItem(object.toString())
        this
    }

    /**
     * Creates a new DocItem instance of the same type as this instance and with the same base DocItem config as this.
     */
    @NotNull DocItem createNewWithSameConfig() {
        copyConfig(this.class.newInstance() as DocItem)
    }

    @NotNull protected DocItem copyConfig(final DocItem docItem) {
        docItem.setKeepConsecutiveTogether(this.keepConsecutiveTogether)
        docItem.setAddBetweenKeepTogether(this.addBetweenKeepTogether)
        docItem.setIsHierarchy(this.isHierarchy)
        docItem.setRenderPrefixedSpace(this.renderPrefixedSpace)

        docItem
    }

    /**
     * Adds an item.
     *
     * @param paragraph A Paragraph to add.
     */
    void addItem(@NotNull final DocItem docItem) {
        this.items.add(docItem)
    }

    /**
     * Adds a list of items.
     *
     * @param items the items to add.
     */
    void addItems(@NotNull final JList<DocItem> items) {
        items.each { final DocItem item ->
            addItem(item);
        }
    }

    /**
     * Adds an item as text.
     *
     * @param text Text to add.
     */
    void addItem(@NotNull final String text) {
        final PlainText pt = new PlainText(text: text)
        this.items.add(pt);
    }

    /**
     * Adds an item as a Line.
     *
     * @param line Text to add.
     */
    void addItem(@NotNull final Line line) {
        addItem(line.toString())
    }

    /**
     * Returns true if this DocItem has sub items.
     */
    boolean hasSubItems() {
        return this.items.size() > 0
    }

    /**
     * Returns true if the hierarchy should move one indentLevel down. This is only valid when isHierarchy is true.
     *
     * @param prevItem The previous item to compare to.
     */
    boolean isHierarchyDown(@NotNull final DocItem prevItem) {
        false
    }

    /**
     * Returns true if the hierarchy should move one indentLevel up. This is only valid when isHierarchy is true.
     *
     * @param prevItem The previous item to compare to.
     */
    boolean isHierarchyUp(@NotNull final DocItem prevItem) {
        false
    }

    /**
     * Returns true if the passed docItem is the same type as this docItem.
     *
     * @param docItem The DocItem to test.
     */
    boolean isSameType(@NotNull final DocItem docItem) {
        this.class == docItem.class
    }

    /**
     * This returns the format of the sub model or if this method is not overridden NOT_RELEVANT. Only
     * the models representing the formats in DocFormat will override this and they must return non null.
     * For other subclasses this is simply not used.
´    */
    @NotNull
    DocFormat getFormat() {
        DocFormat.NOT_RELEVANT
    }

    /**
     * Subclasses should override this to provide validation.
     *
     * @return true if valid.
     */
    boolean validate() {
        boolean valid = true

        for (final DocItem di : this.items) {
            if (!di.validate()) {
                valid = false;
                break;
            }
        }

        valid
    }

    /**
     * Returns a String representation for debugging purposes.
     */
    @NotNull String toString() {
        def str = ""

        boolean first = true
        for (final DocItem item : this.items) {
            if (item.renderPrefixedSpace && !first) str += " "
            first = false
            str += item.toString()
        }

        str
    }
}
