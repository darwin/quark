(ns quark.cogs.editor.commands
  (:require [quark.frame.core :refer [subscribe register-handler]]
            [quark.schema.paths :as paths]
            [quark.cogs.editor.cursor :as cursor])
  (:require-macros [quark.macros.logging :refer [log info warn error group group-end]]))

(defmulti handle (fn [command & _] command))

; ----------------------------------------------------------------------------------------------------------------

(defmethod handle :move-up [_ editor]
  (cursor/move-up editor))

(defmethod handle :move-down [_ editor]
  (cursor/move-down editor))

(defmethod handle :move-left [_ editor]
  (cursor/move-left editor))

(defmethod handle :move-right [_ editor]
  (cursor/move-right editor))

; ----------------------------------------------------------------------------------------------------------------

(defmethod handle :default [command]
  (error (str "Unknown editor command for dispatch '" command "'"))
  nil)

(defn dispatch-command [editors [editor-id command]]
  (if-let [new-editor (handle command (get editors editor-id))]
    (assoc-in editors [editor-id] new-editor)
    editors))

; ----------------------------------------------------------------------------------------------------------------
; register handlers

(register-handler :editor-command paths/editors-path dispatch-command)