(ns logic.data
  (:require [clojure.spec.alpha :as spec]
            [datomic.api :as d]
            [clojure.walk :refer [prewalk postwalk]]))

;; Specs
(spec/def :meta/transaction-id uuid?)
(spec/def :meta/units int?)
(spec/def :meta/dollars float?)
(spec/def :meta/pool (spec/coll-of :meta/transaction-id :kind set? :min-count 1))
(spec/def :transaction/purchase (spec/keys :req-un [:meta/units :meta/dollars]))
(spec/def :transaction/sale (spec/keys :req-un [:meta/units :meta/dollars
                                                :meta/pool]))

;; Insert into database
             ;; {:db/ident :assertion/id
             ;;  :db/valueType :db.type/bigint
             ;;  :db/unique :db.unique/identity
             ;;  :db/cardinality :db.cardinality/one
             ;;  :db/doc "A unique identifier for an assertion."}
             ;; {:db/ident :assertion/keyword
             ;;  :db/valueType :db.type/keyword
             ;;  :db/unique :db.unique/identity
             ;;  :db/cardinality :db.cardinality/one
             ;;  :db/doc "The short keyword id for an assertion."}
             ;; {:db/ident :assertion/description
             ;;  :db/valueType :db.type/string
             ;;  :db/cardinality :db.cardinality/one
             ;;  :db/doc "A linguistic description of the assertion."}
             ;; {:db/ident :assertion/depends-on
             ;;  :db/valueType :db.type/ref
             ;;  :db/cardinality :db.cardinality/many
             ;;  :db/doc "A logical dependent for the assertion."}

;; Utilities
(defn single-hash [hset]
  (->> hset
       (map first)
       (apply hash-set)))

;; Database
;; Create and connect to database
(def db-uri "datomic:sql://logic?jdbc:postgresql://localhost:5432/datomic?user=postgres&password=PnHJGWm4FlaajEa")
;; (d/create-database db-uri)
(def conn (d/connect db-uri))

(defn retrieve-assertion-id [kw]
  (ffirst (d/q '[:find ?a
                 :in $ ?akw
                 :where [?a :assertion/keyword ?akw]] (d/db conn) kw)))
(defn retrieve-assertion-ids [v]
  (mapv retrieve-assertion-id v))

(defn db-retract [id]
  @(d/transact conn [[:db/retractEntity id]]))

(defn db-assert
  ([kw desc]
   @(d/transact conn [[:db/add "assert-add" :assertion/keyword kw]
                      [:db/add "assert-add" :assertion/description desc]]))
  ([kw desc vd]
   (cond
     (string? vd) 
     @(d/transact conn [[:db/add "assert-add" :assertion/keyword kw]
                        [:db/add "assert-add" :assertion/description desc]
                        [:db/add "assert-add" :assertion/required-value vd]])
     (map? vd) (let [rv-query '[:find ?rvid :in $ ?desc ?dt
                                :where [?rvid :required-value/description ?desc]
                                [?rvid :required-value/data-type ?dt]]
                     {rvid :db/id
                      rdesc :required-value/description
                      rdt :required-value/data-type} vd]
                 (if rvid
                   @(d/transact conn [[:db/add "assert-add" :assertion/keyword kw]
                                      [:db/add "assert-add" :assertion/description desc]
                                      [:db/add "assert-add" :assertion/require-value rvid]
                                      [:db/add rvid :required-value/description rdesc]
                                      [:db/add rvid :required-value/data-type rdt]])
                   (do @(d/transact conn [[:db/add "rv1" :required-value/description rdesc]
                                          [:db/add "rv1" :required-value/data-type rdt]])
                       @(d/transact conn [[:db/add "value-add" :assertion/keyword kw]
                                          [:db/add "value-add" :assertion/description desc]
                                          [:db/add "value-add" :assertion/require-value
                                           (ffirst (d/q rv-query (d/db conn) rdesc rdt))]]))))

     (vector? vd) (do @(d/transact conn [[:db/add "assert-add" :assertion/keyword kw]
                                         [:db/add "assert-add" :assertion/description desc]])
                      (for [v vd]
                        @(d/transact conn [[:db/add "value-add" :assertion/keyword kw]
                                           [:db/add "value-add" :assertion/required-value v]])))
     :else "What is this thing you gave me?")))

(defn db-parents [child]
  (single-hash
   (d/q '[:find ?parent
          :in $ ?child
          :where [?parent :assertion/dependent ?child]]
        (d/db conn) (retrieve-assertion-id child))))

(defn db-children [parent]
  (single-hash
   (d/q '[:find ?child
          :in $ ?parent
          :where [?parent :assertion/dependent ?child]]
        (d/db conn) (retrieve-assertion-id parent))))

