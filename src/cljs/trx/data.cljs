(ns trx.data
   (:require [re-frame.core :as re-frame]))

(defn- delete-db [name]
  (let [req (-> js/window .-indexedDB (.deleteDatabase name))]
    (set! (.-onerror req) (fn [ev]
                            (throw js/Error.
                                   (str "Failed to delete database: "
                                        name
                                        ". "
                                        (-> ev .-target .-result)))))
    (set! (.-onsuccess req) #(prn (str "Database " name " deleted.")))))

(defn- create-test-db [name version]
  (let [req (-> js/window .-indexedDB (.open name version))]
    (set! (.-onupgradeneeded req)
          (fn [ev]
            (let [db (-> ev .-target .-result)]
              (.createObjectStore db "todos" #js {:autoincrement true})
              (println
               (str "Database " name ":" version " initialized")))))
    (set! (.-onsuccess req)
          (fn [ev]
            (let [db (-> ev .-target .-result)]
              (re-frame/dispatch [:database-ready db]))))))

(defn- init
  [name version]
  (let [req (-> js/window .-indexedDB (.open name version))
        handlers {:test (fn [ev]
                          (create-test-db name version))}
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
  ([] (up :trx-data 1))
  ([name version]
   (init name version)))



