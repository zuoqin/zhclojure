# World most famous financial blog now in Clojure!


## Usage

### run locally

lein repl

user> (require 'zerohedgecl.core)

user> (zerohedgecl.core/-main)

### deploy locally

lein compile 
lein uberjar


## Test

(require '[ring.mock.request :as mock])

(application (mock/request :get "/api/page/1"))

### deploy to [heroku](http://zhcltest.herokuapp.com/page/0)

heroku create

git push heroku master

heroku ps
