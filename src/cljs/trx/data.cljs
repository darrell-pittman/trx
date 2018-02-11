(ns trx.data
  (:require [re-frame.core :as rf]))

(defn delete-db
  ([name] (delete-db name #()))
  ([name success]
   (let [req (-> js/window .-indexedDB (.deleteDatabase name))]
     (set! (.-onerror req) (fn [ev]
                             (throw js/Error.
                                    (str "Failed to delete database: "
                                         name
                                         ". "
                                         (-> ev .-target .-result)))))
     (set! (.-onsuccess req) (fn []
                               (success)
                               (prn (str "Database " name " deleted.")))))))

(defn- create-test-db [name version success]
  (let [req (-> js/window .-indexedDB (.open name version))]
    (set! (.-onupgradeneeded req)
          (fn [ev]
            (let [db (-> ev .-target .-result)
                  todos (.createObjectStore db "todos" #js {:keyPath "key" 
                                                            :autoIncrement true})]
              (println
               (str "Database " name ":" version " initialized")))))
    (set! (.-onsuccess req)
          (fn [ev]
            (let [db (-> ev .-target .-result)]
              (rf/dispatch [success db]))))))

(defn- init
  [name version success]
  (let [req (-> js/window .-indexedDB (.open name version))
        handlers {:test (fn [_]
                          (create-test-db "test" version, success))}
        unknown (fn [_] (throw (js/Error. (str "Unknown database: " name))))]
    (set! (.-onerror req) (fn [ev]
                            (prn ev)
                            (throw (js/Error. (.-errorCode! req)))))
    (set! (.-onsuccess req)
          (fn [ev]
            ((get handlers name unknown))))))

(defn- open [name version action]
  (let [req (-> js/window .-indexedDB (.open name version))]
    (set! (.-onerror req) (fn [ev]
                            (prn ev)
                            (throw (js/Error. (.-errorCode! req)))))
    (set! (.-onsuccess req)
          (fn [ev]
            (let [db (-> ev .-target .-result)]
              (action db))))))


(defn up
  ([success] (up :trx-data 1 success))
  ([name version success]
   (init name version success)))

(defn load-todos [db {:keys [success]}]
  (let [req  (-> db
                 (.transaction "todos")
                 (.objectStore "todos")
                 .getAll)]
    (set! (.-onsuccess req)
          (fn [ev]
            (let [todos (-> ev .-target .-result)]
              (success (map #(js->clj % :keywordize-keys true) todos)))))))

(defn- success-actions [events & event-data]
  (doall (map
          (fn [ev]
            (rf/dispatch (vec (concat [ev] event-data))))
          events)))

(rf/reg-fx
 ::action
 (fn [{:keys [store object-store entity action onsuccess]}]
   (let [i-store (-> store
                     (.transaction #js [object-store] "readwrite")
                     (.objectStore object-store))]
     (cond
       (= :insert action)
       (let [req (.add i-store (clj->js (dissoc entity :key)))]
         (set! (.-onsuccess req)
               (fn [ev]
                 (let [new-entity (assoc entity :key (-> ev .-target .-result))]
                   (success-actions onsuccess new-entity action)))))
       (= :update action)
       (let [req (.put  i-store (clj->js entity))]
         (set! (.-onsuccess req)
               (fn [ev]
                 (success-actions onsuccess entity action))))
       (= :delete action)
       (let [req (.delete i-store entity)]
         (set! (.-onsuccess req)
               (fn [ev]
                 (success-actions onsuccess entity action))))))))





