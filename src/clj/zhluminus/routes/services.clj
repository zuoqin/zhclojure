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

    ;; (GET "/plus" []
    ;;   :return       Long
    ;;   :query-params [x :- Long, {y :- Long 1}]
    ;;   :summary      "x+y with query-parameters. y defaults to 1."
    ;;   (ok (+ x y)))

    ;; (POST "/minus" []
    ;;   :return      Long
    ;;   :body-params [x :- Long, y :- Long]
    ;;   :summary     "x-y with body-parameters."
    ;;   (ok (- x y)))

    ;; (GET "/times/:x/:y" []
    ;;   :return      Long
    ;;   :path-params [x :- Long, y :- Long]
    ;;   :summary     "x*y with path-parameters"
    ;;   (ok (* x y)))

    ;; (POST "/divide" []
    ;;   :return      Double
    ;;   :form-params [x :- Long, y :- Long]
    ;;   :summary     "x/y with form-parameters"
    ;;   (ok (/ x y)))

    ;; (GET "/power" []
    ;;   :return      Long
    ;;   :header-params [x :- Long, y :- Long]
    ;;   :summary     "x^y with header-parameters"
    ;;   (ok (long (Math/pow x y))))



))
