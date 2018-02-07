(ns trx.subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 ::name
 (fn [db]
   (:name db)))

(rf/reg-sub
 ::time
 (fn [db]
   (:time db)))

(rf/reg-sub
 ::time-color
 (fn [db]
   (:time-color db)))

(rf/reg-sub
 ::store-ready
 (fn [db]
   (:store-ready db)))

(rf/reg-sub
 ::todos
 (fn [db]
   (get-in db [:todos :items])))

(rf/reg-sub
 ::edited-todo
 (fn [db]
   (get-in db [:todos :edited])))
