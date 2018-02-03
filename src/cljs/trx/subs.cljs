(ns trx.subs
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::name
 (fn [db]
   (:name db)))

(re-frame/reg-sub
 :time
 (fn [db]
   (:time db)))

(re-frame/reg-sub
 :time-color
 (fn [db]
   (:time-color db)))

(re-frame/reg-sub
 :database-ready
 (fn [db]
   (:database-ready db)))
