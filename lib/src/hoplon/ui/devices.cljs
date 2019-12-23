(ns hoplon.ui.devices)

(def devices
  ^{:doc "assumes landscape orientation see https://www.paintcodeapp.com/news/ultimate-guide-to-iphone-resolutions"}

  {:iphone-3               {:description "iPhone 2G, 3G, 3GS"         :ppi 163  :scales [1                                              ] :resolution {:eh 480  :ev 320}}
   :iphone-4               {:description "iPhone 5, 5s, 5c, SE"       :ppi 326  :scales [(/  1  2)                                      ] :resolution {:eh 960  :ev 640}}
   :iphone-5               {:description "iPhone 5, 5s, 5c, SE"       :ppi 326  :scales [(/  1  2)                                      ] :resolution {:eh 1136 :ev 640}}
   :iphone-6               {:description "iPhone 6, 6s, 7, 8"         :ppi 326  :scales [(/  1  2)                                      ] :resolution {:eh 1334 :ev 750} :downsample (/ 20 23)}
   :iphone-7               {:description "iPhone 6, 6s+, 7+, 8+"      :ppi 326  :scales [(/  1  3)                                      ] :resolution {:eh 2208 :ev 1242}}
   :iphone-xs              {:description "iPhone 11 Pro, X, Xs"       :ppi 458  :scales [(/  1  3)                                      ] :resolution {:eh 2436 :ev 1125}}
   :iphone-11              {:description "iPhone 11, Xr"              :ppi 326  :scales [(/  1  2)                                      ] :resolution {:eh 1792 :ev 828}}
   :iphone-max             {:description "iPhone 11 Pro Max, Xs Max"  :ppi 458  :scales [(/  1  3)                                      ] :resolution {:eh 2688 :ev 1242}}

   :2015-15-mbp            {:description "Mid 2015 15\" Macbook Pro"  :ppi 220  :scales [(/  2  3) (/  7 12) (/ 8 5) (/  4  9) (/ 16 45)] :resolution {:eh 2880 :ev 1800}}
   :2019-13-mbp            {:description "Late 2019 13\" Macbook Pro" :ppi 227  :scales [(/ 21 32) (/  9 16) (/  2  5)                  ] :resolution {:eh 2560 :ev 1600}}
   :2019-16-mbp            {:description "Late 2019 16\" Macbook Pro" :ppi 226  :scales [(/  2  3) (/  7 12) (/  7 16) (/  3  8)        ] :resolution {:eh 3072 :ev 1920}}
   :27-cinema-display      {:description "Apple 27\" Cinema Display"  :ppi 108  :scales [        1 (/  3  4) (/  1  2)                  ] :resolution {:eh 2560 :ev 1440}}

   :zebra-zt410-printer    {:description "Zebra ZT 410 Printer"       :ppi 203  :scales [1 (/ 1 2)]}
   :sharp-mx-m654n-printer {:description "Sharp MX-M654N Printer"     :ppi 1200 :scales [1        ]}})

(defn screen-size
  [eh ev]
  (Math/sqrt (+ (Math/pow eh 2) (Math/pow ev 2))))

(defn layout-resolution
  "given a device map and resolution setting selector as a function, compute the
   resolution of the coordinate system."
  [device setting]
  (let [scale (part * (-> device :scales setting))]
    (into {} (map #(update % 1 scale)) (:resolution device))))

(defn layout-ppi
  [device setting]
  (js/Math.floor (* (:ppi device) (-> device :scales setting))))
