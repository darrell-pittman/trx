(ns trx.views
  (:require [re-frame.core :as re-frame]
            [trx.subs :as subs]
            ))

(defn clock []
  [:div.example-clock
   {:style {:color @(re-frame/subscribe [:time-color])}}
   (-> @(re-frame/subscribe [:time])
       .toTimeString
       (clojure.string/split " ")
       first)])

(defn color-input []
  [:div.color-input 
  "Time color: "
  [:input {:type "text"
           :value @(re-frame/subscribe [:time-color])
           :on-change #(re-frame/dispatch [:time-color-change
                                           (-> % .-target .-value)])}]])


(defn main-panel []
  (let [name (re-frame/subscribe [::subs/name])]
    [:div "Hello from " @name
     [clock]
     [color-input]]))
