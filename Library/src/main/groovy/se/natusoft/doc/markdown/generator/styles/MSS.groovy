package se.natusoft.doc.markdown.generator.styles

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import se.natusoft.doc.markdown.util.TestSafeResource
import se.natusoft.json.JSON
import se.natusoft.json.JSONArray
import se.natusoft.json.JSONBoolean
import se.natusoft.json.JSONErrorHandler
import se.natusoft.json.JSONNumber
import se.natusoft.json.JSONObject
import se.natusoft.json.JSONString
import se.natusoft.json.JSONValue

import java.lang.annotation.Documented

/**
 * Markdown Style Sheet. This can be used for all non HTML generators to allow users
 * provide style data at generation time.
 */
@CompileStatic
@TypeChecked
class MSS {

    // NOTE: These enums are also used to validate JSON object field names in addition to being used as input
    //       to several of the methods of this class.

    /**
     * This represents top level sections of the MSS
     */
    static enum MSS_Top {
        pdf,
        colors,
        document,
        front_page,
        toc
    }

    /**
     * This represents sub sections of the "document" section of the MSS.
     */
    static enum MSS_Document {
        pages,
        divs
    }

    /**
     * This represents style sections of the "pages" section.
     */
    static enum MSS_Pages {
        standard,
        h1, h2, h3, h4, h5, h6,
        block_quote,
        emphasis,
        strong,
        code,
        anchor,
        list_item,
        footer
    }

    /**
     * This represents style sections of the "front_page" section.
     */
    static enum MSS_Front_Page {
        title,
        subject,
        version,
        copyright,
        author,
        label
    }

    /**
     * This represents style sections of the "toc" section.
     */
    static enum MSS_TOC {
        toc,
        h1,
        h2,
        h3,
        h4,
        h5,
        h6
    }

    /**
     * This represents style values for a font.
     */
    static enum MSS_Font {
        family,
        size,
        style,
        /** Meant for h1 to h6 to produce an horizontal line under the heading text. */hr
    }

    /**
     * This represents style values for foreground and background color.
     */
    static enum MSS_Colors {
        color,
        background
    }

    /**
     * This represents values for the "pdf" section.
     */
    static enum MSS_PDF {
        ttf,
        family,
        path
    }

    //
    // Private Members
    //

    /** We hold the whole MSS files as a top level JSONObject in general. Some values are cached in other form when fetched. */
    private final JSONObject mss

    /** A cached instance of the "document" section of the MSS. */
    private JSONObject _document = null

    /** A cached instance of the "pages" section of the MSS. */
    private JSONObject _pages = null

    /** A cached instance of the "divs" section of the MSS. */
    private JSONObject _divs = null

    /** A cached instance of the "front_page" section of the MSS. */
    private JSONObject _frontPage = null

    /** A cached instance of the "toc" section of the MSS. */
    private JSONObject _toc = null

    /** Holds a cache of resolved color values. */
    private Map<String, MSSColor> colorMap = new HashMap<>()

    //
    // Constructors
    //

    /**
     * Creates a new MSS instance. This is private. Users have to use the static fromInputStream(...) method.
     *
     * @param mss The top level MSS JSON object.
     */
    private MSS(@NotNull JSONObject mss) {
        this.mss = mss
    }

    //
    // Methods
    //

    /**
     * A null safe way to get the caches "document" section of the MSS.
     */
    @NotNull private JSONObject getDocument() {
        if (this._document == null) {
            this._document = this.mss.getProperty(MSS_Top.document.name()) as JSONObject
            if (this._document == null) {
                this._document = new JSONObject()
            }
        }

        return this._document
    }

    /**
     * A null safe way to get the "pages" section of the MSS.
     */
    @NotNull private JSONObject getPages() {
        if (this._pages == null) {
            this._pages = this.document.getProperty(MSS_Document.pages.name()) as JSONObject
            if (this._pages == null) {
                this._pages = new JSONObject()
            }
        }

        return this._pages
    }

    /**
     * A null safe way to get the "divs" section of the MSS.
     */
    @NotNull private JSONObject getDivs() {
        if (this._divs == null) {
            this._divs = this.document.getProperty(MSS_Document.divs.name()) as JSONObject
            if (this._divs == null) {
                this._divs = new JSONObject()
            }
        }

        return this._divs
    }

