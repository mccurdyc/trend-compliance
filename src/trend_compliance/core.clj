(ns trend-compliance.core
  (:require [clojure.java.io :as io]
            [clojure.pprint :as pp]
            [clojure.string :as str]
            [clojure-csv.core :as csv])
  (:gen-class))

(defn create-map
  "parse the list of lists returned from read-csv"
  [item]
  (let [[ip cp-name domain mac-ad off-scan-status ping prod-name platform os-server version patt-file scan-eng prev-pol cp-des rem-install] item]
    {:ip ip
     :off-scan-status off-scan-status
     :ping ping
     :platform platform}))

(defn trend-installed?
  "check to see if the device has trend"
  [item]
  (if (re-matches #"(?i)not installed" (get item :off-scan-status)) true false))

(defn ping-successful?
  "check to see if the device ping was successful"
  [item]
  (if (re-matches #".*(?i)ping successful" (get item :ping)) true false))

(defn windows?
  "check to see if the device is windows os"
  [item]
  (if (re-matches #"(?i)windows*" (get item :platform)) true false))

(defn uncompliant-map
  "create a list of windows machines with successful ping that fail compliance check"
  [item]
  (filter (every-pred windows? ping-successful? trend-installed?) item))

(def user-map (atom {}))

(defn line-to-map
  "take a line from detail and put into map using an atom"
  [line]
  ;; (reset! state {})
  (if (= (str (first line)) (re-find #"\w{3} \w{3} \d{2} \d{2}:\d{2}:\d{2} \d{4}" (str (first line))))
    (swap! state assoc :date (str (first line)))
    (swap! state assoc
      (keyword (re-find #"(?x)\S+" (str (first line))))
      (re-find #"[^\\\"]+" (str (second line))))))

;; (defn compare-ip
;;   "compare the ip held in log to the ips on compliance list"
;;   [ip-list]
;;   (first (filter #(= % (get @state :Framed-IP-Address)) ip-list)))

;; (defn get-ip
;;   "get the ip from map"
;;   [item]
;;   (get item :ip))

(defn read-large-file
  "read in a large detail file"
  [file-name ip-list]
  (with-open [rdr (io/reader file-name)]
    (doseq [line (line-seq rdr)]
      (if (empty? line)
        (compare-ip ip-list)
        (line-to-map (str/split line #" = "))))))

(defn read-csv
  "read in a csv file"
  [file-name]
  (with-open [rdr (io/reader file-name)]
    (doall (csv/parse-csv rdr))))

(defn -main
  "do some fun stuff with csv files"
  [& args]
  (println "Files (<path/to/WIRED.csv> <path/to/WIRELSS.csv> <path/to/DETAIL>): ")
    (let [in (read-line)
          file-names (str/split in #" ")
          wired (first file-names)
          wireless (second file-names)
          detail (nth file-names 2)
          wired-ip-list (set (map get-ip (uncompliant-map (map create-map (read-csv wired)))))
          wireless-ip-list (set (map get-ip (uncompliant-map (map create-map (read-csv wireless)))))]
          ;; wireless-ip-list (map get-ip wireless-list)
          ;; matching-ip (read-large-file detail wireless-ip-list)]
      ;; (println matching-ip)))
      ;; (println wireless-ip-list)))
      (println "=============== Wired List: ===============")
      (println wired-ip-list)
      (println "\n=============== Wireless List: ===============")
      (println wireless-ip-list)))
