(ns zerohedgecl.core
  (:require [compojure.core :refer [wrap-routes routes defroutes GET]]
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

(defn wrap-api-middleware
  [handler]
  (fn [req]
    (println "API Middleware")
    (handler req)))

(defn wrap-app-middleware
  [handler]
  (fn [req]
    (println "App Middleware")
    (handler req)))


(defroutes app-routes
  pageCtrl/app-routes
  (route/resources "/")
  (route/not-found (layout/four-oh-four)))

(defroutes api-routes2
  (GET "/api" _ "API"))

(defroutes api-routes
  pageCtrl/api-routes
)

(def application
  (routes

    (-> api-routes
      (wrap-routes middleware/wrap-json-body)
      (wrap-routes middleware/wrap-json-params)
      (wrap-routes middleware/wrap-json-response)
    )

    (-> app-routes
      (wrap-defaults site-defaults)
    )
  )

)


(defn start [port]
  (ring/run-jetty application {:port port
                               :join? false}))

(defn -main []  
  (let [port (Integer. (or (System/getenv "PORT") "8080"))]
    (start port)))
