(ns trx.db)

(def default-db
  {:name "re-frame"
   :time (js/Date.)
   :time-color "#f88"
   :store-ready false
   :todos nil})

(def ^:const NEW-ENTITY-ID 0)

(defn new-todo []
  {:key NEW-ENTITY-ID
   :text ""})

  
