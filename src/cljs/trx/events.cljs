(ns trx.events
  (:require [re-frame.core :as rf]
            [trx.db :as db]))

(def id-gen (atom 10))

(def trim-event
  (rf/->interceptor
   :id :trim-event
   :before (fn [context]
             (let [trim-fn #(-> % rest vec)]
               (update-in context [:coeffects :event] trim-fn)))))

(rf/reg-event-db
 ::initialize-db
 [trim-event]
 (fn  [db [store]]
   (assoc db/default-db
          :store store
          :store-ready true)))

(rf/reg-event-db
 ::timer
 [trim-event]
 (fn [db [new-time]]
   (assoc db :time new-time)))

(rf/reg-event-db
 ::time-color-change
 [trim-event]
 (fn [db [new-color]]
   (assoc db :time-color new-color)))

(rf/reg-event-fx
 ::store-ready
 [trim-event]
 (fn [_ [store]]
   {:dispatch [::initialize-db store]}))



(rf/reg-event-db
 ::delete-todo
 [trim-event]
 (fn [db [key]]
   (update-in db
              [:todos :items]
              (fn [items]
                (remove #(= (:key %) key) items)))))

 (rf/reg-event-db
  ::save-todo
  [trim-event]
  (fn [db [{:keys [key] :as todo}]]
    (let [insert? (= key trx.db/NEW-ENTITY-ID)]
      (if insert?
        (update-in db [:todos :items] #(conj % (assoc todo :key (swap! id-gen inc))))
        (update-in db
                   [:todos :items]
                   (fn [items]
                     (map
                      (fn [item]
                        (if (= (:key item) key)
                          (merge item todo)
                          item))
                      items)))))))
