(ns plastic.main.servant
  (:require-macros [plastic.logging :refer [log info warn error group group-end]]
                   [plastic.main :refer [react! dispatch dispatch-args]])
  (:require [plastic.env :as env]
            [cognitect.transit :as transit]
            [plastic.main.frame :as frame]))

(defonce worker-script "/worker.js")
(defonce ^:dynamic worker nil)
(defonce ^:dynamic msg-id 0)

(defn next-msg-id! []
  (set! msg-id (inc msg-id))
  msg-id)

(defn ^:export dispatch-message [data]
  (let [reader (transit/reader :json)
        command (aget data "command")
        id (aget data "id")]
    (condp = command
      "dispatch" (dispatch-args id (transit/read reader (aget data "args"))))))

(defn process-message [message]
  (dispatch-message (.-data message)))

(defn spawn-workers [base-path]
  (if-not env/run-worker-on-main-thread
    (let [script-url (str base-path worker-script)
          new-worker (js/Worker. script-url)]
      (.addEventListener new-worker "message" process-message)
      (set! worker new-worker))))

(defn post-dispatch-message [args id]
  (let [writer (transit/writer :json)
        data (js-obj
               "id" id
               "command" "dispatch"
               "args" (transit/write writer args))]
    (if-not env/run-worker-on-main-thread
      (.postMessage worker data)
      (plastic.worker.servant.dispatch-message data))))

(defn dispatch-on-worker
  ([event-v] (dispatch-on-worker event-v nil))
  ([event-v after-effect] (let [id (next-msg-id!)]
                            (if after-effect
                              (frame/register-after-effect id after-effect))
                            (post-dispatch-message event-v id))))