(ns zhluminus.routes.search
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [clj-http.client :as client]
            )

  (:import [java.net URLEncoder]
    [java.net URLDecoder]
  )

)



(def pages (atom []))

(defn pagelifetime [] 2)

(defn now [] (new java.util.Date))


(defn createMessage [title updated introduction reference]
  (let []
    { :title title :updated updated :introduction introduction :reference reference}
  )
)


(defn parse-srch-itemrow [source]
   ( let [
       reference1 (str 
            (URLEncoder/encode (str ""
              (subs source 
                (+ (.indexOf source "href=\"") 6)

                (.indexOf source "\""
                  (+ (.indexOf source "href=\"") 12)
                )
              )            
            ) 
              "UTF-8"
            )
          )
       title (subs source 
          (+
            (.indexOf source ">"
                  (+ (.indexOf source "href" 0) 5 )
            )
            1
          )

          (.indexOf source "</a>"
                (+ (.indexOf source "<href" 0) 5)
          ) 
       )
       introduction (subs source 
          (+ (.indexOf source "class=\"views-field views-field-search-api-excerpt\">" 0) 51 )
          
          (+
            (.indexOf source "</span>"
                  (+ (.indexOf source "class=\"views-field views-field-search-api-excerpt\">" 0) 52)
            ) 7
          )
       )
       updated (subs source
            
            ( + (.indexOf  source  "field-content"
                (+ (.indexOf source "class=\"views-field views-field-created\"") 0 )
            ) 15 )
            
            ( + (.indexOf  source  "</span>"
                (+ (.indexOf source "class=\"views-field views-field-created\"") 0 )
            ) 0 )
          )
      reference (if (= (str/index-of (URLDecoder/decode reference1 "UTF-8") "https://www.zerohedge.com/") nil)  reference1 reference1)
    ]
    (createMessage
      title
      updated
      introduction
      reference
    )  
  )
)

(defn delete-page-by-number [search id]
  (swap! pages #(remove (fn [page] (and  (= (:pageid page) id) (= (:search page) search)  )) % ))
)


(defn download-zerohedge-byid [search id]
  (let [
        page (if (= id 0) 
               (client/get (str "https://www.zerohedge.com/search-content?search_api_fulltext=" search "&sort_by=search_api_relevance"))
               (client/get (str "https://www.zerohedge.com/search-content?search_api_fulltext=" search "&sort_by=search_api_relevance" "&page=" id))

             )
        ]
  (:body  page)

  )
)

(defn check-page-cache-need-refresh [search id]
  (if(
    or
      (< (count (filter #(and  (= (compare (% :pageid) id) 0 ) (= (compare (% :search) search) 0 ) ) @pages )) 1)
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
    (download-zerohedge-byid search id)
  )
)

(defn refresh-page-cache [search id array]

  (delete-page-by-number search id)
     ;; (swap! pages conj 
     ;;    { :pageid id :downloaded (now) :search search :updated (:updated (nth array 0)) 
     ;;      :introduction (:introduction x) :title (:title x) :reference (:reference x) 
     ;;     }
     ;; )

  (doseq [x array] 
     (swap! pages conj 
        { :pageid id :downloaded (now) :search search :updated (:updated x) 
          :introduction (:introduction x) :title (:title x) :reference (:reference x) 
         }
     )
  )

)

(defn parse-zerohedge-page [page search id]
  (    
    let [ 
    page1 page ;(slurp "E://DEV//clojure//zerohedgecl//doc//testsrch.txt")
    mainContent (nth (str/split page1 #"class=\"view-content\"") 2)
    contentItems (str/split mainContent #"<div class=\"views-row\">")
    contentItemsCount (count contentItems)
    items (take-last (- contentItemsCount 1) contentItems)
    outarr (map parse-srch-itemrow items)
    ]
    (refresh-page-cache search id outarr)
    outarr
    ;contentItemsCount
  )
)


(defn loadPage [search pageid]
  (let [

    id(
      if( .startsWith (.getName (.getClass  pageid)) "java.lang.String")
        (Integer. pageid)
        pageid
    )
    ]

    (if-let [
      page (check-page-cache-need-refresh search id)      
        ]        

        (if-let [outarr 
                    (parse-zerohedge-page page search id)
          ]
          outarr)
        (reverse(filter #( and (= (compare (% :pageid) pageid) 0) (= (compare (% :search) search) 0)) @pages ))
        
    )
  )

)


(defn loadandsetpage [search pageid]
  (
    let
      [
        page (loadPage search pageid)
      ]
      page
  )  
)


(defn get-page-items [search pageid]
  (loadandsetpage search pageid)
)

(defn get-items [search pageid]
  (let [
    ;foundpage 0;(count (filter #( and (= (compare (% :pageid) pageid) 0) (= (compare (% :search) search) 0)) @pages ))

    all-items (get-page-items search pageid)

    ] 

    all-items
  )

)
