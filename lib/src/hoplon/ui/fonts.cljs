(ns hoplon.ui.fonts
  (:refer-clojure
    :exclude [name])
  (:require
    [javelin.core    :refer [cell]]
    [hoplon.ui.utils :refer [clean name]]))

(defn loaded=
  "returns the font once it has been loaded" 
  [font]
  (let [res (cell (when (= (:status font) "loaded") font))]
    (.then (.-loaded (:obj font)) #(reset! res font))
    res))

(def font*
  (let [widths   #{:ultra-condensed :extra-condensed :condensed :semi-condensed :normal :semi-expanded :expanded :extra-expanded :ultra-expanded}
        weights  #{:100 :200 :300 :400 :500 :600 :700 :800 :900}
        slopes   #{:normal :italic :oblique}
        generics #{:serif :sans-serif :monospace :cursive :fantasy}]
    (fn [src & [[width weight slope :as path] range]]
      (assert (or (string? src) (generics src))      (str "Error validating generic font value " src))
      (when width          (assert (widths   width)  (str "Error validating font width with value "  width)))
      (when weight         (assert (weights  weight) (str "Error validating font weight with value " weight)))
      (when slope          (assert (slopes   slope)  (str "Error validating font slope with value "  slope)))
      (let [id    (str (gensym "font-"))
            props {"stretch" (name width) "weight" (name weight) "style" (name slope) "unicodeRange" (name range)}
            font  (js/FontFace. id (str "url('" (name src) "')") (clj->js (clean props)))]
        (set! (.-display font) "block")
        (.add js/document.fonts font)
        {:id (.-family font) :src src :obj font}))))



