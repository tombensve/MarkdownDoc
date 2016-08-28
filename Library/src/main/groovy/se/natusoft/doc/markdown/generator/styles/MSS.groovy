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
 *         2015-07-15: Created!
 *
 */
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

/**
 * Markdown Style Sheet. This can be used for all non HTML generators to allow users
 * provide style data at generation time.
 */
@CompileStatic
@TypeChecked
class MSS {

    // NOTE: These enums are also used to validate JSON object field names in addition to being used as input
    //       to several of the methods of this class.

    static interface Section {}

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
     * This represents values of the "document" section of the MSS that provides general page data.
     */
    static enum MSS_Page {
        topMargin,
        leftMargin,
        rightMargin,
        bottomMargin
    }

    /**
     * This represents style sections of the "pages" section.
     */
    static enum MSS_Pages implements Section {
        standard,
        h1, h2, h3, h4, h5, h6,
        block_quote,
        emphasis,
        strong,
        code,
        anchor,
        list_item,
        image,
        horizontal_ruler,
        footer
    }

    static enum MSS_HR {
        thickness,
        color
    }

    /**
     * This represents a rendering style for code blocks.
     */
    static enum MSS_Boxed {

        /** This is true or false. */
        boxed,
        boxedColor
    }

    /**
     * This represents style sections of the "front_page" section.
     */
    static enum MSS_Front_Page implements Section {
        title,
        subject,
        version,
        copyright,
        author,
        image
    }

    /**
     * This represents style sections of the "toc" section.
     */
    static enum MSS_TOC implements Section {
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
     * Values for section number behavior.
     */
    static enum MSS_SectionNumber {
        sectionNumberYOffset,
        sectionNumberXOffset
    }

    /**
     * This represents values for the "pdf" section.
     */
    static enum MSS_PDF {
        extFonts,
        family,
        path,
        encoding
    }

    /**
     * Represents values for images.
     */
    static enum MSS_IMAGE {
        /** The percent to scale images. */
        imgScalePercent,

        /** Image alignment. LEFT, MIDDLE, RIGHT */
        imgAlign,

        /** The number of degrees to rotate image. */
        imgRotateDegrees,

        /**
         * If true then text will flow around the image. If false the image will be its own paragraph and text will
         * continue below it.
         */
        imgFlow,

        /** When 'imgFlow' is true this is the margin around the image. */
        imgFlowMargin,

        /** Overriden X coordinate of image. */
        imgX,

        /** Overridden Y coordinate of image. */
        imgY
    }

    //
    // Properties
    //

    /** The current divs. */
    LinkedList<String> currentDivs

    //
    // Private Members
    //

    /**
     * We hold the whole MSS files as a top level JSONObject in general. Some values are cached in other form when
     * fetched.
     */
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
    private final Map<String, MSSColor> colorMap = new HashMap<>()

    //
    // Constructors
    //

    /**
     * Creates a new MSS instance. This is private. Users have to use the static fromInputStream(...) method.
     *
     * @param mss The top level MSS JSON object.
     */
    private MSS(@NotNull final JSONObject mss) {
        this.mss = mss
    }

    //
    // Methods
    //

    /**
     * A null safeNameValidation way to get the cached "document" section of the MSS.
     */
    @NotNull private JSONObject getDocument() {
        if (this._document == null) {
            this._document = this.mss.getProperty(MSS_Top.document.name()) as JSONObject
            if (this._document == null) {
                this._document = new JSONObject()
            }
        }

        this._document
    }

    /**
     * A null safeNameValidation way to get the "pages" section of the MSS.
     */
    @NotNull private JSONObject getPages() {
        if (this._pages == null) {
            this._pages = this.document.getProperty(MSS_Document.pages.name()) as JSONObject
            if (this._pages == null) {
                this._pages = new JSONObject()
            }
        }

        this._pages
    }

    /**
     * A null safeNameValidation way to get the "divs" section of the MSS.
     */
    @NotNull private JSONObject getDivs() {
        if (this._divs == null) {
            this._divs = this.document.getProperty(MSS_Document.divs.name()) as JSONObject
            if (this._divs == null) {
                this._divs = new JSONObject()
            }
        }

        this._divs
    }