(defn db-conflicts [assertion]
  (single-hash
   (d/q '[:find ?a
          :in $ ?assertion
          :where [?assertion :assertion/conflicts-with ?a]]
        (d/db conn) (retrieve-assertion-id assertion))))

(defn db-all-relations [assertion]
  (into (db-conflicts assertion)
        (into (db-parents assertion)
              (db-children assertion))))

(defn db-relate [parent child]
  (let [[pid cid] (retrieve-assertion-ids [parent child])]
    (do @(d/transact conn [[:db/add cid :assertion/depends-on pid]])
        @(d/transact conn [[:db/add pid :assertion/dependent cid]])
        "Relation added!")))

(defn db-unrelate [parent child]
  (let [[pid cid] (retrieve-assertion-ids [parent child])]
    (do @(d/transact conn [[:db/retract cid :assertion/depends-on pid]])
        @(d/transact conn [[:db/retract pid :assertion/dependent cid]])
        "Relation removed!")))

(defn db-has-conflict? [a1 a2]
  (let [[a1id a2id] (retrieve-assertion-ids [a1 a2])
        conflicts (single-hash
                   (d/q '[:find ?conflict
                          :in $ ?a1
                          :where [?a1 :assertion/conflicts-with ?conflict]]
                        (d/db conn) a1id))]
    (contains? conflicts a2id)))

(defn db-conflict [a1 a2]
  (let [[a1id a2id] (retrieve-assertion-ids [a1 a2])]
    (do @(d/transact conn [[:db/add a1id :assertion/conflicts-with a2id]])
        @(d/transact conn [[:db/add a2id :assertion/conflicts-with a1id]])
        "Conflict added!")))

(defn db-unconflict [a1 a2]
  (let [[a1id a2id] (retrieve-assertion-ids [a1 a2])]
    (do @(d/transact conn [[:db/retract a1id :assertion/conflicts-with a2id]])
        @(d/transact conn [[:db/retract a2id :assertion/conflicts-with a1id]])
        "Conflict removed!")))

(defn db-show-assertion [kw]
  (let [m (d/pull (d/db conn) '[*] (retrieve-assertion-id kw))]
    (if-let [rv (:assertion/require-value m)]
      (assoc m :assertion/require-value (d/pull (d/db conn) '[*] (:db/id (first rv))))
      m)))

(defn db-all-assertions []
  (mapv (comp db-show-assertion first) (d/q '[:find ?akw
                                 :where [?a :assertion/keyword]
                                 [?a :assertion/keyword ?akw]] (d/db conn))))

(defn db-all-depends [kw]
  (let [a (retrieve-assertion-id kw)]
    (d/pull (d/db conn) '[:assertion/keyword {:assertion/depends-on ...}] a)))

(defn db-all-dependents [kw]
  (let [a (retrieve-assertion-id kw)]
    (d/pull (d/db conn) '[:assertion/keyword {:assertion/dependent ...}] a)))

(defn walk-for-assertions [kw type-kw]
  (let [asserted (atom [])
        m (case type-kw
            :depends-on (db-all-depends kw)
            :dependents (db-all-dependents kw))] 
    (postwalk (fn [x] (when (keyword? x) (swap! asserted conj x))) m)
    (remove #{:assertion/keyword :assertion/depends-on :assertion/dependent} @asserted)))

;; Rels
;; (db-rel transaction t)
;; (db-rel assertion a)
;; (db-rel assert t a)
;; (db-rel associate t t)
;; (db-rel cause t t)
;; (db-rel parent a a)

;; ;; Structs
;; (def ttoa (atom {}))
;; (def atot (atom {}))
;; (def assertions (atom {}))
;; (def attr-db (atom (db)))

;; (defn add-transaction [[t a]]
;;   (do (swap! ttoa update t (fnil conj #{}) a)
;;       (swap! atot update a (fnil conj #{}) t)))

;; (defn add-transactions [v]
;;   (doall (for [i v]
;;            (add-transaction i))))

;; (def transactions-old {:purchase {:dollars 800 :units 100}
;;                    :sale1 {:dollars 40 :units 5}
;;                    :inv1 {:units -5}})

;; (def assertions {:econtrol "The firm has economic control over this resource."
;;                  :counterparty "There is a counterparty to this transaction."
;;                  :owed "This transaction results in an obligation to a counterparty from the firm."
;;                  :owe "This transaction results in an obligation from a counterparty to the firm."})

;; (def parent-facts (db
;;                    [assertion :counterparty]
;;                    [assertion :owed]
;;                    [assertion :owe]
;;                    [parent :counterparty :owed]
;;                    [parent :counterparty :owe]))

;; (def assert-facts (db
;;                    [transaction :purchase]
;;                    [transaction :sale1]
;;                    [assert :sale1 :owed]
;;                    [transaction :inv1]
;;                    [cause :sale1 :inv1]))
