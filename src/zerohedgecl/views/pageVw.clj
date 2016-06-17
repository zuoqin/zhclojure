(ns zerohedgecl.views.pageVw
  (:require [zerohedgecl.views.layout :as layout]
            [hiccup.core :refer [h]]
            [hiccup.form :as form]
            [ring.util.anti-forgery :as anti-forgery]
            [zerohedgecl.models.pageM :as pageM]
  )
)
(def pages (atom []))

(def stories (atom []))




(defn loadandsetpage [pageid]
  (
    let
      [
        page (pageM/loadPage pageid)

      ]

      ;(println "in loadandsetpage")

        


      


      page


  )
  
)

(defn get-page-items [found pageid]
  (if (< found 1)
    (loadandsetpage pageid) ;(pageM/loadPage pageid)
    (filter #(= (compare (% :pageid) pageid) 0 ) @pages )
  )
)

(defn build-items-html [all-items]

  [:div#blogItems.col-md-12 {:style "margin-top: 60px;"}
    (for [{:keys [title updated introduction reference]} all-items] 
      [:div {:class "panel-primary"}
        [:div {:class "panel-heading"}
          [:h3 {:class "panel-title"}
            [:a {:href (str "../story/" reference)}
              title
            ]
          ]
        ]
        [:div {:class "panel-body"}
            introduction
            updated
        ]
      ]

    )
  ]
)

(defn show-items [pageid]
  (let [
    foundpage (count (filter #(= (compare (% :pageid) pageid) 0 ) @pages ))

    all-items (get-page-items foundpage pageid)

    ] 

    (
      build-items-html all-items

    )
  )

)

(defn api-page [& [pageid]]
  (let [
    foundpage (count (filter #(= (compare (% :pageid) pageid) 0 ) @pages ))

    all-items (get-page-items foundpage pageid)
    response {:body {:Page pageid :Data all-items} } 
    ] 
    response
  )
)


(defn display-page [& [pageid]]
  (layout/page

    [:div {:role "navigation", :class "navbar navbar-inverse navbar-fixed-top"}
      [:div {:class "navbar-collapse collapse"}
        [:div {:align="left"}
          [:ul {:class "nav navbar-nav"}
            [:li
              [:a {:href "/page/0"} "Home"]
            ]
            [:li {:id="page1li"}
              [:a {:href "/page/1"} "Page 1"]
            ]
            [:li {:id="page2li"}
              [:a {:href "/page/2"} "Page 2"]
            ]

            [:li {:id="page3li"}
              [:a {:href "/page/3"} "Page 3"]
            ]

            [:li {:id="page4li"}
              [:a {:href "/page/4"} "Page 4"]
            ]

            ; [:li {:id="page5li"}
            ;   [:a {:href "/page/5"} "Page 5"]
            ; ]

          ]
        ]
      ]
    ]
    
    [:section {:class "content"}
      [:div {:class "container top-padding-med"}
        (show-items pageid)
      ]
    ]
  )
)


(defn display-story-html [title body updated]
  [:div {:class "panel-primary"}
    [:div {:class "panel-heading"}
      [:h3 {:class "panel-title"}
        title
      ]
    ]
    [:div {:class "panel-body"}
      body
      updated
    ]
  ]
)

(defn get-story-body[title body updated]
  [:section {:class "content"}
    [:div {:class "container top-padding-med"}
      (display-story-html title body updated)
    ]
  ] 
)


(defn display-story [page] 
  (
    let [ 
      ;page (scrape/download-story reference)

      title (map (nth page 0) [:title] )
      body (map (nth page 0) [:body] )
      updated (map (nth page 0) [:updated] )
    ]

    (layout/story
    
      (get-story-body title body updated)
      ;here we call our show-guests function
      ;to generate the list of existing comments
      

      title
      ;(map (nth page 0) [:title] )
    )
  )
)
