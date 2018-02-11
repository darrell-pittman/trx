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


(defn todo-item [todo {{:keys [save del edit cancel]} :actions}]
  (let [edit-item (r/atom todo)]
    (fn [todo {:keys [edit-id editing]}]
      (let [edit-this (and editing (= (:key todo) @edit-id))
            html [:tr]]
        (if edit-this
          (conj html                
                [:td.blue-text
                 [:input
                  {:type "text"
                   :value (:text @edit-item)
                   :on-change (fn [ev]
                                (swap!
                                 edit-item
                                 #(assoc % :text (-> ev .-target .-value))))}]]
                [:td
                 [:a.btn-floating.blue
                  {:on-click #(save @edit-item)}
                  [:i.material-icons "save"]]]
                [:td
                 [:a.btn-floating.blue
                  {:on-click cancel}
                  [:i.material-icons "cancel"]]])
          (conj html
                [:td.blue-text (:text todo)]
                (if editing
                  [:td {:colSpan 2 } ""]
                  (list
                   ^{:key "edit"}
                   [:td
                    [:a.btn-floating.blue
                     {:on-click #(edit (:key todo))}
                     [:i.material-icons "edit"]]]
                   ^{:key "save"}
                   [:td
                    [:a.btn-floating.blue
                     {:on-click #(del (:key todo))}
                     [:i.material-icons "delete"]]]))))))))


(defn todo-list []
  (let [todos (rf/subscribe [::subs/todos])
        edit-id (r/atom nil)
        actions {:save (fn [item]
                         (rf/dispatch [::events/save-todo item])
                         (reset! edit-id nil))
                 :del #(rf/dispatch [::events/delete-todo %])
                 :edit #(reset! edit-id %)
                 :cancel #(reset! edit-id nil)}]
    (fn []
      (let [adding (= db/NEW-ENTITY-ID @edit-id)
            state {:edit-id edit-id
                   :editing (or adding (not (nil? @edit-id)))
                   :actions actions}]
        [:table.todos.striped.bordered.short-row
         [:thead
          [:tr
           [:th.center-align.light-blue.white-text
            {:colSpan 3}
            "TODO List"]]]
         [:tbody
          (when (seq @todos)
            (for [todo @todos]            
              ^{:key (:key todo)}[todo-item todo state]))         
          
          (if adding
            [todo-item (db/new-todo) state]
            (when (nil? @edit-id)
              [:tr              
               [:td ""]
               [:td ""]
               [:td
                [:a.btn-floating.blue
                 {:on-click #((:edit actions) db/NEW-ENTITY-ID)}
                 [:i.tiny.material-icons "add"]]]]))]]))))


(defn main-panel []
  (let [ready (rf/subscribe [::subs/store-ready])
        name (rf/subscribe [::subs/name])]
    [:div.container.center
     (if @ready
       [:div
        [todo-list]]
       [:div
        [preloader "small"]
        [:div "Initializing..."]]
       )]
    ))

