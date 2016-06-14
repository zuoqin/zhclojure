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

(defn all []
  ;(into [] (sql/query spec ["select * from shouts order by id desc"]))
  [{:name "First" :action "About"} {:name "Second" :action "Help"}]
)

(defn createStoryMessage [title updated body]
  (let []
    [{ :title title :updated updated :body body }]
  )
)

(defn download-story
  "Downloading story from ZeroHedge by reference"
  [reference]
  (let [
    
    page (
      if (= (count (filter #(= (compare (% :reference) reference) 0 ) @stories )) 0)
        (slurp (str "http://zerohedge.com" (URLDecoder/decode  reference)) )
        
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
            
            ( + (.indexOf page ">"
                (+ (.indexOf page "<span title"  (+ (.indexOf page "link-created last" 0) 0 ) ) 0 )
            ) 1 )
            
            ( - (.indexOf page "</span>"
               (.indexOf page ">"
                (+ (.indexOf page "<span title"  (+ (.indexOf page "link-created last" 0) 0 ) ) 0 )
            )
            ) 0 )
          )

          ; body
          (subs page 
            (+ (.indexOf page "<div class=\"content\">" 0) 21 )
            (+ 
               (if ( = (.indexOf page "<div class=\"taxonomy\">" (.indexOf page "<div class=\"content\">" 0)) -1) 
		              (.indexOf page "<div class=\"node-full_links\">" (.indexOf page "<div class=\"content\">" 0) )
                  (.indexOf page "<div class=\"taxonomy\">" (.indexOf page "<div class=\"content\">" 0) ) 
               ) 0 )
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
   ( let [
       reference (str 
            (URLEncoder/encode (str ""
              (subs source 
                (+
                  (.indexOf source "href=\""
                        (+ (.indexOf source "<h2 class=\"title teaser-title\">" 0) 20 )
                  )
                  6
                )

                (.indexOf source "\">"
                  (.indexOf source "href=\""
                        (+ (.indexOf source "<h2 class=\"title teaser-title\">" 0) 20 )
                  )
                )
              )            
            ) 
              "UTF-8"
            )
          )
       title (subs source 
          (+
            (.indexOf source ">"
                  (+ (.indexOf source "<h2 class=\"title teaser-title\">" 0) 32 )
            )
            1
          )

          (.indexOf source "</a>"
                (+ (.indexOf source "<h2 class=\"title teaser-title\">" 0) 32)
          ) 
       )
       introduction (subs source 
          (+ (.indexOf source "<span class=\"teaser-text\">" 0) 0 )
          
          (+
            (.indexOf source "</section>"
                  (+ (.indexOf source "<span class=\"teaser-text\">" 0) 0)
            ) 0
          )
       )
       updated (subs source
            
            ( + (.indexOf  source  ">"
                (+ (.indexOf source "<span title"  (+ (.indexOf source "link-created" 0) 0 ) ) 0 )
            ) 1 )
            
            ( - (.indexOf source "</span>"
               (+ (.indexOf source "<span title"  (+ (.indexOf source "link-created" 0) 0 ) ) 0 )
            ) 0 )
          )   
    ]
    ;(println "==================")
    ;(println updated)
    ;(println "=========================================================================")
    (createMessage

      ;title
      title
      ;(str "My Title") 
      ;(str "My updated")
      updated
      introduction
      ;(str "My Introduction")
      ;(str "My reference")
      reference
    )  
  )
)


(defn delete-page-by-number [num]
     (swap! pages #(remove (fn [page] (= (:pageid page) num)) %)))


(defn download-zerohedge-byid [id]
  ;(println id)
  (slurp (str "http://www.zerohedge.com/?page=" id))
)

(defn check-page-cache-need-refresh [id]
  (if(
    or
      (< (count (filter #(= (compare (% :pageid) id) 0 ) @pages )) 1)
      (
        >
        (t/in-minutes 
          (t/interval 
            (f/parse  (f/formatters :date-hour-minute-second-ms) (.format (java.text.SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ss.SSS") (:downloaded (first (filter #(= (compare (% :pageid) id) 0 ) @pages ) ))))
            (f/parse  (f/formatters :date-hour-minute-second-ms) (.format (java.text.SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ss.SSS") (now)) )    
            
          )
        )
        (pagelifetime)
      )
    )
    (download-zerohedge-byid id)
  )
)

(defn refresh-page-cache [id array]

  (delete-page-by-number id)

  (doseq [x array] 

     (swap! pages conj 
        {:pageid id :downloaded (now) :updated (:updated x) :introduction (:introduction x) :title (:title x) :reference (:reference x) }
     )
  )
)

(defn parse-zerohedge-page [page id]
  (    
    let [ 
    mainContent (nth (str/split page #"<div class=\"view-content\">") 1) 
    contentItems (str/split mainContent #"views-row views-row-")
    contentItemsCount (count contentItems)
    items (take-last (- contentItemsCount 1) contentItems)
    outarr (map get-introduction items)
    ]
    ;(println mainContent)
    (refresh-page-cache id outarr)
    outarr
  )
)


(defn loadPage [pageid]
  (let [

    id(
      if( .startsWith (.getName (.getClass  pageid)) "java.lang.String")
        (Integer. pageid)
        pageid
    )
    ]

    (if-let [
      page (check-page-cache-need-refresh id)
        ]


        (if-let [outarr 
                    (parse-zerohedge-page page id)
          ]
          outarr)
        (reverse(filter #(= (compare (% :pageid) id) 0 ) @pages ))
        
    )
  )
)
