(ns guestbook.controllers.scrape3
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


(defn createMessage [source]
  (let []
    {:message source :name "Bob" :timestamp nil}
  )
)

(defn add20 [source]
  (+ source 20)
)

(defn get-introduction [source]
 (createMessage
    (subs source 
      (+
        (.indexOf source ">"
              (+ (.indexOf source "<h2 class=\"title\">" 0) 20 )
        )
        1
      )

      (.indexOf source "</a>"
            (add20 (.indexOf source "<h2 class=\"title\">" 0))
      ) 
    )
  )
)


(defn print-headlines-and-points []
  (let [
    page (slurp "http://www.zerohedge.com/?page=2")
    output (str/split page #"<div class=\"content-box-1\">")
    listofintro (map get-introduction output)
    dalmatians (take-last 3 listofintro)
    outarr (into [] dalmatians)
    
    ]

    ;(doseq [item output] (


    (println outarr)
    ;  )

    outarr
    
    ;(doseq [item (map get-introduction output)] 
    ;    (println item))    
  ;(doseq [
  ;		line (map #(str %1 " (" (get(str/split %2 #"on") 1)  ")" "\n" %3) (hn-headlines) (hn-time) (hn-introduction))
  		;intro (map #(str %1) (hn-introduction))
  ;		]
  ;  (println)
  ;  (println)
  ;  (println line)
    ;(println page)
    )
)