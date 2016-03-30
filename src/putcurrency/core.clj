(ns putcurrency.core
  (:use org.httpkit.server)
  (:require [putcurrency.api :refer :all]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [taoensso.timbre :as timbre :refer (debug info error)])
  (:gen-class))

(defonce server (atom nil))

(def cli-options-server
  ;; An option with a required argument
  [["-i" "--input INPUT" "dynamodb client options"
    :validate [#(.isFile (io/file %)) "Must be a file."]
    :required true]
   ;; A boolean option defaulting to nil
   ["-p" "--port PORT" "server port num"
    :default 8080
    :parse-fn #(Integer. %)]
   ["-r" "--read-throughput READTHROUGHPUT" "read throuhput"
    :default 5
    :parse-fn #(Integer. %)]
   ["-w" "--write-throughput WRITETHROUGHPUT" "write throuhput"
    :default 30
    :parse-fn #(Integer. %)]
   [nil "--debug"]
   ["-h" "--help"]])

(def cli-options-bootstrap
  ;; An option with a required argument
  [;; A boolean option defaulting to nil
   ["-h" "--help"]])

(defn action-usage [action options-summary]
  (->> ["putcurrency.jar"
        ""
        (str "Usage: program-name " action " [options]")
        ""
        "Options:"
        options-summary
        ""]
       (str/join \newline)))

(defn usage [options-summary]
  (->> ["putcurrency.jar"
        ""
        "Usage: program-name action [options]"
        ""
        "Options:"
        options-summary
        ""
        "Actions:"
        "  bootstrap                     Outputs a sample file of dynamodb crient options"
        "  server                        Start web server."
        ""]
       (str/join \newline)))

(defn stop-server []
  (when-not (nil? @server)
    ;; graceful shutdown: wait 100ms for existing requests to be finished
    ;; :timeout is optional, when no timeout, stop immediately
    (@server :timeout 100)
    (reset! server nil)))

(defn start-server [port]
  (reset! server (run-server #'app {:port port})))

(def cli-opts-samp
"{;;; For DDB Local just use some random strings here, otherwise include your
 ;;; production IAM keys:
 :access-key \"<AWS_DYNAMODB_ACCESS_KEY> \"
 :secret-key \"<AWS_DYNAMODB_SECRET_KEY> \"

 ;;; You may optionally override the default endpoint if you'd like to use DDB
 ;;; Local or a different AWS Region (Ref. http://goo.gl/YmV80o), etc.:
 ;; :endpoint \"http://localhost:8000 \"                   ; For DDB Local
 ;; :endpoint \"http://dynamodb.eu-west-1.amazonaws.com \" ; For EU West 1 AWS region
}")

(defn bootstrap
  "Outputs a sample file of dynamodb crient options"
  []
  (let [output (io/file (System/getProperty "user.dir") "opts-sample.edn")]
    (spit output cli-opts-samp)
    (info (str  (.getPath output) " write."))))

(defn create-table [^Integer read-throughput
                    ^Integer write-throughput]
  (when (nil? dynamodb-cil-opt)
    (throw (Exception. "client option is nil.")))
  (api-create-table read-throughput write-throughput))

(defn reset-cli-option [file-name]
  (do
    (info (str "file-name: " file-name))
    (reset! dynamodb-cil-opt (read-string (slurp (io/file file-name))))
    (info (str "cli-option: " @dynamodb-cil-opt))))

(defn -main
  "entry point"
  [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args nil :in-order true)
        [subcmd & subargs] arguments]
    (cond (= subcmd "bootstrap")
          (let [{:keys [options arguments errors summary]}
                (parse-opts subargs cli-options-bootstrap)]
            (if (:help options)
              (println (action-usage subcmd summary))
              (do
                (bootstrap)
                (System/exit 0))))
          (= subcmd "server")
          (let [{:keys [options arguments errors summary]}
                (parse-opts subargs cli-options-server)]
            (if (:help options)
              (println (action-usage subcmd summary))
              (do
                (if (:debug options)
                  (timbre/set-level! :debug)
                  (timbre/set-level! :info))
                (reset-cli-option (:input options))
                (create-table (:read-throughput options) (:write-throughput options))
                (info "start server. port: " (:port options))
                (start-server (:port options)))))
          :else
          (do
            (println (usage summary))
            (System/exit 0)))))
