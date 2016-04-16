(ns zerohedgecl.views.layout
  (:require [hiccup.page :as h]))

(defn common [title & body]
  (h/html5
   [:head
    [:meta {:charset "utf-8"}]
    [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge,chrome=1"}]
    [:meta {:name "viewport" :content
            "width=device-width, initial-scale=1, maximum-scale=1"}]
    [:title title]
    (h/include-css "/stylesheets/bootstrap.css"
                 "/stylesheets/bootstrap-theme.css"
                 "/stylesheets/screen.css")
    (h/include-css "/stylesheets/styles.css")]
   [:body
    [:div {:id "header"}
     [:h1 {:class "container"} "SHOUTER"]]
    [:div {:id "content" :class "container"} body]]))


(defn page [& body]
  (h/html5
    [:head
     [:title "ZeroHedge"]
     (h/include-css "/stylesheets/screen.css")
     (h/include-css "/stylesheets/bootstrap.css")
     (h/include-css "/stylesheets/bootstrap-theme.css")
     (h/include-css "/stylesheets/styles.css")]
    [:body body]
  )
)



(defn story [body title]
  (h/html5
    [:head
     [:title title]
     (h/include-css "/stylesheets/screen.css")
     (h/include-css "/stylesheets/bootstrap.css")
     (h/include-css "/stylesheets/bootstrap-theme.css")
     (h/include-css "/stylesheets/styles.css")]
    [:body body]
   )
 )


(defn four-oh-four []
  (common "Page Not Found"
          [:div {:id "four-oh-four"}
           "The page you requested could not be found"]))