    /**
     * A null safe way to get the "front_page" section of the MSS.
     */
    @NotNull private JSONObject getFrontPage() {
        if (this._frontPage == null) {
            this._frontPage = this.mss.getProperty(MSS_Top.front_page.name()) as JSONObject
            if (this._frontPage == null) {
                this._frontPage = new JSONObject()
            }
        }

        return this._frontPage
    }

    /**
     * A null safe way to get the "toc" section of the MSS.
     */
    @NotNull private JSONObject getTOC() {
        if (this._toc == null) {
            this._toc = this.mss.getProperty(MSS_Top.toc.name()) as JSONObject
            if (this._toc == null) {
                this._toc = new JSONObject()
            }
        }

        return this._toc
    }

    /**
     * Gets a JSONObject value and handles null by throwing an IOException.
     * <p/>
     * So why not use the null-safe operator in Groovy ? Because I don't just want to return a null back,
     * not providing any clue as to what went wrong! Any nulls here is probably because the user has done
     * something wrong in the MSS file, and the provided error message is to help the user find where.
     *
     * @param object The object to get value from.
     * @param field The name of the field to get.
     * @param errorMessage The error message to use on null result.
     *
     * @return a JSONValue instance.
     *
     * @throws IOException on null result.
     */
    private static JSONValue getNullSafe(@NotNull final JSONObject object, @NotNull final String field, @NotNull final String errorMessage)
            throws MSSException {
        JSONValue result = object.getProperty(field)
        if (result == null) throw new MSSException(errorMessage)
        return result
    }

    /**
     * Returns a path to a true type font as specified in the MSS file. Do note that if the specified path
     * returned is relative to something, the MSS file for example it is the responsibility of the user of
     * this call to resolve what it is relative to. This method can never provide a full path!
     *
     * @param name
     * @return
     * @throws IOException
     */
    @NotNull String getPdfTrueTypeFontPath(@NotNull String name) throws MSSException {
        String result = null

        JSONObject pdf = (JSONObject) this.mss.getProperty(MSS_Top.pdf.name())
        if (pdf == null) throw new MSSException("No TTF fonts specified under 'pdf' section in MSS file!")

        JSONArray ttfArray = (JSONArray) pdf.getProperty(MSS_PDF.ttf.name());
        if (ttfArray == null) throw new MSSException("No TTF fonts specified under 'pdf/ttf' seciton in MSS file!")

        ttfArray.asList.each { JSONValue entry -> // Notera att detta är en "closure"!
            if (!(entry instanceof JSONObject)) throw new MSSException("Bad MSS: pdf/ttf does not contain a list of objects!")

            JSONObject entryObject = entry as JSONObject
            if (getNullSafe(entryObject, MSS_PDF.family.name(), "Error: TTF entry without 'family' field!").toString() == name) {
                result = getNullSafe(entryObject, MSS_PDF.path.name(), "Error: TTF entry without 'path' field!").toString()
                return // from closure!
            }
        }

        if (result == null) throw new MSSException("Error: Asked for TTF font '${name}' was not defined in MSS!")

        return result
    }

    /**
     * Uses the "colors" section of the MSS to translate a color from a name to its numeric values.
     *
     * @param name The name to translate.
     *
     * @return a MSSColor object.
     *
     * @throws MSSException On reference to non defined color.
     */
    @NotNull private MSSColor lookupColor(@NotNull String name) throws MSSException {
        MSSColor color = this.colorMap.get(name)

        if (color == null) {
            if (name.contains(":")) {
                color = new MSSColor(color: name)
            } else {
                JSONObject jssColors = this.mss.getProperty(MSS_Top.colors.name()) as JSONObject
                if (jssColors == null) {
                    throw new MSSException("No color names have been defined in the \"colors\" section of the MSS file! " +
                            "'${name}' was asked for!")
                } else {
                    String colorValue = jssColors.getProperty(name)?.toString()
                    if (colorValue == null) throw new MSSException("The color '${name}' has not been defined in the \"colors\" section " +
                            "of the MSS file!")
                    color = new MSSColor(color: colorValue)
                }
            }

            if (color != null) {
                this.colorMap.put(name, color)
            }
        }

        return color
    }

