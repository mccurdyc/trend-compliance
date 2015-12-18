(ns trend-compliance.core
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.string :as str])
  (:gen-class))

(defn windows?
  "check to see if the device is windows os"
  [item]
  (if (re-matches #"(?i)windows*" (get item :platform)) true false))
    ;; (when (re-matches #"(?i)windows*" (get item :platform))))
      ;; {:windows-os true
      ;;  :item item}))

(defn create-windows-list
  "create a list of windows machines with corresponding attributes"
  [windows-map]
  (filter windows? windows-map))

(defn create-data-map
  "parse the list of lists returned from read-csv"
  [item]
  (let [[ip cp-name domain mac-ad off-scan-status ping prod-name platform os-server version patt-file scan-eng prev-pol cp-des rem-install] item]
    {:ip ip
     :cp-name cp-name
     :domain domain
     :mac-ad mac-ad
     :off-scan-status off-scan-status
     :ping ping
     :prod-name prod-name
     :platform platform
     :os-server os-server
     :version version
     :patt-file patt-file
     :scan-eng scan-eng
     :prev-pol prev-pol
     :cp-des cp-des
     :rem-install rem-install}))

(defn read-csv
  "read in a csv file"
  [file-name]
  (with-open [in-file (io/reader file-name)]
    (doall
       (csv/read-csv in-file))))

(defn -main
  "do some fun stuff with csv files"
  [& args]
  (println "give me a file: ")
    (let [file-name (read-line)
          csv-data (read-csv file-name)
          data-map (map create-data-map csv-data)]
          ;; windows-map (map windows? data-map)
          ;; win-list '()]
          (create-windows-list data-map)))
