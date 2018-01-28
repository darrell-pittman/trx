(ns trx.data
  (:require [cljsjs.dexie]))


(defn- init!
  [name]
  (let [db (js/Dexie. (clj->js name))
        handlers {:test (fn
                          []
                          (-> db
                              (.version 1)
                              (.stores
                               (clj->js {:todos "++id, text"}))))}
        unknown #(throw (js/Error. (str "Unknown database: " name)))]
    ((get handlers name unknown))
    (println (str "Database " name " initialized"))
    db))

(defn up!
  ([] (up! :trx-data))
  ([name]
   (def db (init! name))))

  
  
