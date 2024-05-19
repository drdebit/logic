(ns logic.test
  (:require  [clojure.test :as t]))
(use 'clojure.core.logic)
(use 'clojure.core.logic.pldb)

(run* [q]
  (== q true))

(db-rel person p)
(db-rel food f)
(db-rel likes p f)

(def facts (db
  [person 'phil]
  [person 'mike]
  [food 'cheese]
  [food 'apple]
  [likes 'phil 'apple]
  [likes 'phil 'cheese]))

(with-db facts (run* [p f] (food f) (person p) (likes p f)))

;; Test functions
(defn rand-units []
  (rand-int 100))
(defn rand-dollars []
  (float (rand-int 100)))
(defn sample-purchase []
  [:transaction/purchase (random-uuid) {:units (rand-units)
                                        :dollars (rand-dollars)}])
(defn wrong-purchase []
  [:transaction/purchase (random-uuid) {:units (rand-units)
                                        :dollars (rand-units)}])
(defn wrong-sale []
  [:transaction/sale (random-uuid) {:units (rand-units)
                                    :dollars (rand-dollars)}])
(defn sample-sale []
  [:transaction/sale (random-uuid) {:units (rand-units)
                                    :dollars (rand-dollars)
                                    :pool (hash-set (random-uuid))}])