    /**
     * A null safeNameValidation way to get the "front_page" section of the MSS.
     */
    @NotNull private JSONObject getFrontPage() {
        if (this._frontPage == null) {
            this._frontPage = this.mss.getProperty(MSS_Top.front_page.name()) as JSONObject
            if (this._frontPage == null) {
                this._frontPage = new JSONObject()
            }
        }

        this._frontPage
    }

    /**
     * A null safeNameValidation way to get the "toc" section of the MSS.
     */
    @NotNull private JSONObject getTOC() {
        if (this._toc == null) {
            this._toc = this.mss.getProperty(MSS_Top.toc.name()) as JSONObject
            if (this._toc == null) {
                this._toc = new JSONObject()
            }
        }

        this._toc
    }

    /**
     * Gets a JSONObject value and handles null by throwing an IOException.
     * <p/>
     * So why not use the null-safeNameValidation operator in Groovy ? Because I don't just want to return a null back,
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
    private static JSONValue getJSONValue(@NotNull final JSONObject object, @NotNull final String field,
                                         @NotNull final String errorMessage)
            throws MSSException {
        JSONValue result = object.getProperty(field)
        if (result == null) throw new MSSException(message: errorMessage)

        result
    }

    /**
     * Returns a path to an external font as specified in the MSS file. Do note that if the specified path
     * returned is relative to something, the MSS file for example it is the responsibility of the user of
     * this call to resolve what it is relative to. This method can never provide a full path!
     *
     * @param name The name of the font to find an MSSExtFont entry for.
     *
     * @throws MSSException on failure to resolve name.
     */
    @NotNull MSSExtFont getPdfExternalFontPath(@NotNull String name) throws MSSException {
        MSSExtFont result = null

        JSONObject pdf = (JSONObject) this.mss.getProperty(MSS_Top.pdf.name())
        if (pdf == null) {
            throw new MSSException(message: "No TTF fonts specified under 'pdf' section in MSS file!")
        }

        JSONArray extFontsArray = (JSONArray) pdf.getProperty(MSS_PDF.extFonts.name());
        if (extFontsArray == null) {
            throw new MSSException(message: "No external fonts specified under 'pdf/extFonts' seciton in MSS file!")
        }

        extFontsArray.asList.each { JSONValue entry ->
            if (!(entry instanceof JSONObject)) {
                throw new MSSException(message: "Bad MSS: pdf/extFonts does not contain a list of objects!")
            }

            if (getJSONValue(
                    entry as JSONObject,
                    MSS_PDF.family.name(),
                    "Error: extFonts entry without 'family' field!"
               ).toString() == name
            ) {

                result = new MSSExtFont(
                    fontPath: getJSONValue(
                            entry as JSONObject,
                            MSS_PDF.path.name(),
                            "Error: extFonts entry without 'path' field!"
                    ).toString(),
                    encoding: getJSONValue(
                            entry as JSONObject,
                            MSS_PDF.encoding.name(),
                            "Error ectFonts entry without 'encoding' field!"
                    ).toString()
                )

                return // from each closure!
            }
        }

        if (result == null) {
            throw new MSSException(message: "Error: Asked for extFonts font '${name}' was not defined in MSS!")
        }

        result
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
    private @NotNull MSSColor lookupColor(@NotNull final String name) throws MSSException {
        MSSColor color = this.colorMap.get(name)

        if (color == null) {
            if (name.contains(":") || name.startsWith("#")) {
                color = new MSSColor(color: name)
            } else {
                final JSONObject mssColors = this.mss.getProperty(MSS_Top.colors.name()) as JSONObject
                if (mssColors == null) {
                    throw new MSSException(message: "No color names have been defined in the \"colors\" section of the MSS file! " +
                            "'${name}' was asked for!")
                } else {
                    final String colorValue = mssColors.getProperty(name)?.toString()
                    if (colorValue == null) throw new MSSException(message: "The color '${name}' has not been defined in the \"colors\" section " +
                            "of the MSS file!")
                    color = new MSSColor(color: colorValue)
                }
            }

            this.colorMap.put(name, color)
        }

        color
    }

    /**
     * Updates color pairs from data in the specified MSS section.
     *
     * @param colorPair The color pair to update.
     * @param section The section to update from.
     */
    private void updateMSSColorPairIfNotSet(@NotNull final MSSColorPair colorPair, @Nullable final JSONObject section) {
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
    private static void updateMSSFontIfNotSet(@NotNull final MSSFont font, @Nullable final JSONObject section) {
        final JSONString family = section?.getProperty(MSS_Font.family.name()) as JSONString
        font.updateFamilyIfNotSet(family?.toString())

        final JSONNumber size = section?.getProperty(MSS_Font.size.name()) as JSONNumber
        font.updateSizeIfNotSet(size != null ? size.toInt() : -1i)

        final JSONString style = section?.getProperty(MSS_Font.style.name()) as JSONString
        if (style != null) {
            MSSFontStyle mssFontStyle = MSSFontStyle.valueOf(style.toString().toUpperCase())
            if (mssFontStyle == null) { throw new MSSException(message: "'${style}' is not a valid font style!") }
            font.updateStyleIfNotSet(mssFontStyle)
        }

        final JSONBoolean hr = section?.getProperty(MSS_Font.hr.name()) as JSONBoolean
        if (hr != null) {
            font.updateHrIfNotSet(hr.asBoolean)
        }
    }

    /**
     * Updates image properties from data in the specified MSS section.
     *
     * @param image The image properties to update.
     * @param section The section to update from.
     */
    private static void updateMSSImageIfNotSet(@NotNull final MSSImage image, @Nullable final JSONObject section) {
        final JSONNumber scale = section?.getProperty(MSS_IMAGE.imgScalePercent.name()) as JSONNumber
        image.updateScaleIfNotSet(scale)

        final JSONString align = section?.getProperty(MSS_IMAGE.imgAlign.name()) as JSONString
        image.updateAlignIfNotSet(align)

        final JSONNumber rotate = section?.getProperty(MSS_IMAGE.imgRotateDegrees.name()) as JSONNumber
        image.updateRotateIfNotSet(rotate)

        final JSONBoolean flow = section?.getProperty(MSS_IMAGE.imgFlow.name()) as JSONBoolean
        image.updateImgFlowIfNotSet(flow)

        final JSONNumber flowMargin = section?.getProperty(MSS_IMAGE.imgFlowMargin.name()) as JSONNumber
        image.updateImgFlowMarginIfNotSet(flowMargin)

        final JSONNumber imgX = section?.getProperty(MSS_IMAGE.imgX.name()) as JSONNumber
        image.updateImgXIfNotSet(imgX)

        final JSONNumber imgY = section?.getProperty(MSS_IMAGE.imgY.name()) as JSONNumber
        image.updateImgYIfNotSet(imgY)
    }

    /**
     * Ensures that the specified color pair at least have default values set.
     *
     * @param colorPair The color pair to ensure.
     */
    private static @NotNull MSSColorPair ensureColorPair(@NotNull final MSSColorPair colorPair) {
        colorPair.updateForegroundIfNotSet(MSSColor.BLACK)
        colorPair.updateBackgroundIfNotSet(MSSColor.WHITE)

        colorPair
    }

    /**
     * Ensures that the specified font at least have default values set.
     *
     * @param font The font to ensure.
     */
    private static @NotNull MSSFont ensureFont(@NotNull final MSSFont font) {
        font.updateFamilyIfNotSet("HELVETICA")
        font.updateSizeIfNotSet(10)
        font.updateStyleIfNotSet(MSSFontStyle.NORMAL)

        font
    }

    /**
     * Ensures that the specified image data at lest have default values set.
     *
     * @param image The image data to ensure.
     */
    private static @NotNull MSSImage ensureImage(@NotNull final MSSImage image) {
        image.updateScaleIfNotSet(new JSONNumber(60.0f))
        image.updateAlignIfNotSet(new JSONString("LEFT"))
        image.updateRotateIfNotSet(new JSONNumber(0.0f))
        image.updateImgFlowIfNotSet(new JSONBoolean(false))
        image.updateImgFlowMarginIfNotSet(new JSONNumber(4.0f))
        image
    }

    /**
     * Returns a MSSColorPair containing foreground color and background color to use for the section.
     *
     * @param section A section type like h1, blockquote, etc.
     */
    @NotNull MSSColorPair getColorPairForDocument(@NotNull final MSS_Pages section) {
        MSSColorPair colorPair = new MSSColorPair()

        if (this.currentDivs != null) {
            this.currentDivs.each { String divName ->
                JSONObject div = this.divs.getProperty(divName) as JSONObject
                updateMSSColorPairIfNotSet(colorPair, div?.getProperty(section.name()) as JSONObject)
                updateMSSColorPairIfNotSet(colorPair, div)
            }
        }

        updateMSSColorPairIfNotSet(colorPair, this.pages.getProperty(section.name()) as JSONObject)
        updateMSSColorPairIfNotSet(colorPair, this.pages)

        updateMSSColorPairIfNotSet(colorPair, this.document)

        ensureColorPair(colorPair)
    }

    /**
     * Returns a MSSFont to use for the specified div and section.
     *
     * @param section A section type like h1, blockquote, etc.
     */
    @NotNull MSSFont getFontForDocument(@NotNull final MSS_Pages section) {
        MSSFont font = new MSSFont()

        if (this.currentDivs != null) {
            this.currentDivs.each { String divName ->
                JSONObject div = this.divs.getProperty(divName) as JSONObject
                updateMSSFontIfNotSet(font, div?.getProperty(section.name()) as JSONObject)
                updateMSSFontIfNotSet(font, div)
            }
        }

        updateMSSFontIfNotSet(font, this.pages.getProperty(section.name()) as JSONObject)
        updateMSSFontIfNotSet(font, this.pages)

        updateMSSFontIfNotSet(font, this.document)

        ensureFont(font)
    }

    /**
     * Returns a MSSImage containing image format information.
     */
    @NotNull MSSImage getImageStyleForDocument() {
        final MSSImage image = new MSSImage()

        JSONObject standard

        if (this.currentDivs != null) {
            this.currentDivs.each { final String divName ->
                final JSONObject div = this.divs.getProperty(divName) as JSONObject

                standard = div?.getProperty(MSS_Pages.standard.name()) as JSONObject
                if (standard != null) {
                    updateMSSImageIfNotSet(image, standard.getProperty(MSS_Pages.image.name()) as JSONObject)
                }
                updateMSSImageIfNotSet(image, standard)
                updateMSSImageIfNotSet(image, div?.getProperty(MSS_Pages.image.name()) as JSONObject)
                updateMSSImageIfNotSet(image, div)
            }
        }

        standard = this.pages.getProperty(MSS_Pages.standard.name()) as JSONObject
        if (standard != null) {
            updateMSSImageIfNotSet(image, standard.getProperty(MSS_Pages.image.name()) as JSONObject)
        }
        updateMSSImageIfNotSet(image, standard)
        updateMSSImageIfNotSet(image, this.pages?.getProperty(MSS_Pages.image.name()) as JSONObject)
        updateMSSImageIfNotSet(image, this.pages)
        updateMSSImageIfNotSet(image, this.document?.getProperty(MSS_Pages.image.name()) as JSONObject)
        updateMSSImageIfNotSet(image, this.document)

        ensureImage(image)
    }

    /**
     * Returns the top margin of a page.
     */
    @NotNull float getTopMarginForDocument() {
        JSONNumber margin = this.document?.getProperty(MSS_Page.topMargin.name()) as JSONNumber
        margin != null ? margin.toFloat() : 50.0f
    }

    /**
     * Returns the left margin of a page.
     */
    @NotNull float getLeftMarginForDocument() {
        JSONNumber margin = this.document?.getProperty(MSS_Page.leftMargin.name()) as JSONNumber
        margin != null ? margin.toFloat() : 50.0f
    }

    /**
     * Returns the right margin of a page.
     */
    @NotNull float getRightMarginForDocument() {
        JSONNumber margin = this.document?.getProperty(MSS_Page.rightMargin.name()) as JSONNumber
        margin != null ? margin.toFloat() : 50.0f
    }

    /**
     * Returns the bottom margin of a page.
     */
    @NotNull float getBottomMarginForDocument() {
        JSONNumber margin = this.document?.getProperty(MSS_Page.bottomMargin.name()) as JSONNumber
        margin != null ? margin.toFloat() : 50.0f
    }

    /**
     * Returns true if the section should be boxed.
     *
     * @param section The section to check.
     */
    @NotNull boolean isBoxedForDocument(MSS_Pages section) {
        JSONBoolean boxed = getSingleValueForDocument(MSS_Boxed.boxed.name(), section.name()) as JSONBoolean
        boxed != null ? boxed.asBoolean : false
    }

    /**
     * Returns the box color for a boxed section.
     *
     * @param section The section to get box color for.
     */
    @NotNull MSSColor getBoxColorForDocument(MSS_Pages section) {
        JSONString boxedColor = getSingleValueForDocument(MSS_Boxed.boxedColor.name(), section.name()) as JSONString
        boxedColor != null ? lookupColor(boxedColor.toString()) : new MSSColor(color: "240:240:240")
    }

    /**
     * Returns the thickness of an hr.
     */
    float getHrThicknessForDocument() {
        JSONNumber hrThickness = getSingleValueForDocument(MSS_HR.thickness.name(), MSS_Pages.horizontal_ruler.name()) as JSONNumber
        hrThickness != null ? hrThickness.toFloat() : 0.5f
    }

    /**
     * Returns the color of an hr.
     */
    String getHrColorForDocument() {
        JSONString hrColor = getSingleValueForDocument(MSS_HR.color.name(), MSS_Pages.horizontal_ruler.name()) as JSONString
        hrColor != null ? hrColor : "0:0:0"
    }

    /**
     * Returns the section number Y offset.
     *
     * @param section The section to get Y offset for.
     */
    float getSectionNumberYOffsetForDocument(MSS_Pages section) {
        JSONNumber snYOff = getSingleValueForDocument(MSS_SectionNumber.sectionNumberYOffset.name(), section.name()) as JSONNumber
        snYOff != null ? snYOff.toFloat() : 0.0f
    }

    /**
     * Returns the section number X offset.
     *
     * @param section The section to get X offset for.
     */
    float getSectionNumberXOffsetForDocument(MSS_Pages section) {
        JSONNumber snXOff = getSingleValueForDocument(MSS_SectionNumber.sectionNumberXOffset.name(), section.name()) as JSONNumber
        snXOff != null ? snXOff.toFloat() : 0.0f
    }

    /**
     * Generic value fetch.
     *
     * @param checkIn The MSS JSON object to check in.
     * @param propName The name of the JSON object value to fetch.
     * @param sectionName The section to get the MSS JSON object for.
     */
    private static JSONValue checkSingleValue(JSONObject checkIn, String propName, String sectionName) {
        JSONValue hrValue = null
        JSONObject hrObject = checkIn?.getProperty(sectionName) as JSONObject
        if (hrObject != null) {
            hrValue = hrObject.getProperty(propName)
        }

        if (hrValue == null) {
            hrValue = checkIn?.getProperty(propName)
        }

        hrValue
    }

    /**
     * Fetches a specified value from a specified section of the MSS JSON document.
     *
     * @param valueName The name of the value to fetch.
     * @param sectionName The name of the section in which to look for the value.
     */
    private JSONValue getSingleValueForDocument(String valueName, String sectionName) {
        JSONValue value = null
        if (this.currentDivs != null) {
            this.currentDivs.each { String divName ->
                JSONObject div = this.divs.getProperty(divName) as JSONObject
                JSONValue pValue = checkSingleValue(div, valueName, sectionName)
                if (pValue != null) {
                    value = pValue
                }
            }
        }

        if (value == null) {
            JSONValue pValue = checkSingleValue(this.pages, valueName, sectionName)
            if (pValue != null) {
                value = pValue
            }
        }

        if (value == null) {
            JSONValue pValue = checkSingleValue(this.document, valueName, sectionName)
            if (pValue != null) {
                value = pValue
            }
        }

        value
    }


    class ForDocument {
        @NotNull MSSColorPair getColorPair(@NotNull final MSS_Pages section) {
            getColorPairForDocument(section)
        }

        @NotNull MSSFont getFont(@NotNull final MSS_Pages section) {
            getFontForDocument(section)
        }

        @NotNull MSSImage getImageStyle() {
            getImageStyleForDocument()
        }

        @NotNull float getTopMargin() {
            getTopMarginForDocument()
        }

        @NotNull float getLeftMargin() {
            getLeftMarginForDocument()
        }

        @NotNull float getRightMargin() {
            getRightMarginForDocument()
        }

        @NotNull float getBottomMargin() {
            getBottomMarginForDocument()
        }

        boolean isBoxed(@NotNull MSS_Pages section) {
            isBoxedForDocument(section)
        }

        @NotNull MSSColor getBoxColor(@NotNull MSS_Pages section) {
            getBoxColorForDocument(section)
        }

        float getHrThickness() {
            getHrThicknessForDocument()
        }

        @NotNull MSSColor getHrColor() {
            lookupColor(getHrColorForDocument())
        }

        float getSectionNumberYOffset(@NotNull MSS_Pages section) {
            getSectionNumberYOffsetForDocument(section)
        }

        float getSectionNumberXOffset(@NotNull MSS_Pages section) {
            getSectionNumberXOffsetForDocument(section)
        }
    }

    private ForDocument forDocument = new ForDocument()

    ForDocument getForDocument() { this.forDocument }

    /**
     * Returns a MSSColorPair containing foreground and background color to use for the section.
     *
     * @param section The front page section to get color pair for.
     */
    @NotNull MSSColorPair getColorPairForFrontPage(@NotNull final MSS_Front_Page section) {
        MSSColorPair colorPair = new MSSColorPair()

        updateMSSColorPairIfNotSet(colorPair, this.frontPage.getProperty(section.name()) as JSONObject)
        updateMSSColorPairIfNotSet(colorPair, this.frontPage)

        ensureColorPair(colorPair)
    }

    /**
     * Returns an MSSFont to use for the section.
     *
     * @param section The front page section to get font for.
     */
    @NotNull MSSFont getFontForFrontPage(@NotNull final MSS_Front_Page section) {
        MSSFont font = new MSSFont()

        updateMSSFontIfNotSet(font, this.frontPage.getProperty(section.name()) as JSONObject)
        updateMSSFontIfNotSet(font, this.frontPage)

        ensureFont(font)
    }

    /**
     * Returns an MSSImage with image info.
     */
    @NotNull MSSImage getImageDataForFrontPage() {
        MSSImage image = new MSSImage()

        updateMSSImageIfNotSet(image, this.frontPage.getProperty(MSS_Front_Page.image.name()) as JSONObject)
        updateMSSImageIfNotSet(image, this.frontPage)

        ensureImage(image)
    }

    class ForFrontPage {
        @NotNull MSSColorPair getColorPair(@NotNull final MSS_Front_Page section) {
            getColorPairForFrontPage(section)
        }

        @NotNull MSSFont getFont(@NotNull final MSS_Front_Page section) {
            getFontForFrontPage(section)
        }

        @NotNull MSSImage getImageData() {
            getImageDataForFrontPage()
        }
    }

    private ForFrontPage forFrontPage = new ForFrontPage()

    ForFrontPage getForFrontPage() { this.forFrontPage }


    /**
     * Returns a MSSColorPair containing foreground and background color to use for the TOC section.
     *
     * @param section The TOC section to use the color pair for.
     */
    @NotNull MSSColorPair getColorPairForTOC(@NotNull final MSS_TOC section) {
        MSSColorPair colorPair = new MSSColorPair()

        updateMSSColorPairIfNotSet(colorPair, this.TOC.getProperty(section.name()) as JSONObject)
        updateMSSColorPairIfNotSet(colorPair, this.TOC)

        ensureColorPair(colorPair)
    }

    /**
     * Returns a MSSFont to use for the TOC section.
     *
     * @param section The TOC section to use the font for.
     */
    @NotNull MSSFont getFontForTOC(@NotNull final MSS_TOC section) {
        MSSFont font = new MSSFont()

        updateMSSFontIfNotSet(font, this.TOC.getProperty(section.name()) as JSONObject)
        updateMSSFontIfNotSet(font, this.TOC)

        ensureFont(font)
    }

    class ForTOC {
        @NotNull MSSColorPair getColorPair(@NotNull final MSS_TOC section) {
            getColorPairForTOC(section)
        }

        @NotNull MSSFont getFont(@NotNull final MSS_TOC section) {
            getFontForTOC(section)
        }
    }

    private ForTOC forTOC = new ForTOC()

    ForTOC getForTOC() { this.forTOC }


    //
    // Static Methods
    //

    /**
     * Loads styles from JSON looking like this:
     *
     * <pre>
     *   {
     *      "pdf": {
     *         "extFonts": [
     *            {
     *                "family": "<name>",
     *                "encoding": "<encoding>",
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
     *         "topMargin": 50,
     *         "leftMargin": 50,
     *         "rightMargin": 50,
     *         "bottomMargin": 50,
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
     *                "background": "white",
     *                "boxed": true
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
     *            "image": {
     *                "scalePercent": 60.0,
     *                "align": "LEFT/MIDDLE/RIGHT",
     *                "rotateDegrees": 45.0
     *            }
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
        final JSONObject mss = (JSONObject) JSON.read(styleStream, new JSONErrorHandler() {
            @Override
            void warning(@NotNull final String message) {
                System.err.println(message);
            }

            @Override
            void fail(@NotNull final String message, @Nullable final Throwable cause) throws RuntimeException {
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
        final InputStream mssStream = TestSafeResource.getResource("mss/default.mss")
        try {
            fromInputStream(mssStream)
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
    private static void validateMSS(@NotNull final JSONObject jssPart, @NotNull final String path) throws IOException {

        jssPart.propertyNames.each { final JSONString name ->
            if (!validName(name.toString())) {
                if (!path.endsWith("divs/") && !path.endsWith("colors/")) {
                    throw new IOException("Bad MSS field name: '${name}' in path '${path}'!")
                }
            }
            final JSONValue value = jssPart.getProperty(name)
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
    private static boolean validName(@NotNull final String name) {
        boolean ok = false

        if (!ok) {
            ok = safeNameValidation { MSS_Top.valueOf(name) != null }
        }
        if (!ok) {
            ok = safeNameValidation { MSS_Document.valueOf(name) != null }
        }
        if (!ok) {
            ok = safeNameValidation { MSS_Pages.valueOf(name) != null }
        }
        if (!ok) {
            ok = safeNameValidation { MSS_Front_Page.valueOf(name) != null }
        }
        if (!ok) {
            ok = safeNameValidation { MSS_TOC.valueOf(name) != null }
        }
        if (!ok) {
            ok = safeNameValidation { MSS_Font.valueOf(name) != null }
        }
        if (!ok) {
            ok = safeNameValidation { MSS_Colors.valueOf(name) != null }
        }
        if (!ok) {
            ok = safeNameValidation { MSS_PDF.valueOf(name) != null }
        }
        if (!ok) {
            ok = safeNameValidation { MSS_IMAGE.valueOf(name) != null }
        }
        if (!ok) {
            ok = safeNameValidation { MSS_Page.valueOf(name) != null }
        }
        if (!ok) {
            ok = safeNameValidation { MSS_Boxed.valueOf(name) != null }
        }
        if (!ok) {
            ok = safeNameValidation { MSS_HR.valueOf(name) != null }
        }
        if (!ok) {
            ok = safeNameValidation { MSS_SectionNumber.valueOf(name) != null }
        }
        if (!ok) {
            if (name.startsWith("#") || name.startsWith("*")) ok = true
        }

        ok
    }

    private static boolean safeNameValidation(@NotNull final Closure<Boolean> enumCheck) {
        try {
            return enumCheck.call()
        }
        catch (final IllegalArgumentException ignored) { }

        false
    }
}
