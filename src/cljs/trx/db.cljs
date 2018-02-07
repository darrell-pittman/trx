(ns trx.db)

(def default-db
  {:name "re-frame"
   :time (js/Date.)
   :time-color "#f88"
   :store-ready false
   :todos {:items [
                   {:key 1 :text "Test"}]}})

(def ^:const NEW-ENTITY-ID 0)

(defn new-todo []
  {:key NEW-ENTITY-ID
   :text ""})
