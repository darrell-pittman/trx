(ns trx.views
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [trx.subs :as subs]
            [trx.events :as events]
            [trx.config :as config]
            [trx.db :as db]))


(defn preloader [size]
  [:div {:class (str "preloader-wrapper " size " active")}
   [:div {:class "spinner-layer spinner-blue-only"}
    [:div {:class "circle-clipper left"}
     [:div {:class "circle"}]]
    [:div {:class "gap-patch"}
     [:div {:class "circle"}]]
    [:div {:class "circle-clipper right"}
     [:div {:class "circle"}]]]])


(defn todo-item [todo {:keys [save del edit cancel]}]
  (let [edit-item (r/atom todo)]
    (fn [todo {:keys [edit-id editing]}]
      (let [edit-this (and editing (= (:key todo) @edit-id))
            html [:div.row]]
        (if edit-this
          (conj html                
                [:div.col.s8
                 [:input
                  {:type "text"
                   :value (:text @edit-item)
                   :on-change (fn [ev]
                                (swap!
                                 edit-item
                                 #(assoc % :text (-> ev .-target .-value))))}]]
                [:div.col.s2
                 [:a.btn-floating.light-blue
                  {:on-click #(save @edit-item)}
                  [:i.material-icons "save"]]]
                [:div.col.s2
                 [:a.btn-floating.light-blue
                  {:on-click cancel}
                  [:i.material-icons "cancel"]]])
          
          (conj html
                [:div.col.s8 (:text todo)]
                (if editing
                  [:div.col.s4 ""]
                  (list
                   ^{:key "edit"}
                   [:div.col.s2
                    [:a.btn-floating.light-blue
                     {:on-click #(edit (:key todo))}
                     [:i.material-icons "edit"]]]
                   ^{:key "save"}
                   [:div.col.s2
                    [:a.btn-floating.light-blue
                     {:on-click #(del (:key todo))}
                     [:i.material-icons "delete"]]]))))))))
                

(defn todo-list []
  (let [todos (rf/subscribe [::subs/todos])
        edit-id (r/atom nil)]
    (fn []
      (let [adding (= db/NEW-ENTITY-ID @edit-id)
            state {:edit-id edit-id
                   :editing (or adding (> @edit-id db/NEW-ENTITY-ID))
                   :save (fn [item]
                           (rf/dispatch [::events/save-todo item])
                           (reset! edit-id nil))
                   :del #(rf/dispatch [::events/delete-todo %])
                   :edit #(reset! edit-id %)
                   :cancel #(reset! edit-id nil)}]
        [:div.todos
          [:div.row
          [:div.col.s12.light-blue.white-text "TODO List"]]
         (when (seq @todos)
           (for [todo (sort-by :key < @todos)]            
            ^{:key (:key todo)}[todo-item todo state]))         
         (let [html [:div.row]]
           (if adding
              (conj html [todo-item (db/new-todo) state])
             (when (nil? @edit-id)
               (conj html
                     [:div.col.s8 ""]
                     [:div.col.s2 ""]
                     [:div.col.s2
                      [:a.btn-floating.light-blue
                       {:on-click #((:edit state) db/NEW-ENTITY-ID)}
                       [:i.material-icons "add"]]]))))]))))
      

(defn main-panel []
  (let [ready (rf/subscribe [::subs/store-ready])
        name (rf/subscribe [::subs/name])]
    [:div.container.center
    (if @ready
      [:div
       [todo-list]]
      [:div
       [preloader "small"]
       [:div "Initializing..."]])]))
  
