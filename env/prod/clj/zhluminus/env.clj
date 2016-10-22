(ns zhluminus.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[zhluminus started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[zhluminus has shut down successfully]=-"))
   :middleware identity})