    /**
     * Updates color pairs from data in the specified MSS section.
     *
     * @param colorPair The color pair to update.
     * @param section The section to update from.
     */
    private void updateMSSColorPairIfNotSet(@NotNull MSSColorPair colorPair, @Nullable final JSONObject section) {
        // If a null section is passed this code will not break, it will just not do anything at all in that case.
        JSONString color = section?.getProperty(MSS_Colors.color.name()) as JSONString
        if (color != null) {
            colorPair.updateForegroundIfNotSet(lookupColor(color.toString()))
        }
        color = section?.getProperty(MSS_Colors.background.name()) as JSONString
        if (color != null) {
            colorPair.updateBackgroundIfNotSet(lookupColor(color.toString()))
        }
    }

    /**
     * Updates font properties from data in the specified MSS section.
     *
     * @param font The font to update.
     * @param section The section to update from.
     */
    private static void updateMSSFontIfNotSet(@NotNull MSSFont font, @Nullable final JSONObject section) {
        JSONString family = section?.getProperty(MSS_Font.family.name()) as JSONString
        font.updateFamilyIfNotSet(family?.toString())

        JSONNumber size = section?.getProperty(MSS_Font.size.name()) as JSONNumber
        font.updateSizeIfNotSet((int)size != null ? size.toInt() : (int)-1)

        JSONString style = section?.getProperty(MSS_Font.style.name()) as JSONString
        if (style != null) {
            MSSFontStyle mssFontStyle = MSSFontStyle.valueOf(style.toString().toUpperCase())
            if (mssFontStyle == null) { throw new MSSException("'${style}' is not a valid font style!") }
            font.updateStyleIfNotSet(mssFontStyle)
        }

        JSONBoolean hr = section?.getProperty(MSS_Font.hr.name()) as JSONBoolean
        if (hr != null) {
            font.updateHrIfNotSet(hr.asBoolean)
        }
    }

    /**
     * Ensures that the specified color pair at least have default values set.
     *
     * @param colorPair The color pair to ensure.
     */
    private static @NotNull MSSColorPair ensureColorPair(@NotNull MSSColorPair colorPair) {
        colorPair.updateForegroundIfNotSet(MSSColor.BLACK)
        colorPair.updateBackgroundIfNotSet(MSSColor.WHITE)
        return colorPair
    }

    /**
     * Ensures that the specified font at least have default values set.
     *
     * @param font The font to ensure.
     */
    private static @NotNull MSSFont ensureFont(@NotNull MSSFont font) {
        font.updateFamilyIfNotSet("HELVETICA")
        font.updateSizeIfNotSet(10)
        font.updateStyleIfNotSet(MSSFontStyle.NORMAL)
        return font
    }

    /**
     * Returns a MSSColorPair containing foreground color and background color to use for the section.
     *
     * @param divName The name of a div whose setting will override default for section. Optional, can be null.
     * @param section A section type like h1, blockquote, etc.
     */
    @NotNull MSSColorPair getColorPairForDocument(@Nullable String divName, @NotNull MSS_Pages section) {
        MSSColorPair colorPair = new MSSColorPair()

        if (divName != null) {
            JSONObject div = this.divs.getProperty(divName) as JSONObject
            updateMSSColorPairIfNotSet(colorPair, div?.getProperty(section.name()) as JSONObject)
            updateMSSColorPairIfNotSet(colorPair, div)
        }

        updateMSSColorPairIfNotSet(colorPair, this.pages.getProperty(section.name()) as JSONObject)
        updateMSSColorPairIfNotSet(colorPair, this.pages)

        updateMSSColorPairIfNotSet(colorPair, this.document)

        return ensureColorPair(colorPair)
    }

    /**
     * Returns a MSSFont to use for the specified div and section.
     * @param divName The name of a div whose setting will override default for section. Optional, can be null.
     * @param section A section type like h1, blockquote, etc.
     */
    @NotNull MSSFont getFontForDocument(@Nullable String divName, @NotNull MSS_Pages section) {
        MSSFont font = new MSSFont()

        if (divName != null) {
            JSONObject div = this.divs.getProperty(divName) as JSONObject
            updateMSSFontIfNotSet(font, div?.getProperty(section.name()) as JSONObject)
            updateMSSFontIfNotSet(font, div)
        }

        updateMSSFontIfNotSet(font, this.pages.getProperty(section.name()) as JSONObject)
        updateMSSFontIfNotSet(font, this.pages)

        updateMSSFontIfNotSet(font, this.document)

        return ensureFont(font)
    }

