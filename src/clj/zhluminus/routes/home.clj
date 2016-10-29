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
            [zhluminus.config :refer [env]]
            )

  (:import [java.net URLEncoder]
    [java.net URLDecoder]
  )

)

(def stories (atom []))

(defn now [] (new java.util.Date))


(defn replaceLinks [body root]
  (let[
          list1 [ "http://www.zerohedge.com/news/" "http://www.zerohedge.com/article/" ]
          newbody1 (str/replace body (nth list1 0) (str root "/story/news/") )
          newbody2 (str/replace newbody1 (nth list1 1) (str root "/story/news/" ) )
  ]
  ;(println root)
  newbody2
  )
)

(defn replaceLinksBack [body]
  (let[
          list1 [ "\"/sites/default\"" ]
          newbody1 (str/replace body (nth list1 0) "http://www.zerohedge.com/sites/default")
  ]
  newbody1
  )
)



(defn download-story
  "Downloading story from ZeroHedge by reference"
  [reference root]
  (let [
    port (env :port)
    story (if (= (count (filter #(= (compare (% :reference) reference) 0 ) @stories )) 0)
        (first (json/read-str (:body (client/get (str "http://127.0.0.1:" port "/api/story?url=" reference) {:accept :json})   )  )  )  
        ;(json/read-str (slurp (str "http://127.0.0.1/api/story?url=" reference) ) ) 
        
    )
    listofintro
      (if (= (count (filter #(= (compare (% :reference) reference) 0 ) @stories )) 0) 
        
        {:body (replaceLinksBack(replaceLinks (get story "body" ) root))  :title (get story "title" ) :updated (get story "updated" )} 
        (first (filter #(= (compare (% :reference) reference) 0 ) @stories )) 
      )
    
    ]
    ;;(println "count =" (count (filter #(= (compare (% :reference) reference) 0 ) @stories ))  )
    ;;(println listofintro )
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



(defn loadPage [pageid]
  (let [
    port (env :port)
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
              (json/read-str (slurp (str "http://127.0.0.1:" port "/api/stories?page=" pageid) ) )
 ) 

        ]
    stories
  )
)

(defn loadSearchPage [search pageid]
  (let [
    port (env :port)
    id(
      if( .startsWith (.getName (.getClass  pageid)) "java.lang.String")
        (Integer. pageid)
        pageid
    )
    url (str "http://127.0.0.1:" port "/api/search?srchtext=" search "&page=" pageid)
    fullpage (slurp url )  
    ;res (println fullpage)

    stories (if (= (str/index-of fullpage "Your search yielded no results") nil)  
              (map (fn [x] {
                                         :introduction (get x "introduction")
                                         :updated (get x "updated")
                                         :title (get x "title")
                                         :reference (get x "reference")
                                         }
                   
                   
                           )  
                    (json/read-str fullpage )
                    )
              [{:introduction "<p>Your search yielded no results</p>" :updated "2016-01-01" :title "Search results" :reference ""}]
 )  
        ]
    stories
  )
)



(defn loadandsetsearchpage [search pageid]
  (
    let
      [
        page (loadSearchPage search pageid)
      ]
      page
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

(defn get-page-items [pageid]
  (loadandsetpage pageid)
)

(defn get-search-page-items [search pageid]
  (loadandsetsearchpage search pageid)
)

(defn show-items [pageid]
  (let [
    all-items (get-page-items pageid)
    ] 
    all-items
  )
)

(defn show-search-items [search pageid]
  (let [
    all-items (get-search-page-items search pageid)
    ] 
    all-items
  )
)

(defn stories-page [pageid]
  (layout/render
    "stories.html" {:stories (show-items pageid) :pageid pageid}
  )
)

(defn search-stories-page [key pageid]
  ;(println "key=" key)
  (layout/render
    "search.html" {:stories (show-search-items key pageid) :search key :pageid pageid}
  )
)


(defn story-page [reference root]
  (let [story  (download-story reference root)
        ]
    ;(println "reference=" reference)
    ;;(println story)
    (layout/render
        "story.html" {:story  story   }     ;
      )
  )
)


(defn testreq [request]
  (println request)
)

(defroutes stories-routes
  (GET "/" [] (stories-page 0))
  (GET "/page/:id" [id] (stories-page id))
  ;(GET "/story/:reference" [reference] (story-page reference))
  (GET "/story*" params (story-page (URLEncoder/encode (str "" (subs (:uri params)  (+ (.indexOf (:uri params) "/story/") 6 )  )    ))  (str (:schema params) "//" (:server-name params) (if (= (:server-port params) 80) "" (str ":" (:server-port params) )  )   



)))
  (GET "/search/:key/:page" [key page] (search-stories-page key page))
  (GET "/search*" {params :query-params} (search-stories-page (get params "srchtext")  (if (= (get params "page") nil) 0 (get params "page")) ))
  (GET "/test*" params (testreq params))  
)
