(ns zerohedgecl.controllers.pageCtrl
  (:require [compojure.core :refer [defroutes GET POST]]
            [clojure.string :as str]
            [ring.util.response :as ring]
            [zerohedgecl.views.pageVw :as pageVw]
            [zerohedgecl.models.pageM :as pageM]))

(defn index []
  (pageVw/display-page "0")

)


(defn display-story [reference]
  (pageVw/display-story (pageM/download-story reference)  )
)

; (defn create
;   [shout]
;   (when-not (str/blank? shout)
;     (pageM/create shout))
;   (ring/redirect "/"))

(defroutes app-routes
  (GET  "/" [] (pageVw/display-page "0"))
  (GET "/page/:id" [id] (pageVw/display-page id))
  (GET "/story/:reference" [reference] (display-story reference))

)

(defroutes api-routes
  ;(GET "/api" _ "API")
  (GET "/api/page/:id" [id] (pageVw/api-page id))

)
