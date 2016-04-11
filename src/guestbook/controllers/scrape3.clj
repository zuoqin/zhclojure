(ns guestbook.controllers.scrape3
  (:require [net.cgrand.enlive-html :as html]
  			[clojure.string :as str]
  )
  (:import [java.net URLEncoder]
    [java.net URLDecoder]
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


(defn createMessage [title updated introduction reference]
  (let []
    { :title title :updated updated :introduction introduction :reference reference}
  )
)

(defn add20 [source]
  (+ source 20)
)

(defn get-introduction [source]
 (createMessage
    
    ;title
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

    
    ;updated
    (subs source 
      (+
        (.indexOf source "</a> on"
              (+ (.indexOf source "<h2 class=\"title\">" 0) 20 )
        )
        8
      )

      (.indexOf source "</span>"
            (+ (.indexOf source "<h2 class=\"title\">" 0) 20)
      ) 
    )

    
    ;introduction
    (subs source 
      (+
        (.indexOf source "</a> on"
              (+ (.indexOf source "<h2 class=\"title\">" 0) 20 )
        )
        34
      )

      (+
        (.indexOf source "</div>"
              (+ (.indexOf source "<h2 class=\"title\">" 0) 20)
        ) 0
      )
    )

    
    ;reference
    (str 
      (URLEncoder/encode 
        (subs source 
          (+
            (.indexOf source "href=\""
                  (+ (.indexOf source "<h2 class=\"title\">" 0) 20 )
            )
            6
          )

          (.indexOf source "\">"
            (.indexOf source "href=\""
                  (+ (.indexOf source "<h2 class=\"title\">" 0) 20 )
            )
          )
        )
        "UTF-8"
      )
    )

  )
)

(defn createStoryMessage [title updated body]
  (let []
    [{ :title title :updated updated :body body}]
  )
)


(defn download-story [reference]
  (let [
    page (slurp (URLDecoder/decode reference))
    ; output
    ;   (subs page 
    ;     (+ (.indexOf page "<div class=\"clear-block clear\">&nbsp;</div>" 0) 43 )
    ;     (+ (.indexOf page "<div class=\"fivestar-static-form-item\">" 0) 0 )
    ;   )
    listofintro 
      (createStoryMessage 

        ; title
        (subs page 
          (+ (.indexOf page "<title>" 0) 7 )
          (+ (.indexOf page " | Zero Hedge</title>" 0) 0 )
        )
        
        ;updated
        (subs page 
          
          ( + (.indexOf page " on  "
            (+ (.indexOf page "<span class=\"submitted\">" 0) 0 )
          ) 6 )
          
          ( - (.indexOf page "</span>"
            (+ (.indexOf page "<span class=\"submitted\">" 0) 0 )
          ) 6 )
        )

        ; body
        (subs page 
          (+ (.indexOf page "<div class=\"clear-block clear\">&nbsp;</div>" 0) 43 )
          (+ (.indexOf page "<div class=\"fivestar-static-form-item\">" 0) 0 )
        )


    )    
    ;dalmatians (take-last 1 listofintro)
    ;outarr (into [] dalmatians)
    
    ]

    ;(doseq [item output] (


    ;(println listofintro)
    ;  )

    listofintro
    
    ;(doseq [item (map get-introduction output)] 
    ;    (println item))    
  ;(doseq [
  ;   line (map #(str %1 " (" (get(str/split %2 #"on") 1)  ")" "\n" %3) (hn-headlines) (hn-time) (hn-introduction))
      ;intro (map #(str %1) (hn-introduction))
  ;   ]
  ;  (println)
  ;  (println)
  ;  (println line)
    ;(println page)
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