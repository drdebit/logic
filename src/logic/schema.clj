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
             {:db/ident :transaction/timestamp
              :db/valueType :db.type/instant
              :db/cardinality :db.cardinality/one
              :db/doc "The timestamp of the transaction."}
             {:db/ident :transaction/description
              :db/valueType :db.type/string
              :db/cardinality :db.cardinality/one
              :db/doc "An optional description of the transaction."}
             ;; Transaction-assertion pairs
             {:db/ident :asserted/id
              :db/valueType :db.type/bigint
              :db/unique :db.unique/identity
              :db/cardinality :db.cardinality/one
              :db/doc "A pairing id."}
             {:db/ident :asserted/transaction
              :db/valueType :db.type/ref
              :db/cardinality :db.cardinality/one
              :db/doc "A transaction id to pair with an assertion."}
             {:db/ident :asserted/assertion
              :db/valueType :db.type/ref
              :db/cardinality :db.cardinality/one
              :db/doc "An assertion id to pair with a transaction."}
             {:db/ident :asserted/value
              :db/valueType :db.type/string
              :db/cardinality :db.cardinality/one
              :db/doc "An asserted value for the transaction-assertion pair."}
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
             {:db/ident :assertion/required-value
              :db/valueType :db.type/string
              :db/cardinality :db.cardinality/many
              :db/doc "A required value for the assertion."}
             {:db/ident :assertion/require-value
              :db/valueType :db.type/ref
              :db/cardinality :db.cardinality/many
              :db/doc "A reference to a required value for the assertion."}
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
             ;; Required values
             {:db/ident :required-value/id
              :db/valueType :db.type/bigint
              :db/unique :db.unique/identity
              :db/cardinality :db.cardinality/one
              :db/doc "The id for the required value."}
             {:db/ident :required-value/description
              :db/valueType :db.type/string
              :db/cardinality :db.cardinality/one
              :db/doc "A description of the required value."}
             {:db/ident :required-value/data-type
              :db/valueType :db.type/keyword
              :db/cardinality :db.cardinality/one
              :db/doc "The data type of the required value."}
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
