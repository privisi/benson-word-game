(ns ^:figwheel-hooks hello-world.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [goog.dom :as gdom]
   [goog.dom.forms :as gform]
   [goog.dom.classes :as gclass]
   [reagent.core :as reagent :refer [atom]]
   [cljs.core.async :refer [<! >! chan timeout]]
   [cljs-http.client :as http]))

;; This command will cause our printlns to also show up in the console's log,
;; which can sometimes be useful.
(enable-console-print!)

(declare dispatch-event!)

(defonce word-list (reagent/atom []))

(defonce current-input (atom ""))

(defonce event-channel (chan 10))

(defn send-event! [e]
  (go (>! event-channel e)))

(declare dispatch-event!)

(defonce global-handler
  (go
    (while true
      (let [e (<! event-channel)]
        (dispatch-event! e)))))

(defn get-element-value [id]
  (-> (gdom/getElement id)
      (gform/getValue)))

(defn clear-element-value [id]
  (-> (gdom/getElement id)
      (gform/setValue "")))

(defn display-toast [text]
  (let [toast (gdom/getElement "snackbar")]
    (gdom/setTextContent toast text)
    (gclass/set toast "show")
    (js/setTimeout #(gclass/set toast "") 3000)))

(defn word-exists? [word]
  (some #{word} @word-list))

(defn format-word [word]
  (-> (clojure.string/trim word)
      (clojure.string/capitalize)))

(defn insert-word [word]
  (let [word (format-word word)]
    (cond
      (> (count (clojure.string/split word #"\s")) 1)
      (display-toast "Please type one word at a time.")

      (empty? word)
      (display-toast "Please type a word in.")

      (word-exists? word)
      (display-toast "Word already exists!")

      :else
      (swap! word-list conj word))))

(defn dispatch-event! [e]
  (condp = (:type e)
    :insert-word       (insert-word (:word e))
    (println "Don't know how to handle event: " e)))

(defn sort-word-list [words]
  [:ul
   (for [word (sort words)]
     [:li word])])


(defn word-game []
  [:div
   [:center
    [:h1 "Word Game :)"]
    [:h2 "Type your words in here and press enter"]
    [:input {:type :text :id "typeword" :name :word :value @current-input
             :on-change #(reset! current-input (-> % .-target .-value))
             :on-key-press (fn [e]
                             (if (= "Enter" (.-key e))
                               (do
                                (dispatch-event! {:type :insert-word
                                                  :word (-> e .-target .-value)})
                                (reset! current-input ""))))}]
    [:div#word-div (sort-word-list @word-list)]
    [:div#snackbar ""]]])




;;;; Mounting boilerplate below.
(defn mount [el]
  (reagent/render-component [word-game] el))

(defn get-app-element []
  (gdom/getElement "app"))

(defn mount-app-element []
  (when-let [el (get-app-element)]
    (mount el)))

;; conditionally start your application based on the presence of an "app" element
;; this is particularly helpful for testing this ns without launching the app
(mount-app-element)

;; specify reload hook with ^;after-load metadata
(defn ^:after-load on-reload []
  (mount-app-element)
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
