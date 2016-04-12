(ns zerohedgecl.core
  (:require [compojure.core :refer [defroutes GET]]
            [ring.adapter.jetty :as ring]
			[hiccup.page :as page]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [zerohedgecl.controllers.pageCtrl :as pageCtrl]
            [zerohedgecl.views.layout :as layout]
  )
  (:gen-class)
)


(defroutes routes
  pageCtrl/routes
  (route/resources "/")
  (route/not-found (layout/four-oh-four)))

(def application (wrap-defaults routes site-defaults))


(defn start [port]
  (ring/run-jetty application {:port port
                               :join? false}))

(defn -main []  
  (let [port (Integer. (or (System/getenv "PORT") "8080"))]
    (start port)))