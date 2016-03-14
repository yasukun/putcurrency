(ns putcurrency.api
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [putcurrency.dbs :refer :all]
            [clj-time.local :as l]
            [clj-time.core :as t]
            [taoensso.timbre :as timbre :refer (debug info error)]))

(def dynamodb-cil-opt (atom nil))

(defn format-table-name
  [^String ym]
  (keyword (str "t" ym)))

(def table-name (memoize format-table-name))

(defn api-put
  "To register the price of the currency"
  [^String currency ^String price]
  (try
    (let [d-price (Double/parseDouble price)
          table (table-name (l/format-local-time (l/local-now) :year-month))
          in (str "table: " table ", currency: " currency ", price: " price)]
      (debug in)
      (put-currency @dynamodb-cil-opt table currency price)
      in)
    (catch Exception e (do
                         (error "caught exception: "  (.getMessage e))
                         (str "caught exception: " (.getMessage e))))))

(defn api-create-table
  "To create a table with the name of the current month and the next month"
  []
  (let [next-year-month (l/format-local-time (t/plus (l/local-now) (t/months 1)) :year-month)
        year-month (l/format-local-time (l/local-now) :year-month)]
    (try
      (doseq [ym [next-year-month year-month]]
        (info (str "create-table: " (table-name ym)))
        (let [ret (currency-table @dynamodb-cil-opt (table-name ym))]
          (info ret)))
      (catch Exception e (do
                           (error "caught exception: " (.getMessage e))
                           (str "caught exception: " (.getMessage e)))))))

(defroutes app
  (GET "/put/:currency/:price" [currency price] (api-put currency price)))
