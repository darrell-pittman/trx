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

(defn eip-button
  ([action name] (eip-button action name name))
  ([action name key]   
   ^{:key key}
   [:td
    [:a.btn-floating.blue
     {:on-click action}
     [:i.material-icons name]]]))

(defn todo-item [todo {{:keys [save del edit cancel]} :actions}]
  (let [edit-item (r/atom todo)
        update-text (fn [ev]
                      (let [text (-> ev .-target .-value)]
                        (swap! edit-item
                               #(assoc % :text text))))]
    (fn [todo {:keys [edit-id edit-action mode]}]
      (let [editing (= :edit mode)
            key (:key todo)
            edit-this (and editing (= key @edit-id))
            text-td (fn[]
                      [:td
                       (if edit-this
                         [:input {:type "text"
                                  :value (:text @edit-item)
                                  :on-change update-text }]
                         (:text todo))])]
        [:tr
         (text-td)
         (if edit-this
           (list
            (eip-button #(save @edit-item) "save")
            (eip-button cancel "cancel"))
           (if editing
             [:td {:colSpan 2 } ""]
             (list
              (eip-button #(edit key :update) "edit")
              (eip-button #(del key) "delete"))))]))))


(defn todo-list []
  (let [todos (rf/subscribe [::subs/todos])
        edit-id (r/atom nil)
        edit-action (r/atom nil)
        reset (fn []
                (reset! edit-id nil)
                (reset! edit-action nil))
        actions {:save (fn [item]
                         (rf/dispatch [::events/save-todo item @edit-action])
                         (reset))
                 :del #(rf/dispatch [::events/save-todo % :delete])
                 :edit (fn [key action]
                         (reset! edit-id key)
                         (reset! edit-action action))
                 :cancel #(reset)}]
    (fn []
      (let [adding (= :insert @edit-action)
            editing (= :update @edit-action)
            mode (if (or adding editing) :edit :view)
            state {:edit-id edit-id
                   :edit-action edit-action
                   :mode mode
                   :actions actions}]
        [:table.todos.striped.bordered.short-row
         [:thead
          [:tr
           [:th.center-align.light-blue.white-text
            {:colSpan 3}
            "TODO List"]]]
         [:tbody
          (when (seq @todos)
            (for [todo (sort-by :key < @todos)]            
              ^{:key (:key todo)}[todo-item todo state]))         
          
          (if adding
            [todo-item (db/new-todo) state]
            (when-not editing
              [:tr              
               [:td ""]
               [:td ""]
               (eip-button #((:edit actions) nil :insert) "add" "add")]))]]))))


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

