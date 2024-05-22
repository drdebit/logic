(ns logic.schema
  (:require [datomic.api :as d]))


;; Database
;; Create and connect to database
(def db-uri "datomic:sql://logic?jdbc:postgresql://localhost:5432/datomic?user=postgres&password=PnHJGWm4FlaajEa")
;; (d/create-database db-uri)
(def conn (d/connect db-uri))

;; Schema
(def schema [;; Users
             {:db/ident :user/email
              :db/valueType :db.type/string
              :db/unique :db.unique/identity
              :db/cardinality :db.cardinality/one
              :db/doc "The user's e-mail address."}
             ;; Transactions
             {:db/ident :transaction/id
              :db/valueType :db.type/bigint
              :db/cardinality :db.cardinality/one
              :db/doc "A unique identifier for a transaction."}
             {:db/ident :transaction/attribute
              :db/valueType :db.type/ref
              :db/cardinality :db.cardinality/many
              :db/doc "A link to an attribute for the transaction."}
             ;; Assertions
             {:db/ident :assertion/id
              :db/valueType :db.type/bigint
              :db/unique :db.unique/identity
              :db/cardinality :db.cardinality/one
              :db/doc "A unique identifier for an assertion."}
             {:db/ident :assertion/keyword
              :db/valueType :db.type/keyword
              :db/unique :db.unique/identity
              :db/cardinality :db.cardinality/one
              :db/doc "The short keyword id for an assertion."}
             {:db/ident :assertion/description
              :db/valueType :db.type/string
              :db/cardinality :db.cardinality/one
              :db/doc "A linguistic description of the assertion."}
             ;; {:db/ident :assertion/requires-value?
             ;;  :db/valueType :db.type/boolean
             ;;  :db/cardinality :db.cardinality/one
             ;;  :db/doc "A true-false value for whether an assertion requires a value (such as monetary)."}
             {:db/ident :assertion/required-value
              :db/valueType :db.type/string
              :db/cardinality :db.cardinality/many
              :db/doc "A description of a required value when associating an assertion with a transaction."}
             {:db/ident :assertion/depends-on
              :db/valueType :db.type/ref
              :db/cardinality :db.cardinality/many
              :db/doc "A logical dependent for the assertion."}
             {:db/ident :assertion/dependent
              :db/valueType :db.type/ref
              :db/cardinality :db.cardinality/many
              :db/doc "A logical dependent of the assertion."}
             {:db/ident :assertion/conflicts-with
              :db/valueType :db.type/ref
              :db/cardinality :db.cardinality/many
              :db/doc "A logical conflict for the assertion."}
             ;; Classes
             {:db/ident :class/id
              :db/valueType :db.type/bigint
              :db/unique :db.unique/identity
              :db/cardinality :db.cardinality/one
              :db/doc "A unique identifier for a class."}
             {:db/ident :class/keyword
              :db/valueType :db.type/keyword
              :db/unique :db.unique/identity
              :db/cardinality :db.cardinality/one
              :db/doc "A short keyword id for a class."}
             {:db/ident :class/assertion
              :db/valueType :db.type/ref
              :db/cardinality :db.cardinality/many
              :db/doc "An assertion associated with a class."}
             ])

;; Add schema
;; @(d/transact conn schema)
