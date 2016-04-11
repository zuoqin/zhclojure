(ns guestbook.routes.home
  	(:require [compojure.core :refer :all]
        [guestbook.views.layout :as layout]
		[hiccup.form :refer :all]
		[guestbook.models.db :as db]
		[guestbook.controllers.scrape3 :as scrape]
		[guestbook.routes.page :as page]
	)
)






(defn show-guests []
	[:ul.guests
		(for [{:keys [message name timestamp]} (scrape/print-headlines-and-points)]  ; (db/read-guests)]
			;[{:message "Howdy" :name "Bob" :timestamp nil}
			;{:message "Hello" :name "Bob" :timestamp nil}]]
		[:li
			[:blockquote message]
			[:p "-" [:cite name]]
			[:time timestamp]])
	]
)


(defn home [& [name message error]]
	(layout/common
		[:h1 "Guestbook"]
		[:p "Welcome to my guestbook"]
		[:p error]
		;here we call our show-guests function
		;to generate the list of existing comments
		(show-guests)
		[:hr]
		;here we create a form with text fields called "name" and "message"
		;these will be sent when the form posts to the server as keywords of
		;the same name
		(form-to [:post "/"]
			[:p "Name:"]
			(text-field "name" name)
			[:p "Message:"]
			(text-area {:rows 10 :cols 40} "message" message)
			[:br]
			(submit-button "comment"))))




(defn save-message [name message]
	(cond
		(empty? name)
		(home name message "Some dummy forgot to leave a name")
		(empty? message)
		(home name message "Don't you have something to say?")
		:else
		(do
			(scrape/print-headlines-and-points)
			;(println name message)
			(home))))


(defroutes home-routes
	(GET "/" [] (home))
	(POST "/" [name message] (save-message name message))
	(GET "/page/:id" [id] (page/display-page id))
	(GET "/story/:reference" [reference] (page/display-story reference))
)
