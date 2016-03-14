(defproject putcurrency "0.1.0-SNAPSHOT"
  :description "To register the price of the currency to dynamodb"
  :url "https://github.com/yasukun/putcurrency"
  :uberjar-name "putcurrency.jar"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [com.taoensso/encore "2.45.1"]
                 [http-kit "2.1.18"]
                 [compojure "1.5.0"]
                 [com.taoensso/faraday "1.8.0"]
                 [com.taoensso/timbre "4.3.1"]
                 [org.clojure/tools.cli "0.3.3"]
                 [clj-time "0.11.0"]]
  :dev-depentencies [[org.clojure/tools.namespace "0.2.11"]]
  :main putcurrency.core
  :profiles {:uberjar {:aot :all}})
