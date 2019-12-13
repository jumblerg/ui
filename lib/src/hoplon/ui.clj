(ns hoplon.ui)

(defmacro set-in!! [elem path value]
  `(set! ~(reduce #(list %2 %1) elem path) ~value))

(defmacro bind-in!! [elem path value]
  `(bind-with! (fn [v#] (set-in!! ~elem ~path (or v# ~(reduce #(list %2 %1) elem path)))) ~value))

(defmacro set-in! [elem path value]
  `(set! ~(reduce #(list %2 %1) elem path) (hoplon.ui.attrs/->attr ~value)))

(defmacro bind-in! [elem path value]
  `(bind-with! (fn [v#] (set-in! ~elem ~path (or v# ~(reduce #(list %2 %1) elem path)))) ~value))

(defmacro window [& args]
  `(window* ~@args))
