package se.natusoft.doc.markdown.generator.styles

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.json.JSON
import se.natusoft.json.JSONErrorHandler
import se.natusoft.json.JSONObject
import se.natusoft.json.JSONValue

/**
 * JSON Style Sheet. This can be by for all non HTML generators to allow users
 * provide style data at generation time.
 */
@CompileStatic
@TypeChecked
class JSS extends HashMap<String, JSSStyleValue> implements JSSStyleValue, JSSColorNameResolver {

    static enum TopFields {
        ttf,
        colors,
        document
    }

    static enum DocumentFields {
        pages,
        divs,
        front_page,
        toc

    }

    static enum PagesFields {
        h1,
        h2,
        h3,
        h4,
        h5,
        h6,
        block_quote,
        emphasis,
        strong,
        code,
        list_item,
        footer
    }

    static enum ColorFields {
        color,
        background
    }

    static enum FontFields {
        family,
        size,
        style
    }

    static enum TTFFields {
        ttfs,
        family,
        path
    }

    //
    // Private Members
    //

    private JSSColorNameResolver colorNameResolver = this

    private Map<String, JSSColor> colorMap = null

    //
    // Constructors
    //

    JSS(JSONObject jss) {

    }

    protected JSS() {}

    //
    // Methods
    //

    /**
     * Resolves a JSSColor by name or color value.
     *
     * @param name The name of the color to resolve.
     *
     * @return
     */
    @Override
    JSSColor resolve(String name) {
        JSSColor color = JSSColor.BLACK
        if (this.colorMap != null) {
            JSSColor lookupColor = this.colorMap.get(name)
            if (lookupColor != null) color = lookupColor
        }

        return color
    }

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
     *            }
     *      },
     *
     *      "divs": {
     *         "<divname>": {
     *             ... (same as pages)
     *         }
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
     * }
     * </pre>
     *
     * @param styleStream
     * @return
     * @throws IOException
     */
    private static JSS fromInputStream(InputStream styleStream) throws IOException {
        JSONObject jss = (JSONObject)JSON.read(styleStream, new JSONErrorHandler() {
            @Override
            void warning(String message) {
                System.err.println(message);
            }

            @Override
            void fail(String message, Throwable cause) throws RuntimeException {
                throw new RuntimeException(message, cause)
            }
        });


    }

    //
    // Inner Classes
    //

    static class PDF extends JSS {

    }
}
