# World most famous financial blog now in Clojure!


## Usage

### run locally

lein repl

user> (require 'zerohedgecl.core)

user> (zerohedgecl.core/-main)

### deploy to [heroku](http://zhcltest.herokuapp.com/page/0)

heroku create

git push heroku master

heroku ps
