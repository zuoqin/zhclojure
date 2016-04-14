(ns zerohedgecl.models.pageM
  (:require [clojure.java.jdbc :as sql]
  	[clojure.string :as str]
    [clj-time.core :as t]
    [clj-time.format :as f]
    )

  (:import [java.net URLEncoder]
    [java.net URLDecoder]
  )


)

(def pages (atom []))

(def stories (atom []))

(defn pagelifetime [] 2)

(defn now [] (new java.util.Date))


(def spec (or (System/getenv "DATABASE_URL")
              "postgresql://localhost:5432/shouter"))

(defn all []
  ;(into [] (sql/query spec ["select * from shouts order by id desc"]))
  [{:name "First" :action "About"} {:name "Second" :action "Help"}]
)

(defn create [shout]
  (sql/insert! spec :shouts [:body] [shout]))




(defn createStoryMessage [title updated body]
  (let []
    [{ :title title :updated updated :body body }]
  )
)

(defn download-story [reference]
  (let [
    ;page (slurp (URLDecoder/decode reference))
    
    page (
      if (= (count (filter #(= (compare (% :reference) reference) 0 ) @stories )) 0)
        (slurp (URLDecoder/decode reference))
        
    )
    listofintro
      (if (= (count (filter #(= (compare (% :reference) reference) 0 ) @stories )) 0) 
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

        (filter #(= (compare (% :reference) reference) 0 ) @stories )
      )
    
    ]
    ;(println (:updated (get listofintro 0)))
    (
      if (= (count (filter #(= (compare (% :reference) reference) 0 ) @stories )) 0) 
        (swap! stories conj 

          ;{:pageid pageid :updated "jhkjh"}
          {:downloaded (now) :updated (:updated (get listofintro 0)) :body (:body (get listofintro 0)) :title (:title (get listofintro 0)) :reference reference }
        )
      
    )
    listofintro
    
    )
)


(defn createMessage [title updated introduction reference]
  (let []
    { :title title :updated updated :introduction introduction :reference reference}
  )
)


(defn get-introduction [source]
 (createMessage
    
    ;title
    ;"hgjjhhjghjghj"
    (subs source 
      (+
        (.indexOf source ">"
              (+ (.indexOf source "<h2 class=\"title\">" 0) 20 )
        )
        1
      )

      (.indexOf source "</a>"
            (+ (.indexOf source "<h2 class=\"title\">" 0) 20)
      ) 
    )

    
    ;updated
    ;"jhgjhgjhghjghj"
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
    ;"kjjhkhkjhjkhjk"
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
    ;"jkhjhkhkjhjk"
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


(defn delete-page-by-number [num]
     (swap! pages #(remove (fn [page] (= (:pageid page) num)) %)))


(defn loadPage [pageid]
  (let [

    
    page (
      if (< (count (filter #(= (compare (% :pageid) pageid) 0 ) @pages )) 1)
        (slurp (str "http://www.zerohedge.com/?page=" pageid))
        
    )
    
    
    outarr (into [] 
        (
        if (> (count (filter #(= (compare (% :pageid) pageid) 0 ) @pages )) 0)
          (
            if (
                <
                (t/in-minutes 
                  (t/interval 
                    (f/parse  (f/formatters :date-hour-minute-second-ms) (.format (java.text.SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ss.SSS") (:downloaded (first (filter #(= (compare (% :pageid) pageid) 0 ) @pages ) ))))
                    (f/parse  (f/formatters :date-hour-minute-second-ms) (.format (java.text.SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ss.SSS") (now)) )    
                    
                  )
                )
                (pagelifetime)
              )

              (filter #(= (compare (% :pageid) pageid) 0 ) @pages )
          )

          (take-last (- (count (map get-introduction (str/split page #"<div class=\"content-box-1\">"))) 1) 
            (map get-introduction (str/split page #"<div class=\"content-box-1\">")))

          


          
        )

      )
    
    ]


    (
      if (or
           ( < (count (filter #(= (compare (% :pageid) pageid) 0 ) @pages )) 5)
           (
            >
            (t/in-minutes 
              (t/interval 
                (f/parse  (f/formatters :date-hour-minute-second-ms) (.format (java.text.SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ss.SSS") (:downloaded (first (filter #(= (compare (% :pageid) pageid) 0 ) @pages ) ))))
                (f/parse  (f/formatters :date-hour-minute-second-ms) (.format (java.text.SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ss.SSS") (now)) )    
                
              )
            )
            (pagelifetime)
           )
          )
          (delete-page-by-number pageid)
    )


    (
      if (< (count (filter #(= (compare (% :pageid) pageid) 0 ) @pages )) 1)
        (doseq [x outarr] 

           (swap! pages conj 

              ;{:pageid pageid :updated "jhkjh"}
              {:pageid pageid :downloaded (now) :updated (:updated x) :introduction (:introduction x) :title (:title x) :reference (:reference x) }
           )
        )
    )


    outarr
    
    )
)