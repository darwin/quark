(ns plastic.main.editor.ops.editing
  (:require-macros [plastic.logging :refer [log info warn error group group-end]]
                   [plastic.main.glue :refer [worker-dispatch worker-dispatch-with-effect worker-dispatch-args]])
  (:require [plastic.main.frame :refer [subscribe register-handler]]
            [plastic.main.editor.model :as editor]
            [plastic.main.editor.ops.cursor :as cursor]
            [plastic.main.editor.toolkit.id :as id]
            [plastic.onion.core :as onion]))

(defn xform-on-worker [editor command+args & [callback]]
  (worker-dispatch-args (vec (concat [:editor-xform (editor/get-id editor)] command+args)) callback)
  editor)

(defn make-continuation [cb f]
  (if cb (comp cb f) f))

(defn call-continuation [cb editor]
  (if cb (cb editor) editor))

(defn move-cursor-for-case-of-selected-node-removal [editor]
  (let [cursor-id (id/id-part (editor/get-cursor editor))
        moves-to-try (if (= cursor-id (editor/get-focused-form-id editor))
                       [:move-next-form :move-prev-form]    ; case of deleting whole focused form
                       [:structural-left :structural-right :structural-up])]
    (apply cursor/apply-move-cursor editor moves-to-try)))

(defn should-commit? [editor-id]
  (onion/is-inline-editor-modified? editor-id))

(defn editing-string? [editor]
  (if (editor/editing? editor)
    (let [editor-id (editor/get-id editor)
          mode (onion/get-inline-editor-mode editor-id)]
      (= mode :string))))

(defn is-inline-editor-empty? [editor]
  {:pre [(editor/editing? editor)]}
  (let [editor-id (editor/get-id editor)]
    (onion/is-inline-editor-empty? editor-id)))

(defn get-edit-point [editor]
  (if (editor/editing? editor)
    (editor/get-editing editor)
    (editor/get-cursor editor)))

(defn walk-structural-web [web start path]
  (let [walker (fn [pos dir]
                 (let [info (get web pos)
                       _ (assert info)
                       new-pos (dir info)]
                   new-pos))]
    (reduce walker start path)))

(declare start-editing)

(defn select-neighbour-and-start-editing [form-id edit-point path editor]
  (let [structural-web (editor/get-structural-web-for-form editor form-id)
        target-node-id (walk-structural-web structural-web edit-point path)]
    (-> editor
      (editor/set-cursor target-node-id)
      (start-editing))))

(defn start-editing [editor & [cb]]
  (if (editor/editing? editor)
    (call-continuation cb editor)
    (let [cursor-id (editor/get-cursor editor)
          editor-id (editor/get-id editor)]
      (if (id/spot? cursor-id)
        (let [edit-point (get-edit-point editor)
              focused-form-id (editor/get-focused-form-id editor)
              select (fn [db]
                       (editor/update-in-db db editor-id (make-continuation cb (partial select-neighbour-and-start-editing focused-form-id edit-point [:up :down]))))]
          (xform-on-worker editor [:insert-placeholder-as-first-child edit-point] select))
        (call-continuation cb (editor/set-editing editor cursor-id))))))

(defn reset-editing [editor moved-cursor]
  (-> editor
    (editor/set-editing nil)
    (editor/replace-cursor-if-not-valid moved-cursor)))

(defn stop-editing [editor & [cb]]
  (or
    (let [editor-id (editor/get-id editor)]
      (if (editor/editing? editor)
        (if-not (should-commit? editor-id)
          (call-continuation cb (editor/set-editing editor nil))
          (let [edited-node-id (editor/get-editing editor)
                value-after-editing (onion/get-value-after-editing editor-id)
                editor-with-moved-cursor (move-cursor-for-case-of-selected-node-removal editor)
                moved-cursor (editor/get-cursor editor-with-moved-cursor)
                effect (fn [db]
                         (editor/update-in-db db editor-id (make-continuation cb reset-editing) moved-cursor))]
            (xform-on-worker editor [:edit-node edited-node-id value-after-editing] effect)))))
    (call-continuation cb editor)))

(defn perform-enter [editor]
  (let [continuation (fn [editor]
                       (let [edit-point (get-edit-point editor)]
                         (xform-on-worker editor [:enter edit-point])))]
    (stop-editing editor continuation)))

(defn perform-alt-enter [editor]
  (let [continuation (fn [editor]
                       (let [edit-point (get-edit-point editor)]
                         (xform-on-worker editor [:alt-enter edit-point])))]
    (stop-editing editor continuation)))

(defn perform-backspace-in-empty-cell [editor]
  {:pre [(is-inline-editor-empty? editor)]}
  (stop-editing editor))

(defn perform-backspace [editor & [cb]]
  (let [editor-id (editor/get-id editor)
        edit-point (get-edit-point editor)
        editor-with-moved-cursor (move-cursor-for-case-of-selected-node-removal editor)
        moved-cursor (editor/get-cursor editor-with-moved-cursor)
        move-cursor (fn [db]
                      (editor/update-in-db db editor-id (make-continuation cb reset-editing) moved-cursor))]
    (xform-on-worker editor [:backspace edit-point] move-cursor)))

(defn delete-linebreak-or-token-after-cursor [editor]
  (let [edit-point (get-edit-point editor)]
    (xform-on-worker editor [:delete edit-point])))

(defn delete-linebreak-or-token-before-cursor [editor]
  (let [edit-point (get-edit-point editor)]
    (xform-on-worker editor [:alt-delete edit-point])))

(defn open [editor op path]
  (let [editor-id (editor/get-id editor)
        continuation (fn [editor]
                       (let [edit-point (get-edit-point editor)
                             focused-form-id (editor/get-focused-form-id editor)
                             select-placeholder (fn [db] (editor/update-in-db db editor-id (partial select-neighbour-and-start-editing focused-form-id edit-point path)))]
                         (xform-on-worker editor [op edit-point] select-placeholder)))]
    (stop-editing editor continuation)))

(defn perform-space [editor]
  (open editor :space [:right]))

(defn open-list [editor]
  (open editor :open-list [:right :down]))

(defn open-vector [editor]
  (open editor :open-vector [:right :down]))

(defn open-map [editor]
  (open editor :open-map [:right :down]))

(defn open-set [editor]
  (open editor :open-set [:right :down]))

(defn open-fn [editor]
  (open editor :open-fn [:right :down]))

(defn open-meta [editor]
  (open editor :open-meta [:right :down]))

(defn open-quote [editor]
  (open editor :open-quote [:right :down]))

(defn open-deref [editor]
  (open editor :open-deref [:right :down]))