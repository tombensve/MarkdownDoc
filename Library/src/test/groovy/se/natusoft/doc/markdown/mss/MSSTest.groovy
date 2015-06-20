package se.natusoft.doc.markdown.mss

import se.natusoft.doc.markdown.generator.styles.MSS
import se.natusoft.doc.markdown.generator.styles.MSSColor
import se.natusoft.doc.markdown.generator.styles.MSSFontStyle

class MSSTest extends GroovyTestCase {

    void testMSSColor() throws Exception {
        MSSColor c1 = new MSSColor(color: "127:128:129")
        assert c1.red == 127
        assert c1.green == 128
        assert c1.blue == 129

        MSSColor c2 = new MSSColor(color: "FAEBD7")
        assert c2.red == 0xfa
        assert c2.green == 0xeb
        assert c2.blue == 0xd7

        MSSColor c3 = new MSSColor(color: "#faebd7;")
        assert c3.red == 0xfa
        assert c3.green == 0xeb
        assert c3.blue == 0xd7
    }

    void testMSS() throws Exception {
        File testFile = new File("src/test/resources/test.mss") // Maven
        if (!testFile.exists()) {
            testFile = new File("Library/src/test/resources/test.mss") // IntelliJ
        }

        MSS mss = MSS.fromInputStream(new FileInputStream(testFile))

        //
        // document
        //

        MSS.ForDocument forDocument = mss.forDocument
        assert forDocument.getFont(null, MSS.MSS_Pages.block_quote).family == "HELVETICA"
        assert forDocument.getFont(null, MSS.MSS_Pages.block_quote).style == MSSFontStyle.ITALIC
        assert forDocument.getFont(null, MSS.MSS_Pages.block_quote).size == 11
        assert forDocument.getColorPair(null, MSS.MSS_Pages.block_quote).foreground == MSSColor.GREY
        assert forDocument.getColorPair(null, MSS.MSS_Pages.block_quote).background == MSSColor.WHITE

        assert forDocument.getFont("testdiv", MSS.MSS_Pages.block_quote).family == "COURIER"
        assert forDocument.getFont("testdiv", MSS.MSS_Pages.block_quote).style == MSSFontStyle.ITALIC
        assert forDocument.getFont("testdiv", MSS.MSS_Pages.block_quote).size == 11
        assert forDocument.getColorPair("testdiv", MSS.MSS_Pages.block_quote).foreground == new MSSColor(color: "120:120:120")
        assert forDocument.getColorPair("testdiv", MSS.MSS_Pages.block_quote).background == new MSSColor(color: "10:11:12")

        //
        // front_page
        //

        MSS.ForFrontPage forFrontPage = mss.forFrontPage
        // copyright section sets no own setting. It defaults to the general settings for "front_page".
        assert forFrontPage.getFont(MSS.MSS_Front_Page.copyright).family == "HELVETICA"
        assert forFrontPage.getFont(MSS.MSS_Front_Page.copyright).size == 10
        assert forFrontPage.getFont(MSS.MSS_Front_Page.copyright).style == MSSFontStyle.NORMAL
        assert forFrontPage.getColorPair(MSS.MSS_Front_Page.copyright).foreground == MSSColor.BLACK
        assert forFrontPage.getColorPair(MSS.MSS_Front_Page.copyright).background == MSSColor.WHITE

        assert forFrontPage.getFont(MSS.MSS_Front_Page.title).family == "COURIER"
        assert forFrontPage.getFont(MSS.MSS_Front_Page.title).size == 25
        assert forFrontPage.getFont(MSS.MSS_Front_Page.title).style == MSSFontStyle.UNDERLINE
        assert forFrontPage.getColorPair(MSS.MSS_Front_Page.title).foreground == MSSColor.BLACK
        assert forFrontPage.getColorPair(MSS.MSS_Front_Page.title).background == MSSColor.WHITE

        assert forFrontPage.getAuthorLabel("not this!") == "Author:"
        assert forFrontPage.getVersionLabel("not this!") == "Version:"

        //
        // toc
        //

        MSS.ForTOC forTOC = mss.forTOC
        assert forTOC.getFont(MSS.MSS_TOC.h6).family == "HELVETICA"
        assert forTOC.getFont(MSS.MSS_TOC.h6).size == 10
        assert forTOC.getFont(MSS.MSS_TOC.h6).style == MSSFontStyle.NORMAL
        assert forTOC.getColorPair(MSS.MSS_TOC.h6).foreground == MSSColor.BLACK
        assert forTOC.getColorPair(MSS.MSS_TOC.h6).background == MSSColor.WHITE

        assert forTOC.getFont(MSS.MSS_TOC.h1).family == "COURIER"
        assert forTOC.getFont(MSS.MSS_TOC.h1).size == 9
        assert forTOC.getFont(MSS.MSS_TOC.h1).style == MSSFontStyle.BOLD
        assert forTOC.getColorPair(MSS.MSS_TOC.h1).foreground == MSSColor.BLACK
        assert forTOC.getColorPair(MSS.MSS_TOC.h1).background == MSSColor.WHITE

        //
        // pdf
        //

        assert mss.getPdfTrueTypeFontPath("HELVETICA") == "/fonts/ttf/hell_vetica.ttf"
    }

}
