(ns plastic.dev.figwheel
  (:require-macros [plastic.logging :refer [log info warn error group group-end]])
  (:require [plastic.env]
            [clojure.string :as string]
            [figwheel.client :as figwheel]
            [figwheel.client.socket :as socket]
            [figwheel.client.utils :as utils]))

(defonce ^:dynamic *inside-repl-plugin* false)
(def ^:const repl-style "color:white; background-color:black; padding:0px 2px; border-radius:2px;")

(defn figwheel-repl-fix [code]
  (.replace code
    #"^\(function \(\)\{try\{return cljs\.core\.pr_str\.call"
    "(function (){try{return cljs.core.identity.call"))

(defn intellij-repl-fix [code]
  (.replace code
    #"^try\{cljs\.core\.pr_str\.call"
    "try{cljs.core.identity.call"))

(defn rewrite-repl-code-snippet [code]
  (-> code figwheel-repl-fix intellij-repl-fix))

(defn eval [code]
  (if plastic.env.need-loophole
    (.runInThisContext (js/require "vm") code)                                                                        ; https://github.com/atom/loophole
    (js* "eval(~{code})")))

(defn present-repl-result [result]
  (log "%cREPL" repl-style result))

(defn fancy-eval [code]
  (if-not *inside-repl-plugin*
    (eval code)
    (let [rewritten-code (rewrite-repl-code-snippet code)
          result (eval rewritten-code)]
      (present-repl-result result)
      (pr-str result))))

(defn repl-plugin [& args]
  (let [standard-impl (apply figwheel/repl-plugin args)]
    (fn [& args]
      (binding [*inside-repl-plugin* true]
        (apply standard-impl args)))))

(defn on-js-load [])

(when-not plastic.env.dont-start-figwheel
  (figwheel/start
    {:on-jsload     on-js-load
     :eval-fn       fancy-eval
     :websocket-url "ws://localhost:7000/figwheel-ws"
     :build-id      'dev
     :merge-plugins {:repl-plugin repl-plugin}}))