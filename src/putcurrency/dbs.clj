(ns putcurrency.dbs
  (:require [clj-time.local :as l]
            [taoensso.faraday :as far]
            [taoensso.timbre :as timbre :refer (debug)]))

(def client-opts-dev
  { ;;; For DDB Local just use some random strings here, otherwise include your
    ;;; production IAM keys:
   :access-key "<AWS_DYNAMODB_ACCESS_KEY>"
   :secret-key "<AWS_DYNAMODB_SECRET_KEY>"

   ;;; You may optionally override the default endpoint if you'd like to use DDB
   ;;; Local or a different AWS Region (Ref. http://goo.gl/YmV80o), etc.:
   :endpoint "http://localhost:8000"                   ; For DDB Local
   ;; :endpoint "http://dynamodb.eu-west-1.amazonaws.com" ; For EU West 1 AWS region
   })

(defn currency-table
  "To create a table in dynamodb"
  ([^clojure.lang.PersistentArrayMap cli-opts
    ^String table-name] (currency-table cli-opts table-name 5 30))
  ([^clojure.lang.PersistentArrayMap cli-opts
    ^String table-name
    ^Integer read-throughput
    ^Integer write-throughput]
   (far/ensure-table cli-opts table-name
                     [:currency :s]
                     {:range-keydef [:timestamp :s]
                      :throughput {:read read-throughput :write write-throughput}
                      :block? true})))

(defn put-currency
  "To register the data"
  [^clojure.lang.PersistentArrayMap cli-opts
   ^clojure.lang.Keyword table-name
   ^String currency
   ^Double price]
  (far/put-item cli-opts
                table-name
                {:currency currency
                 :timestamp (l/format-local-time (l/local-now) :date-hour-minute-second-ms)
                 :price price}))
