(ns hello-world.app-server
  (:require
   [compojure.core :refer [defroutes GET POST]]
   [compojure.route :as route]
   [hiccup.page :refer [html5 include-js include-css]]))

(defn index-html []
  (html5
   [:head
    [:meta {:charset "UTF-8"}]
    [:meta {:name "viewport"
            :content "width=device-width, initial-scale=1"}]
    (include-css "/css/style.css")]
   [:body
    [:h2 "Text being served by the app-server Ring handler."]
    [:div {:id "app"}]
    (include-js "/cljs-out/dev-main.js")]))

(defroutes handler
  (GET "/" [] (index-html))
  (route/not-found "<h1>Page not found</h1>"))