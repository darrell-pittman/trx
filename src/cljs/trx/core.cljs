(ns trx.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [trx.events :as events]
            [trx.views :as views]
            [trx.config :as config]
            [trx.data :as data]))

(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (println "dev mode")))



(defn mount-root []
  (rf/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn dispatch-timer-event []
  (let [now (js/Date.)]
    (rf/dispatch [::events/timer now])))

(defonce do-timer (js/setInterval dispatch-timer-event 1000))

(defn ^:export init []
  (dev-setup)
  (data/up :test 1)
  (mount-root))

