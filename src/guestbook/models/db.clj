(ns guestbook.models.db
  (:require [monger.core :as mg]
            [monger.collection :as mc])
  (:import [org.bson.types ObjectId]
           [com.mongodb DB WriteConcern]))

(defn read-guests2 []
	(let [] [{:message "Howdy" :name "Bob" :timestamp nil}
			{:message "Hello" :name "Bob" :timestamp nil}])
)

  ;[{:message "Howdy" :name "Bob" :timestamp nil} {:message "Hello" :name "Bob" :timestamp nil}])

(def dalmatian-list
  [{:message "Howdy" :name "Bob" :timestamp nil} {:message "Hello" :name "Bob" :timestamp nil}])

(defn read-guests []
	;; localhost, default port
	(let [conn (mg/connect)
	      db   (mg/get-db conn "test")
	      coll "clojure"
	      dalmatians (take 2 dalmatian-list)
  
	      ]
	    (println "Found total documents")
	 	(println (mc/count db coll))   
		;(mc/find db coll {:name "Bob"}))
		;; find one document by id, as `com.mongodb.DBObject` instance
		(println (mc/find-one db coll { :name "Bob" }))

	    (println)
	    (println)
	    (println)
	    (doseq [item (map list (mc/find-maps db coll))] 
	        (println item))
	    	


		;(let [] mc/find db coll {})
		map list (mc/find-maps db coll)

		   ;dalmatian-list


	    ;;(println name message)
	;(println "kjhkjhjkkhjkhkjhkjh kjhkjhjkhjkhkj")
	)
)

