package se.natusoft.doc.markdown.generator.pdfbox.internal

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageXYZDestination
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem

/**
 * This manages a PDF outline
 */
class Outline {
    //
    // Properties
    //

    PDDocumentOutline pdOutline = new PDDocumentOutline()

    //
    // Private Members
    //

    private outlineAdded = false

    private int currentLevel = 0

    private Deque<PDOutlineItem> levels = new LinkedList<>()

    private PDOutlineItem currentItem = new PDOutlineItem(title: "Table of Contents")

    //
    // Constructors
    //

    public Outline() {
        this.currentItem.title = "Table of Conent"
        pdOutline.addFirst(this.currentItem)
        //this.pdOutline.addLast(this.currentItem)
    }

    //
    // Methods
    //

    void addEntry(String number, String title, PDPage page) {
        addEntry(number.split("\\.").length, title, page)
    }

    void addEntry(int level, String title, PDPage page) {
        if (level < 1) level = 1
        switch (level) {
            case { it > this.currentLevel }:
                this.currentLevel = level
                PDOutlineItem item = new PDOutlineItem(title: title, destination: new PDPageXYZDestination(page: page))
                this.currentItem.addLast(item)
                this.levels.push(this.currentItem)
                this.currentItem = item
                break

            case { it < this.currentLevel }:
                this.currentLevel = level
                this.currentItem = this.levels.pop()
                PDOutlineItem item = new PDOutlineItem(title: title, destination: new PDPageXYZDestination(page: page))
                this.currentItem.addLast(item)
                break
            default:
                PDOutlineItem item = new PDOutlineItem(title: title, destination: new PDPageXYZDestination(page: page))
                this.currentItem.insertSiblingAfter(item)
        }
    }

    void addToDocument(PDDocument doc) {
        doc.documentCatalog.documentOutline = this.pdOutline
    }
}
