(ns quark.onion.inface
  (:require [quark.cogs.editor.renderer :refer [mount-editor]]
            [quark.onion.api :as api]
            [clojure.string :as string])
  (:require-macros [quark.macros.logging :refer [log info warn error group group-end]]
                   [quark.macros.glue :refer [dispatch react!]]))

(defonce ids->views (atom {}))

(defn register-view [editor-id atom-view]
  (swap! ids->views assoc editor-id atom-view))

(defn unregister-view [editor-id]
  (swap! ids->views dissoc editor-id))

; -------------------------------------------------------------------------------------------

(defmulti process (fn [command & _] (keyword command)))

(defmethod process :default [command]
  (error (str "Invalid onion message '" command "'")))

(defmethod process :apis [_ apis]
  (api/register-apis! apis))

(defmethod process :init [_ state]
  (dispatch :init (js->clj state :keywordize-keys true)))

(defmethod process :register-editor [_ atom-view]
  (let [editor-id (.-id atom-view)
        editor-def {:id  editor-id
                    :uri (.-uri atom-view)}]
    (register-view editor-id atom-view)
    (dispatch :add-editor editor-id editor-def)
    (mount-editor (.-element atom-view) editor-id)))

(defmethod process :unregister-editor [_ atom-view]
  (let [editor-id (.-id atom-view)]
    (dispatch :remove-editor editor-id)
    (unregister-view editor-id)))

(defmethod process :editor-command [_ atom-view command event]
  (let [editor-id (.-id atom-view)
        internal-command (keyword (string/replace command #"^quark:" ""))]
    (dispatch :editor-command editor-id internal-command)
    (.stopPropagation event)))

; -------------------------------------------------------------------------------------------

(defn ^:export send [& args]
  (apply process args))