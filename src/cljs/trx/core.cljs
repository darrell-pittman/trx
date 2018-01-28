(ns trx.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [trx.events :as events]
            [trx.views :as views]
            [trx.config :as config]
            [trx.data :as data]
            [cljsjs.dexie]))

(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (println "dev mode")))



(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn dispatch-timer-event []
  (let [now (js/Date.)]
    (re-frame/dispatch [:timer now])))

(defonce do-timer (js/setInterval dispatch-timer-event 1000))

(defn ^:export init []
  (re-frame/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (data/up! :test)
  (mount-root))

