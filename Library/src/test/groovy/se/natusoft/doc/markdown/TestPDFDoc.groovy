package se.natusoft.doc.markdown

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.doc.markdown.generator.pdfbox.PDFBoxDocRenderer
import se.natusoft.doc.markdown.generator.pdfbox.PDFBoxFontMSSAdapter
import se.natusoft.doc.markdown.generator.pdfbox.internal.PageMargins
import se.natusoft.doc.markdown.generator.styles.MSSColor
import se.natusoft.doc.markdown.generator.styles.MSSColorPair
import se.natusoft.doc.markdown.generator.styles.MSSFont
import se.natusoft.doc.markdown.generator.styles.MSSFontStyle

@CompileStatic
@TypeChecked
class TestPDFDoc {
    public static void main(String... args) {
        MSSFont mssFont = new MSSFont(size: 12, family: "HELVETICA", style: MSSFontStyle.NORMAL)
        MSSColorPair mssColorPair = new MSSColorPair(foreground: MSSColor.GREY, background: MSSColor.WHITE)
        PDFBoxFontMSSAdapter fontMSSAdapter = new PDFBoxFontMSSAdapter(mssFont)

        PDFBoxDocRenderer doc = new PDFBoxDocRenderer(
                margins: new PageMargins(
                    topMargin: 50,
                    bottomMargin: 50,
                    leftMargin: 50,
                    rightMargin: 50
                ),
                pageSize: PDFBoxDocRenderer.A4
        )

        doc.newPage()

        doc.applyFont(fontMSSAdapter)
        doc.applyColorPair(mssColorPair)

        doc.center("Table of Content")
        doc.tocEntry("1.2.3", "First toc entry", 1)
        doc.tocEntry("1.2.3.4.5.6", "Second toc entry", 2)
        doc.tocEntry(null, "Third toc entry", 5)
        doc.newPage()

        doc.addOutlineEntry("1", "One")
        doc.addOutlineEntry("1.1", "One-one")
        doc.addOutlineEntry("2", "Two")

        doc.text("This is a test paragraph. It contains a bit of text that should be longer than one line to see if it breaks " +
                "correctly. Thereby I need to write some more text in this. Just a little more text now. Well, this should at " +
                "least give me 2 lines.")

        doc.newLine()
        doc.newLine()

        MSSColor boxColor = new MSSColor(color: "240:240:240")
        doc.startBox(new MSSColorPair(foreground: boxColor, background: boxColor))
        doc.text("This text should hopefully be in a box!")
        doc.newParagraph()
        doc.text("Another line in the box.")
        doc.endBox()

        doc.newLine()
        doc.newLine()
        doc.text("Some more text under box. ")
        doc.link("MarkdownDoc on GitHub", "http://github.com/tombensve/MarkdownDoc")

        doc.hr()
        doc.text("Some text under the horizontal ruler.")

        doc.text("This is a test paragraph. It contains a bit of text that should be longer than one line to see if it breaks " +
                "correctly. Thereby I need to write some more text in this. Just a little more text now. Well, this should at " +
                "least give me 2 lines.")

        doc.newLine()
        doc.newLine()
        doc.text("This is a test paragraph. It contains a bit of text that should be longer than one line to see if it breaks " +
                "correctly. Thereby I need to write some more text in this. Just a little more text now. Well, this should at " +
                "least give me 2 lines.")

        doc.newLine()
        doc.newLine()
        doc.text("This is a test paragraph. It contains a bit of text that should be longer than one line to see if it breaks " +
                "correctly. Thereby I need to write some more text in this. Just a little more text now. Well, this should at " +
                "least give me 2 lines.")

        doc.newLine()
        doc.newLine()
        doc.text("This is a test paragraph. It contains a bit of text that should be longer than one line to see if it breaks " +
                "correctly. Thereby I need to write some more text in this. Just a little more text now. Well, this should at " +
                "least give me 2 lines.")

        doc.newLine()
        doc.newLine()
        doc.text("This is a test paragraph. It contains a bit of text that should be longer than one line to see if it breaks " +
                "correctly. Thereby I need to write some more text in this. Just a little more text now. Well, this should at " +
                "least give me 2 lines.")

        doc.newLine()
        doc.newLine()
        doc.text("This is a test paragraph. It contains a bit of text that should be longer than one line to see if it breaks " +
                "correctly. Thereby I need to write some more text in this. Just a little more text now. Well, this should at " +
                "least give me 2 lines.")

        doc.newLine()
        doc.newLine()
        doc.text("This is a test paragraph. It contains a bit of text that should be longer than one line to see if it breaks " +
                "correctly. Thereby I need to write some more text in this. Just a little more text now. Well, this should at " +
                "least give me 2 lines.")

        doc.newLine()
        doc.newLine()
        doc.text("This is a test paragraph. It contains a bit of text that should be longer than one line to see if it breaks " +
                "correctly. Thereby I need to write some more text in this. Just a little more text now. Well, this should at " +
                "least give me 2 lines.")

        doc.newLine()
        doc.newLine()
        doc.text("This is a test paragraph. It contains a bit of text that should be longer than one line to see if it breaks " +
                "correctly. Thereby I need to write some more text in this. Just a little more text now. Well, this should at " +
                "least give me 2 lines.")

        doc.newLine()
        doc.newLine()

        doc.text("This is a test paragraph. It contains a bit of text that should be longer than one line to see if it breaks " +
                "correctly. Thereby I need to write some more text in this. Just a little more text now. Well, this should at " +
                "least give me 2 lines.")

        doc.newLine()
        doc.newLine()
        doc.text("This is a test paragraph. It contains a bit of text that should be longer than one line to see if it breaks " +
                "correctly. Thereby I need to write some more text in this. Just a little more text now. Well, this should at " +
                "least give me 2 lines.")

        doc.hr() // Not rendered due to page break with font size 12! This is intentionally and thus correct behavior.

        doc.newLine()
        doc.newLine()
        doc.text("This is a test paragraph. It contains a bit of text that should be longer than one line to see if it breaks " +
                "correctly. Thereby I need to write some more text in this. Just a little more text now. Well, this should at " +
                "least give me 2 lines.")

        doc.newLine()
        doc.newLine()
        doc.text("This is a test paragraph. It contains a bit of text that should be longer than one line to see if it breaks " +
                "correctly. Thereby I need to write some more text in this. Just a little more text now. Well, this should at " +
                "least give me 2 lines.")

        doc.newLine()
        doc.newLine()
        doc.text("This is a test paragraph. It contains a bit of text that should be longer than one line to see if it breaks " +
                "correctly. Thereby I need to write some more text in this. Just a little more text now. Well, this should at " +
                "least give me 2 lines.")

        doc.newLine()
        doc.newLine()
        doc.text("This is a test paragraph. It contains a bit of text that should be longer than one line to see if it breaks " +
                "correctly. Thereby I need to write some more text in this. Just a little more text now. Well, this should at " +
                "least give me 2 lines.")

        doc.newLine()
        doc.newLine()
        doc.text("This is a test paragraph. It contains a bit of text that should be longer than one line to see if it breaks " +
                "correctly. Thereby I need to write some more text in this. Just a little more text now. Well, this should at " +
                "least give me 2 lines.")

        doc.newLine()
        doc.newLine()
        doc.text("This is a test paragraph. It contains a bit of text that should be longer than one line to see if it breaks " +
                "correctly. Thereby I need to write some more text in this. Just a little more text now. Well, this should at " +
                "least give me 2 lines.")

        doc.newLine()
        doc.newLine()
        doc.text("This is a test paragraph. It contains a bit of text that should be longer than one line to see if it breaks " +
                "correctly. Thereby I need to write some more text in this. Just a little more text now. Well, this should at " +
                "least give me 2 lines.")

        doc.newLine()
        doc.newLine()
        doc.text("This is a test paragraph. It contains a bit of text that should be longer than one line to see if it breaks " +
                "correctly. Thereby I need to write some more text in this. Just a little more text now. Well, this should at " +
                "least give me 2 lines.")

        doc.newLine()
        doc.newLine()
        doc.text("This is a test paragraph. It contains a bit of text that should be longer than one line to see if it breaks " +
                "correctly. Thereby I need to write some more text in this. Just a little more text now. Well, this should at " +
                "least give me 2 lines.")

        doc.newLine()
        doc.newLine()


        doc.save("Library/target/TestPDFDoc.pdf")

        doc.close()
    }
}
