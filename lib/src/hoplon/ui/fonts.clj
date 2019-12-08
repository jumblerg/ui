(ns hoplon.ui.fonts
  (:require
    [hoplon.ui.utils :refer [data-uri]]))

(def mime-types
  {:truetype          "application/x-font-truetype"
   :opentype          "application/x-font-opentype"
   :woff              "application/font-woff"
   :woff2             "application/font-woff2"
   :embedded-opentype "application/vnd.ms-fontobject"
   :svg               "image/svg+xml"})

(defmacro font
  ;todo: select the type face at build time.
  "load and embed a font in the document, returning a string identifier that
   may be passed to the tf attribute of any pane. note that the width (stretch),
   weight (boldness), and slope (style) of a font are not determined on a pane,
   but when the font is constructed, typically like this:

     (def geometos      (font :opentype \"geometos.ttf\"))
     (def lato-regular  (font :opentype \"lato-regular.ttf\"))
     (def lato-italic   (font :opentype \"lato-italic.ttf\"))
     (def lato-medium   (font :opentype \"lato-medium.ttf\"))
     (def lato-semibold (font :opentype \"lato-semibold.ttf\"))

  or like this when a single font must be selected from a typeface that includes
  multiple fonts:

  (def tnr-bold (font :generic :serif [:normal :400 :700))

  each font constructor takes a format, a source path, and an optional type face
  selector path.
   
  format. the format of the source must be one of the following. note that these
  formats differ from the file extensions.
   - :generic
   - :woff (.woff)
   - :woff2 (.woff2)
   - :truetype (.ttf)
   - :opentype (.ttf, .otf)
   - :embedded-opentype (.eot)
   - :svg (.svg, .svgz)

  source.  the source is embedded as the path to the file, which is embedded
  as a data uri, or one of the following generic typefaces.
   - :serif
   - :sans-serif
   - :cursive
   - :fantasy
   - :monospace
 
  path. each source typface may be thought of as a data structure containing
  more than one font in a format something like this:

    {:normal   {:400 {:regular <font Lato Regular>}}
               {:700 {:regular <font Lato Bold>
                      :italic  <font Lato Italic>}}
     :extended {:900 {:oblique <font Lato Extended Heavy Oblique}}}

  accordingly, the path is a vector of font descriptors of the form [<width>
  <weight> <slope>], used to select the appropriate font from a source containing
  multiple fonts (an entire typeface or font family).  if the path or any of its
  discriptors are nil or ommitted, the default will be selected.

    width. must be one of the following keywords:
     - :ultra-condensed
     - :extra-condensed
     - :condensed
     - :semi-condensed
     - :normal (default)
     - :semi-expanded
     - :expanded
     - :extra-expanded
     - :ultra-expanded

     weight. must be one of the following:
     - :100 thin
     - :200 extra/ultra light
     - :300 light
     - :400 normal (default)
     - :500 medium
     - :600 semi/demi bold
     - :700 bold
     - :800 extra/ultra bold
     - :900 black/heavy

     slope. must be one of the following:
     - :regular (default)
     - :oblique
     - :italic

   notes. when relying on local fonts, that there's no way to guarantee that the
   font installed on the user's os is the one intended for use based on the name
   alone (font names are global in the truest sense of the word). consequently,
   system fonts are not supported.

   there's currently no support for creating compound fonts from multiple
   typefaces. support for the variant descriptors is also limited.

   everything you ever wanted to know about the way the browser handles fonts
   can be found here: https://www.w3.org/TR/css-fonts-3/. additionally, see
   https://www.zachleat.com/web/comprehensive-webfonts/ for a comprehensive
   description of the various options available for loading fonts."
  [format src & args]
  `(font* ~(if (= :generic format) src (data-uri src (mime-types format))) ~@args))
