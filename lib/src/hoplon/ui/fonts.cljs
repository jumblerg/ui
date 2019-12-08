(ns hoplon.ui.fonts
  (:refer-clojure
    :exclude [name])
  (:require
    [hoplon.ui.utils :refer [clean name]]))

(def font*
  (let [widths   #{:ultra-condensed :extra-condensed :condensed :semi-condensed :normal :semi-expanded :expanded :extra-expanded :ultra-expanded}
        weights  #{:100 :200 :300 :400 :500 :600 :700 :800 :900}
        slopes   #{:regular :italic :oblique}
        generics #{:serif :sans-serif :monospace :cursive :fantasy}]
    (fn [src & [[width weight slope :as path] range]]
      (let [props {"stretch" (name width) "weight" (name weight) "style" (name slope) "unicodeRange" (name range)}
            source (if-not (keyword? src) (str "url('" src "')") (do (assert (generics src) (str "Error validating generic font value " src)) (name src)))
            font  (js/FontFace. (str (gensym "font-")) source (clj->js (clean props)))]
    (when width  (assert (widths   width) (str "Error validating font width with value "  width)))
    (when weight (assert (weights weight) (str "Error validating font weight with value " weight)))
    (when slope  (assert (slopes   slope) (str "Error validating font slope with value "  slope)))
    (set! (.-display font) "block")
    (.add js/document.fonts font)
    font))))

(def font? (partial instance? js/FontFace))
