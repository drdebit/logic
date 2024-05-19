(ns logic.core
  (:gen-class))
(use 'clojure.core.logic)
(use 'clojure.core.logic.pldb)


(defn all-parents [a d]
  (with-db d (run* [p] (assertion p) (assertion a) (parent p a))))

(with-db assert-facts (run* [t1 t2] (transaction t1) (transaction t2) (cause t1 t2)))

;; Example of adding a fact to an existing database, should be useful for fns.
;; (db-fact assert-facts transaction :purchase2)
;; Retracting is db-retraction.

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
