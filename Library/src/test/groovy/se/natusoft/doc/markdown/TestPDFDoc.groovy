package se.natusoft.doc.markdown

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.doc.markdown.generator.FileResource
import se.natusoft.doc.markdown.generator.models.TOC
import se.natusoft.doc.markdown.generator.pdfbox.PDFBoxDocRenderer
import se.natusoft.doc.markdown.generator.pdfbox.PDFBoxFontMSSAdapter
import se.natusoft.doc.markdown.generator.pdfbox.PDFBoxStylesMSSAdapter
import se.natusoft.doc.markdown.generator.pdfbox.PageMargins
import se.natusoft.doc.markdown.util.StructuredNumber
import se.natusoft.doc.markdown.generator.styles.*

@CompileStatic
@TypeChecked
class TestPDFDoc {
    public static void main(String... args) {
        MSSFont mssFont = new MSSFont(size: 12, family: "HELVETICA", style: MSSFontStyle.NORMAL)
        MSSColorPair textColor = new MSSColorPair(foreground: MSSColor.GREY, background: MSSColor.WHITE)
        MSS mss = MSS.defaultMSS()
        PDFBoxStylesMSSAdapter styles = new PDFBoxStylesMSSAdapter(mss: mss, fileResource: new FileResource(rootDir: new File("..")))
        PDFBoxFontMSSAdapter textFont = new PDFBoxFontMSSAdapter(mssFont)

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

        doc.setStyle(styles, MSS.MSS_Pages.standard)
        doc.setColorPair(textColor)

        doc.center("Table of Content")
//        doc.tocEntry(new TOC(sectionNumber: "1.2.3", sectionTitle: "First toc entry", pageNumber: 1 ))
//        doc.tocEntry(new TOC(sectionNumber:  "1.2.3.4.5.6", sectionTitle:  "Second toc entry", pageNumber:  2),
//                new PDFBoxDocRenderer.TocSettings(sectionTitleColor: new MSSColorPair(
//                        foreground: MSSColor.BLUE,
//                        background: MSSColor.WHITE
//                ))
//        )
        //doc.tocEntry(new TOC(sectionTitle: "Thirds toc entry", pageNumber: 5))
        doc.pageNoActive = true
        doc.newPage()

        doc.text("This is a test paragraph. It contains a bit of text that should be longer than one line to see if it breaks " +
                "correctly. Thereby I need to write some more text in this. Just a little more text now. Well, this should at " +
                "least give me 2 lines.")

        doc.newLine()
        doc.newLine()

        doc.startBox(new MSSColor(color: "240:240:240"))
        doc.text("This text should hopefully be in a box!")
        doc.newParagraph()
        doc.text("Another line in the box.")
        doc.endBox()

        doc.newLine()
        doc.newLine()
        doc.text("Some more text under box. ")
        doc.link("MarkdownDoc on GitHub", "http://github.com/tombensve/MarkdownDoc")

        //doc.hr()
        doc.text("Some text under the horizontal ruler.")

        //doc.image("http://download.natusoft.se/Images/MarkdownDoc/MDD_Laptop_2_Fotor.png", PDFBoxDocRenderer.X_OFFSET_CENTER, 0.2f)

        doc.text("This is a test paragraph. It contains a bit of text that should be longer than one line to see if it breaks " +
                "correctly. Thereby I need to write some more text in this. Just a little more text now. Well, this should at " +
                "least give me 2 lines.")

        doc.newLine()
        doc.newLine()
        PDFBoxStylesMSSAdapter sa
        PDFBoxFontMSSAdapter dwerneck = doc.loadExternalFont("file:Docs/dwerneck.ttf", new MSSFont(size: 16, style: MSSFontStyle.NORMAL))
        doc.setFont(dwerneck)
        doc.text("Some text in external ttf font.")

        doc.setFont(textFont)

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

        //doc.hr() // Not rendered due to page break with font size 12! This is intentionally and thus correct behavior.

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


        StructuredNumber sn = new StructuredNumber()

        doc.addOutlineEntry(sn, "${sn} First page", doc.getPage(1))
//        sn.downLevel()
//        doc.addOutlineEntry(sn, "${sn} Also on first page", doc.getPage(1))
//        sn.upLevel()
//        sn.incrementCurrentLevel()
//        doc.addOutlineEntry(sn, "${sn} Second page", doc.getPage(2))
//        sn.downLevel()
//        doc.addOutlineEntry(sn, "${sn} further down on second", doc.getPage(2))
//        sn.level = 5
//        doc.addOutlineEntry(sn, "${sn} Yes, still on second", doc.getPage(2))
//        sn.setLevel(1).incrementCurrentLevel()
//        doc.addOutlineEntry(sn, sn.toString())

        doc.save("Library/target/TestPDFDoc.pdf")

        doc.close()
    }
}
