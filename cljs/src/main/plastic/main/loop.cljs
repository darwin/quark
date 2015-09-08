(ns plastic.main.loop
  (:require-macros [plastic.logging :refer [log info warn error group group-end fancy-log]])
  (:require [plastic.main.frame :refer [main-loop frame]]
            [plastic.main.init]))

(when-not plastic.env.dont-run-loops
  (fancy-log "MAIN LOOP" @frame)
  (main-loop))                                                                                                        ; start event processing
