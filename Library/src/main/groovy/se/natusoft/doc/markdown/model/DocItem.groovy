/* 
 * 
 * PROJECT
 *     Name
 *         MarkdownDoc Library
 *     
 *     Code Version
 *         1.0
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

import java.util.List as JList
import java.util.LinkedList as JLinkedList
import se.natusoft.doc.markdown.io.Line

/**
 * This is a base API implemented by most of the document structure models.
 */
public class DocItem {
    //
    // Private Members
    //

    /** The sub items of this DocItem.  */
    JList<DocItem> items = new JLinkedList<DocItem>()

    /** If true the consecutive instances are merged. */
    boolean keepConsecutiveTogether = false

    /** If non null a new item with this content will be added between kept together blocks merged. */
    String addBetweenKeepTogether = null

    /** If true the this belongs in an hierarchy of items. */
    boolean isHierarchy = false;

    /** This is a rendering hint. */
    boolean renderPrefixedSpace = true

    //
    // Methods
    //

    /**
     * Provides the left shift operator.
     *
     * @param object The object to left shift in.
     */
    public DocItem leftShift(Object object) {
        addItem(object.toString())
        return this
    }

    /**
     * Creates a new DocItem instance of the same type as this instance and with the same base DocItem config as this.
     */
    public DocItem createNewWithSameConfig() {
        DocItem di = this.class.newInstance()
        copyConfig(di)
        return di
    }

    protected void copyConfig(DocItem docItem) {
        docItem.setKeepConsecutiveTogether(this.keepConsecutiveTogether)
        docItem.setAddBetweenKeepTogether(this.addBetweenKeepTogether)
        docItem.setIsHierarchy(this.isHierarchy)
        docItem.setRenderPrefixedSpace(this.renderPrefixedSpace)
    }

    /**
     * Adds an item.
     *
     * @param paragraph A Paragraph to add.
     */
    public void addItem(DocItem docItem) {
        this.items.add(docItem)
    }

    /**
     * Adds an item as text.
     *
     * @param text Text to add.
     */
    public void addItem(String text) {
        PlainText pt = new PlainText(text: text)
        this.items.add(pt);
    }

    /**
     * Adds an item as a Line.
     *
     * @param line Text to add.
     */
    public void addItem(Line line) {
        addItem(line.toString())
    }

    /**
     * Returns true if this DocItem has sub items.
     */
    public boolean hasSubItems() {
        return this.items.size() > 0
    }

    /**
     * Returns true if the hierarchy should move one indentLevel down. This is only valid when isHierarchy is true.
     *
     * @param prevItem The previous item to compare to.
     */
    public boolean isHierarchyDown(DocItem prevItem) {
        return false
    }

    /**
     * Returns true if the hierarchy should move one indentLevel up. This is only valid when isHierarchy is true.
     *
     * @param prevItem The previous item to compare to.
     */
    public boolean isHierarchyUp(DocItem prevItem) {
        return false
    }

    /**
     * Returns true if the passed docItem is the same type as this docItem.
     *
     * @param docItem The DocItem to test.
     */
    public boolean isSameType(DocItem docItem) {
        return this.class == docItem.class
    }

    /**
     * This returns the format of the sub model of DocFormat or if this method is not overridden null. Only
     * the models representing the formats in DocFormat will override this.
´    */
    public DocFormat getFormat() {
        return null
    }

    /**
     * Subclasses should override this to provide validation.
     *
     * @return true if valid.
     */
    public boolean validate() {
        boolean valid = true

        for (DocItem di : this.items) {
            if (!di.validate()) {
                valid = false;
                break;
            }
        }

        return valid
    }

    /**
     * Returns a String representation for debugging purposes.
     */
    public String toString() {
        def str = ''

        boolean first = true
        for (DocItem item : this.items) {
            if (item.renderPrefixedSpace && !first) str += " "
            first = false
            str += item.toString()
        }

        return str
    }
}
