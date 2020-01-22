(ns zhluminus.routes.services
  (:require 

    [reitit.swagger :as swagger]
    [reitit.swagger-ui :as swagger-ui]
    [reitit.ring.coercion :as coercion]
    [reitit.coercion.spec :as spec-coercion]
    [reitit.ring.middleware.muuntaja :as muuntaja]
    [reitit.ring.middleware.multipart :as multipart]
    [reitit.ring.middleware.parameters :as parameters]

    [zhluminus.middleware.formats :as formats]
    [zhluminus.middleware.exception :as exception]

            [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [schema.core :as s]
            [zhluminus.routes.stories :as stories]
            [zhluminus.routes.search :as search]
            [clojure.java.io :as io]
  )
)

(defn service-routes []
  ["/api"
   {:coercion spec-coercion/coercion
    :muuntaja formats/instance
    :swagger {:id ::api}
    :middleware [;; query-params & form-params
                 parameters/parameters-middleware
                 ;; content-negotiation
                 muuntaja/format-negotiate-middleware
                 ;; encoding response body
                 muuntaja/format-response-middleware
                 ;; exception handling
                 exception/exception-middleware
                 ;; decoding request body
                 muuntaja/format-request-middleware
                 ;; coercing response bodys
                 coercion/coerce-response-middleware
                 ;; coercing request parameters
                 coercion/coerce-request-middleware
                 ;; multipart
                 multipart/multipart-middleware]}

   ;; swagger documentation
   ["" {:no-doc true
        :swagger {:info {:title "my-api"
                         :description "https://cljdoc.org/d/metosin/reitit"}}}

    ["/swagger.json"
     {:get (swagger/create-swagger-handler)}]

    ["/api-docs/*"
     {:get (swagger-ui/create-swagger-ui-handler
             {:url "/api/swagger.json"
              :config {:validator-url nil}})}]]



   ["/stories"
    {:get
         {
          :summary      "Stories for given page."
          :parameters {:query {:page int?}}
          :responses {200 {:body  [{ :title string? :updated string? :introduction string? :reference string?}]}}
          :handler (fn [{{{:keys [page]} :query} :parameters}]
                     {:status 200
                      :body  (stories/get-items page)})
          }
    }
   ]

   ["/story"
    {:get
         {
          :summary      "One story data."
          :parameters {:query {:url string?}}
          :responses {200 {:body  [{ :title string? :updated string? :body string?}]}}
          :handler (fn [{{{:keys [url]} :query} :parameters}]
                     {:status 200
                      :body  (stories/download-story url)})
          }
    }
   ]

   ["/search"
    {:get
         {
          :summary      "Search web site for stories."
          :parameters {:query {:srchtext  string? :page int?}}
          :responses {200 {:body  [{ :title string? :updated string? :introduction string? :reference string?}]}}
          :handler (fn [{{{:keys [srchtext page]} :query} :parameters}]
                     {:status 200
                      :body  (search/get-items srchtext page)})
          }
    }
   ]
 ]
)
