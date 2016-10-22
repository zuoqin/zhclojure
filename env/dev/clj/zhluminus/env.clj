(ns zhluminus.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [zhluminus.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[zhluminus started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[zhluminus has shut down successfully]=-"))
   :middleware wrap-dev})
