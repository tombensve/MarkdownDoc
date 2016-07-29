/* 
 * 
 * PROJECT
 *     Name
 *         MarkdownDoc Library
 *     
 *     Code Version
 *         1.5.0
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
 *     tommy ()
 *         Changes:
 *         2016-07-29: Created!
 *         
 */
package se.natusoft.doc.markdown.generator.pdfbox.internal

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem

/**
 * This manages a PDF outline
 */
@CompileStatic
@TypeChecked
class Outline {
    //
    // Inner Classes
    //

    /**
     * Workaround for the ungroovyness of the PDFBox API.
     */
    private static class OutlineItem  {
        PDOutlineItem item = new PDOutlineItem()

        void setDestination(PDPage pdPage) {
            this.item.setDestination(pdPage)
        }

        void setTitle(String title) {
            this.item.setTitle(title)
        }
    }


    //
    // Properties
    //

    /** The PDFBox outline object. */
    PDDocumentOutline pdOutline = new PDDocumentOutline()

    //
    // Private Members
    //

    /** Keeps track of the items at the available levels. */
    private Map<Integer, PDOutlineItem> levels = new HashMap<>()


    //
    // Constructors
    //

    /**
     * Creates a new Outline.
     */
    public Outline() {
        PDOutlineItem firstItem = new PDOutlineItem(title: "Table of Contents")
        pdOutline.addFirst(firstItem)
        // We need to do this or the top level "Table of Contents" will not be shown!
        firstItem.insertSiblingAfter(new PDOutlineItem(title: ""))
        this.levels.put(0, firstItem)
    }

    //
    // Methods
    //

    /**
     * Adds a new outline entry.
     *
     * @param number The section number of the entry. Used to calculate entry level.
     * @param title The title of the entry.
     * @param page The page that should be shown when the entry is clicked.
     */
    void addEntry(String number, String title, PDPage page) {
        addEntry(number.split("\\.").length, title, page)
    }

    /**
     * Adds a new outline entry.
     *
     * @param level The level of the entry (0-6)
     * @param title The title of the entry.
     * @param page The page that should be shown when the entry is clicked.
     */
    void addEntry(int level, String title, PDPage page) {
        if (level < 0) level = 0
        if (level > 6) level = 6

        PDOutlineItem parentItem = getParentItem(level - 1)
        PDOutlineItem item = new OutlineItem(title: title, destination: page).item
        this.levels.put(level, item)
        parentItem.addLast(item)

    }

    /**
     * Returns the current parent item for the specified level.
     *
     * @param level The level to get parent for.
     */
    private PDOutlineItem getParentItem(int level) {
        PDOutlineItem item = this.levels.get(level)
        if (item == null) {
            item = getParentItem(level - 1)
        }
        return item
    }

    /**
     * This adds this outline to the specified document.
     *
     * @param doc The document to add outline to.
     */
    void addToDocument(PDDocument doc) {
        this.pdOutline.firstChild.destination = doc.getPage(0)
        doc.documentCatalog.documentOutline = this.pdOutline
    }
}
