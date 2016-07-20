package se.natusoft.doc.markdown

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.doc.markdown.generator.pdf.PDFDoc
import se.natusoft.doc.markdown.generator.pdf.PDFFontMSSAdapter
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
        PDFFontMSSAdapter fontMSSAdapter = new PDFFontMSSAdapter(mssFont, mssColorPair)

        PDFDoc doc = new PDFDoc(
                topMargin: 50,
                bottomMargin: 50,
                leftMargin: 50,
                rightMargin: 50,
                pageSize: PDFDoc.A4
        )

        doc.newPage()

        doc.applyFont(fontMSSAdapter)
        doc.applyColorPair(mssColorPair)

        doc.text("This is a test paragraph. It contains a bit of text that should be longer than one line to see if it breaks " +
                "correctly. Thereby I need to write some more text in this. Just a little more text now. Well, this should at " +
                "least give me 2 lines.")

        doc.newLine()
        doc.newLine()

        doc.startBox("200:200:180")
        doc.text("This text should hopefully be in a box!")
        doc.newParagraph()
        doc.text("Another line in the box.")
        doc.endBox()

        doc.newLine()
        doc.newLine()
        doc.text("Some more text under box.")

        doc.save("Library/target/TestPDFDoc.pdf")

        doc.close()
    }
}