    boolean isHr(MSS_Pages section) {
        JSONString hr = this.pages.getProperty(section.name()) as JSONString
        return hr != null && hr.toString().toLowerCase().equals("hr")
    }

    class ForDocument {
        @NotNull MSSColorPair getColorPair(@Nullable String divName, @NotNull MSS_Pages section) {
            return getColorPairForDocument(divName, section)
        }

        @NotNull MSSFont getFont(@Nullable String divName, @NotNull MSS_Pages section) {
            return getFontForDocument(divName, section)
        }

        boolean isHr(MSS_Pages section) {
            return MSS.this.isHr(section)
        }
    }

    private ForDocument forDocument = new ForDocument()

    ForDocument getForDocument() {return this.forDocument}

    /**
     * Returns a MSSColorPair containing foreground and background color to use for the section.
     *
     * @param section The front page section to get color pair for.
     */
    @NotNull MSSColorPair getColorPairForFrontPage(@NotNull MSS_Front_Page section) {
        MSSColorPair colorPair = new MSSColorPair()

        updateMSSColorPairIfNotSet(colorPair, this.frontPage.getProperty(section.name()) as JSONObject)
        updateMSSColorPairIfNotSet(colorPair, this.frontPage)

        return ensureColorPair(colorPair)
    }

    /**
     * Returns an MSSFont to use for the section.
     *
     * @param section The front page section to get font for.
     */
    @NotNull MSSFont getFontForFrontPage(@NotNull MSS_Front_Page section) {
        MSSFont font = new MSSFont()

        updateMSSFontIfNotSet(font, this.frontPage.getProperty(section.name()) as JSONObject)
        updateMSSFontIfNotSet(font, this.frontPage)

        return ensureFont(font)
    }

    /**
     * Returns the version label to use on the front page.
     *
     * @param _default The default label to use if none have been specified in the MSS.
     */
    @NotNull String getVersionLabelForFrontPage(@NotNull String _default) {
        String verLabel = _default
        JSONObject version = this.frontPage.getProperty(MSS_Front_Page.version.name()) as JSONObject
        if (version != null) {
            JSONString verLabelStr = version.getProperty(MSS_Front_Page.label.name()) as JSONString
            if (verLabelStr != null) {
                verLabel = verLabelStr.toString()
            }
        }

        return verLabel
    }

    /**
     * Returns the author label to use on the front page.
     *
     * @param _default The default label to use if none have been specified in the MSS.
     */
    @NotNull String getAuthorLabelForFrontPage(@NotNull String _default) {
        String verLabel = _default
        JSONObject version = this.frontPage.getProperty(MSS_Front_Page.author.name()) as JSONObject
        if (version != null) {
            JSONString verLabelStr = version.getProperty(MSS_Front_Page.label.name()) as JSONString
            if (verLabelStr != null) {
                verLabel = verLabelStr.toString()
            }
        }

        return verLabel
    }

    class ForFrontPage {
        @NotNull MSSColorPair getColorPair(@NotNull MSS_Front_Page section) {
            return getColorPairForFrontPage(section)
        }

        @NotNull MSSFont getFont(@NotNull MSS_Front_Page section) {
            return getFontForFrontPage(section)
        }

        @NotNull String getVersionLabel(@NotNull String _default) {
            return getVersionLabelForFrontPage(_default)
        }

        @NotNull String getAuthorLabel(@NotNull String _default) {
            return getAuthorLabelForFrontPage(_default)
        }
    }

    private ForFrontPage forFrontPage = new ForFrontPage()

    ForFrontPage getForFrontPage() {return this.forFrontPage}


    /**
     * Returns a MSSColorPair containing foreground and background color to use for the TOC section.
     *
     * @param section The TOC section to use the color pair for.
     */
    @NotNull MSSColorPair getColorPairForTOC(@NotNull MSS_TOC section) {
        MSSColorPair colorPair = new MSSColorPair()

        updateMSSColorPairIfNotSet(colorPair, this.TOC.getProperty(section.name()) as JSONObject)
        updateMSSColorPairIfNotSet(colorPair, this.TOC)

        return ensureColorPair(colorPair)
    }

