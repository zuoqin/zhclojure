(ns zerohedgecl.controllers.pageCtrl
  (:require [compojure.core :refer [defroutes GET POST]]
            [clojure.string :as str]
            [ring.util.response :as ring]
            [zerohedgecl.views.pageVw :as pageVw]
            [zerohedgecl.models.pageM :as pageM]))

(defn index []
  (pageVw/index (pageM/all))
)


(defn display-story [reference]
  (pageVw/display-story (pageM/download-story reference)  )
)

(defn create
  [shout]
  (when-not (str/blank? shout)
    (pageM/create shout))
  (ring/redirect "/"))

(defroutes routes
  (GET  "/" [] (pageVw/display-page 0))
  (POST "/" [shout] (create shout))


  (GET "/page/:id" [id] (pageVw/display-page id))
  (GET "/story/:reference" [reference] (display-story reference))

)