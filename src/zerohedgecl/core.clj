(ns zerohedgecl.core
  (:require [compojure.core :refer [routes defroutes GET]]
            [ring.adapter.jetty :as ring]
	    [hiccup.page :as page]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.json :as middleware]
            [zerohedgecl.controllers.pageCtrl :as pageCtrl]
            [zerohedgecl.views.layout :as layout]
  )
  (:gen-class)
)


(defn wrap-app-middleware
  [handler]
  (fn [req]
    (println "App Middleware")
    (handler req)))


(defroutes app-routes
  pageCtrl/app-routes
  (route/resources "/")
  (route/not-found (layout/four-oh-four)))



(defroutes api-routes
  pageCtrl/api-routes
  (route/resources "/")
  (route/not-found (layout/four-oh-four)))

(def application
  (routes (-> api-routes
               (middleware/wrap-json-body)
               (middleware/wrap-json-response)
          )
          (-> app-routes
              (wrap-app-middleware)))
)


(defn start [port]
  (ring/run-jetty application {:port port
                               :join? false}))

(defn -main []  
  (let [port (Integer. (or (System/getenv "PORT") "8080"))]
    (start port)))
