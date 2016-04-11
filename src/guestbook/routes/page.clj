(ns guestbook.routes.page
  (:require [compojure.core :refer :all]
            [guestbook.views.layout :as layout]
			[hiccup.form :refer :all]
			[guestbook.models.db :as db]
			[guestbook.controllers.scrape3 :as scrape]
	)
 )


(defn show-items []
	[:div#blogItems.col-md-12 {:style "margin-top: 60px;"}
		(for [{:keys [title updated introduction reference]} (scrape/print-headlines-and-points)]  ; (db/read-guests)]
			;[{:message "Howdy" :name "Bob" :timestamp nil}
			;{:message "Hello" :name "Bob" :timestamp nil}]]

			[:div {:class "panel-primary"}
				[:div {:class "panel-heading"}
					[:h3 {:class "panel-title"}
						[:a {:href (str '../story?q=' reference)}
							title
						]
					]
				]
				[:div {:class "panel-body"}
					;[:p 
						introduction
						;"<p><strong>The Greater Depression has started. Most people don't know it</strong> because  they can neither confront the thought nor understand the differences  between this one and the last. As a climax approaches,<em><strong> many of the things that you've built your life  around in the past are going to change and change radically.</strong></em></p>"
						updated
					;]
					;[:p updated]
				]
			]

		)
	]


)


(defn home [& [pageid]]
	(layout/common
		[:h1 "Guestbook"]
		

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
					]
				]
			]
		]
		
		[:section {:class "content"}
			[:div {:class "container top-padding-med"}
				(show-items)
			]
		]
		;here we call our show-guests function
		;to generate the list of existing comments
		[:hr]
	)
)


(defn display-page [pageid]
	(home)
)


(defn show-story [reference]
	[:div#blogItems.col-md-12
		(for 
			[{:keys [title updated body]} (scrape/download-story reference)] 

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
	]


)


(defn display-story [reference]
	(let [ page (show-story reference)])
	(layout/common
		
		[:section {:class "content"}
			[:div {:class "container top-padding-med"}
				(show-story reference)
			]
		]
		;here we call our show-guests function
		;to generate the list of existing comments
		[:hr]



	)
)