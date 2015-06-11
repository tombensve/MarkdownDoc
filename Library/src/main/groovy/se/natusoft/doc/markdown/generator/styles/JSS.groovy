package se.natusoft.doc.markdown.generator.styles

/**
 * JSON Style Sheet. This can be by for all non HTML generators to allow users
 * provide style data at generation time.
 */
class JSS {

    /**
     * Loads styles from JSON looking like this:
     *
     * <pre>
     *   {
     *      "color": "0:0:0",
     *      "background": "255:255:255",
     *      "family": "HELVETICA",
     *      "size": 10,
     *      "style": "NORMAL",
     *
     *      "ttf_desc": "This section is only valid for PDF documents!",
     *      "ttf": {
     *         "include": [
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
     *      "document": {
     *         "color": "0:0:0",
     *         "background": "ff:ff:ff",
     *         "family": "HELVETICA",
     *         "size": 10,
     *         "style": "NORMAL",
     *
     *            "blockquote": {
     *                "family": "HELVETICA",
     *                "size": 10,
     *                "style": "ITALIC",
     *                "color": "128:128:128",
     *                "background": "255:255:255"
     *            },
     *            "h1": {
     *                "family": "HELVETICA",
     *                "size": 20,
     *                "style": "BOLD"
     *            },
     *            "h2": {
     *                "family": "HELVETICA",
     *                "size": 18,
     *                "style": "BOLD"
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
     *                "background": "255:255:255"
     *            },
     *            "anchor": {
     *                "family": "HELVETICA",
     *                "size": 10,
     *                "style": "NORMAL",
     *                "color": "128:128:128",
     *                "background": "255:255:255"
     *            },
     *            "list_item": {
     *                "family": "HELVETICA",
     *                "size": 10
     *            },
     *            "footer": {
     *                "family": "HELVETICA",
     *                "size": 8
     *            }
     *      },
     *
     *      "div:<div name>": {
     *        ...
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
     *             "label_desc": "The prefixed text of this value. Can be changed to another language.",
     *             "label": "Version: "
     *         },
     *         "copyright": {
     *             "family": "HELVETICA",
     *             "size": 12,
     *             "style": "NORMAL"
     *         },
     *         "anchor": {
     *             "family": "HELVETICA",
     *             "size": 12,
     *             "style": "NORMAL",
     *             "label_desc": "The prefixed text of this value. Can be changed to another language.",
     *             "label": "Author: "
     *         }
     *      },
     *
     *      "toc": {
     *         "color": 0:0:0",
     *         "background": 255:255:255",
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
     * </pre>
     *
     * @param styleStream
     * @return
     * @throws IOException
     */
    private static JSS fromInputStream(InputStream styleStream) throws IOException {

    }

}
