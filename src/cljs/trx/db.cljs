(ns trx.db)

(def default-db
  {:name "re-frame"
   :time (js/Date.)
   :time-color "#f88"
   :store-ready false
   :todos nil})

(defn new-todo []
  {:text ""})

  
