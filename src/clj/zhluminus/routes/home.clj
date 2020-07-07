(ns zhluminus.routes.home
  (:require
   [zhluminus.layout :as layout]
   [clojure.java.io :as io]
   [zhluminus.middleware :as middleware]
   [ring.util.response]
   [ring.util.http-response :as response]

            [clojure.string :as str]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [clojure.data.json :as json]
            [clj-http.client :as client]
            [zhluminus.config :refer [env]]

            [zhluminus.routes.stories :as stories]

  )


  (:import [java.net URLEncoder]
    [java.net URLDecoder]
  )
)

(def stories (atom []))

(defn now [] (new java.util.Date))


(defn replaceLinks [body]
  (let[
          list1 [ "https://www.zerohedge.com/news" "https://www.zerohedge.com/article" ]
          newbody1 (str/replace body (nth list1 0) (str "/story?url=%2Fnews") )
          newbody2 (str/replace newbody1 (nth list1 1) (str "/story?url=%2Farticle") )

          newbody3 (str/replace newbody2 "\"/s3/files" "\"https://www.zerohedge.com/s3/files" )
          newbody3 (str/replace newbody3 "https://www.zerohedge.com/markets" "https://news.ehedge.xyz/story?url=/markets")
          newbody3 (str/replace newbody3 "https://www.zerohedge.com/health" "https://news.ehedge.xyz/story?url=/health")
          newbody3 (str/replace newbody3 "https://www.zerohedge.com/economics" "https://news.ehedge.xyz/story?url=/economics")
  ]
  ;(println root)
  newbody3
  )
)

(defn replaceLinksBack [body]
  (let[
          list1 [ "\"/sites/default\"" ]
          newbody1 (str/replace body (nth list1 0) "https://www.zerohedge.com/sites/default")
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
        
        {:body (replaceLinksBack(replaceLinks (get story "body" )))  :title (get story "title" ) :updated (get story "updated" )} 
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
                          :picture (get x "picture")
                          }
                   
                   
                   )  
              (json/read-str (slurp (str "http://127.0.0.1:" port "/api/stories?page=" pageid) ) )
              ;(stories/get-items pageid)
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

(defn stories-page [request]
  (let [id (if (nil? (:id (:params request))) 0 (:id (:params request)))]
    ;(println (str "66666666666" (:params request)))
    (layout/render request "stories.html" {:stories (show-items id) :pageid id})
  )
)

(defn search-stories-page [request]
  (let [
      key (:srchtext (:params request))
      pageid (if (nil? (:page (:params request))) 0 (:page (:params request)))
    ]
    (layout/render request
      "search.html" {:stories (show-search-items key pageid) :search key :pageid pageid}
    )
  )
)


(defn story-page [request]
  (let [story  (download-story (:url (:params request)) "hhhhhh")
        ]
    ;(println "reference=" reference)
    ;;(println story)
    (layout/render request "story.html" {:story  story})
  )
)


(defn testreq [request]
  (println request)
)

(defn home-page [request]
  (layout/render request "home.html" {:docs (-> "docs/docs.md" io/resource slurp)}))

(defn about-page [request]
  (layout/render request "about.html"))

(defn stories-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get stories-page}]
   ["/story" {:get story-page}]
   ["/page" {:get stories-page}]
   ["/search" {:get search-stories-page}]
  ]
)
