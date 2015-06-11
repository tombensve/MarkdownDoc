package se.natusoft.doc.markdown.generator.pdfgenerator

import com.itextpdf.text.BaseColor
import com.itextpdf.text.Font

/**
 * This represents all PDF styles.
 */
class PDFStyles {

    Font FONT_DEFAULT = new Font(Font.FontFamily.HELVETICA, 10)
    Font FONT_BLOCKQUOTE = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC, BaseColor.GRAY)
    Font FONT_H1 = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD)
    Font FONT_H2 = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD)
    Font FONT_H3 = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD)
    Font FONT_H4 = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD)
    Font FONT_H5 = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD)
    Font FONT_H6 = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD)
    Font FONT_EMPHASIS = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC)
    Font FONT_STRONG = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD)
    Font FONT_CODE = new Font(Font.FontFamily.COURIER, 9, Font.NORMAL, BaseColor.DARK_GRAY)
    Font FONT_ANCHOR = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.GRAY)
    Font FONT_LIST_ITEM = new Font(Font.FontFamily.HELVETICA, 10)
    Font FONT_FOOTER = new Font(Font.FontFamily.HELVETICA, 8)
    Font FONT_TOC = new Font(Font.FontFamily.HELVETICA, 9)
    Font FONT_TOC_H1 = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD)

    PDFColor COLOR_BACKGROUND = new PDFColor("ff:ff:ff")
    PDFColor COLOR_BLOCKQUOTE = new PDFColor("0:0:0")
    PDFColor COLOR_CODE = new PDFColor("0:0:0")

    public static PDFStyles fromStyleFile(String path) throws IOException {
        FileInputStream styleFileStream = new FileInputStream(path)
        try {
            return fromInputStream(styleFileStream)
        }
        finally {
            styleFileStream.close()
        }
    }

    public static PDFStyles fromStyles(String styles) throws IOException {
        ByteArrayInputStream stylesStream = styles.getBytes("UTF-8")
        try {
            return fromInputStream(stylesStream)
        }
        finally {
            stylesStream.close()
        }
    }

}
