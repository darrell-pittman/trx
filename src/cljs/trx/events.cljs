(ns trx.events
  (:require [re-frame.core :as rf]
            [trx.db :as db]
            [trx.data :as data]))

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
 ::write-to
 [trim-event]
 (fn [db [path data]]
   (assoc-in db path data)))

(rf/reg-event-db
 ::remove
 [trim-event]
 (fn [db path]
   (assoc-in db path nil)))

 (rf/reg-event-fx
  ::save-todo
  [trim-event]
  (fn [{db :db} [{:keys [key] :as todo}]]
    (let [insert? (= key trx.db/NEW-ENTITY-ID)
          action (if insert? :insert :update)]
    {:db (assoc-in db [:todos :saving] true)
     ::data/action  {:store (:store db)
                     :object-store "todos"
                     :entity todo
                     :action action
                     :onsuccess [::update-todo]}})))

(rf/reg-event-db
 ::update-todo
 [trim-event]
 (fn [db [todo action]]
   (let [key (:key todo)
         insert? (= :insert action)]
     (if insert?
       (update-in db
                  [:todos :items]
                  #(conj % todo))
       (update-in db
                  [:todos :items]
                  (fn [items]
                    (map
                     (fn [item]
                       (if (= (:key item) key)
                         (merge item todo)
                         item))
                     items)))))))
