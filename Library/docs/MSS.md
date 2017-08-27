# MSS (MarkdownDoc Style Sheet)

The MSS format is a JSON document describing the styles (colors and fonts) to use for different sections of a markdown document (standard text, heading, bold, code, etc). It contains 3 main sections: front page, TOC, pages. There is a _default.mss_ embedded in the jar that is used if no external mss files is provided. The default MSS have changed in version 2.0.0 and now produces output that looks different than previous versions. Not only different, but better IMHO :-). It still defaults to A4 page size, but now also have correct margins according to standards. Maybe iText also did that, but it feels like the margins are larger now (2.54 cm).

Note that page size is now set in the MSS file and not provided as an option when generating. The margins are of course also set in MSS.

Currently the MSS file is only used by the PDF generator. But it could also be relevant for other formats, like word if I ever add such a generator. I gladly take a pull request if anybody wants to do that :-). 

Here is an example of an MSS file with descriptions:

    {

This section is specific to PDF files, and specifies ttf, otf, and other font types supported by iText. For the _internal_ fonts "HELVETICA" or "COURIER" is specified as "family", but to use a font specified here, just use the name set as "family" here. If you are using an exernal Helvetica font specified here, dont call it just "HELVETICA" since there will be confusion!

A best effort is used to resolve the font in "path":. If the specified path does not match relative to current directory then it will try the parent directory and so on all the way upp to the filesytem root. 

      "pdf": {
        "extFonts": [
          {
            "family": "MDD-EXAMPLE",
            "encoding": "UTF-8",
            "path": "/fonts/ttf/some-font.ttf"
          }
        ]
      },

The "colors" section just provide names for colors. This list was taken from the default color names for CSS colors, with the exception of the first 3. Any color specification in sections below that does not contain any ":" character will be taken as a name and looked up here.

      "colors": {
        "white": "255:255:255",
        "black": "0:0:0",
        "mddgrey": "128:128:128",
        "AliceBlue": "F0F8FF",
        "AntiqueWhite": "FAEBD7",
        "Aqua": "00FFFF",
        "Aquamarine": "7FFFD4",
        "Azure": "F0FFFF",
        "Beige": "F5F5DC",
        "Bisque": "FFE4C4",
        "Black": "000000",
        "BlanchedAlmond": "FFEBCD",
        "Blue": "0000FF",
        "BlueViolet": "8A2BE2",
        "Brown": "A52A2A",
        "BurlyWood": "DEB887",
        "CadetBlue": "5F9EA0",
        "Chartreuse": "7FFF00",
        "Chocolate": "D2691E",
        "Coral": "FF7F50",
        "CornflowerBlue": "6495ED",
        "Cornsilk": "FFF8DC",
        "Crimson": "DC143C",
        "Cyan": "00FFFF",
        "DarkBlue": "00008B",
        "DarkCyan": "008B8B",
        "DarkGoldenRod": "B8860B",
        "DarkGray": "A9A9A9",
        "DarkGreen": "006400",
        "DarkKhaki": "BDB76B",
        "DarkMagenta": "8B008B",
        "DarkOliveGreen": "556B2F",
        "DarkOrange": "FF8C00",
        "DarkOrchid": "9932CC",
        "DarkRed": "8B0000",
        "DarkSalmon": "E9967A",
        "DarkSeaGreen": "8FBC8F",
        "DarkSlateBlue": "483D8B",
        "DarkSlateGray": "2F4F4F",
        "DarkTurquoise": "00CED1",
        "DarkViolet": "9400D3",
        "DeepPink": "FF1493",
        "DeepSkyBlue": "00BFFF",
        "DimGray": "696969",
        "DodgerBlue": "1E90FF",
        "FireBrick": "B22222",
        "FloralWhite": "FFFAF0",
        "ForestGreen": "228B22",
        "Fuchsia": "FF00FF",
        "Gainsboro": "DCDCDC",
        "GhostWhite": "F8F8FF",
        "Gold": "FFD700",
        "GoldenRod": "DAA520",
        "Gray": "808080",
        "Green": "008000",
        "GreenYellow": "ADFF2F",
        "HoneyDew": "F0FFF0",
        "HotPink": "FF69B4",
        "IndianRed": "CD5C5C",
        "Indigo": "4B0082",
        "Ivory": "FFFFF0",
        "Khaki": "F0E68C",
        "Lavender": "E6E6FA",
        "LavenderBlush": "FFF0F5",
        "LawnGreen": "7CFC00",
        "LemonChiffon": "FFFACD",
        "LightBlue": "ADD8E6",
        "LightCoral": "F08080",
        "LightCyan": "E0FFFF",
        "LightGoldenRodYellow": "FAFAD2",
        "LightGray": "D3D3D3",
        "LightGreen": "90EE90",
        "LightPink": "FFB6C1",
        "LightSalmon": "FFA07A",
        "LightSeaGreen": "20B2AA",
        "LightSkyBlue": "87CEFA",
        "LightSlateGray": "778899",
        "LightSteelBlue": "B0C4DE",
        "LightYellow": "FFFFE0",
        "Lime": "00FF00",
        "LimeGreen": "32CD32",
        "Linen": "FAF0E6",
        "Magenta": "FF00FF",
        "Maroon": "800000",
        "MediumAquaMarine": "66CDAA",
        "MediumBlue": "0000CD",
        "MediumOrchid": "BA55D3",
        "MediumPurple": "9370DB",
        "MediumSeaGreen": "3CB371",
        "MediumSlateBlue": "7B68EE",
        "MediumSpringGreen": "00FA9A",
        "MediumTurquoise": "48D1CC",
        "MediumVioletRed": "C71585",
        "MidnightBlue": "191970",
        "MintCream": "F5FFFA",
        "MistyRose": "FFE4E1",
        "Moccasin": "FFE4B5",
        "NavajoWhite": "FFDEAD",
        "Navy": "000080",
        "OldLace": "FDF5E6",
        "Olive": "808000",
        "OliveDrab": "6B8E23",
        "Orange": "FFA500",
        "OrangeRed": "FF4500",
        "Orchid": "DA70D6",
        "PaleGoldenRod": "EEE8AA",
        "PaleGreen": "98FB98",
        "PaleTurquoise": "AFEEEE",
        "PaleVioletRed": "DB7093",
        "PapayaWhip": "FFEFD5",
        "PeachPuff": "FFDAB9",
        "Peru": "CD853F",
        "Pink": "FFC0CB",
        "Plum": "DDA0DD",
        "PowderBlue": "B0E0E6",
        "Purple": "800080",
        "RebeccaPurple": "663399",
        "Red": "FF0000",
        "RosyBrown": "BC8F8F",
        "RoyalBlue": "4169E1",
        "SaddleBrown": "8B4513",
        "Salmon": "FA8072",
        "SandyBrown": "F4A460",
        "SeaGreen": "2E8B57",
        "SeaShell": "FFF5EE",
        "Sienna": "A0522D",
        "Silver": "C0C0C0",
        "SkyBlue": "87CEEB",
        "SlateBlue": "6A5ACD",
        "SlateGray": "708090",
        "Snow": "FFFAFA",
        "SpringGreen": "00FF7F",
        "SteelBlue": "4682B4",
        "Tan": "D2B48C",
        "Teal": "008080",
        "Thistle": "D8BFD8",
        "Tomato": "FF6347",
        "Turquoise": "40E0D0",
        "Violet": "EE82EE",
        "Wheat": "F5DEB3",
        "White": "FFFFFF",
        "WhiteSmoke": "F5F5F5",
        "Yellow": "FFFF00",
        "YellowGreen": "9ACD32"
      },

This section deals with document styles. It has 3 sections: "pages", "front\_page", and "toc". If a style is not set in a specific section it will fall back to what is specified in a more general section. For example, if a subsection of "document" does not specify "color" then it will fall back to the "color": "black" directly under "document". 

      "document": {
        "pageFormat": "A4",
    
For the margins the suffix can be "cm" for centimeters, "in" for inches or "pt" for points. This value can also be specified as a JSON number in which case it is in points. 
    
        "leftMargin": "2.54cm",
        "rightMargin": "2.54cm",
        "topMargin": "2.54cm",
        "bottomMargin": "2.54cm",
        
        "color": "black",
        "background": "white",
        "family": "HELVETICA",
        "size": 10,
        "style": "Normal",
    
The section number offsets allows for changeing the position slightly for the section numbers when they are enabled.
    
        "sectionNumberYOffset": 2.0,
        "sectionNumberXOffset": -10.0,
    
        "image": {
           "imgScalePercent": 60.0,
    
The alignment can be either "LEFT", "MIDDLE" or "RIGHT". Note that if "imgX" and "imgY" is set, then this does not apply.
    
           "imgAlign": "LEFT",
           "imgRotateDegrees": 0.0,
    
If "imgFlow" is set to true then text will flow around the image. Basically what happens is that when text is about to overwrite the image then it is moved right to after the image and continues from there. To get this effect you can place an image in the middle of a paragraph and it will flow around the image.     
    
           "imgFlow": false,
    
This margin is always in points and determins the space to reserve to the left and right of an image when "imgFlow" is set to true. This to avoid having text and image exactly side by side with no space, since that tends to look strange. 
    
           "imgFlowMargin": 4.0,
    
These 2 allows you to override the position of an image on the page. This works best in conjunction with "imgFlow". Also note that this does not specify a specific image! If you specify this directly under "document" then all images on the page will be rendered over each other at this coordinate! So it makes much more sense to use this feature in conjunction with a div, in which you also place the image. I'm only putting it here now to show its association with "imgFlow".
    
           "imgX": 127.0,
           "imgY": 430.0
        },
    
        "pages": {
    
The style value can be any of NORMAL, BOLD, ITALIC, and UNDERLINE. UNDERLINE can be used in conjunction with the other, comma separated. Example ITALIC,UNDERLINE. 
    
          "block_quote": {
            "style": "ITALIC",
            "color": "mddgrey"
          },
          "h1": {
            "size": 20,
            "style": "BOLD"
          },
          "h2": {
            "size": 18,
            "style": "BOLD",
    
"underlined" draws and underline under the heading from left margin to right margin. The "underline_offset" is how many points below the text to draw the line. In previous versions this was called "hr".    
    
            "underlined": true,
            "underline_offset": 3.0
          },
          "h3": {
            "size": 16,
            "style": "BOLD"
          },
          "h4": {
            "size": 14,
            "style": "BOLD"
          },
          "h5": {
            "size": 12,
            "style": "BOLD"
          },
          "h6": {
            "size": 10,
            "style": "BOLD"
          },
          "emphasis": {
            "style": "ITALIC"
          },
          "strong": {
            "style": "BOLD"
          },
          "code": {
            "family": "COURIER",
            "size": 9,
            "color": "64:64:64",
    
If "preformattedWordWrap" is set to true, then "code" style text will not entirely be left as is, but will wrap around to a new line if the text does not fit within the margins, and this will be with an indent matching the "code" text plus some more indent to show that it is a continuation of the previous line. Depending on the text this sometimes works well, sometimes less than well. 
    
            "preformattedWordWrap": false,
    
When "boxed" is set to true then a filled box is drawn below the text. It ranges from the left margin to the right margin for multiline (indented) "code" text. For `text` variant only the text is boxed. 
    
            "boxed": true,
            "boxedColor": "#f8f8f8"
          },
          "anchor": {
            "color": "128:128:128"
          },
          "list_item": {
          },
    
This is also new in 2.0.0 and sets the thickness and color of a horizontal ruler. 
    
          "horizontal_ruler": {
            "thickness": 0.5,
            "color": "grey"
          }
        },
    
        "divs": {
          "mdd-example": {
            "color": "white",
            "background": "black",
            "block_quote": {
              "family": "COURIER",
              "color": "120:120:120",
              "background": "10:11:12"
            }
          },
          "center-page5-image": {
            "imgX": 127.0,
            "imgY": 430.0
          },
    
This is a new feature as of 2.1.0. Setting "freeFloating" to true also allows you to change page X & Y coordinates, and also changeing margins is useful in combination. This basically lets you put text at a very specific place on the page. This really only makes sense to use within a div. The previous position on page is remembered and when the div is exited the X & Y is restored to the previous location. Yes, this make it possible to make text overwrite each other! You have to be veruy careful when this is used. Also note that if the placement on page is rather low and the text triggers a page break, things will get very weird! Use this feature very carefully! You can really mess things up if you are not careful! You have been warned!    
    
          "freeFloating-Example": {
            "freeFloating": true,
            "pageX": 220.0,
            "pageY": 380.0,
            "leftMargin": 220.0
        }
      },
    
      "front_page": {
        "color": "0:0:0",
        "background": "255:255:255",
        "family": "HELVETICA",
        "size": 10,
        "style": "NORMAL",
    
        "image": {
           "imgScalePercent": 60.0,
           "imgRotateDegrees": 0.0
        },
    
        "title": {
          "size": 25,
          "style": "UNDERLINE"
        },
        "subject": {
          "size": 15
        },
        "version": {
          "size": 12,
        },
        "copyright": {
        },
        "author": {
          "size": 12,
        }
      },
    
      "toc": {
        "color": "0:0:0",
        "background": "255:255:255",
        "family": "HELVETICA",
        "size": 9,
        "style": "NORMAL",
        "h1": {
          "style": "BOLD"
        },
        "h2": {
        },
        "h3": {
        },
        "h4": {
        },
        "h5": {
        },
        "h6": {
        }
      }
    }
    
 
