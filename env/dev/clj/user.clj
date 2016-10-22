(ns user
  (:require [mount.core :as mount]
            zhluminus.core))

(defn start []
  (mount/start-without #'zhluminus.core/repl-server))

(defn stop []
  (mount/stop-except #'zhluminus.core/repl-server))

(defn restart []
  (stop)
  (start))