    /**
     * Returns a MSSFont to use for the TOC section.
     *
     * @param section The TOC section to use the font for.
     */
    @NotNull MSSFont getFontForTOC(@NotNull MSS_TOC section) {
        MSSFont font = new MSSFont()

        updateMSSFontIfNotSet(font, this.TOC.getProperty(section.name()) as JSONObject)
        updateMSSFontIfNotSet(font, this.TOC)

        return ensureFont(font)
    }

    class ForTOC {
        @NotNull MSSColorPair getColorPair(@NotNull MSS_TOC section) {
            return getColorPairForTOC(section)
        }

        @NotNull MSSFont getFont(@NotNull MSS_TOC section) {
            return getFontForTOC(section)
        }
    }

    private ForTOC forTOC = new ForTOC()

    ForTOC getForTOC() {return this.forTOC}



    //
    // Util
    //

    //
    // Static Methods
    //

    /**
     * Loads styles from JSON looking like this:
     *
     * <pre>
     *   {
     *      "pdf": {
     *         "ttf": [
     *            {
     *                "family_desc": "A name to be used for 'family' property under 'fonts'."
     *                "family": "<name>",
     *                "path_desc": "A full or relative path to the .ttf file to load and make available."
     *                "path": "<path>/font.ttf"
     *            },
     *            ...
     *         ]
     *      },
     *
     *      "colors": {
     *         "white": "255:255:255",
     *         "black": "0:0:0",
     *         ...
     *      },
     *
     *      "document": {
     *         "color": "0:0:0",
     *         "background": "ff:ff:ff",
     *         "family": "HELVETICA",
     *         "size": 10,
     *         "style": "Normal",
     *
     *         "pages": {
     *            "block_quote": {
     *                "family": "HELVETICA",
     *                "size": 10,
     *                "style": "Italic",
     *                "color": "128:128:128",
     *                "background": "white"
     *            },
     *            "h1": {
     *                "family": "HELVETICA",
     *                "size": 20,
     *                "style": "BOLD",
     *                "color": "black",
     *                "background": "white"
     *            },
     *            "h2": {
     *                "family": "HELVETICA",
     *                "size": 18,
     *                "style": "BOLD",
     *                "hr": true
     *            },
     *            "h3": {
     *                "family": "HELVETICA",
     *                "size": 16,
     *                "style": "BOLD"
     *            },
     *            "h4": {
     *                "family": "HELVETICA",
     *                "size": 14,
     *                "style": "BOLD"
     *            },
     *            "h5": {
     *                "family": "HELVETICA",
     *                "size": 12,
     *                "style": "BOLD"
     *            },
     *            "h6": {
     *                "family": "HELVETICA",
     *                "size": 10,
     *                "style": "BOLD"
     *            },
     *            "emphasis": {
     *                "family": "HELVETICA",
     *                "size": 10,
     *                "style": "ITALIC"
     *            },
     *            "strong": {
     *                "family": "HELVETICA",
     *                "size": 10,
     *                "style": "BOLD"
     *            },
     *            "code": {
     *                "family": "HELVETICA",
     *                "size": 9,
     *                "style": "NORMAL",
     *                "color": "64:64:64",
     *                "background": "white"
     *            },
     *            "anchor": {
     *                "family": "HELVETICA",
     *                "size": 10,
     *                "style": "NORMAL",
     *                "color": "128:128:128",
     *                "background": "white"
     *            },
     *            "list_item": {
     *                "family": "HELVETICA",
     *                "size": 10
     *            },
     *            "footer": {
     *                "family": "HELVETICA",
     *                "size": 8
     *            },
     *         },
     *
     *         "divs": {
     *            "testdiv": {
     *               "block_quote": {
     *                  "family": "COURIER",
     *                   "color": "120:120:120",
     *                   "background": "10:11:12"
     *               }
     *            }
     *         },
     *      },
     *
     *      "front_page": {
     *         "color": "0:0:0",
     *         "background": "255:255:255",
     *         "family": "HELVETICA",
     *         "size": 10,
     *         "style": "NORMAL",
     *
     *         "title": {
     *             "family": "HELVETICA",
     *             "size": 25,
     *             "style": "NORMAL"
     *         },
     *         "subject": {
     *             "family": "HELVETICA",
     *             "size": 15,
     *             "style": "NORMAL"
     *         },
     *         "version": {
     *             "family": "HELVETICA",
     *             "size": 12,
     *             "style": "NORMAL",
     *             "label": "Version: "
     *         },
     *         "copyright": {
     *             "family": "HELVETICA",
     *             "size": 12,
     *             "style": "NORMAL"
     *         },
     *         "author": {
     *             "family": "HELVETICA",
     *             "size": 12,
     *             "style": "NORMAL",
     *             "label": "Author: "
     *         }
     *      },
     *
     *      "toc": {
     *         "color": "0:0:0",
     *         "background": "255:255:255",
     *         "family": "HELVETICA",
     *         "size": 10,
     *         "style": "NORMAL",
     *
     *         "h1": {
     *             "family": "HELVETICA",
     *             "size": 9,
     *             "style": "BOLD"
     *         },
     *         "h2": {
     *             "family": "HELVETICA",
     *             "size": 9,
     *             "style": "NORMAL"
     *         },
     *         "h3": {
     *             "family": "HELVETICA",
     *             "size": 9,
     *             "style": "NORMAL"
     *         },
     *         "h4": {
     *             "family": "HELVETICA",
     *             "size": 9,
     *             "style": "NORMAL"
     *         },
     *         "h5": {
     *             "family": "HELVETICA",
     *             "size": 9,
     *             "style": "NORMAL"
     *         },
     *         "h6": {
     *             "family": "HELVETICA",
     *             "size": 9,
     *             "style": "NORMAL"
     *         }
     *      }
     *   }
     * }
     * </pre>
     *
     * @param styleStream
     * @return
     * @throws IOException
     */
    static @NotNull MSS fromInputStream(@NotNull final InputStream styleStream) throws IOException {
        JSONObject mss = (JSONObject) JSON.read(styleStream, new JSONErrorHandler() {
            @Override
            void warning(String message) {
                System.err.println(message);
            }

            @Override
            void fail(String message, Throwable cause) throws RuntimeException {
                throw new RuntimeException(message, cause)
            }
        });

        validateMSS(mss, "/")

        new MSS(mss)
    }

