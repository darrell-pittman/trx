(ns trx.subs
  (:require [re-frame.core :as rf]
            [reagent.ratom :as ra]
            [trx.events :as events]
            [trx.data :as data]))

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

(rf/reg-sub-raw
 ::todos
 (fn [app-db _]
   (let [path [:todos :items]
         db-token (data/load-todos
                   (:store @app-db)
                   {:success (fn [todos]
                               (rf/dispatch
                                [::events/write-to path todos]))})] 
     (ra/make-reaction
      (fn []
        (get-in @app-db path))
      :on-dispose #(rf/dispatch [::events/remove path])))))

(rf/reg-sub
 ::edited-todo
 (fn [db]
   (get-in db [:todos :edited])))
