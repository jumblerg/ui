(ns hoplon.ui.attrs
  (:refer-clojure
    :exclude [name load + - * /])
  (:require
    [clojure.string  :refer [blank? join]]
    [hoplon.ui.utils :refer [clean name]]))

(declare + - * /)

;;; protocols ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defprotocol IAttr
  "Serialize to DOM Attr"
  (-dom-attribute [_]))

(defprotocol ICalc
  (-calc [_]))

;;; types ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(extend-type nil
  IAttr
  (-dom-attribute [this]
    ""))

(extend-type number ;explicit
  IAttr
  (-dom-attribute [this]
    (str this "px"))
  ICalc
  (-calc [this]
    this))

(extend-type boolean
  IAttr
  (-dom-attribute [this]
    (str this)))

(extend-type string ;ext
  IAttr
  (-dom-attribute [this]
    (when-not (blank? this) this))
  ICalc
  (-calc [this]
    this))

(extend-type PersistentVector
  IAttr
  (-dom-attribute [this]
    (join "," (map -dom-attribute this))))

(defn attr?     [v] (satisfies? IAttr v))
(defn ->attr    [v] (-dom-attribute   v)) ;; remove

;;; sizes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftype Ratio [n d]
  Object
  (toString [_]
    (str (* (/ n d) 100) "%"))
  IPrintWithWriter
  (-pr-writer [this w _]
    (write-all w (str "#<Size " this ">")))
  ICalc
  (-calc [this]
    (str n " / " d " * 100%"))
  IAttr
  (-dom-attribute [this]
    (.toString this)))

(defn ratio? [v] (instance? Ratio v))
(defn ratio  [n d] (Ratio. n d))
(def r ratio)

;;; calculations ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftype Calc [op vs]
  Object
  (toString [this]
    (str "calc(" (-calc this) ")"))
  IPrintWithWriter
  (-pr-writer [this w _]
    (write-all w "#<Calc: " this ">"))
  IAttr
  (-dom-attribute [this]
    (.toString this))
  ICalc
  (-calc [_]
    (join (str " " op " ") (mapv -calc vs))))

(defn mkcalc [f op type-fn]
  (fn [& vs]
    (doseq [v vs]
      (assert (satisfies? ICalc v) (str "Calc function " op " was passed incalculable value " v ".")))
    (cond (every? number? vs) (apply f vs) (every? (comp not blank?) vs) (Calc. op (type-fn vs)))))

(defn calc?      [v] (instance? Calc      v))

(def + (mkcalc clojure.core/+ "+" (partial mapv ->attr)))
(def - (mkcalc clojure.core/- "-" (partial mapv ->attr)))
(def * (mkcalc clojure.core/* "*" #(update % [0] ->attr)))
(def / (mkcalc clojure.core// "/" #(update % [0] ->attr)))

;;; transformations ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftype Transform [a1 b1 c1 d1 a2 b2 c2 d2 a3 b3 c3 d3 a4 b4 c4 d4]
  Object
  (toString [_]
    (if (and (= c1 0) (= d1 0) (= c2 0) (= d2 0) (= a3 0) (= b3 0) (= c3 1) (= d3 0) (= c4 0) (= d4 1))
      (str   "matrix(" (apply str (interpose ", " [a1 b1       a2 b2                   a4 b4      ])) ")")
      (str "matrix3d(" (apply str (interpose ", " [a1 b1 c1 d1 a2 b2 c2 d2 a3 b3 c3 d3 a4 b4 c4 d4])) ")")))
  IPrintWithWriter
  (-pr-writer [this w _]
    (write-all w "#<Transform " this ">"))
  IAttr
  (-dom-attribute [this]
    (.toString this)))

(defn transform? [v] (instance? Transform v))

(defn transform
  "transformation"
  ([a b c d tx ty]
   (transform a b 0 0 c d 0 0 0 0 1 0 tx ty 0 1))
  ([a1 b1 c1 d1 a2 b2 c2 d2 a3 b3 c3 d3 a4 b4 c4 d4]
   (Transform. a1 b1 c1 d1 a2 b2 c2 d2 a3 b3 c3 d3 a4 b4 c4 d4)))

(def t transform)

(defn scale
  ([x]
   (scale x x))
  ([x y]
   (transform x 0 0 y 0 0)))

(defn skew
  ([x]
   (skew x x))
  ([x y]
   (transform 1 x y 1 0 0)))

(defn translate
  ([x]
   (translate x x))
  ([x y]
   (transform 1 0 0 1 x y)))

(defn rotate
  [d]
  (let [q (* (/ js/Math.PI 180) d)]
    (transform (js/Math.cos q) (js/Math.sin q) (- (js/Math.sin q)) (js/Math.cos q) 0 0)))
