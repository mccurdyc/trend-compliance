(ns trend-compliance.core
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.pprint :as pp]
            [clojure-csv.core :as csv])
  (:gen-class))

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

(defn create-final-list
  "create a list of windows machines with successful ping"
  [item]
  (let [windows-machines (filter windows? item)
        ping-successful-list (filter ping-successful? windows-machines)]
  (filter trend-installed? ping-successful-list)))

(defn create-data-map
  "parse the list of lists returned from read-csv
  If you want more data returned un-comment the fields below"
  [item]
  (let [[ip cp-name domain mac-ad off-scan-status ping prod-name platform os-server version patt-file scan-eng prev-pol cp-des rem-install] item]
    {:ip ip
     ;; :cp-name cp-name
     ;; :domain domain
     ;; :mac-ad mac-ad
     :off-scan-status off-scan-status
     :ping ping
     ;; :prod-name prod-name
     :platform platform
     ;; :os-server os-server
     ;; :version version
     ;; :patt-file patt-file
     ;; :scan-eng scan-eng
     ;; :prev-pol prev-pol
     ;; :cp-des cp-des
     ;; :rem-install rem-install
     }))

(defn create-detail-map
  "parse the detail file"
  [item]
  (let [[ip cp-name domain mac-ad off-scan-status ping prod-name platform os-server version patt-file scan-eng prev-pol cp-des rem-install] item]
    {:ip ip
     ;; :cp-name cp-name
     ;; :domain domain
     ;; :mac-ad mac-ad
     :off-scan-status off-scan-status
     :ping ping
     ;; :prod-name prod-name
     :platform platform
     ;; :os-server os-server
     ;; :version version
     ;; :patt-file patt-file
     ;; :scan-eng scan-eng
     ;; :prev-pol prev-pol
     ;; :cp-des cp-des
     ;; :rem-install rem-install
     }))

(defn parse-line
  "parse each line of a large file"
  [line]
  (str/split line #" = "))

(def state (atom {}))

(defn line-to-map
  "take a line from detail and put into map using an atom"
  [line]
  (reset! state {})
  (swap! state assoc
    (keyword (re-find #"(?x)\S+" (str (first line))))
    (re-find #"[^\\\"]\S+[^\\\"]" (str (second line)))))

(defn read-detail
  "read in a large detail file"
  [file-name]
  (with-open [rdr (io/reader file-name)]
    (let [lines (doall (map parse-line (line-seq rdr)))]
      (map line-to-map lines))))

(defn read-csv
  "read in a csv file"
  [file-name]
  (with-open [in-file (io/reader file-name)]
    (doall (csv/parse-csv in-file))))
    ;; (doall (csv/parse-csv in-file :end-of-line "\n\n"))))

(defn -main
  "do some fun stuff with csv files"
  [& args]
  (println "Files (<path/to/WIRED.csv> <path/to/WIRELSS.csv> <path/to/DETAIL>): ")
    (let [in (read-line)
          file-names (str/split in #" ")
          wired (first file-names)
          wireless (second file-names)
          detail (nth file-names 2)
          wired-csv-data (read-csv wired)
          wireless-csv-data (read-csv wireless)
          wired-data-map (map create-data-map wired-csv-data)
          wireless-data-map (map create-data-map wireless-csv-data)
          wired-list (create-final-list wired-data-map)
          wireless-list (create-final-list wireless-data-map)]
      (println "=============== Wired List: ===============")
      (pp/pprint wired-list)
      (println "\n=============== Wireless List: ===============")
      (pp/pprint wireless-list)))
