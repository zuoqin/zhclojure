(ns zhluminus.routes.stories
  (:require [clojure.java.io :as io]
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
        (slurp (str "https://zerohedge.com" (URLDecoder/decode  reference)) )
        
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
                (+ (.indexOf page "span property=\"schema:dateCreated\""  (+ (.indexOf page "submitted-datetime" 0) 0 ) ) 0 )
            ) 1 )
            
            ( - (.indexOf page "</span>"
              (+ (.indexOf page "span property=\"schema:dateCreated\""  (+ (.indexOf page "submitted-datetime" 0) 0 ) ) 0 )
            
            ) 0 )
          )          
          ; body
          
          (str/replace (subs page 
                   (+ (.indexOf page ">" (.indexOf page "div property=\"schema:text\"" (.indexOf page "node__content" 0) ))1)

                   (- (.indexOf page "<div class=\"extras-section\">" (.indexOf page "node__content" 0))1)
                   ) #"src=\"/sites" "src=\"https://zerohedge.com/sites") 
          
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
    ;(println reference)
    listofintro
    
    )
)


(defn createMessage [title updated introduction reference]
  (let []
    { :title title :updated updated :introduction introduction :reference reference}
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
    ;contentItems (str/split mainContent #"views-row")
    ;contentItemsCount (count contentItems)
    ;items (take-last (- contentItemsCount 1) contentItems)
    ;outarr (map get-introduction items)
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

(defn get-items [pageid]
  (let [
    foundpage (count (filter #(= (compare (% :pageid) pageid) 0 ) @pages ))

    all-items (get-page-items foundpage pageid)

    ] 

    all-items
  )

)
