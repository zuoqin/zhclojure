(ns zhluminus.handler
  (:require [compojure.core :refer [routes wrap-routes]]
            [zhluminus.layout :refer [error-page]]
            [zhluminus.routes.home :refer [stories-routes]]
            [zhluminus.routes.services :refer [service-routes]]
            [compojure.route :as route]
            [zhluminus.env :refer [defaults]]
            [mount.core :as mount]
            [zhluminus.middleware :as middleware]))

(mount/defstate init-app
                :start ((or (:init defaults) identity))
                :stop  ((or (:stop defaults) identity)))

(def app-routes
  (routes
    (-> #'stories-routes
        (wrap-routes middleware/wrap-csrf)
        (wrap-routes middleware/wrap-formats))
    #'service-routes
    (route/not-found
      (:body
        (error-page {:status 404
                     :title "page not found"})))))


(defn app [] (middleware/wrap-base #'app-routes))
