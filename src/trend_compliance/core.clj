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

(def log-map (atom {}))
(def user-map (atom {}))

(defn create-log-map
  "take a line from detail and put into map using an atom"
  [user-map]
  (swap! log-map assoc :ip (get user-map :ip))
  (swap! log-map assoc :user-name (get user-map :user-name))
  (swap! log-map assoc :mac-ad (get user-map :mac-ad))
  (swap! log-map assoc :date (get user-map :date)))

(defn create-user-map
  "parse each line of a large file"
  [line]
  (let [l (str/split line #" = ")]
  (cond
    (re-matches #"(?i)\w{3} \w{3} \d{2} \d{2}:\d{2}:\d{2} \d{4}" (str (first l)))
      (swap! user-map assoc :date (str (first l)))
    (re-matches #"(?i)(.*)Acct-Session-Id" (str (first l)))
      (swap! user-map assoc :Acct-Session-Id (re-find #"[^\\\"]+" (str (second l))))
    (re-matches #"(?i)(.*)Framed-IP-Address" (str (first l)))
      (swap! user-map assoc :Framed-IP-Address (re-find #"[^\\\"]+" (str (second l))))
    (re-matches #"(?i)(.*)User-Name" (str (first l)))
      (swap! user-map assoc :User-Name (re-find #"[^\\\"]+" (str (second l)))))))

(defn read-large-file
  "read in a large detail file"
  [file-name]
  (with-open [rdr (io/reader file-name)]
    (let [line (line-seq rdr)
          pop-user-map (doall (map create-user-map line))
          user-map {:ip (get @user-map :Framed-IP-Address)
                    :user-name (get @user-map :User-Name)
                    :mac-ad (get @user-map :Acct-Session-Id)
                    :date (get @user-map :date)}]
    (create-log-map user-map))))

(defn read-csv
  "read in a csv file"
  [file-name]
  (with-open [rdr (io/reader file-name)]
    (doall (csv/parse-csv rdr))))

(defn get-ip
"get the ip from map"
[item]
(get item :ip))

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
      (println "=============== Wired List: ===============")
      (println wired-ip-list)))
