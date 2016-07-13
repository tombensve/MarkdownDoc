package se.natusoft.doc.markdown

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.font.PDType1Font

@CompileStatic
@TypeChecked
class TestPDFBoxDocGen {

    private static int pageHeight = 780
    private static int pageWidth = 610

    public static void main(String... args) {
        println("width: ${PDRectangle.A4.width}, height: ${PDRectangle.A4.height}")
        // Create a document and add a page to it
        PDDocument document = new PDDocument()
        PDPage page = new PDPage()
        page.setMediaBox(PDRectangle.A4)
        document.addPage( page )

// Create a new font object selecting one of the PDF base fonts
        PDFont font = PDType1Font.HELVETICA

// Start a new content stream which will "hold" the to be created content
        PDPageContentStream contentStream = new PDPageContentStream(document, page)

// Define a text content stream using the selected font, moving the cursor and drawing the text "Hello World"
        contentStream.beginText()
        int fontSize = 8
        contentStream.setFont( font, fontSize )
        String text = "Tommy Svensson"
        float textWidth = (font.getStringWidth(text) / 1000.0f * (float)fontSize) as float

        println("font width: ${textWidth}")
        contentStream.leading = (fontSize + 2) as float
        contentStream.newLineAtOffset(20f, (PDRectangle.A4.height - 12.0f) as float)

        contentStream.showText(text)
        contentStream.showText(". Lite mera text.")
        contentStream.showText(" skjdf hs sdhsjasdfshf jsdhf kjjsdh jshf jkshjshfshf  sdkj shjsdhfjhfjhfjk ajshf sajahf asjhf las")
        contentStream.newLine()
        contentStream.setNonStrokingColor(0, 0, 240)
        contentStream.showText("Detta är en ny rad")
        contentStream.newLine()
        contentStream.newLine()
        contentStream.showText("Ny paragraf.")
        contentStream.endText()

// Make sure that the content stream is closed:
        contentStream.close()

// Save the results and ensure that the document is properly closed:
        document.save( "Hello World.pdf")
        document.close()
    }
}
