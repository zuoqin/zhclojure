(ns zhluminus.routes.home
  (:require [zhluminus.layout :as layout]
            [compojure.core :refer [defroutes GET]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [clojure.data.json :as json]
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



(defn download-story
  "Downloading story from ZeroHedge by reference"
  [reference]
  (let [
    
    story (if (= (count (filter #(= (compare (% :reference) reference) 0 ) @stories )) 0)
        (first (json/read-str (:body (client/get (str "http://127.0.0.1:3000/api/story?url=" reference) {:accept :json})   )  )  )  
        ;(json/read-str (slurp (str "http://127.0.0.1:3000/api/story?url=" reference) ) ) 
        
    )
    listofintro
      (if (= (count (filter #(= (compare (% :reference) reference) 0 ) @stories )) 0) 
        
        {:body (get story "body" ) :title (get story "title" ) :updated (get story "updated" )} 
        (first (filter #(= (compare (% :reference) reference) 0 ) @stories )) 
      )
    
    ]
    (println "count =" (count (filter #(= (compare (% :reference) reference) 0 ) @stories ))  )
    (println listofintro )
    (
      if (= (count (filter #(= (compare (% :reference) reference) 0 ) @stories )) 0) 
        (swap! stories conj 
          {:downloaded (now) :updated (:updated listofintro) :body (:body listofintro) :title (:title listofintro) :reference reference }
        )
      
    )
     listofintro
    ;story
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
    ;(download-zerohedge-byid id)
    true
    false
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

;; (defn parse-zerohedge-page [id]
;;   (    
;;     let [ 
;;     mainContent (nth (str/split page #"<div class=\"view-content\">") 1) 
;;     contentItems (str/split mainContent #"views-row views-row-")
;;     contentItemsCount (count contentItems)
;;     items (take-last (- contentItemsCount 1) contentItems)
;;     outarr (map get-introduction items)
;;     ]
;;     ;(println mainContent)
;;     (refresh-page-cache id outarr)
;;     outarr
;;   )
;; )


(defn loadPage [pageid]
  (let [

    id(
      if( .startsWith (.getName (.getClass  pageid)) "java.lang.String")
        (Integer. pageid)
        pageid
    )

    stories (map (fn [x] {
                          :introduction (get x "introduction")
                          :updated (get x "updated")
                          :title (get x "title")
                          :reference (get x "reference")
                          }
                   
                   
                   )  
              (json/read-str (slurp (str "http://127.0.0.1:3000/api/stories?page=" pageid) ) )
 ) 

        ]
    ;(refresh-page-cache pageid stories)
    stories
  )
)






(defn loadandsetpage [pageid]
  (
    let
      [
        page (loadPage pageid)

      ]
      page
  )
)

(defn get-page-items [found pageid]
  (if (< found 1)
    (loadandsetpage pageid) ;(pageM/loadPage pageid)
    (filter #(= (compare (% :pageid) pageid) 0 ) @pages )
  )
)


(defn show-items [pageid]
  (let [
    foundpage (count (filter #(= (compare (% :pageid) pageid) 0 ) @pages ))

    all-items (get-page-items foundpage pageid)

    ] 
    all-items
  )
)


(defn stories-page [pageid]
  (layout/render
    "stories.html" {:stories (show-items pageid)}
  )
)

(defn story-page [reference]
  (let [story  (download-story reference)              ]
    (println "reference=" reference)
    (println story)
    (layout/render
        "story.html" {:story  story   }     ;
      )
  )
  
)


(defroutes stories-routes
  (GET "/" [] (stories-page 0))
  (GET "/page/:id" [id] (stories-page id))
  (GET "/story/:reference" [reference] (story-page reference))
)
