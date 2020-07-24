(ns logic.core
  (:gen-class))

(use 'clojure.core.logic)
(run* [q]
  (== q true))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
