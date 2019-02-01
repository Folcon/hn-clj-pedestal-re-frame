(ns hn-clj-pedestal-re-frame.views
  (:require
   [reagent.core :as reagent]
   [re-frame.core :as re-frame]
   [hodgepodge.core :refer [local-storage get-item remove-item]]
   [hn-clj-pedestal-re-frame.subs :as subs]))

(defn header-panel []
  (let [loading? (re-frame/subscribe [:loading?])
        token (reagent/atom (get-item local-storage "token" "empty"))
        route (if (= @token "empty") "login" "")
        label (if (= @token "empty") "login" "logout")
        on-click (fn [_]
                   (remove-item local-storage "token")
                   (reset! token "empty"))]
    [:div.flex.pa1.justify-between.nowrap.orange
     [:div.flex.flex-fixed.black
      [:div.fw7.mr1 "Hacker News"]
      [:a.ml1.no-underline.black {:href "#/"} "new"]
      [:div.ml1 "|"]
      [:a.ml1.no-underline.black {:href "#/create"} "submit"]]
     [:div.flex.flex-fixed
      [:a.ml1.no-underline.black
       {:href (str "#/" route)
        :on-click #(when-not @loading? (on-click %))}
       label]]]))

(defn link-list-panel []
  (let [links (re-frame/subscribe [::subs/links])]
    [:div
      (map (fn [link]
        (let [{:keys [id description url]} link]
          [:div.flex.mt2.items-start {:key id}
           [:div.ml1
             [:div (str description " (" url ")")]]]))
                @links)]))

(defn link-create-panel []
  (let [loading? (re-frame/subscribe [:loading?])
        error? (re-frame/subscribe [:error?])
        description (reagent/atom "")
        url (reagent/atom "")
        on-click (fn [_]
                   (when-not (or (empty? @description) (empty? @url))
                     (re-frame/dispatch [:create-link @description @url])
                     (reset! description "")
                     (reset! url "")))]
    (fn []
      [:div
       [:div.flex.flex-column.mt3
        [:input.mb2 {:type "text"
                     :placeholder "A description for the link"
                     :on-change #(reset! description (-> % .-target .-value))}]
        [:input.mb2 {:type "text"
                     :placeholder "The URL for the link"
                     :on-change #(reset! url (-> % .-target .-value))}]
        [:span.input-group-btn
         [:button.btn.btn-default {:type "button"
                                   :on-click #(when-not @loading? (on-click %))}
          "Go"]
         ]]
       (when @error?
         [:p.error-text.text-danger "Error in link creation"])])))

(defn login-panel []
  (let [loading? (re-frame/subscribe [:loading?])
        error? (re-frame/subscribe [:error?])
        name (reagent/atom "")
        email (reagent/atom "")
        password (reagent/atom "")
        login (reagent/atom true)
        on-click-signup (fn [_]
                   (when-not (or (empty? @name) (empty? @email) (empty? @password))
                     (re-frame/dispatch [:signup @name @email @password])
                     (reset! name "")
                     (reset! email "")
                     (reset! password "")))
        on-click-login (fn [_]
                   (when-not (or (empty? @email) (empty? @password))
                     (re-frame/dispatch [:login @email @password])
                     (reset! email "")
                     (reset! password "")))]
    (fn []
      [:div
       [:h4 {:class "mv3"} (if @login "Login" "Sign Up")]
       [:div.flex.flex-column.mt3
        (when (not @login)
          [:input.mb2 {:type "text"
                       :placeholder "Your name"
                       :on-change #(reset! name (-> % .-target .-value))}])
        [:input.mb2 {:type "text"
                     :placeholder "Your email address"
                     :on-change #(reset! email (-> % .-target .-value))}]
        [:input.mb2 {:type "text"
                     :placeholder "Choose a safe password"
                     :on-change #(reset! password (-> % .-target .-value))}]
        [:span.input-group-btn
         (if @login
           [:button.btn.btn-default {:type "button"
                                     :on-click #(when-not @loading? (on-click-login %))}
                                     "login"]
           [:button.btn.btn-default {:type "button"
                                     :on-click #(when-not @loading? (on-click-signup %))}
                                     "create account"])
         [:button.btn.btn-default {:type "button"
                                   :on-click #(swap! login not)}
          (if @login
            "need to create an account?"
            "already have an account?")]]]
       (when @error?
         [:p.error-text.text-danger "Error in login / signup"])])))

;; main
(defn- panels [panel-name]
  (case panel-name
    :link-list-panel [link-list-panel]
    :link-create-panel [link-create-panel]
    :login-panel [login-panel]
    [:div]))

(defn show-panel [panel-name]
  [panels panel-name])

(defn main-panel []
  (let [active-panel (re-frame/subscribe [::subs/active-panel])]
    [:div.ph3.pv1.background-gray
      [header-panel]
      [show-panel @active-panel]]))
