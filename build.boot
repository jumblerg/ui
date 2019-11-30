(set-env!
  :resource-paths #{"lib/src"}
  :dependencies '[[org.clojure/clojure                      "1.9.0"          :scope "provided"]
                  [org.clojure/clojurescript                "1.10.339"       :scope "provided"]
                  [adzerk/env                               "0.4.0"          :scope "test"]
                  [adzerk/boot-cljs                         "1.7.228-2"      :scope "test"]
                  [adzerk/boot-test                         "1.1.2"          :scope "test"]
                  [adzerk/boot-reload                       "0.6.0"          :scope "test"]
                  [adzerk/bootlaces                         "0.1.13"         :scope "test"]
                  [org.seleniumhq.selenium/selenium-support "3.14.0"         :scope "test"]
                  [tailrecursion/boot-static                "0.1.0"          :scope "test"]
                  [tailrecursion/boot-bucket                "2.0.0"          :scope "test"]
                  [tailrecursion/boot-front                 "1.1.0"          :scope "test"]
                  [jumblerg/hoplon                          "7.0.4"]
                  [cljsjs/markdown                          "0.6.0-beta1-0"]])

(require
  '[adzerk.bootlaces          :refer :all]
  '[adzerk.boot-test          :as    t]
  '[adzerk.boot-env           :refer [init]]
  '[adzerk.boot-cljs          :refer [cljs]]
  '[adzerk.boot-reload        :refer [reload]]
  '[hoplon.boot-hoplon        :refer [hoplon]]
  '[tailrecursion.boot-bucket :refer [spew]]
  '[tailrecursion.boot-front  :refer [burst]]
  '[tailrecursion.boot-static :refer [serve]])

(ns-unmap 'boot.user 'test)

;;; configs ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def +version+ "0.3.0-SNAPSHOT")
(bootlaces! +version+ :dont-modify-paths? true)

(def buckets
  {:staging    (System/getenv "UI_STAGING_S3_BUCKET")
   :production (System/getenv "UI_PRODUCTION_S3_BUCKET")})

(def distributions
  {:staging    (System/getenv "UI_STAGING_CLOUDFRONT_DISTRIBUTION")
   :production (System/getenv "UI_PRODUCTION_CLOUDFRONT_DISTRIBUTION")})

;;; tasks ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftask develop-lib []
  "Continuously rebuild and reinstall the library."
  (comp (watch) (speak) (build-jar)))

(deftask deploy-lib []
  "Deploy the library snapshot to clojars"
  (comp (speak) (build-jar) (push-snapshot)))

;;; app ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftask build-app
  [e environment ENV   kw "The server the client should connect to."
   o optimizations OPT kw "Optimizations to pass the cljs compiler."]
  (set-env! :source-paths #{"lib/src" "app/src"} :resource-paths #{"app/rsc"})
  (let [o (or optimizations :advanced)
        e (or environment   :local)]
    (comp (speak) (hoplon) (cljs :optimizations o :compiler-options {:language-in :ecmascript5-strict :elide-asserts true}) (sift))))

(deftask develop-app
  "Serve the test app locally"
  [o optimizations OPM   kw   "Optimizations to pass the cljs compiler."
   v no-validate         bool "Elide assertions used to validate attibutes."]
  (set-env! :source-paths #{"lib/src" "app/src"} :resource-paths #{"app/rsc"})
  (let [o (or optimizations :none)
        c {:elide-asserts no-validate}]
    (comp (watch) (speak) (hoplon) (reload) (cljs :optimizations o :compiler-options c) (serve))))

(deftask deploy-app
  "Build the application with advanced optimizations then deploy it to s3."
  [e environment ENV   kw "The aws environment to deploy to."
   o optimizations OPT kw "Optimizations to pass the cljs compiler."]
  (assert environment "Missing required environment argument.")
  (let [b (buckets       environment)
        d (distributions environment)]
    (comp (build-app :optimizations optimizations :environment environment) (spew :bucket b) #_(burst :distribution d))))

(deftask dump-app
  "Build the application with advanced optimizations then dump it into the tgt folder."
  [e environment ENV   kw "The server the client should connect to."
   o optimizations OPT kw "Optimizations to pass the cljs compiler."]
  (comp (build-app :optimizations optimizations :environment environment) (target :dir #{"tgt"})))

(deftask distribute-app
  "Build the application with advanced optimizations then zip it."
  [e environment ENV   kw "The server the client should connect to."
   o optimizations OPT kw "Optimizations to pass the cljs compiler."]
  (comp (build-app :optimizations optimizations :environment environment)
        (zip :file (str "ui-app-" +version+  ".zip"))
        (sift :include #{#"ui-app-*."} :invert false)
        (target :dir #{"dst"})))

;;; tests ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftask connect
  "Launch Sauce Connect Proxy"
  [u username USER str "Username"
   k access-key PASS str "Access Key"]
  (with-pass-thru _
    (let [u (or username   (System/getProperty "SAUCE_LABS_USERNAME"))
          k (or access-key (System/getProperty "SAUCE_LABS_ACCESS_KEY"))]
      (boot.util/info "Starting Sauce Connect proxy...\n")
      (prn sh (sh "sc" "-u" u "-k" k)))))

(deftask test-local
  "Continuously rebuild the test suite during development.

  To simulate a production environment, the tests should be built with advanced
  optimizations and without validations"
  [n namespaces NS       #{sym}   "Namespaces containing unit tests."
   o optimizations OPM   kw       "Optimizations to pass the cljs compiler."
   v no-validate         bool     "Elide assertions used to validate attibutes."]
  (let [o (or optimizations :none)
        c {:elide-asserts no-validate}]
    (set-env! :source-paths #{"lib/src" "app/src"} :resource-paths #{"tst/src" "app/rsc"})
    (comp (init) (connect) (watch) (speak) (hoplon) (cljs :optimizations o :compiler-options c) (serve) (t/test :namespaces namespaces))))

(deftask test
  "Continuously rebuild the test suite during development.

  To simulate a production environment, the tests should be built with advanced
  optimizations and without validations"
  [n namespaces NS       #{sym}   "Namespaces containing unit tests."
   o optimizations OPM   kw       "Optimizations to pass the cljs compiler."
   v no-validate         bool     "Elide assertions used to validate attibutes."]
  (let [o (or optimizations :none)
        c {:elide-asserts no-validate}]
    (set-env! :source-paths #{"lib/src" "app/src"} :resource-paths #{"tst/src" "app/rsc"})
    (comp (hoplon) (cljs :optimizations o :compiler-options c) (serve) (t/test :namespaces namespaces))))

(task-options!
  pom    {:project         'hoplon/ui
          :version         +version+
          :description     "a cohesive layer of composable abstractions over the dom."
          :url             "https://github.com/hoplon/ui"
          :scm             {:url "https://github.com/hoplon/ui"}
          :license         {"EPL" "http://www.eclipse.org/legal/epl-v10.html"}}
  serve {:port 5000}
  sift  {:include #{#"index.html.out/" #"hoplon/"} :invert true}
  spew  {:access-key (System/getenv "UI_AWS_ACCESS_KEY")
         :secret-key (System/getenv "UI_AWS_SECRET_KEY")}
  burst {:access-key (System/getenv "UI_AWS_ACCESS_KEY")
         :secret-key (System/getenv "UI_AWS_SECRET_KEY")}
  test  {:namespaces '#{hoplon-test.ui}})
