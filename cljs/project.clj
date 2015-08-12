(defproject plastic "0.1.0-SNAPSHOT"
  :description "Plastic - Experimental ClojureScript editor component for Atom"
  :url "http://github.com/darwin/plastic"

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.107"]
                 [org.clojure/tools.reader "0.10.0-SNAPSHOT"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [com.cognitect/transit-cljs "0.8.220"]
                 [rewrite-cljs "0.3.1"]
                 [binaryage/devtools "0.1.2"]               ; Electron 0.28.2 has old Blink, we have to stick with this old version of devtools for now
                 [figwheel "0.3.7"]
                 [rm-hull/inkspot "0.0.1-SNAPSHOT"]
                 [spellhouse/phalanges "0.1.6"]
                 [funcool/cuerdas "0.6.0"]
                 [reagent "0.5.0" :exclusions [cljsjs/react]]
                 [cljsjs/react "0.13.3-1"]]

  :plugins [[lein-cljsbuild "1.0.5"]
            [lein-figwheel "0.3.3"]]

  :source-paths ["src/macros"
                 "target/classes"]

  :clean-targets ^{:protect false} ["../lib/_build" "target" ".tmp"]

  :figwheel {:server-port    7000
             :nrepl-port     7777
             :server-logfile ".tmp/figwheel_server.log"}

  :cljsbuild {:builds [{:id           "main"
                        :source-paths ["src/macros"
                                       "src/dev"
                                       "src/common"
                                       "src/main"]
                        :compiler     {:main           plastic.main.init
                                       :output-to      "../lib/_build/main/plastic-main.js"
                                       :output-dir     "../lib/_build/main"
                                       :optimizations  :none
                                       :target         :nodejs
                                       :compiler-stats true
                                       :cache-analysis true
                                       :figwheel       true
                                       :source-map     true}}
                       {:id           "worker"
                        :source-paths ["src/macros"
                                       "src/dev"
                                       "src/common"
                                       "src/worker"]
                        :compiler     {:main           plastic.worker.init
                                       :output-to      "../lib/_build/worker/plastic-worker.js"
                                       :output-dir     "../lib/_build/worker"
                                       :optimizations  :none
                                       :target         :nodejs
                                       :compiler-stats true
                                       :cache-analysis true
                                       :figwheel       true
                                       :source-map     true}}]})
