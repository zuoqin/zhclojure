(ns zhluminus.core
  (:require [zhluminus.handler :as handler]
            [luminus.repl-server :as repl]
            [luminus.http-server :as http]
            [zhluminus.config :refer [env]]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.tools.logging :as log]
            [clojure.java.io :as io]
            [mount.core :as mount])
  (:import 
   (java.security KeyStore)
   (java.util.TimeZone)
   (org.joda.time DateTimeZone)
  )    
  (:gen-class))


; (defn keystore [file pass]
;   (doto (KeyStore/getInstance "JKS")
;     (.load (io/input-stream (io/file file)) (.toCharArray pass))
;   )
; )

(def cli-options
  [["-p" "--port PORT" "Port number"
    :parse-fn #(Integer/parseInt %)]])

(mount/defstate ^{:on-reload :noop}
                http-server
                :start
                (let [ssl-options (:ssl env)]
                  (http/start
                    (merge
                       env
                       {:handler (handler/app)}
                       ; (if ssl-options
                       ;   {
                       ;    ;:port nil ;disables access on HTTP port
                       ;    :ssl-port (:port ssl-options)
                       ;    :keystore     (keystore (:keystore ssl-options) (:keystore-password ssl-options))
                       ;    :key-password (:keystore-password ssl-options)
                       ;   }
                       ; )
                    )
                  )
                )
                :stop
                (http/stop http-server))


(mount/defstate ^{:on-reload :noop}
                repl-server
                :start
                (when-let [nrepl-port (env :nrepl-port)]
                  (repl/start {:port nrepl-port}))
                :stop
                (when repl-server
                  (repl/stop repl-server)))


(defn stop-app []
  (doseq [component (:stopped (mount/stop))]
    (log/info component "stopped"))
  (shutdown-agents))

(defn start-app [args]
  (doseq [component (-> args
                        (parse-opts cli-options)
                        mount/start-with-args
                        :started)]
    (log/info component "started"))
  (.addShutdownHook (Runtime/getRuntime) (Thread. stop-app)))

(defn -main [& args]
  (start-app args))
