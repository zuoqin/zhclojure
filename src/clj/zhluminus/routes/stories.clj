(ns zhluminus.routes.stories
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



(defn createStoryMessage [title updated body]
  [{ :title title :updated updated :body body }]
  
)


(defn download-story
  "Downloading story from ZeroHedge by reference"
  [reference]
  (let [
    
    url (str "https://zerohedge.com" (URLDecoder/decode (URLDecoder/decode  reference "UTF-8") "UTF-8")   )

    ;url2 (println "url=" url)
    page (
      if (= (count (filter #(= (compare (% :reference) reference) 0 ) @stories )) 0)
        (:body (client/get url ) ) 
        
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
            (.indexOf page "<div class=\"submitted-datetime" 0)
            
            (+ (.indexOf page "</div>" (.indexOf page "<div class=\"submitted-datetime" 0)) 6)
          )          
          ; body
          
          (str/replace (str/replace (subs page 
                    (.indexOf page "<div class=\"node__content\"" 0)
 
                    (- (.indexOf page "<div class=\"extras-section\">" (.indexOf page "node__content" 0))1)
                    ) #"src=\"/sites" "src=\"https://zerohedge.com/sites")
                      #"srcset=\"/sites" "src=\"https://zerohedge.com/sites")
          
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
    ;;(println reference)
    listofintro
    
    )
)


(defn createMessage [title updated introduction reference picture]
  (let []
    { :title title :updated updated :introduction introduction :reference reference :picture picture}
  )
)


(defn get-introduction [source]
  (let [
    ;tr1 (println (take 300 source))
    teasertitleind (.indexOf source "teaser-title" 0)
    ;tr1 (println (str "teaserind=" teasertitleind))
    reference 
      (str 
        (URLEncoder/encode
          (str ""
            (subs source 
                (+
                  (.indexOf source "href=\""
                        (+ teasertitleind 15 )
                  )
                  6
                )
                (- 
                  (.indexOf source " rel"
                    (.indexOf source "href=\""
                          (+ teasertitleind 15 )
                    )
                  ) 
                1)

              )            
            )
            "UTF-8"
          )
       )


    title
      (subs source 
        (+ (.indexOf source ">"
            (+ (.indexOf source "<span property=\"schema:name\""
              (+ teasertitleind 15)
              )
              0
            )
          )
          1
        )

        (-
          (.indexOf source "</span"
            (+ (.indexOf source "<span property=\"schema:name\""  (+ teasertitleind 15 ))
              0
            )
          )
          0
        )
      )

    picture
      (str/replace (subs source
        (+ (.indexOf source "src="
            (+ (.indexOf source "teaser-image"
              (+ teasertitleind 15)
              )
              0
            )
          )
          5
        )

        (-
          (.indexOf source "\""
            ( + (.indexOf source "src="
              (+ (.indexOf source "teaser-image"  (+ teasertitleind 15 ))
                0
              )
            ) 10 )
          )
          0
        )
      ) #"/s3/files/" "https://zerohedge.com/s3/files/")

    introduction (subs source 
          (+ 
            (.indexOf source ">"
              (.indexOf source "div property=\"schema:text\"" 
                (+ (.indexOf source "<span class=\"teaser-text\">" 0) 10)
              )
            )
            1
          )
          
          (-
            (.indexOf source "</div>"
              (.indexOf source "div property=\"schema:text\"" 
                (+ (.indexOf source "<span class=\"teaser-text\">" 0) 10)
              )
            )
            1
          )

       )

       updated (subs source
          (+ 
            (.indexOf source "<span>"
              (.indexOf source "extras__created" 
                (+ (.indexOf source "<footer class=\"teaser-details\">" 0) 10)
              )
            )
            6
          )

          (+ 
            (.indexOf source "</span>"
              (.indexOf source "extras__created" 
                (+ (.indexOf source "<footer class=\"teaser-details\">" 0) 10)
              )
            )
            0
          )
        )   
    ]
    (createMessage
      title
      updated
      introduction
      reference
      picture
    )  
  )
)

(defn delete-page-by-number [num]
     (swap! pages #(remove (fn [page] (= (:pageid page) num)) %)))

(defn download-zerohedge-byid [id]
  ;(println "downloading page from zerohedge")
  (slurp (str "https://www.zerohedge.com/articles?page=" id))
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
    ;(println "no need to refresh cache")
  )
)

(defn refresh-page-cache [id array]

  (delete-page-by-number id)

  (doseq [x array] 

     (swap! pages conj 
        {:pageid id :downloaded (now) :updated (:updated x) :introduction (:introduction x) :title (:title x) :reference (:reference x) :picture (:picture x)}
     )
  )
)

(defn parse-zerohedge-page [page id]
  (    
    let [ 
    mainContent (nth (str/split page #"view view-articles view-id-articles view-display-id-page") 1) 
    mainContent (subs mainContent (.indexOf mainContent "<div class=\"views-row\">" 0))

    outarr 
      (loop [result [] content mainContent]
        (if (>= (.indexOf content "<div class=\"views-row\">") 0)
          (let [
            endofarticle (.indexOf content "<div class=\"views-row\">" (+ (.indexOf content "<div class=\"views-row\">") 10))
            endofarticle (if (> endofarticle 0) endofarticle (.indexOf content "<nav class=\"pager\""))

            source (subs content (+ (.indexOf content "<div class=\"views-row\">" 0) 10))

            ;tr1 (println (str "end of paragraph=" endofarticle))
            ]
            (recur (conj result (get-introduction source)) (subs content endofarticle))
          )
          result
        )
      )
    ]
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

  ;(println "from loadPage")
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
  ;; (if (< found 1)
  ;;   (loadandsetpage pageid) ;(pageM/loadPage pageid)
  ;;   (filter #(= (compare (% :pageid) pageid) 0 ) @pages )
  ;; )

  (loadandsetpage pageid)
)

(defn get-items [pageid]
  (let [
    foundpage (count (filter #(= (compare (% :pageid) pageid) 0 ) @pages ))

    all-items (get-page-items foundpage pageid)

    ] 

    all-items
  )

)
