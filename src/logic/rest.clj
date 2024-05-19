(ns logic.rest
  (:gen-class)
  (:require [clojure.spec.alpha :as spec]
            [io.pedestal.http :as http]
            [io.pedestal.http.body-params :as body-params]
            [environ.core :refer [env]]
            [datomic.api :as d]
            [logic.schema :as schema]
            [logic.data :as data]))

(def conn schema/conn)

(defn list-assertions [m]
  {:status 200 :body (data/db-all-assertions)})

(defn get-assertion [{{k :assert-key} :path-params}]
  {:status 200 :body (data/db-show-assertion k)})

(defn add-assertion [{{kw :assertion/keyword
                       desc :assertion/description
                       vd :assertion/required-value} :edn-params}]
  (cond
    vd (data/db-assert kw desc vd)
    :else (data/db-assert kw desc))
  {:status 200 :body "Assertion added!"})

(defn make-server []
  (let [routes #{;Routes
                 ["/all-assertions/" :get list-assertions :route-name :all-assertions]
                 ["/get-assertion/:assert-key" :get get-assertion :route-name :get-assertion]
                 ["/add-assertion/" :post [(body-params/body-params) add-assertion] :route-name :add-assertion]
                 ;; ["/grade-question/" :post [(body-params/body-params) grade-question]
                 ;;  :route-name :grade-question]
                 ;; ["/get-user-questions/:user/:category" :get get-filtered-questions :route-name :get-user-questions-filtered]
                 
                 }
        service-map (-> {::http/routes routes ; Routes
                         ::http/type   :immutant
                         ::http/host   "0.0.0.0"
                         ::http/join?  false
                         ::http/port (Integer. (or (env :port) 5001))
                         ::http/allowed-origins (constantly true)})]
    (http/create-server service-map)))

(def server (make-server))
;; (http/stop server)
(http/start server)
