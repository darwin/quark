(ns plastic.frame.core
  (:require
    [plastic.frame.handlers :as handlers]
    [plastic.frame.subs :as subs]
    [plastic.frame.router :as router]
    [plastic.frame.utils :as utils]
    [plastic.frame.middleware :as middleware])
  (:require-macros [plastic.macros.logging :refer [log info warn error group group-end]]))


;; --  API  -------

(def dispatch router/dispatch)
(def dispatch-sync router/dispatch-sync)

(def register-sub subs/register)
(def clear-sub-handlers! subs/clear-handlers!)
(def subscribe subs/subscribe)


(def clear-event-handlers! handlers/clear-handlers!)


(def pure middleware/pure)
(def debug middleware/debug)
(def undoable middleware/undoable)
(def path middleware/path)
(def enrich middleware/enrich)
(def trim-v middleware/trim-v)
(def after middleware/after)
(def log-ex middleware/log-ex)

;; ALPHA - EXPERIMENTAL MIDDLEWARE
(def on-changes middleware/on-changes)


;; --  Convenience API -------

;; Almost 100% of handlers will be pure, so make it easy to
;; register with "pure" middleware in the correct (left-hand-side) position.
(defn register-handler
  ([id handler]
   (handlers/register-base id [pure trim-v log-ex] handler))
  ([id middleware handler]
   (handlers/register-base id [pure trim-v log-ex middleware] handler)))
