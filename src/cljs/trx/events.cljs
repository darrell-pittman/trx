(ns trx.events
  (:require [re-frame.core :as re-frame]
            [trx.db :as db]))

(def trim-event
  (re-frame/->interceptor
   :id :trim-event
   :before (fn [context]
             (let [trim-fn #(-> % rest vec)]
               (update-in context [:coeffects :event] trim-fn)))))

(re-frame/reg-event-db
 ::initialize-db
 (fn  [_ _]
   db/default-db))

(re-frame/reg-event-db
 :timer
 [trim-event]
 (fn [db [new-time]]
   (assoc db :time new-time)))

(re-frame/reg-event-db
 :time-color-change
 [trim-event]
 (fn [db [new-color]]
   (assoc db :time-color new-color)))

(re-frame/reg-event-db
 :database-ready
 [trim-event]
 (fn [db [indexedDB]]
   (assoc db
          :database indexedDB
          :database-ready true)))
   
   
