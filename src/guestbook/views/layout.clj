(ns guestbook.views.layout
  (:require [hiccup.page :refer [html5 include-css]]))

(defn common [& body]
  (html5
    [:head
     [:title "Welcome to guestbook"]
     (include-css "/css/screen.css")
     (include-css "/css/bootstrap.css")
     (include-css "/css/bootstrap-theme.css")
     (include-css "/css/styles.css")]
    [:body body]))




(defn story [body title]
  (html5
    [:head
     [:title title]
     (include-css "/css/screen.css")
     (include-css "/css/bootstrap.css")
     (include-css "/css/bootstrap-theme.css")
     (include-css "/css/styles.css")]
    [:body body]))