    /**
     * Returns the default MSS.
     *
     * @throws IOException Theoretically this should never happen ...
     */
    static @NotNull MSS defaultMSS() throws IOException {
        InputStream mssStream = TestSafeResource.getResource("mss/default.mss")
        try {
            return fromInputStream(mssStream)
        }
        finally {
            mssStream.close()
        }
    }

    /**
     * Validates the MSS property names, but not the structure. This recurse down sub objects.
     *
     * @param jssPart The JSONObject to validate.
     * @param path The current path (to help provide a better error message).
     * @throws IOException On validation failures.
     */
    private static void validateMSS(@NotNull final JSONObject jssPart, @NotNull String path) throws IOException {
        jssPart.propertyNames.each { JSONString name ->
            if (!validName(name.toString())) {
                if (!path.endsWith("divs/") && !path.endsWith("colors/")) {
                    throw new IOException("Bad MSS field name: '${name}' in path '${path}'!")
                }
            }
            JSONValue value = jssPart.getProperty(name)
            if (value instanceof JSONObject) {
                validateMSS((JSONObject) value, "${path}${name}/")
            }
        }
    }

    /**
     * Validates the field names that is part of an MSS JSONObject.
     *
     * @param name The name to validate.
     *
     * @return true on OK name, false otherwise.
     */
    private static boolean validName(@NotNull String name) {
        boolean ok = false

        if (!ok) {
            ok = safe( { MSS_Top.valueOf(name) != null } )
        }
        if (!ok) {
            ok = safe( { MSS_Document.valueOf(name) != null } )
        }
        if (!ok) {
            ok = safe( { MSS_Pages.valueOf(name) != null } )
        }
        if (!ok) {
            ok = safe( { MSS_Front_Page.valueOf(name) != null } )
        }
        if (!ok) {
            ok = safe( { MSS_TOC.valueOf(name) != null } )
        }
        if (!ok) {
            ok = safe( { MSS_Font.valueOf(name) != null } )
        }
        if (!ok) {
            ok = safe( { MSS_Colors.valueOf(name) != null } )
        }
        if (!ok) {
            ok = safe( { MSS_PDF.valueOf(name) != null } )
        }

        return ok
    }

    private static boolean safe(Closure<Boolean> enumCheck) {
        try {
            return enumCheck.call()
        }
        catch (IllegalArgumentException iae) {}

        return false
    }
}
