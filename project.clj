(defproject logic "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.0"]
                 [org.clojure/core.logic "1.1.0"]
                 [io.pedestal/pedestal.service "0.5.9"]
                 [io.pedestal/pedestal.route "0.5.9"]
                 [io.pedestal/pedestal.immutant "0.5.9"]
                 [com.datomic/peer "1.0.7075"]
                 [org.slf4j/slf4j-simple "1.7.32"]
                 [environ/environ "1.2.0"]
                 [org.postgresql/postgresql "42.7.3"]]
  :main ^:skip-aot logic.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
