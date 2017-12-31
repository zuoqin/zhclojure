(ns zhluminus.routes.services
  (:require [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [schema.core :as s]
            [zhluminus.routes.stories :as stories]
  )
)

(defapi service-routes
  {:swagger {:ui "/swagger-ui"
             :spec "/swagger.json"
             :data {:info {:version "1.0.0"
                           :title "ZeroHedge API"
                           :description "ZeroHedge Services"}}}}
  
  (context "/api" []
    :tags ["stories"]

    (GET "/stories" []
      :query-params [{page :- Long 0}]
      :summary      "Stories for given page."
      (ok (stories/get-items page)))

    (GET "/story" []
      :query-params [url :- String]
      :summary      "One story data."
      (ok (stories/download-story url)))

))
