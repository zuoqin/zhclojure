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

(def stories (atom []))

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
                (+
                  (.indexOf source "href=\""
                        (+ (.indexOf source "<dt class=\"title\">" 0) 0 )
                  )
                  6
                )

                (.indexOf source "\">"
                  (.indexOf source "href=\""
                        (+ (.indexOf source "<dt class=\"title\">" 0) 0 )
                  )
                )
              )            
            ) 
              "UTF-8"
            )
          )
       ;res (println  )
       title (subs source 
          (+
            (.indexOf source ">"
                  (+ (.indexOf source "<dt class=\"title\">" 0) 20 )
            )
            1
          )

          (.indexOf source "</a>"
                (+ (.indexOf source "<dt class=\"title\">" 0) 20)
          ) 
       )
       introduction (subs source 
          (+ (.indexOf source "<dd>" 0) 0 )
          
          (+
            (.indexOf source "<p class=\"search-info\">"
                  (+ (.indexOf source "<dd>" 0) 0)
            ) 0
          )
       )
       updated (subs source
            
            ( + (.indexOf  source  "</a>"
                (+ (.indexOf source "<dd>"  (+ (.indexOf source "</dt>" 0) 0 ) ) 0 )
            ) 7 )
            
            ( + (.indexOf source "</a>"
               (+ (.indexOf source "<dd>"  (+ (.indexOf source "</dt>" 0) 0 ) ) 0 )
            ) 25 )
          )
      reference (if (= (str/index-of (URLDecoder/decode reference1 "UTF-8") "http://www.zerohedge.com/") nil)  reference1 (subs reference1 30))
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

(defn delete-page-by-number [search id]
     (swap! pages #(remove (fn [page] (and  (= (:pageid page) id) (= (:search page) search)  ) %)))
)


(defn download-zerohedge-byid [search id]
  (let [
        page (if (= id 0) 
               (client/post (str "http://www.zerohedge.com/search/apachesolr_search/" search))
               (client/get (str "http://www.zerohedge.com/search/apachesolr_search/" search "?page=" id))

             )
        ]
  ;(println "downloading page from zerohedge")
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
    ;(println "no need to refresh cache")
  )
)

(defn refresh-page-cache [search id array]

  (delete-page-by-number search id)

  (doseq [x array] 

     (swap! pages conj 
        { :pageid id :downloaded (now) :search search :updated (:updated x) 
          :introduction (:introduction x) :title (:title x) :reference (:reference x) }
     )
  )
)

(defn parse-zerohedge-page [page search id]
  (    
    let [ 
    page1 page ;(slurp "E://DEV//clojure//zerohedgecl//doc//testsrch.txt")
    mainContent (nth (str/split page1 #"<dl class=\"search-results apachesolr_search-results\">") 1) 
    contentItems (str/split mainContent #"<dt class=\"title\">")
    contentItemsCount (count contentItems)
    items (take-last (- contentItemsCount 1) contentItems)
    outarr (map parse-srch-itemrow items)
    ]
    ;(println mainContent)
    ;(refresh-page-cache search id outarr)
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

  ;(println "from loadPage")
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


(defn get-page-items [found search pageid]
  (loadandsetpage search pageid)
)

(defn get-items [search pageid]
  (let [
    foundpage (count (filter #( and (= (compare (% :pageid) pageid) 0) (= (compare (% :search) search) 0)) @pages ))

    all-items (get-page-items foundpage search pageid)

    ] 

    all-items
  )

)
