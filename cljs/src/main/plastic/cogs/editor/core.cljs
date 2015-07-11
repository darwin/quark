(ns plastic.cogs.editor.core
  (:require [plastic.cogs.editor.model]
            [plastic.cogs.editor.lifecycle]
            [plastic.cogs.editor.watcher]
            [plastic.cogs.editor.loader]
            [plastic.cogs.editor.parser]
            [plastic.cogs.editor.layout.core]
            [plastic.cogs.editor.analyzer]
            [plastic.cogs.editor.commands]
            [plastic.cogs.editor.cursors]
            [plastic.cogs.editor.selections])
  (:require-macros [plastic.macros.logging :refer [log info warn error group group-end]]))
