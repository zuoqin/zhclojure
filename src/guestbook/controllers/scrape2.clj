(ns guestbook.controllers.scrape2
  (:require [net.cgrand.enlive-html :as html]
  			[clojure.string :as str]
  	)
  )

(def ^:dynamic *base-url* "http://www.zerohedge.com/?page=2")

(defn fetch-url [url]
  (html/html-resource (java.net.URL. url)))

(defn hn-headlines []
  (map
  	html/text (
  	  html/select (fetch-url *base-url*) [:h2.title :a]  	
  	)	
  )
)

(defn hn-time []
  (map html/text
  	;(take 1
  		;(str/split 
  			(html/select (fetch-url *base-url*) [:span.submitted])
  		; "on")
  	;)
  )
)

(defn hn-introduction []
  (map str html/html-content
  		(html/select (fetch-url *base-url*) [:div.content-box-1 :p])  	
  )
)


(defn print-headlines-and-points []
  (doseq [
  		line (map #(str %1 " (" (get(str/split %2 #"on") 1)  ")" "\n" %3) (hn-headlines) (hn-time) (hn-introduction))
  		;intro (map #(str %1) (hn-introduction))
  		]
    (println)
    (println)
    (println line)
    ;(println intro)
    ))